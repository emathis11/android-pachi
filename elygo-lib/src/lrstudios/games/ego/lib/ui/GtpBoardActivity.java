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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import lrstudios.games.ego.lib.*;

import java.io.FileInputStream;
import java.text.DecimalFormat;


public class GtpBoardActivity extends BaseBoardActivity implements BoardView.BoardListener {
    private static final String TAG = "GtpBoardActivity";

    public static final int
            MSG_GTP_MOVE = 2,
            MSG_FINAL_SCORE = 3;

    public static final String
            INTENT_PLAY_RESTORE = "lrstudios.games.ego.PLAY_RESTORE",
            INTENT_GTP_BOT_CLASS = "lrstudios.games.ego.BOT_CLASS";


    private ScoreView _scoreView;
    private static GtpThread _gtpThread;
    private ActivityHandler _handler = new ActivityHandler();
    private GtpBot _bot;
    private ProgressDialog _waitingScoreDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.board_activity);

        _scoreView = (ScoreView) findViewById(R.id.score_view);

        final Bundle extras = getIntent().getExtras();
        IntentGameInfo gameInfo = (IntentGameInfo) extras.getParcelable(INTENT_GAME_INFO);
        if (gameInfo == null) {
            showToast(R.string.err_internal);
            finish();
            return;
        }

        // Wait if a previous instance of the bot is still running (this may happen if the user closed
        // this activity during the bot's turn, and reopened it quickly)
        if (_gtpThread != null && _gtpThread.isAlive()) {
            _gtpThread.quit();
            try {
                _gtpThread.join(); // TODO show a ProgressDialog?
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Class<?> botClass = (Class<?>) extras.getSerializable(INTENT_GTP_BOT_CLASS);
        try {
            _bot = (GtpBot) botClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.err_internal);
            finish();
        }

        _bot.setLevel(gameInfo.botLevel);
        _gtpThread = new GtpThread(_bot, _handler, getApplicationContext());
        _gtpThread.start();

        boolean restore = extras.getBoolean(INTENT_PLAY_RESTORE, false);
        boolean loadOk = false;
        if (restore) {
            try {
                FileInputStream stream = openFileInput("gtp_save.sgf");
                _bot.newGame(stream);
                stream.close();
                if (_bot.isBotTurn()) // The game is never saved before the bot's turn
                    _bot.switchColors();
                loadOk = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!loadOk || !restore) {
            int boardsize = gameInfo.boardSize;
            double komi = gameInfo.komi;
            byte color = gameInfo.color;
            int handicap = gameInfo.handicap;
            if (color == GoBoard.EMPTY)
                color = _random.nextBoolean() ? GoBoard.BLACK : GoBoard.WHITE;

            _bot.newGame(boardsize, color, komi, handicap);
        }

        _boardView.setBoardListener(this);
        _boardView.changeGame(_bot.getGame(), false);

        String botName = _bot.getName();
        String botLevel = getString(R.string.board_bot_level, gameInfo.botLevel);
        String blackName, whiteName, blackRank, whiteRank;
        if (_bot.getPlayerColor() == GoBoard.BLACK) {
            whiteName = botName;
            whiteRank = botLevel;
            blackName = getString(R.string.player);
            blackRank = "";
        } else {
            whiteName = getString(R.string.player);
            whiteRank = "";
            blackName = botName;
            blackRank = botLevel;
        }
        _scoreView.setWhiteName(whiteName);
        _scoreView.setWhiteRank(whiteRank);
        _scoreView.setBlackName(blackName);
        _scoreView.setBlackRank(blackRank);
        GameInfo info = _bot.getGame().info;
        info.blackName = blackName;
        info.blackRank = blackRank;
        info.whiteName = whiteName;
        info.whiteRank = whiteRank;

        setTitle(getString(R.string.board_game_vs, botName, botLevel));
        _scoreView.setBlackPrisoners(_bot.getGame().getBlackPrisoners());
        _scoreView.setWhitePrisoners(_bot.getGame().getWhitePrisoners());

        setSupportProgressBarIndeterminateVisibility(false);
        _updateGameLogic();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.actionbar_gtp_board, menu);

        if (_bot.getGame().getCurrentNode().parentNode == null)
            disableOptionItem(R.id.menu_undo);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if (id == R.id.menu_undo) {
            _bot.undo();
            _updatePrisoners();
            _updateGameLogic();
        }
        /*else if (id == R.id.menu_save)
        {
            // Give a default name to the game : "BotName_MonthDay_HoursMinutes"
            Date now = new Date();
            String defaultName = String.format("%s_%02d%02d_%02d%02d",
                _bot.getName(), now.getMonth(), now.getDay(), now.getHours(), now.getMinutes());
            _showSaveDialog(_bot.getGame(), defaultName);
        }*/
        else if (id == R.id.menu_pass) {
            onPress(-1, -1);
        }
        return true;
    }


    @Override
    public void onPress(int x, int y) {
        if (_bot.playMove(new Coords(x, y))) {
            _updatePrisoners();
            _updateGameLogic();
        } else
            Log.w(TAG, "The move was illegal.");
    }


    private void _updateGameLogic() {
        GoGame game = _bot.getGame();

        // Enter scoring
        if (game.hasTwoPasses()) {
            _lockPlaying();
            _gtpThread.getFinalScore();
            _waitingScoreDialog = new ProgressDialog(this);
            _waitingScoreDialog.setIndeterminate(true);
            _waitingScoreDialog.setCancelable(true);
            _waitingScoreDialog.setMessage(getString(R.string.board_compute_territory));
            _waitingScoreDialog.show();
        } else if (!game.isFinished()) {
            if (_bot.isBotTurn()) {
                _lockPlaying();
                setSupportProgressBarIndeterminateVisibility(true);
                _gtpThread.playMove();
            } else {
                _unlockPlaying();
            }
        }
        setSubtitleMoveNumber(_bot.getGame().getCurrentMoveNumber());
        _boardView.invalidate();
    }


    protected void _lockPlaying() {
        _boardView.lockPlaying();
        disableOptionItem(R.id.menu_undo);
        disableOptionItem(R.id.menu_pass);
    }

    protected void _unlockPlaying() {
        _boardView.unlockPlaying();
        GoGame game = _bot.getGame();
        boolean isFinished = game.getCurrentNode().x >= -1 && !game.isFinished();
        setOptionItemEnabled(R.id.menu_undo, isFinished);
        setOptionItemEnabled(R.id.menu_pass, isFinished);
    }

    protected void _updatePrisoners() {
        GoGame game = _bot.getGame();
        _scoreView.setBlackPrisoners(game.getBlackPrisoners());
        _scoreView.setWhitePrisoners(game.getWhitePrisoners());
        _boardView.addPrisoners(game.getLastPrisoners());
    }


    private class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_GTP_MOVE) {
                GoGame game = _bot.getGame();
                GameNode move = game.getCurrentNode();

                if (game.isFinished()) // resign
                {
                    showInfoDialog(getString(R.string.board_player_resigned, _bot.getName()));
                    setTitle(R.string.gtp_resign_win);
                } else if (move.x == -1) {
                    // Don't show two dialogs if the game is finished
                    if (!_bot.getGame().hasTwoPasses())
                        showInfoDialog(getString(R.string.board_player_passes, _bot.getName()));
                } else if (move.x >= 0) {
                    _updatePrisoners();
                } else {
                    Log.e(TAG, "invalid move coordinates : " + move);
                }

                setSupportProgressBarIndeterminateVisibility(false);
                _updateGameLogic();
            } else if (msg.what == MSG_FINAL_SCORE) {
                GoGameResult result = (GoGameResult) msg.obj;
                if (_waitingScoreDialog != null) {
                    _waitingScoreDialog.dismiss();
                    _waitingScoreDialog = null;
                }
                _boardView.showFinalStatus(true);
                _boardView.invalidate();

                String winner = getString(
                        result.getWinner() == GoGameResult.BLACK ? R.string.black : R.string.white);
                setTitle(getString(R.string.gtp_game_result,
                        winner, new DecimalFormat("#0.#").format(result.getScore())));
                disableOptionItem(R.id.menu_undo);
                disableOptionItem(R.id.menu_pass);
            }
        }
    }
}
