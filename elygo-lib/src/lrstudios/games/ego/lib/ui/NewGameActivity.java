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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import lrstudios.games.ego.lib.GoBoard;
import lrstudios.games.ego.lib.IntentGameInfo;
import lrstudios.games.ego.lib.R;
import lrstudios.games.ego.lib.UpdatePrefsTask;
import lrstudios.util.android.ui.BetterFragmentActivity;


/**
 * Allows to start a game against a bot.
 */
public abstract class NewGameActivity extends BetterFragmentActivity implements View.OnClickListener
{
    private static final String TAG = "NewGameActivity";

    private Spinner _spn_boardSize;
    private Spinner _spn_komi;
    private Spinner _spn_color;
    private Spinner _spn_handicap;
    private Spinner _spn_level;
    private Button _btn_continue;

    private static final String
            _PREF_BOARDSIZE = "newgame_boardsize",
            _PREF_KOMI = "newgame_komi",
            _PREF_COLOR = "newgame_color",
            _PREF_LEVEL = "newgame_level",
            _PREF_HANDICAP = "newgame_handicap";


    /**
     * This method should return the {@link Class} of the desired bot.
     */
    protected abstract Class<?> getBotClass();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame_activity);

        _spn_boardSize = (Spinner) findViewById(R.id.spn_play_boardsize);
        _spn_komi = (Spinner) findViewById(R.id.spn_play_komi);
        _spn_color = (Spinner) findViewById(R.id.spn_play_color);
        _spn_level = (Spinner) findViewById(R.id.spn_play_level);
        _spn_handicap = (Spinner) findViewById(R.id.spn_play_handicap);
        _btn_continue = (Button) findViewById(R.id.btn_play_continue);

        // Restore last values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        _spn_boardSize.setSelection(prefs.getInt(_PREF_BOARDSIZE, 3));
        _spn_komi.setSelection(prefs.getInt(_PREF_KOMI, 1));
        _spn_color.setSelection(prefs.getInt(_PREF_COLOR, 0));
        _spn_level.setSelection(prefs.getInt(_PREF_LEVEL, 9));
        _spn_handicap.setSelection(prefs.getInt(_PREF_HANDICAP, 0));

        findViewById(R.id.btn_play_start).setOnClickListener(this);
        _btn_continue.setOnClickListener(this);

        _updateButtons();
    }


    @Override
    public void onClick(View v)
    {
        Intent intent;
        IntentGameInfo gameInfo;
        int level = _spn_level.getSelectedItemPosition() + 1;

        int id = v.getId();
        if (id == R.id.btn_play_start)
        {
            byte color;
            int colorPos = _spn_color.getSelectedItemPosition();
            if (colorPos == 0)
                color = GoBoard.BLACK;
            else if (colorPos == 1)
                color = GoBoard.WHITE;
            else
                color = GoBoard.EMPTY;

            int boardsize = Integer.parseInt((String) _spn_boardSize.getSelectedItem());
            double komi = Double.parseDouble((String) _spn_komi.getSelectedItem());
            int handicap = Integer.parseInt((String) _spn_handicap.getSelectedItem());

            new UpdatePrefsTask(PreferenceManager.getDefaultSharedPreferences(this),
                    _PREF_BOARDSIZE, _PREF_KOMI, _PREF_COLOR, _PREF_LEVEL, _PREF_HANDICAP)
                    .execute(
                            _spn_boardSize.getSelectedItemPosition(),
                            _spn_komi.getSelectedItemPosition(),
                            colorPos,
                            _spn_level.getSelectedItemPosition(),
                            _spn_handicap.getSelectedItemPosition());

            gameInfo = new IntentGameInfo();
            gameInfo.boardSize = boardsize;
            gameInfo.komi = komi;
            gameInfo.botLevel = level;
            gameInfo.color = color;
            gameInfo.handicap = handicap;

            intent = new Intent(NewGameActivity.this, GtpBoardActivity.class);
            intent.putExtra(GtpBoardActivity.INTENT_GTP_BOT_CLASS, getBotClass());
            intent.putExtra(BaseBoardActivity.INTENT_GAME_INFO, gameInfo);
            startActivityForResult(intent, 0);
        }
        else if (id == R.id.btn_play_continue)
        {
            gameInfo = new IntentGameInfo();
            gameInfo.botLevel = level;

            intent = new Intent(NewGameActivity.this, GtpBoardActivity.class);
            intent.putExtra(GtpBoardActivity.INTENT_PLAY_RESTORE, true);
            intent.putExtra(GtpBoardActivity.INTENT_GTP_BOT_CLASS, getBotClass());
            intent.putExtra(BaseBoardActivity.INTENT_GAME_INFO, gameInfo);
            startActivityForResult(intent, 0);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        _updateButtons();
    }


    private void _updateButtons()
    {
        // Disable "Resume" button if there is no game saved
        _btn_continue.setEnabled(getFileStreamPath("gtp_save.sgf").exists());
    }
}
