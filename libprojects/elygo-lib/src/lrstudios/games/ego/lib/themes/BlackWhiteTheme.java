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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;


/**
 * Basic black and white Theme.
 */
public final class BlackWhiteTheme extends Theme {
    private Paint _backgroundPaint;


    public BlackWhiteTheme(Context context) {
        super(context);

        _backgroundPaint = new Paint();
        _backgroundPaint.setColor(Color.WHITE);

        _gridPaint.setColor(Color.rgb(64, 64, 64));
        _gridPaint.setStyle(Paint.Style.STROKE);
        _gridPaint.setStrokeWidth(0);

        _hoshiPaint.setAntiAlias(true);
        _hoshiPaint.setColor(Color.BLACK);
        _hoshiPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    public void init(Config config) {
        super.init(config);
        Resources res = _context.getResources();

        _deadBlackStone = new BitmapDrawable(res, blackStoneBitmap);
        _deadBlackStone.setAlpha(104);
        _deadWhiteStone = new BitmapDrawable(res, whiteStoneBitmap);
        _deadWhiteStone.setAlpha(104);
        _blackVariation = new BitmapDrawable(res, blackStoneBitmap);
        _blackVariation.setAlpha(112);
        _whiteVariation = new BitmapDrawable(res, whiteStoneBitmap);
        _whiteVariation.setAlpha(144);
    }


    @Override
    public void drawBackground(Canvas canvas, int left, int top, int right, int bottom) {
        canvas.drawRect(left, top, right, bottom, _backgroundPaint);
    }

    @Override
    public Bitmap createBlackStoneBitmap(int stoneSize) {
        Bitmap bitmap = Bitmap.createBitmap(stoneSize, stoneSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        Paint blackStonePaint = drawable.getPaint();
        blackStonePaint.setAntiAlias(true);
        blackStonePaint.setColor(Color.BLACK);

        drawable.setBounds(0, 0, stoneSize, stoneSize);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public Bitmap createWhiteStoneBitmap(int stoneSize) {
        Bitmap bitmap = Bitmap.createBitmap(stoneSize, stoneSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        Paint whiteStonePaint = drawable.getPaint();
        whiteStonePaint.setAntiAlias(true);
        whiteStonePaint.setColor(Color.WHITE);

        drawable.setBounds(1, 1, stoneSize - 1, stoneSize - 1);
        drawable.draw(canvas);

        whiteStonePaint.setColor(Color.BLACK);
        whiteStonePaint.setStyle(Paint.Style.STROKE);
        whiteStonePaint.setStrokeWidth(0);
        drawable.draw(canvas);
        return bitmap;
    }
}
