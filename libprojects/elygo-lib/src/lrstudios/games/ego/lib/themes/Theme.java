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
import android.graphics.*;
import android.graphics.Path.Direction;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;


public abstract class Theme {
    private static final String TAG = "Theme";


    /**
     * Le bitmap contenant l'image d'une pierre noire.
     */
    public Bitmap blackStoneBitmap;

    /**
     * Le Drawable représentant une pierre blanche.
     */
    public Bitmap whiteStoneBitmap;

    /**
     * Objet Paint à utiliser pour dessiner les marques sur des pierres noires.
     */
    public Paint blackMarkPaint;
    /**
     * Objet Paint à utiliser pour dessiner les marques sur des pierres blanches.
     */
    public Paint whiteMarkPaint;
    /**
     * Objet Paint à utiliser pour dessiner les marques directement sur le goban.
     */
    public Paint boardMarkPaint;

    /**
     * Objet Paint à utiliser pour dessiner un texte sur une pierre noire.
     */
    public Paint blackLabelPaint;
    /**
     * Objet Paint à utiliser pour dessiner un texte sur une pierre blanche.
     */
    public Paint whiteLabelPaint;
    /**
     * Objet Paint à utiliser pour dessiner un texte directement sur le goban.
     */
    public Paint boardLabelPaint;

    /**
     * Objet Paint à utiliser pour dessiner les lignes du cross cursor.
     */
    public Paint crossCursorPaint;
    /**
     * Objet Paint à utiliser pour dessiner les lignes du cross cursor pour un coup illégal.
     */
    public Paint illegalCrossCursorPaint;

    /**
     * Objet Paint à utiliser pour dessiner les marques représentant une bonne variation.
     */
    public Paint goodVariationPaint;

    /**
     * Objet Paint à utiliser pour dessiner les marques représentant une mauvaise variation.
     */
    public Paint badVariationPaint;

    /**
     * Marque en forme de triangle.
     */
    public ShapeDrawable triangleMark;
    /**
     * Marque en forme de cercle.
     */
    public ShapeDrawable circleMark;
    /**
     * Marque en forme de croix.
     */
    public ShapeDrawable crossMark;
    /**
     * Marque en forme de carré.
     */
    public ShapeDrawable squareMark;

    public ShapeDrawable blackTerritory;
    public ShapeDrawable whiteTerritory;
    public ShapeDrawable anyStoneDrawable;

    protected Paint _hoshiPaint;
    protected Paint _gridPaint;
    protected Paint _anyPaint;
    protected PathShape _hoshiShape;
    protected PathShape _gridShape;
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

        _gridPaint = new Paint();
        _hoshiPaint = new Paint();

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
        crossCursorPaint.setStrokeWidth(3);

        illegalCrossCursorPaint = new Paint();
        illegalCrossCursorPaint.setColor(Color.rgb(232, 25, 25));
        illegalCrossCursorPaint.setStrokeWidth(2);

        goodVariationPaint = new Paint();
        goodVariationPaint.setAntiAlias(true);
        goodVariationPaint.setColor(Color.rgb(0, 200, 0));

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

        final float intervalDIV2 = stoneSize / 2.0f;
        final float boardWidth = stoneSize * boardSize;
        final float lineEnd = boardWidth - intervalDIV2;

        // Grille
        Path path = new Path();
        for (int i = 0; i < boardSize; i++) {
            final float pos = intervalDIV2 + stoneSize * i;
            path.moveTo(pos, intervalDIV2);
            path.lineTo(pos, lineEnd);
            path.moveTo(intervalDIV2, pos);
            path.lineTo(lineEnd, pos);
        }
        _gridShape = new PathShape(path, boardWidth, boardWidth);


        // Hoshis
        path = new Path();
        float radius = intervalDIV2 / 4.0f;
        int decal = (boardSize > 12) ? 7 : 5;

        path.addCircle(intervalDIV2 * decal, intervalDIV2 * decal, radius, Direction.CW);
        path.addCircle(boardWidth - intervalDIV2 * decal, intervalDIV2 * decal, radius, Direction.CW);
        path.addCircle(intervalDIV2 * decal, boardWidth - intervalDIV2 * decal, radius, Direction.CW);
        path.addCircle(boardWidth - intervalDIV2 * decal, boardWidth - intervalDIV2 * decal, radius, Direction.CW);

        if (boardSize % 2 == 1) {
            path.addCircle(boardWidth / 2f, boardWidth / 2f, radius, Direction.CW);
            if (boardSize > 10) {
                path.addCircle(boardWidth / 2f, intervalDIV2 * decal, radius, Direction.CW);
                path.addCircle(boardWidth / 2f, boardWidth - intervalDIV2 * decal, radius, Direction.CW);
                path.addCircle(intervalDIV2 * decal, boardWidth / 2f, radius, Direction.CW);
                path.addCircle(boardWidth - intervalDIV2 * decal, boardWidth / 2f, radius, Direction.CW);
            }
        }
        _hoshiShape = new PathShape(path, boardWidth, boardWidth);

        int surfaceHeight = config.surfaceHeight;
        int surfaceWidth = config.surfaceWidth;
        int surfaceSmallestSize = (surfaceWidth > surfaceHeight) ? surfaceHeight : surfaceWidth;
        _gridShape.resize(surfaceSmallestSize, surfaceSmallestSize);
        _hoshiShape.resize(surfaceSmallestSize, surfaceSmallestSize);


        /***** Marques ******/

        // Marques de décompte
        float margin = stoneSize / 4.0f;
        path = new Path();
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


        /***** Stones *****/
        int newStoneSize = Math.round(stoneSize) - config.stonesPadding * 2;
        blackStoneBitmap = createBlackStoneBitmap(newStoneSize);
        whiteStoneBitmap = createWhiteStoneBitmap(newStoneSize);
    }


    /**
     * Dessine la grille sur le canvas spécifié.
     */
    public void drawGrid(Canvas canvas) {
        _gridShape.draw(canvas, _gridPaint);
        _hoshiShape.draw(canvas, _hoshiPaint);
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
