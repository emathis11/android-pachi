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

import java.util.ArrayList;


/**
 * Represents a single node of a variation tree.
 */
public final class GameNode {
    public static byte COORD_UNDEFINED = -9;


    /**
     * The x coordinate of the move. Set to COORD_UNDEFINED (default value) if no move is played.
     */
    public byte x;
    /**
     * The y coordinate of the move. Set to COORD_UNDEFINED (default value) if no move is played.
     */
    public byte y;

    /**
     * The color of the move (either GoBoard.BLACK, GoBoard.WHITE or GoBoard.EMPTY if not used).
     */
    public byte color;

    /**
     * The move value, between 0 (lower) and 100 (better). Set to -1 if not used (default value).
     */
    public byte value = -1;

    /**
     * This remembers the last variation index choosed.
     */
    public byte lastVariation = 0;

    /**
     * A list containing the next nodes (never null).
     */
    public ArrayList<GameNode> nextNodes = new ArrayList<GameNode>(1);

    /**
     * The parent node of the current node (can be null if the node is the base node).
     */
    public GameNode parentNode;

    /**
     * Move comments are stored in UTF-8, because the default encoding (UTF-16) uses too much memory.
     */
    private byte[] _comment_utf8;

    /**
     * Contains all stones and empty intersections to be set on the board (SGF commands AB[], AW[], AE[]). Can be null.
     */
    public ArrayList<LightCoords> setStones;

    /**
     * Contains all marks set on the board (can be null).
     */
    public ArrayList<BoardMark> boardMarks;


    /**
     * Creates a new empty node with default values.
     */
    public GameNode() {
        x = y = COORD_UNDEFINED;
    }

    /**
     * Creates a new node with the specified parameters.
     */
    public GameNode(int x, int y, byte color) {
        this.x = (byte) x;
        this.y = (byte) y;
        this.color = color;
    }


    /**
     * Gets the comment associated to this node.
     */
    public String getComment() {
        if (_comment_utf8 == null)
            return "";
        try {
            return new String(_comment_utf8, "UTF-8");
        } catch (Exception ignored) {
            return "[Error]";
        }
    }

    /**
     * Sets the comment associated to this node.
     */
    public void setComment(String comment) {
        try {
            _comment_utf8 = comment.getBytes("UTF-8");
        } catch (Exception ignored) {
        }
    }

    /**
     * Adds a stone or empty intersection to be set on the board.
     */
    public void setStone(int x, int y, byte color) {
        if (setStones == null)
            setStones = new ArrayList<LightCoords>(6);

        LightCoords coords = new LightCoords(x, y, color);
        int index = setStones.indexOf(coords);
        if (index >= 0)
            setStones.remove(index);
        setStones.add(coords);
    }

    /**
     * Removes a stone to be set on the board.
     */
    public void unsetStone(int x, int y) {
        if (setStones == null)
            return;

        LightCoords coords = new LightCoords(x, y, GoBoard.EMPTY);
        int index = setStones.indexOf(coords);
        if (index >= 0)
            setStones.remove(index);
    }

    /**
     * Adds a mark to the current node.
     */
    public void addMark(BoardMark newMark) {
        if (boardMarks == null)
            boardMarks = new ArrayList<BoardMark>(4);
        else
            removeMark(newMark.x, newMark.y);
        boardMarks.add(newMark);
    }

    /**
     * Removes the mark placed on the specified coordinates.
     */
    public void removeMark(int x, int y) {
        for (BoardMark mark : boardMarks) {
            if (mark.x == x && mark.y == y) {
                boardMarks.remove(mark);
                break;
            }
        }
    }

    /**
     * Adds a node to the list of the next nodes.
     * If a node with the same color and coordinates already exists, this node is returned directly.
     *
     * @return The move added to the tree, or the existing node.
     */
    public GameNode addNode(int x, int y, byte color) {
        GameNode move = new GameNode(x, y, color);
        int index = nextNodes.indexOf(move);
        if (index < 0) {
            move.parentNode = this;
            move.value = value;
            nextNodes.add(move);
            return move;
        } else {
            return nextNodes.get(index);
        }
    }

    /**
     * Adds a move (and any associated following move) to the list of the next moves.
     * It is added even if a move played at the same coordinates already exists.
     */
    public void forceAddMove(GameNode move) {
        nextNodes.add(move);
        move.parentNode = this;
    }

    /**
     * Sets the value of the current node to the specified one. This also set the
     * value of all parent nodes as long as no higher value is encountered.
     */
    public void setMoveValue(byte value) {
        this.value = value;
        GameNode move = this;
        while ((move = move.parentNode) != null) {
            byte highestValue = -1;
            for (GameNode nextMove : move.nextNodes) {
                if (nextMove.value > highestValue)
                    highestValue = nextMove.value;
            }

            if (highestValue == move.value) // TODO does this really works?
                break;

            move.value = highestValue;
        }
    }


    /**
     * Returns true if the specified variable is a GameNode with the same color and coordinates as the current one.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof GameNode) {
            GameNode move = (GameNode) object;
            return x == move.x && y == move.y && color == move.color;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("[GameNode] " + color + ", x=" + x + ", y=" + y);
    }
}
