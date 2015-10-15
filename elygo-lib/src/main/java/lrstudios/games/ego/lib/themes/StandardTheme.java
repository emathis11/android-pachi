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
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import lrstudios.games.ego.lib.R;


public class StandardTheme extends Theme {
    private static final int SHADOW_SIZE = 1;

    private Paint _backgroundPaint;
    private ShapeDrawable _shadowDrawable;


    public StandardTheme(Context context) {
        super(context);
        _gridPaint.setStyle(Paint.Style.STROKE);
        _gridPaint.setStrokeWidth(0);
        _gridPaint.setColor(Color.BLACK);

        _hoshiPaint.setAntiAlias(true);
        _hoshiPaint.setColor(Color.BLACK);
        _hoshiPaint.setStyle(Paint.Style.FILL);

        _shadowDrawable = new ShapeDrawable(new OvalShape());
        Paint shadowPaint = _shadowDrawable.getPaint();
        shadowPaint.setColor(Color.argb(100, 0, 0, 0));
        shadowPaint.setAntiAlias(true);

        _backgroundPaint = new Paint();
        defineBackgroundPaint(_backgroundPaint);
    }

    protected void defineBackgroundPaint(Paint backgroundPaint) {
        backgroundPaint.setShader(new BitmapShader(
                BitmapFactory.decodeResource(_context.getResources(), R.drawable.wood6),
                TileMode.MIRROR, TileMode.MIRROR));
    }


    @Override
    public void init(Config config) {
        super.init(config);
        Resources res = _context.getResources();

        _deadBlackStone = new BitmapDrawable(res, blackStoneBitmap);
        _deadBlackStone.setAlpha(112);
        _deadWhiteStone = new BitmapDrawable(res, whiteStoneBitmap);
        _deadWhiteStone.setAlpha(144);
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
        int realStoneSize = stoneSize + SHADOW_SIZE;

        Bitmap bitmap = Bitmap.createBitmap(realStoneSize, realStoneSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        _shadowDrawable.setBounds(2, 2, realStoneSize, realStoneSize);
        _shadowDrawable.draw(canvas);

        ShapeDrawable stoneDrawable = new ShapeDrawable(new OvalShape());
        Paint blackStonePaint = stoneDrawable.getPaint();

        blackStonePaint.setAntiAlias(true);
        blackStonePaint.setShader(new RadialGradient(
                realStoneSize / 3.0f, realStoneSize / 8.2f, realStoneSize / 2.1f,
                Color.rgb(120, 120, 120), Color.BLACK, TileMode.CLAMP));

        stoneDrawable.setBounds(SHADOW_SIZE, SHADOW_SIZE, realStoneSize - SHADOW_SIZE, realStoneSize - SHADOW_SIZE);
        stoneDrawable.draw(canvas);
        return bitmap;
    }

    @Override
    public Bitmap createWhiteStoneBitmap(int stoneSize) {
        int realStoneSize = stoneSize + SHADOW_SIZE;

        Bitmap bitmap = Bitmap.createBitmap(realStoneSize, realStoneSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        _shadowDrawable.setBounds(2, 2, realStoneSize, realStoneSize);
        _shadowDrawable.draw(canvas);

        ShapeDrawable stoneDrawable = new ShapeDrawable(new OvalShape());
        Paint whiteStonePaint = stoneDrawable.getPaint();

        whiteStonePaint.setAntiAlias(true);
        whiteStonePaint.setShader(new LinearGradient(
                (int) (realStoneSize * 0.33), 0,
                realStoneSize, realStoneSize,
                Color.rgb(255, 255, 255), Color.rgb(142, 142, 142), TileMode.CLAMP));

        stoneDrawable.setBounds(SHADOW_SIZE, SHADOW_SIZE, realStoneSize - SHADOW_SIZE, realStoneSize - SHADOW_SIZE);
        stoneDrawable.draw(canvas);
        return bitmap;
    }
}
