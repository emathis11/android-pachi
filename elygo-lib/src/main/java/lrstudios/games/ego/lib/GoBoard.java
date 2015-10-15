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

import android.graphics.Rect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lrstudios.util.io.BitReader;
import lrstudios.util.io.BitWriter;


/**
 * Represents a go board.
 */
public final class GoBoard implements Cloneable {
    // Constantes
    public static final byte
            EMPTY = 0,
            BLACK = 1,
            WHITE = 2,
            FORBIDDEN = 3,
            BLACK_TERRITORY = 4,
            WHITE_TERRITORY = 5,
            DEAD_BLACK_STONE = 6,
            DEAD_WHITE_STONE = 7,
            ANY = 8;

    private static final byte DATA_TYPE_MARK = 1;


    // Variables
    private int _size;
    private byte[] _board;
    private ArrayList<BoardMark> _marks = new ArrayList<>();

    private boolean[] _loop_passed;
    private byte _loop_color;
    private boolean _loop_removeStones;
    private ArrayList<Coords> _loop_stoneList;

    private Coords _ko_prisoner;


    /**
     * Creates a new Go board with the specified size.
     */
    public GoBoard(int size) {
        this(size, new byte[size * size]);
    }

    /**
     * Creates a new Go board with the specified size and stones.
     */
    public GoBoard(int size, byte[] colors) {
        _size = size;
        _board = colors;
    }

    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        GoBoard clone = new GoBoard(_size);

        System.arraycopy(_board, 0, clone._board, 0, _board.length);
        clone._marks = new ArrayList<>();
        clone._marks.addAll(_marks);
        if (_ko_prisoner != null)
            clone._ko_prisoner = new Coords(_ko_prisoner.x, _ko_prisoner.y);
        return clone;
    }

    /**
     * Sets the color of an intersection. The first intersection is (0; 0)
     */
    public void set(int x, int y, byte color) {
        _board[y * _size + x] = color;
    }

    /**
     * Sets the mark to display on the specified intersection.
     */
    public void setMark(BoardMark newMark) {
        for (BoardMark mark : _marks) {
            if (mark.x == newMark.x && mark.y == newMark.y) {
                _marks.remove(mark);
                break;
            }
        }
        _marks.add(newMark);
    }


    /**
     * Gets the size of the board.
     */
    public int getSize() {
        return _size;
    }

    /**
     * Gets the color of an intersection. The first intersection is (0; 0)
     */
    public byte getColor(int x, int y) {
        return _board[y * _size + x];
    }

    public boolean isEmpty(int x, int y) {
        byte color = getColor(x, y);
        return color != GoBoard.WHITE && color != GoBoard.BLACK;
    }

    /**
     * Gets the mark of an intersection. Returns null if no mark is found. The first intersection is (0; 0)
     */
    public BoardMark getMark(int x, int y) {
        for (BoardMark mark : _marks) {
            if (mark.x == x && mark.y == y)
                return mark;
        }

        return null;
    }


    /**
     * Gets an array containing the board colors of each intersection.
     * <p/>
     * To get a specified (x, y) intersection, use : (y * boardSize + x)
     */
    public byte[] getBoardArray() {
        return _board;
    }

    /**
     * Gets an array containing the board marks (like triangles, squares, letters, ...).
     */
    public ArrayList<BoardMark> getMarks() {
        return _marks;
    }

    /**
     * Supprime toutes les marques actuelles sur le goban.
     */
    public void removeMarks() {
        _marks.clear();
    }


    /**
     * Reverse the colors of the board : black stones become white, white stones become black.
     */
    public void reverseColors() {
        int len = _board.length;
        for (int i = 0; i < len; i++) {
            if (_board[i] == BLACK)
                _board[i] = WHITE;
            else if (_board[i] == WHITE)
                _board[i] = BLACK;
        }
    }


    /**
     * Plays the specified move on the board without modifying anything else than the board.
     * Stones without any liberty will be removed from the board.
     */
    public List<Coords> placeMove(int x, int y, byte color) {
        return placeMove(x, y, color, true);
    }

    /**
     * Plays the specified move on the board without modifying anything else than the board.
     *
     * @param captureStones Set to true to remove stones without any liberties after placing the move.
     */
    public List<Coords> placeMove(int x, int y, byte color, boolean captureStones) {
        set(x, y, color);
        if (!captureStones)
            return null;

        byte oppColor = GoBoard.getOppositeColor(color);
        List<Coords> prisoners = new ArrayList<Coords>();

        // Capture every group without any liberty on the board (only stones of the opposite color)
        if (x + 1 < _size && getColor(x + 1, y) == oppColor && !hasLiberty(x + 1, y))
            prisoners.addAll(removeStones(x + 1, y));
        if (x - 1 >= 0 && getColor(x - 1, y) == oppColor && !hasLiberty(x - 1, y))
            prisoners.addAll(removeStones(x - 1, y));
        if (y + 1 < _size && getColor(x, y + 1) == oppColor && !hasLiberty(x, y + 1))
            prisoners.addAll(removeStones(x, y + 1));
        if (y - 1 >= 0 && getColor(x, y - 1) == oppColor && !hasLiberty(x, y - 1))
            prisoners.addAll(removeStones(x, y - 1));

        return prisoners;
    }


    public boolean isLegal(int x, int y, byte color) {
        boolean result = true;
        int size = getSize();

        if (x < 0 || y < 0 || x >= size || y >= size || getColor(x, y) != GoBoard.EMPTY) {
            result = false;
        }
        else {
            // Suicide ?
            set(x, y, color);
            if (!hasLiberty(x, y)) {
                byte oppColor = getOppositeColor(color);
                result = false;

                // Suicide OK si capture d'un groupe, sauf en cas de ko
                if ((x + 1 < size && getColor(x + 1, y) == oppColor && !hasLiberty(x + 1, y))
                        || (x - 1 >= 0 && getColor(x - 1, y) == oppColor && !hasLiberty(x - 1, y))
                        || (y + 1 < size && getColor(x, y + 1) == oppColor && !hasLiberty(x, y + 1))
                        || (y - 1 >= 0 && getColor(x, y - 1) == oppColor && !hasLiberty(x, y - 1)))
                {
                    result = (_ko_prisoner == null || x != _ko_prisoner.x || y != _ko_prisoner.y);
                }
            }
            set(x, y, GoBoard.EMPTY);
        }

        return result;
    }


    /**
     * Returns the coordinates of the actual forbidden move due to a ko on the board,
     * or null if there isn't any ko.
     */
    public Coords getKoCoords() {
        return _ko_prisoner;
    }

    void setKoCoords(int x, int y) {
        _ko_prisoner = (x < 0) ? null : new Coords(x, y);
    }


    /**
     * Returns true if the group placed on the specified coordinates has one liberty or more.
     */
    public boolean hasLiberty(int x, int y) {
        _loop_color = getColor(x, y);
        if (_loop_color == EMPTY)
            return false;

        // Used to not check the same intersections twice
        if (_loop_passed == null)
            _loop_passed = new boolean[_size * _size];
        else
            Arrays.fill(_loop_passed, false);

        return hasLiberty_loop(x, y);
    }

    /**
     * Used by hasLiberty() as a recursive function to find a liberty of a group.
     */
    private boolean hasLiberty_loop(int x, int y) {
        int intersection = y * _size + x;
        if (_loop_passed[intersection])
            return false;

        _loop_passed[intersection] = true;

        return
                // Liberty found
                ((x + 1 < _size && getColor(x + 1, y) == EMPTY)
                        || (x - 1 >= 0 && getColor(x - 1, y) == EMPTY)
                        || (y + 1 < _size && getColor(x, y + 1) == EMPTY)
                        || (y - 1 >= 0 && getColor(x, y - 1) == EMPTY)

                        // Search for other liberties
                        || (x + 1 < _size && getColor(x + 1, y) == _loop_color && hasLiberty_loop(x + 1, y))
                        || (x - 1 >= 0 && getColor(x - 1, y) == _loop_color && hasLiberty_loop(x - 1, y))
                        || (y + 1 < _size && getColor(x, y + 1) == _loop_color && hasLiberty_loop(x, y + 1))
                        || (y - 1 >= 0 && getColor(x, y - 1) == _loop_color && hasLiberty_loop(x, y - 1)));
    }


    /**
     * Removes the group of stones placed on the specified coordinates.
     * Returns a list which contains every stone removed this way, or null
     * if the specified intersection is empty.
     */
    public List<Coords> removeStones(int x, int y) {
        return listStonesInGroup(x, y, true);
    }

    public List<Coords> listStonesInGroup(int x, int y, boolean removeStones) {
        _loop_color = getColor(x, y);
        if (_loop_color == EMPTY)
            return new ArrayList<>();

        _loop_stoneList = new ArrayList<>();
        _loop_removeStones = removeStones;
        _loop_passed = new boolean[_size * _size];

        removeStones_loop(x, y);

        return _loop_stoneList;
    }

    /**
     * Used by removeStones() as a recursive function to remove a group of stones from the board.
     */
    private void removeStones_loop(int x, int y) {
        if (_loop_passed[y * _size + x])
            return;

        if (_loop_removeStones)
            set(x, y, EMPTY);

        _loop_passed[y * _size + x] = true;
        _loop_stoneList.add(new Coords(x, y));

        if (x + 1 < _size && getColor(x + 1, y) == _loop_color)
            removeStones_loop(x + 1, y);
        if (x - 1 >= 0 && getColor(x - 1, y) == _loop_color)
            removeStones_loop(x - 1, y);
        if (y + 1 < _size && getColor(x, y + 1) == _loop_color)
            removeStones_loop(x, y + 1);
        if (y - 1 >= 0 && getColor(x, y - 1) == _loop_color)
            removeStones_loop(x, y - 1);
    }


    /**
     * Rotates the board by 90° CCW.
     */
    public void rotateCCW() {
        byte[] tempBoard = new byte[_board.length];
        System.arraycopy(_board, 0, tempBoard, 0, _board.length);

        for (int x = 0; x < _size; x++)
            for (int y = 0; y < _size; y++)
                set(y, _size - x - 1, tempBoard[y * _size + x]);
    }


    /**
     * Returns the smallest possible bounds which contains every stone on the board.
     * If there are no stones on the board, the bounds will be set to be outside of the board
     * (bottom and right will be set to -1).
     */
    public Rect getBounds() {
        final Rect bounds = new Rect(_size, _size, -1, -1);

        for (int x = 0; x < _size; x++) {
            for (int y = 0; y < _size; y++) {
                if (getColor(x, y) != GoBoard.EMPTY) {
                    if (bounds.left > x) bounds.left = x;
                    if (bounds.top > y) bounds.top = y;
                    if (bounds.right < x) bounds.right = x;
                    if (bounds.bottom < y) bounds.bottom = y;
                }
            }
        }
        return bounds;
    }


    /**
     * Removes all stones and marks from the board.
     */
    public void clear() {
        _marks.clear();
        _ko_prisoner = null;
        Arrays.fill(_board, (byte) 0);
    }

    public void fill(byte color) {
        Arrays.fill(_board, color);
    }


    /**
     * Returns the opposite color of the specified one.
     */
    public static byte getOppositeColor(byte color) {
        return (color == GoBoard.BLACK) ? GoBoard.WHITE : GoBoard.BLACK;
    }

    /**
     * Encodes the specified coordinates in a single integer. Decode them with {@link #decodeCoords(int, int)}.
     */
    public static int encodeCoords(int x, int y, int size) {
        return y * size + x;
    }

    /**
     * Decodes the specified encoded coordinates.
     */
    public static Coords decodeCoords(int intersection, int size) {
        return new Coords(intersection % size, intersection / size);
    }


//---- IMPORT/EXPORT FUNCTIONS ------------------------------------------

    /**
     * Exports this board in LRF format.
     * See the LRF Specification to know more about the resulting data structure.
     * Although this method does not produce a valid LRF file (only a part of it),
     * it can be used to save a board state in a lightweight binary format.
     *
     * @throws IOException An error occured during writing.
     */
    public void exportLrf(BitWriter writer) throws IOException {
        // Header
        writer.write(_size, 5);

        // Bounds
        int lbx = _size, lby = _size;
        int hbx = -1, hby = -1;

        for (int x = 0; x < _size; x++) {
            for (int y = 0; y < _size; y++) {
                if (getColor(x, y) != GoBoard.EMPTY) {
                    if (lbx > x) lbx = x;
                    if (lby > y) lby = y;
                    if (hbx < x) hbx = x;
                    if (hby < y) hby = y;
                }
            }
        }

        // If the board is not empty...
        if (hbx > -1) {
            writer.write(lby * _size + lbx, 9);
            writer.write(hby * _size + hbx, 9);

            for (int x = lbx; x <= hbx; x++) {
                for (int y = lby; y <= hby; y++) {
                    byte color = getColor(x, y);
                    if (color == GoBoard.EMPTY)
                        writer.write(0, 1);
                    else
                        writer.write((color == GoBoard.WHITE) ? 3 : 2, 2);
                }
            }
        }
        else {
            writer.write(1, 9); // Set the UL bound higher than the BR bound to tell it's an empty board
            writer.write(0, 9);
        }

        // Total amount of optional data
        writer.write(_marks.size(), 9);

        for (BoardMark mark : _marks) {
            writer.write(DATA_TYPE_MARK, 2);
            writer.write(mark.getIntersection(_size), 9);
            writer.write(mark.type, 8);
        }
    }

    /**
     * Creates a GoBoard with the given data.
     *
     * @throws IOException An error occured during reading.
     */
    public static GoBoard importLrf(BitReader reader) throws IOException {
        // Header
        int size = (int) reader.read(5);

        GoBoard board = new GoBoard(size);

        int interL = (int) reader.read(9);
        int interH = (int) reader.read(9);
        int lbx = interL % size, lby = interL / size;
        int hbx = interH % size, hby = interH / size;

        // If the board isn't empty...
        if (interL <= interH) {
            for (int x = lbx; x <= hbx; x++) {
                for (int y = lby; y <= hby; y++) {
                    if (!reader.read())
                        board.set(x, y, GoBoard.EMPTY);
                    else
                        board.set(x, y, (reader.read()) ? GoBoard.WHITE : GoBoard.BLACK);
                }
            }
        }


        // Optional data
        int optionalCount = (int) reader.read(9);

        for (int i = 0; i < optionalCount; i++) {
            byte data_type = (byte) reader.read(2);

            if (data_type == DATA_TYPE_MARK) {
                short intersection = (short) reader.read(8);
                board._marks.add(new BoardMark((byte) (intersection % size), (byte) (intersection / size), (byte) reader.read(9)));
            }
        }

        return board;
    }
}
