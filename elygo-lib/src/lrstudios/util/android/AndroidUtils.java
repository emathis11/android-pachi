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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import lrstudios.games.ego.lib.Utils;

import java.util.List;


public class AndroidUtils
{

    private static DialogInterface.OnClickListener _emptyDialogOnClickListener;

    public static InputFilter getFilenameInputFilter()
    {
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

    /** Returns an empty {@link android.content.DialogInterface.OnClickListener}. */
    public static DialogInterface.OnClickListener getEmptyDialogOnClickListener()
    {
        if (_emptyDialogOnClickListener == null)
        {
            _emptyDialogOnClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) { }
            };
        }
        return _emptyDialogOnClickListener;
    }

        /** Returns true if the specified intent is callable. */
    public static boolean isIntentCallable(Context context, Intent intent)
    {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /** Returns true if the android external storage is writeable. */
    public static boolean isExternalStorageWriteable()
    {
        return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
    }

    /** Returns true if the android external storage is readable. */
    public static boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    /** Gets the width of a text given the specified paint. */
    public static float getTextWidth(String text, Paint paint)
    {
        float[] widths = new float[text.length() * 2];
        paint.getTextWidths(text, widths);

        float sum = 0;
        for (float width : widths)
            sum += width;

        return sum;
    }

    /** Etend le rectangle d'un nombre d'unités spécifié, sans sortir des limites données par le rectangle maxRect. */
    public static void Rect_addMargin(Rect rect, int margin, Rect maxRect)
    {
        rect.left -= margin;
        rect.top -= margin;
        rect.right += margin;
        rect.bottom += margin;

        Rect_crop(rect, maxRect);
    }

    /** Réduit le rectangle spécifié pour qu'il soit contenu dans le second rectangle "bounds". */
    public static void Rect_crop(Rect rect, Rect bounds)
    {
        if (rect.left < bounds.left) rect.left = bounds.left;
        if (rect.top < bounds.top) rect.top = bounds.top;
        if (rect.right >= bounds.right) rect.right = bounds.right;
        if (rect.bottom >= bounds.bottom) rect.bottom = bounds.bottom;
    }
}
