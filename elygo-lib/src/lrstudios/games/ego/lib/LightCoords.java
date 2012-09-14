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

/**
 * These move coordinates are stored in 3 bytes, to reduce memory usage on Android.
 */
public final class LightCoords
{
    public byte x;
    public byte y;
    public byte color;

    public LightCoords(int x, int y, byte color)
    {
        this.x = (byte) x;
        this.y = (byte) y;
        this.color = color;
    }


    /** Ne compare que les coordonnées, pas la couleur. */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof LightCoords)
        {
            LightCoords coords = (LightCoords) obj;
            return x == coords.x && y == coords.y;
        }
        return false;
    }
}
