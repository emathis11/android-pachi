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
 * Represents a point defined by two coordinates.
 */
public final class Coords
{
	public int x;
	public int y;
	
	
	public Coords()
	{
	}
	public Coords(final int x, final int y)
	{
		this.x = x;
		this.y = y;
	}

    public void set(final int x, final int y)
    {
        this.x = x;
        this.y = y;
    }


	@Override
    public String toString()
	{
		return String.format("(%d, %d)", x, y);
	}


    /**
     * Returns true if the specified object have the same coordinates that the current object.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Coords)
        {
            Coords coords = (Coords) obj;
            return x == coords.x && y == coords.y;
        }
        return false;
    }
}
