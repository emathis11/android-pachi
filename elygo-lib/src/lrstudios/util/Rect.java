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

package lrstudios.util;


public final class Rect
{
    public int top;
    public int left;
    public int bottom;
    public int right;


    /**
     * Creates a new rectangle with the coordinates set to 0.
     */
    public Rect()
    {
    }

    /**
     * Creates a new rectangle with the specified coordinates.
     */
    public Rect(int left, int top, int right, int bottom)
    {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }


    /**
     * Returns true if (x,y) is inside the Rectangle. Both left, top, bottom, right are considered to be inside.
     */
    public boolean contains(int x, int y)
    {
        return (x >= left && x <= right && y >= top && y <= bottom);
    }


    @Override
    public String toString()
    {
        return "[Rect] top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right;
    }
}
