/*
 * This file is part of Elygo-lib.
 * Copyright (C) 2012   Emmanuel Mathis [emmanuel *at* lr-studios.net]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lrstudios.games.ego.lib.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import lrstudios.games.ego.lib.BoardView;
import lrstudios.games.ego.lib.GoGame;
import lrstudios.games.ego.lib.R;
import lrstudios.games.ego.lib.Utils;
import lrstudios.util.android.AndroidUtils;
import lrstudios.util.android.ui.BetterFragmentActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


/**
 * The base class for all board activities (Tsumego, IGS, ...) which contains the common data and functions.
 */
public abstract class BaseBoardActivity extends BetterFragmentActivity implements BoardView.BoardListener {
    private static final String TAG = "BaseBoardActivity";

    private static final String SD_CARD_GAMES_FOLDER_PATH = "ElyGo/SGF"; // TODO

    public static final String
            INTENT_GAME_INFO = "lrstudios.games.ego.GAME_INFO",
            INTENT_GAME_MODE = "lrstudios.games.ego.GAME_MODE";

    protected static final Random _random = new Random();
    protected static final int CODE_PREFERENCES_ACTIVITY = 501;

    // Values got from preferences
    protected boolean _wakeLockEnabled;
    protected boolean _hideStatusBar;
    protected String _tsumegoColor;

    protected BoardView _boardView;
    private File _internalGamesDir;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _internalGamesDir = getDir("SGF", Context.MODE_PRIVATE);
        _loadPreferences();
    }


    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);
        _boardView = (BoardView) findViewById(R.id.boardView);
        _boardView.setBoardListener(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_PREFERENCES_ACTIVITY) {
            _loadPreferences();
            _boardView.readPreferences();
            _boardView.recreateGraphics();
        }
    }

    protected void setSubtitleMoveNumber(int moveNumber) {
        getSupportActionBar().setSubtitle((moveNumber > 0) ?
                getString(R.string.board_move_number, moveNumber) : getString(R.string.board_no_moves));
    }


    protected void _showSaveDialog(final GoGame game, String defaultName) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_save_file, null);
        if (dialogView == null) {
            Log.e(TAG, "Unable to inflate save dialog layout : " + R.layout.dialog_save_file);
            showToast(R.string.err_internal);
            return;
        }

        final EditText edtFileName = (EditText) dialogView.findViewById(R.id.txt_filename);
        final CheckBox chkSaveSd = (CheckBox) dialogView.findViewById(R.id.chk_save_sd);
        final TextView txtWarning = (TextView) dialogView.findViewById(R.id.txt_warning_file_overwrite);

        if (!AndroidUtils.isExternalStorageWriteable()) {
            chkSaveSd.setChecked(false);
            chkSaveSd.setEnabled(false);
            chkSaveSd.setClickable(false);
        }

        edtFileName.setFilters(new InputFilter[]{AndroidUtils.getFilenameInputFilter()});
        edtFileName.setText(defaultName);
        edtFileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                _checkFileExists(edtFileName.getText().toString(),
                        chkSaveSd.isEnabled() && chkSaveSd.isChecked(), txtWarning);
            }
        });
        chkSaveSd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                _checkFileExists(edtFileName.getText().toString(), isChecked, txtWarning);
            }
        });

        _checkFileExists(edtFileName.getText().toString(), false, txtWarning);

        new AlertDialog.Builder(BaseBoardActivity.this)
                .setView(dialogView)
                .setTitle(R.string.dialog_title_save)
                .setMessage(R.string.enter_game_name)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = edtFileName.getText().toString();
                        boolean saveSd = chkSaveSd.isEnabled() && chkSaveSd.isChecked();

                        if (fileName.length() == 0) {
                            showToast(R.string.err_filename_short);
                            return;
                        }

                        File destFile;
                        String sgfFilename = fileName + ".sgf";
                        if (saveSd) {
                            destFile = new File(
                                    new File(Environment.getExternalStorageDirectory(), SD_CARD_GAMES_FOLDER_PATH),
                                    sgfFilename);
                        } else {
                            destFile = new File(getDir("SGF", Context.MODE_PRIVATE), sgfFilename);
                        }
                        if (destFile.exists())
                            destFile.delete();

                        FileOutputStream stream = null;
                        try {
                            stream = new FileOutputStream(destFile);
                            game.saveSgf(stream);
                            showToast(getString(R.string.game_saved, fileName));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            if (saveSd)
                                showToast(R.string.err_sd_read_only);
                            else
                                showToast(R.string.err_save_game);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.v(TAG, "Deleted file : " + destFile.delete());
                            showToast(R.string.err_save_game);
                        } finally {
                            Utils.closeStream(stream);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, AndroidUtils.getEmptyDialogOnClickListener())
                .show();
    }


    private void _checkFileExists(String desiredFileName, boolean saveOnSd, View warningView) {
        if (desiredFileName.length() == 0)
            return;

        desiredFileName += ".sgf";
        File file;
        if (saveOnSd) {
            file = new File(
                    new File(Environment.getExternalStorageDirectory(), SD_CARD_GAMES_FOLDER_PATH),
                    desiredFileName);
        } else {
            file = new File(_internalGamesDir, desiredFileName);
        }
        warningView.setVisibility(file.exists() ? View.VISIBLE : View.INVISIBLE);
    }


    protected void _loadPreferences() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final Window window = getWindow();

        _wakeLockEnabled = prefs.getBoolean("wakeLockPref", true);
        _hideStatusBar = prefs.getBoolean("statusBarPref", false);
        _tsumegoColor = prefs.getString("tsumegoColorPref", "black");

        window.setFlags(_wakeLockEnabled ? WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON : 0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.setFlags(_hideStatusBar ? WindowManager.LayoutParams.FLAG_FULLSCREEN : 0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
