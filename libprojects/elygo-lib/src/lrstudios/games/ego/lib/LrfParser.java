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

import lrstudios.util.Rect;
import lrstudios.util.io.BitReader;
import lrstudios.util.io.BitWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

/**
 * Provides methods to parse and save LRF files from/to an instance of GoGame.
 * See the LRF_SPEC file for more informations about this format.
 */
public final class LrfParser {
    private GoGame _game;
    private BitWriter _writer_loop;
    private int _lrf_bits;
    private Rect _lrf_bounds;


    public LrfParser() {
    }


    public GoBoard parseBoard(InputStream stream) throws IOException {
        return GoBoard.importLrf(new BitReader(stream));
    }

    public GoGame parse(InputStream stream) throws IOException {
        // Load the base position
        final BitReader reader = new BitReader(stream);
        final GoBoard board = GoBoard.importLrf(reader);
        final GoGame game = new GoGame(board, 6.5);
        final int size = board.getSize();

        // Load the move tree if it exists
        if (reader.read()) {
            Coords topLeft = GoBoard.decodeCoords((int) reader.read(9), size);
            Coords bottomRight = GoBoard.decodeCoords((int) reader.read(9), size);
            Rect bounds = new Rect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
            final int shiftY = bounds.right - bounds.left + 1;
            final int bits = _getRequiredBits(bounds);

            final Stack<Integer> offsetStack = new Stack<Integer>();
            int currentPos = 0;

            offsetStack.push(0);
            while (!offsetStack.empty()) {
                if (reader.read()) // COMMAND_PLAY_MOVE
                {
                    final Coords coords = GoBoard.decodeCoords((int) reader.read(bits), shiftY);
                    game.placeMove(coords.x + bounds.left, coords.y + bounds.top);
                    currentPos++;
                } else {
                    switch ((int) reader.read(2)) {
                        case 1: // COMMAND_SET_RESULT
                            game.setMoveValue((byte) reader.read(7));
                            break;
                        case 2: // COMMAND_NEW_NODE
                            offsetStack.push(currentPos);
                            break;
                        case 3: // COMMAND_END_NODE
                            int nextPos = offsetStack.pop();
                            game.navigate(nextPos - currentPos);
                            currentPos = nextPos;
                            break;
                        default: // special command
                            System.out.println("Special command???");
                            break;
                    }
                }
            }
        } else
            System.out.println("No variations stored in this LRF.");

        return game;
    }


    public void save(GoGame game, OutputStream stream) throws IOException {
        BitWriter writer = new BitWriter(stream);
        GoBoard board = game.board;
        _game = game;
        String firstPlayer = game.info.firstPlayer;
        boolean reverseColors = (firstPlayer != null && firstPlayer.length() > 0
                && Character.toUpperCase(firstPlayer.charAt(0)) == 'W');

        if (reverseColors) {
            GoBoard newBoard = new GoBoard(board.getSize());
            byte[] colors = board.getBoardArray();
            byte[] newColors = newBoard.getBoardArray();
            int len = colors.length;
            for (int i = 0; i < len; i++) {
                byte color = colors[i];
                if (color == GoBoard.WHITE)
                    newColors[i] = GoBoard.BLACK;
                else if (color == GoBoard.BLACK)
                    newColors[i] = GoBoard.WHITE;
            }
            board = newBoard;
        }
        board.exportLrf(writer);

        // Noeuds
        if (game.getBaseNode().nextNodes.size() > 0) {
            writer.write(true);

            // Write tree bounds
            _lrf_bounds = _getTreeBounds();
            _lrf_bits = _getRequiredBits(_lrf_bounds);
            writer.write(GoBoard.encodeCoords(_lrf_bounds.left, _lrf_bounds.top, game.board.getSize()), 9);
            writer.write(GoBoard.encodeCoords(_lrf_bounds.right, _lrf_bounds.bottom, game.board.getSize()), 9);
            _writer_loop = writer;
            _save_loop(game.getBaseNode());
        } else {
            writer.write(false);
        }
        writer.flush();
    }


    /**
     * Recursive function to convert the current game tree in LRF format.
     */
    private void _save_loop(GameNode move) throws IOException {
        int moveCount = move.nextNodes.size();

        // In LRF format, we assume that black plays first
        if (move.color != GoBoard.EMPTY) {
            _writer_loop.write(true); // bit 1 = COMMAND_PLAY_MOVE
            _writer_loop.write(GoBoard.encodeCoords(move.x - _lrf_bounds.left, move.y - _lrf_bounds.top,
                    _lrf_bounds.right - _lrf_bounds.left + 1), _lrf_bits);

            if (moveCount == 0) {
                _writer_loop.write(false);
                _writer_loop.write(1, 2); // bits 01 = COMMAND_SET_RESULT
                _writer_loop.write((move.value < 0) ? 0 : move.value, 7);
                _writer_loop.write(false);
                _writer_loop.write(3, 2); // bits 11 = COMMAND_END_NODE
            }
        }

        int count = 0;
        for (GameNode nextMove : move.nextNodes) {
            // Create new branches for all nodes except the last one
            if (count < moveCount - 1) {
                _writer_loop.write(false);
                _writer_loop.write(2, 2); // bits 010 = COMMAND_NEW_NODE
            }

            _save_loop(nextMove);
            count++;
        }
    }


    /**
     * Obtient le nombre maximum de bits qu'il faut pour stocker les coordonnées des intersections sous forme
     * de nombre allant de 0 à n, où n est le nombre d'intersections comprises dans le rectangle.
     */
    private int _getRequiredBits(Rect bounds) {
        int area = (bounds.bottom - bounds.top + 1) * (bounds.right - bounds.left + 1) - 1;
        int bits = 0;
        while (area > 0) {
            area >>= 1;
            bits++;
        }
        return bits;
    }


    private Rect _getTreeBounds() {
        final int size = _game.board.getSize();
        final Rect bounds = new Rect(size, size, -1, -1);

        GameNode move;
        Stack<GameNode> stack = new Stack<GameNode>();
        stack.push(_game.getBaseNode());

        while (!stack.empty()) {
            move = stack.pop();
            if (move.x >= 0 && move.y >= 0) {
                if (bounds.left > move.x) bounds.left = move.x;
                if (bounds.top > move.y) bounds.top = move.y;
                if (bounds.right < move.x) bounds.right = move.x;
                if (bounds.bottom < move.y) bounds.bottom = move.y;
            }

            for (GameNode nextMove : move.nextNodes)
                stack.push(nextMove);
        }

        return bounds;
    }
}
