package net.lrstudios.android.pachi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.actionbarsherlock.internal.view.menu.ListMenuItemView;
import lrstudios.games.ego.lib.ExternalGtpEngine;
import lrstudios.games.ego.lib.Utils;

import java.io.*;


public class MainActivity extends Activity
{
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame_activity);

        Thread testThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                PachiEngine engine = new PachiEngine(MainActivity.this);
                engine.init();

                Log.v(TAG, "send test commands...");
                engine.sendGtpCommand("known_command level");
                engine.sendGtpCommand("boardsize 9");
                engine.sendGtpCommand("clear_board");
                engine.sendGtpCommand("play black D5");
                engine.sendGtpCommand("genmove white");
                engine.sendGtpCommand("play black D4");
                engine.sendGtpCommand("genmove white");
            }
        });
        testThread.start();
    }
}
