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

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Updates a specified SharedPreferences object on a secondary thread.
 */
public final class UpdatePrefsTask extends AsyncTask<Object, Void, Void>
{
    private static final String TAG = "UpdatePrefsTask";


    private SharedPreferences prefs;
    private String[] keys;
    
    public UpdatePrefsTask(SharedPreferences prefs, String... keys)
    {
        this.prefs = prefs;
        this.keys = keys;
    }

    @Override
    protected Void doInBackground(Object... params)
    {
        try {
            SharedPreferences.Editor editor = prefs.edit();

            for (int i = 0; i < keys.length; i++)
            {
                Object value = params[i];

                if (value instanceof Integer)
                    editor.putInt(keys[i], (Integer)value);
                else if (value instanceof String)
                    editor.putString(keys[i], (String)value);
                else if (value instanceof Float)
                    editor.putFloat(keys[i], (Float)value);
                else if (value instanceof Long)
                    editor.putLong(keys[i], (Long)value);
                else if (value instanceof Boolean)
                    editor.putBoolean(keys[i], (Boolean)value);
                else
                    Log.e(TAG, "The parameter type was not handled. (" + value.getClass().getName() + ")");
            }

            editor.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}