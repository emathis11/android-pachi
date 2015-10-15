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
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import lrstudios.games.ego.lib.R;


public abstract class Theme {
    private static final String TAG = Theme.class.getSimpleName();

    public Bitmap blackStoneBitmap;
    public Bitmap whiteStoneBitmap;

    public Paint blackMarkPaint;
    public Paint whiteMarkPaint;
    public Paint boardMarkPaint;

    public Paint blackLabelPaint;
    public Paint whiteLabelPaint;
    public Paint boardLabelPaint;

    public Paint crossCursorPaint;
    public Paint illegalCrossCursorPaint;
    public Paint goodVariationPaint;
    public Paint goodVariationPaint2;
    public Paint badVariationPaint;

    public Paint coordinatesPaint;

    public ShapeDrawable triangleMark;
    public ShapeDrawable circleMark;
    public ShapeDrawable crossMark;
    public ShapeDrawable squareMark;

    public ShapeDrawable blackTerritory;
    public ShapeDrawable whiteTerritory;
    public ShapeDrawable anyStoneDrawable;

    protected Paint _hoshiPaint;
    protected Paint _gridPaint;
    protected Paint _anyPaint;

    protected Drawable _deadBlackStone;
    protected Drawable _deadWhiteStone;
    protected Drawable _blackVariation;
    protected Drawable _whiteVariation;

    protected Context _context;
    protected Config _config;


    /**
     * Dessine le fond d'écran sur le canvas spécifié.
     */
    public abstract void drawBackground(Canvas canvas, int left, int top, int right, int bottom);

    public abstract Bitmap createBlackStoneBitmap(int stoneSize);

    public abstract Bitmap createWhiteStoneBitmap(int stoneSize);


    /**
     * Dans le constructeur on place l'initialisation indépendante des paramètres du goban à afficher (taille, ...).
     */
    public Theme(Context context) {
        _context = context;
        Resources res = context.getResources();

        _gridPaint = new Paint();
        _hoshiPaint = new Paint();
        _gridPaint.setAntiAlias(true);
        _hoshiPaint.setAntiAlias(true);

        _anyPaint = new Paint();
        _anyPaint.setAntiAlias(true);
        _anyPaint.setColor(Color.argb(170, 128, 125, 44));
        _anyPaint.setStyle(Paint.Style.STROKE);
        _anyPaint.setStrokeWidth(0);

        blackMarkPaint = new Paint();
        blackMarkPaint.setAntiAlias(true);
        blackMarkPaint.setColor(Color.WHITE);
        blackMarkPaint.setStyle(Paint.Style.STROKE);
        blackMarkPaint.setStrokeWidth(4);

        whiteMarkPaint = new Paint();
        whiteMarkPaint.setAntiAlias(true);
        whiteMarkPaint.setColor(Color.BLACK);
        whiteMarkPaint.setStyle(Paint.Style.STROKE);
        whiteMarkPaint.setStrokeWidth(4);

        boardMarkPaint = new Paint();
        boardMarkPaint.setAntiAlias(true);
        boardMarkPaint.setColor(Color.BLACK);
        boardMarkPaint.setStyle(Paint.Style.STROKE);
        boardMarkPaint.setStrokeWidth(4);

        crossCursorPaint = new Paint();
        crossCursorPaint.setColor(Color.rgb(158, 55, 158));
        crossCursorPaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.cross_cursor_size));

        illegalCrossCursorPaint = new Paint();
        illegalCrossCursorPaint.setColor(Color.rgb(232, 25, 25));
        illegalCrossCursorPaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.cross_cursor_size));

        goodVariationPaint = new Paint();
        goodVariationPaint.setAntiAlias(true);
        goodVariationPaint.setColor(Color.rgb(0, 200, 0));

        goodVariationPaint2 = new Paint();
        goodVariationPaint2.setAntiAlias(true);
        goodVariationPaint2.setColor(Color.rgb(0, 200, 0));
        goodVariationPaint2.setStyle(Paint.Style.STROKE);

        badVariationPaint = new Paint();
        badVariationPaint.setAntiAlias(true);
        badVariationPaint.setColor(Color.rgb(200, 0, 0));

        blackLabelPaint = new Paint();
        blackLabelPaint.setAntiAlias(true);
        blackLabelPaint.setStrokeWidth(0);
        blackLabelPaint.setColor(Color.WHITE);

        whiteLabelPaint = new Paint();
        whiteLabelPaint.setAntiAlias(true);
        whiteLabelPaint.setStrokeWidth(0);
        whiteLabelPaint.setColor(Color.rgb(33, 33, 33));

        coordinatesPaint = new Paint();
        coordinatesPaint.setAntiAlias(true);
        coordinatesPaint.setColor(Color.BLACK);
        coordinatesPaint.setTextAlign(Paint.Align.CENTER);
    }


    /**
     * Initialise le skin actuel (cette fonction doit être appelée avant d'utiliser le skin).
     */
    public void init(Config config) {
        _config = config;
        float stoneSize = config.stoneSize;
        int boardSize = config.boardSize;

        _gridPaint.setStrokeWidth((config.gridLineSize > 1) ? config.gridLineSize : 0);
        blackLabelPaint.setTextSize(stoneSize * 0.82f);
        whiteLabelPaint.setTextSize(stoneSize * 0.82f);
        boardLabelPaint = whiteLabelPaint;


        /***** Marques ******/

        // Marques de décompte
        float margin = stoneSize / 4.0f;
        Path path = new Path();
        path.moveTo(margin, margin);
        path.lineTo(stoneSize - margin, stoneSize - margin);
        path.moveTo(margin, stoneSize - margin);
        path.lineTo(stoneSize - margin, margin);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        whiteTerritory = new ShapeDrawable(new PathShape(path, stoneSize, stoneSize));
        whiteTerritory.getPaint().set(paint);

        paint.setColor(Color.BLACK);
        blackTerritory = new ShapeDrawable(new PathShape(path, stoneSize, stoneSize));
        blackTerritory.getPaint().set(paint);


        // Marque : triangle
        path = new Path();
        path.moveTo(0, stoneSize);
        path.lineTo(stoneSize / 2.0f, 0);
        path.lineTo(stoneSize, stoneSize);
        path.close();
        triangleMark = new ShapeDrawable(new PathShape(path, stoneSize, stoneSize));

        // Marque : carré
        path = new Path();
        path.moveTo(0, 0);
        path.lineTo(stoneSize, 0);
        path.lineTo(stoneSize, stoneSize);
        path.lineTo(0, stoneSize);
        path.close();
        squareMark = new ShapeDrawable(new PathShape(path, stoneSize, stoneSize));

        // Marque : croix
        path = new Path();
        path.moveTo(0, 0);
        path.lineTo(stoneSize, stoneSize);
        path.moveTo(stoneSize, 0);
        path.lineTo(0, stoneSize);
        crossMark = new ShapeDrawable(new PathShape(path, stoneSize, stoneSize));

        // Marque : cercle
        path = new Path();
        path.addCircle(stoneSize / 2f, stoneSize / 2f, stoneSize / 2f, Direction.CW);
        circleMark = new ShapeDrawable(new PathShape(path, stoneSize, stoneSize));

        // Represents any stone (token in pattern search)
        path = new Path();
        path.addCircle(stoneSize / 2f, stoneSize / 2f, stoneSize / 2f, Direction.CW);
        anyStoneDrawable = new ShapeDrawable(new PathShape(path, stoneSize, stoneSize));
        anyStoneDrawable.getPaint().set(_anyPaint);

        int newStoneSize = Math.max(1, Math.round(stoneSize) - config.stonesPadding * 2);
        blackStoneBitmap = createBlackStoneBitmap(newStoneSize);
        whiteStoneBitmap = createWhiteStoneBitmap(newStoneSize);
    }


    /**
     * Draw the grid and star points on the specified Canvas.
     */
    public void drawGrid(Canvas canvas, Rect clipBounds) {
        float stoneSize = _config.stoneSize;
        int boardSize = _config.boardSize;

        float intervalDIV2 = stoneSize / 2.0f;
        float boardWidth = stoneSize * (clipBounds.width() + 1);
        float boardHeight = stoneSize * (clipBounds.height() + 1);

        // Grid
        float lineStartX = (clipBounds.left == 0) ? intervalDIV2 : 0;
        float lineEndX = (clipBounds.right == boardSize - 1) ? boardWidth - intervalDIV2 : boardWidth;
        float lineStartY = (clipBounds.top == 0) ? intervalDIV2 : 0;
        float lineEndY = (clipBounds.bottom == boardSize - 1) ? boardHeight - intervalDIV2 : boardHeight;

        for (int x = 0, len = clipBounds.width(); x <= len; x++) {
            float xPos = intervalDIV2 + stoneSize * x;
            canvas.drawLine(xPos, lineStartY, xPos, lineEndY, _gridPaint);
        }

        for (int y = 0, len = clipBounds.height(); y <= len; y++) {
            float yPos = intervalDIV2 + stoneSize * y;
            canvas.drawLine(lineStartX, yPos, lineEndX, yPos, _gridPaint);
        }

        // Star points
        float fullWidth = stoneSize * boardSize;

        float radius = intervalDIV2 / 4.0f;
        int shift = (boardSize > 12) ? 7 : 5;

        float offsetX = stoneSize * clipBounds.left;
        float offsetY = stoneSize * clipBounds.top;

        canvas.drawCircle(-offsetX + intervalDIV2 * shift, -offsetY + intervalDIV2 * shift, radius, _hoshiPaint);
        canvas.drawCircle(-offsetX + fullWidth - intervalDIV2 * shift, -offsetY + intervalDIV2 * shift, radius, _hoshiPaint);
        canvas.drawCircle(-offsetX + intervalDIV2 * shift, -offsetY + fullWidth - intervalDIV2 * shift, radius, _hoshiPaint);
        canvas.drawCircle(-offsetX + fullWidth - intervalDIV2 * shift, -offsetY + fullWidth - intervalDIV2 * shift, radius, _hoshiPaint);

        if (boardSize % 2 == 1) {
            canvas.drawCircle(-offsetX + fullWidth / 2f, -offsetY + fullWidth / 2f, radius, _hoshiPaint);
            if (boardSize > 10) {
                canvas.drawCircle(-offsetX + fullWidth / 2f, -offsetY + intervalDIV2 * shift, radius, _hoshiPaint);
                canvas.drawCircle(-offsetX + fullWidth / 2f, -offsetY + fullWidth - intervalDIV2 * shift, radius, _hoshiPaint);
                canvas.drawCircle(-offsetX + intervalDIV2 * shift, -offsetY + fullWidth / 2f, radius, _hoshiPaint);
                canvas.drawCircle(-offsetX + fullWidth - intervalDIV2 * shift, -offsetY + fullWidth / 2f, radius, _hoshiPaint);
            }
        }
    }

    public void drawDeadBlackStone(Canvas canvas, int left, int top, int interval) {
        int stonePadding = _config.stonesPadding;
        _deadBlackStone.setBounds(left + stonePadding, top + stonePadding, left + interval - stonePadding, top + interval - stonePadding);
        _deadBlackStone.draw(canvas);
        whiteTerritory.setBounds(left, top, left + interval, top + interval);
        whiteTerritory.draw(canvas);
    }

    public void drawDeadWhiteStone(Canvas canvas, int left, int top, int interval) {
        int stonePadding = _config.stonesPadding;
        _deadWhiteStone.setBounds(left + stonePadding, top + stonePadding, left + interval - stonePadding, top + interval - stonePadding);
        _deadWhiteStone.draw(canvas);
        blackTerritory.setBounds(left, top, left + interval, top + interval);
        blackTerritory.draw(canvas);
    }

    public void drawBlackVariation(Canvas canvas, int left, int top, int interval) {
        int stonePadding = _config.stonesPadding;
        _blackVariation.setBounds(left + stonePadding, top + stonePadding, left + interval - stonePadding, top + interval - stonePadding);
        _blackVariation.draw(canvas);
    }

    public void drawWhiteVariation(Canvas canvas, int left, int top, int interval) {
        int stonePadding = _config.stonesPadding;
        _whiteVariation.setBounds(left + stonePadding, top + stonePadding, left + interval - stonePadding, top + interval - stonePadding);
        _whiteVariation.draw(canvas);
    }

    public void drawAnyStone(Canvas canvas, int left, int top, int interval) {
        int stonePadding = _config.stonesPadding;
        anyStoneDrawable.setBounds(left + stonePadding, top + stonePadding, left + interval - stonePadding, top + interval - stonePadding);
        anyStoneDrawable.draw(canvas);
    }


    public static Bitmap makeBitmap(Drawable drawable, int bitmapWidth, int bitmapHeight, Bitmap.Config config) {
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, bitmapWidth, bitmapHeight);
        drawable.draw(canvas);
        return bitmap;
    }


    public static class Config {
        public final int surfaceWidth;
        public final int surfaceHeight;
        public final float stoneSize;
        public final int boardSize;
        public final int gridLineSize;
        public final int stonesPadding;


        public Config(int surfaceWidth, int surfaceHeight, float stoneSize, int boardSize, int gridLineSize, int stonesPadding) {
            this.surfaceWidth = surfaceWidth;
            this.surfaceHeight = surfaceHeight;
            this.stoneSize = stoneSize;
            this.boardSize = boardSize;
            this.gridLineSize = gridLineSize;
            this.stonesPadding = stonesPadding;
        }
    }
}
