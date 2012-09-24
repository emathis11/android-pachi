package net.lrstudios.android.pachi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import lrstudios.games.ego.lib.ExternalGtpEngine;
import lrstudios.games.ego.lib.UpdatePrefsTask;
import lrstudios.games.ego.lib.Utils;

import java.io.*;


public class PachiEngine extends ExternalGtpEngine
{
    /** Increment this each time you update the pachi executable. */
    protected static final int EXE_VERSION = 1;

    protected static final String PREF_KEY_VERSION = "pachi_exe_version";


    public PachiEngine(Context context)
    {
        super(context, new String[]{"-t", "9", "max_tree_size=192"});
    }

    @Override
    protected File getEngineFile()
    {
        File dir = _context.getDir("engines", Context.MODE_PRIVATE);
        File file = new File(dir, "pachi");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        int version = prefs.getInt(PREF_KEY_VERSION, 0);
        if (version < EXE_VERSION)
        {
            if (file.exists())
                file.delete();
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try
            {
                outputStream = new BufferedOutputStream(new FileOutputStream(file), 4096);
                inputStream = new BufferedInputStream(_context.getResources().openRawResource(R.raw.pachi), 4096);
                Utils.copyStream(inputStream, outputStream, 4096);

                try
                {
                    new ProcessBuilder("chmod", "u+x", file.getAbsolutePath()).start().waitFor();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(PREF_KEY_VERSION, EXE_VERSION);
                    editor.commit();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            catch (IOException e) // TODO handle file extracting errors
            {
                e.printStackTrace();
            }
            finally
            {
                Utils.closeStream(inputStream);
                Utils.closeStream(outputStream);
            }
        }

        return file;
    }

    @Override
    public String getName()
    {
        return "Pachi";
    }

    @Override
    public String getVersion()
    {
        return "10.00";
    }
}
