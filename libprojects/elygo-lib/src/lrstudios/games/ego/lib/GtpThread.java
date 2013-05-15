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

package lrstudios.games.ego.lib;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import lrstudios.games.ego.lib.ui.GtpBoardActivity;


/**
 * GTP commands will be sent to the specified {@link GtpEngine} from this thread.
 */
public class GtpThread extends HandlerThread implements Handler.Callback {
    private static final String TAG = "GtpThread";
    private static final int
            _MSG_PLAY = 1,
            _MSG_FINAL_SCORE = 2;

    private Context _appContext;
    private Handler _handler;
    private Handler _notifyHandler;
    private GtpEngine _engine;


    public GtpThread(GtpEngine engine, Handler notifyHandler, Context applicationContext) {
        super("GtpThread");
        _engine = engine;
        _notifyHandler = notifyHandler;
        _appContext = applicationContext;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        synchronized (this) {
            _handler = new Handler(getLooper(), this);
            notifyAll();
        }
    }

    public void playMove() {
        synchronized (this) {
            while (_handler == null) {
                try {
                    Log.v(TAG, "waiting GTP thread initialization");
                    wait();
                }
                catch (InterruptedException ignored) {
                }
            }
        }
        _handler.sendMessage(_handler.obtainMessage(_MSG_PLAY));
    }

    public void getFinalScore() {
        _handler.sendMessage(_handler.obtainMessage(_MSG_FINAL_SCORE));
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == _MSG_PLAY) {
            _engine.genMove();

            if (!_engine.getGame().isFinished()) {
                // Saves an SGF file after each bot move to be able to restore the game
                try {
                    _engine.getGame().saveSgf(_appContext.openFileOutput("gtp_save.sgf", Context.MODE_PRIVATE));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (_notifyHandler != null)
                _notifyHandler.sendMessage(_notifyHandler.obtainMessage(GtpBoardActivity.MSG_GTP_MOVE));
            return true;
        }
        else if (msg.what == _MSG_FINAL_SCORE) {
            _engine.askFinalStatus();
            GoGameResult result = _engine.computeFinalScore();
            if (_notifyHandler != null)
                _notifyHandler.sendMessage(_notifyHandler.obtainMessage(GtpBoardActivity.MSG_FINAL_SCORE, result));
            return true;
        }
        return false;
    }
}