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
 * Represents a board mark with a label (usually letters or digits).
 */
public final class BoardLabelMark extends BoardMark
{
    protected char _label;

    public BoardLabelMark(int x, int y, char label)
    {
        super(x, y, MARK_LABEL);
        _label = label;
    }


    /** {@inheritDoc} */
    @Override
    public char getLabel()
    {
        return _label;
    }
}
