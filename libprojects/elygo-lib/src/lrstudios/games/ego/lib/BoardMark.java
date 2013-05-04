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
 * Represents a mark on a Go Board (like triangles, squares, letters, ...).
 */
public class BoardMark {
    public static final byte
            NO_MARK = 0,
            MARK_TRIANGLE = 1,
            MARK_CIRCLE = 2,
            MARK_SQUARE = 3,
            MARK_CROSS = 4,
            MARK_WHITE_TERRITORY = 5,
            MARK_BLACK_TERRITORY = 6,
            MARK_WHITE_TRANSPARENT = 7,
            MARK_BLACK_TRANSPARENT = 8,
            MARK_LABEL = 9,
            MARK_ADD_BLACK = 50,
            MARK_ADD_WHITE = 51,
            MARK_ADD_EMPTY = 52;


    /**
     * The mark type : one of the MARK_* constants.
     */
    public byte type;
    public byte x;
    public byte y;


    /**
     * Creates a new Board Mark.
     */
    public BoardMark(int x, int y, byte type) {
        this.type = type;
        this.x = (byte) x;
        this.y = (byte) y;
    }

    public short getIntersection(int size) {
        return (short) (y * size + x);
    }


    /**
     * Returns the label associated with this mark (or '\0' if there is none).
     */
    public char getLabel() {
        return 0;
    }


    @Override
    public String toString() {
        return "[BoardMark] type = " + type + " at (" + x + ", " + y + ")";
    }
}
