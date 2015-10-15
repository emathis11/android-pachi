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

package lrstudios.util.android;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.Properties;

import lrstudios.games.ego.lib.Utils;


public class AndroidUtils {
    private static final String TAG = "AndroidUtils";

    private static DialogInterface.OnClickListener _emptyDialogOnClickListener;


    public static String getAppVersion(PackageManager packageManager, String packageName) {
        String version = "?";
        try {
            if (packageManager != null)
                version = packageManager.getPackageInfo(packageName, 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static boolean assetExists(Context context, String path) {
        try {
            Utils.closeObject(context.getAssets().open(path));
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    public static void copyAssetsToMemory(Context context, String fromFolder, File destDir) throws IOException {
        AssetManager assets = context.getAssets();
        String[] assetList = assets.list(fromFolder);

        for (String assetName : assetList) {
            File file = new File(destDir, assetName);
            if (file.exists())
                file.delete();

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                inputStream = assets.open(fromFolder + "/" + assetName);
                Utils.copyStream(inputStream, outputStream, 4096);
            }
            finally {
                Utils.closeObject(inputStream);
                Utils.closeObject(outputStream);
            }
        }
    }

    public static int getBitmapByteCount(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static Properties loadPropertiesFromAssets(Context context, String fileName) throws IOException {
        Properties props = new Properties();
        InputStream stream = null;
        try {
            stream = context.getResources().getAssets().open(fileName);
            props.load(stream);
        }
        finally {
            Utils.closeObject(stream);
        }
        return props;
    }

    public static InputFilter getFilenameInputFilter() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend)
            {
                for (int i = start; i < end; i++) {
                    if (Utils.FILENAME_RESERVED_CHARS.indexOf(source.charAt(i)) >= 0)
                        return "";
                }
                return null;
            }
        };
    }

    /**
     * Returns an empty {@link android.content.DialogInterface.OnClickListener}.
     */
    public static DialogInterface.OnClickListener getEmptyDialogOnClickListener() {
        if (_emptyDialogOnClickListener == null) {
            _emptyDialogOnClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            };
        }
        return _emptyDialogOnClickListener;
    }

    /**
     * Returns true if the android external storage is writeable.
     */
    public static boolean isExternalStorageWriteable() {
        return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
    }

    /**
     * Returns true if the android external storage is readable.
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    // See http://stackoverflow.com/questions/4427608/android-getting-resource-id-from-string
    public static int getResourceId(String variableName, Context context, Class<?> clazz) {
        try {
            Field field = clazz.getDeclaredField(variableName);
            return field.getInt(field);
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Gets the width of a text given the specified paint.
     */
    public static float getTextWidth(String text, Paint paint) {
        float[] widths = new float[text.length() * 2];
        paint.getTextWidths(text, widths);

        float sum = 0;
        for (float width : widths)
            sum += width;

        return sum;
    }

    /**
     * Etend le rectangle d'un nombre d'unités spécifié, sans sortir des limites données par le rectangle maxRect.
     */
    public static void Rect_addMargin(Rect rect, int margin, Rect maxRect) {
        rect.left -= margin;
        rect.top -= margin;
        rect.right += margin;
        rect.bottom += margin;

        Rect_crop(rect, maxRect);
    }

    /**
     * Réduit le rectangle spécifié pour qu'il soit contenu dans le second rectangle "bounds".
     */
    public static void Rect_crop(Rect rect, Rect bounds) {
        if (rect.left < bounds.left) rect.left = bounds.left;
        if (rect.top < bounds.top) rect.top = bounds.top;
        if (rect.right >= bounds.right) rect.right = bounds.right;
        if (rect.bottom >= bounds.bottom) rect.bottom = bounds.bottom;
    }


    /**
     * Returns the total RAM available on the device (or -1 if an error occurred).
     */
    public static long getTotalRam(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            return memInfo.totalMem;
        }
        else {
            RandomAccessFile reader = null;
            try {
                reader = new RandomAccessFile("/proc/meminfo", "r");
                String line = reader.readLine();
                String[] arr = line.split("\\s+");
                return Integer.parseInt(arr[1]) * 1024;
            }
            catch (IOException e) {
                return -1;
            }
            finally {
                Utils.closeObject(reader);
            }
        }
    }
}
