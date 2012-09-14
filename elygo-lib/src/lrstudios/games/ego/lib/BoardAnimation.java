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

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;


public final class BoardAnimation
{
    public Drawable drawable;
    public int x;
    public int y;
    public int width;
    public int height;
    public int alpha;

    private double currentStep;
    public int startX;
    public int startY;
    private int endX;
    private int endY;
    private int startAlpha;
    private int endAlpha;
    public int startWidth;
    private int endWidth;
    public int startHeight;
    private int endHeight;


    public BoardAnimation(Drawable drawable, Rect startBounds, Rect endBounds, int startAlpha, int endAlpha)
    {
        this.drawable = drawable;
        startX = startBounds.left;
        startY = startBounds.top;
        startWidth = startBounds.width();
        startHeight = startBounds.height();
        endX = endBounds.left;
        endY = endBounds.top;
        endWidth = endBounds.width();
        endHeight = endBounds.height();
        this.startAlpha = startAlpha;
        this.endAlpha = endAlpha;

        x = startX;
        y = startY;
        width = startWidth;
        height = startHeight;
        alpha = startAlpha;
    }

    
    public void update(double stepSize)
    {
        currentStep += stepSize;
        if (currentStep > 1.0)
            currentStep = 1.0;

        x = (int)Math.round(startX + (endX - startX) * currentStep);
        y = (int)Math.round(startY + (endY - startY) * currentStep);
        width = (int)Math.round(startWidth + (endWidth - startWidth) * currentStep);
        height = (int)Math.round(startHeight + (endHeight - startHeight) * currentStep);
        alpha = (int)Math.round(startAlpha + (endAlpha - startAlpha) * currentStep);
    }


    public void draw(Canvas canvas)
    {
        drawable.setBounds(x, y, x + width, y + height);
        drawable.setAlpha(alpha);
        drawable.draw(canvas);
    }


    public boolean isFinished()
    {
        return (currentStep > 0.999);
    }
}
