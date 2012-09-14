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

package lrstudios.games.ego.lib.themes;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Paint;
import android.graphics.Shader;
import lrstudios.games.ego.lib.R;


public class DarkBoardTheme extends StandardTheme
{
    public DarkBoardTheme(Context context)
    {
        super(context);
    }

    @Override
    protected void defineBackgroundPaint(Paint backgroundPaint)
    {
        backgroundPaint.setShader(new BitmapShader(
            BitmapFactory.decodeResource(_context.getResources(), R.drawable.wood2),
            Shader.TileMode.MIRROR, Shader.TileMode.MIRROR));
    }
}
