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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;


/**
 * Represents a go game that contains a move tree and some other informations.
 */
public class GoGame {
    private static final String TAG = "GoGame";

    public static final int BASE_NODE_COORD = -9;

    public GameInfo info = new GameInfo();
    public int gameNumber;
    public GoBoard board;
    public GoBoard finalStatus;

    protected byte _currentPlayer = GoBoard.BLACK;
    private boolean _showMoveNumbers = false;

    protected int _size;
    protected int _whitePrisoners;
    protected int _blackPrisoners;

    protected Stack<MoveInfo> _playedMoves = new Stack<MoveInfo>();
    protected GameNode _baseNode;
    protected GameNode _currentNode;
    protected GameNode _playNode;

    private boolean[][] _loop_passed;
    private boolean[][] _loop_localPassed;
    private int _loop_markStoneType;
    private int _loop_count;


    /**
     * Creates a new game with the specified parameters. The handicap isn't placed automatically
     * for New-zealand and Chinese rules.
     */
    public GoGame(int boardSize, double komi, int handicap, String rules) {
        this(new GoBoard(boardSize), komi);
        info.handicap = handicap;

        if (rules == null || rules.length() == 0)
            rules = "Japanese";
        info.rules = rules;
        if (!rules.equalsIgnoreCase("NZ") && !rules.equalsIgnoreCase("Chinese"))
            placeHandicap(handicap);
    }

    /**
     * Creates a new game with the specified parameters. The rule will be the Japanese rule.
     */
    public GoGame(int boardSize, double komi, int handicap) {
        this(boardSize, komi, handicap, "Japanese");
    }

    /**
     * Creates a new game from the specified base position.
     */
    public GoGame(GoBoard board, double komi) {
        this.board = board;
        info.komi = komi;
        _size = board.getSize();
        info.boardSize = _size;
        resetFinalStatus();

        _baseNode = new GameNode(BASE_NODE_COORD, BASE_NODE_COORD, GoBoard.EMPTY);
        _currentNode = _baseNode;

        ArrayList<LightCoords> setStones = new ArrayList<LightCoords>(16);
        byte[] colors = board.getBoardArray();
        for (int x = 0; x < _size; x++) {
            for (int y = 0; y < _size; y++) {
                byte color = colors[y * _size + x];
                if (color != GoBoard.EMPTY)
                    setStones.add(new LightCoords(x, y, color));
            }
        }
        _baseNode.setStones = setStones;
        updateMarks();
    }

    GoGame(GameInfo gameInfo, GameNode baseNode) {
        this(gameInfo.boardSize, gameInfo.komi, 0, gameInfo.rules);
        info = gameInfo;
        _baseNode = baseNode;
        _currentNode = baseNode;
        gotoFirstMove();
        _setRequestedStones();
        updateMarks();
    }


    /**
     * Plays a move on the board. Returns false if the move was illegal (and thus was not played).
     */
    public boolean playMove(int x, int y) {
        return playMove(x, y, _currentPlayer);
    }

    /**
     * Plays a move on the board. Returns false if the move was illegal (and thus was not played).
     */
    public boolean playMove(Coords coords) {
        return playMove(coords.x, coords.y, _currentPlayer);
    }

    /**
     * Plays a move on the board. Returns false if the move was illegal (and thus was not played).<br>
     * Set x to -1 to pass.<br>
     * The move will be added to the variation tree and a mark will be placed on this move.
     * TODO si un ko blanc existe et qu'on rejoue un coup blanc immédiatement dedans, ca devrait être valide
     */
    public boolean playMove(int x, int y, byte color) {
        if (_playNode == null || _playNode == _currentNode) {
            if (x >= 0 && !board.isLegal(x, y, color))
                return false;
            placeMove(x, y, color);
            if (_playNode != null)
                _playNode = _currentNode;
        }
        else {
            GameNode node = new GameNode(x, y, color);
            node.parentNode = _playNode;
            _playNode.nextNodes.add(node);
            _playNode = node;
        }
        return true;
    }


    /**
     * The current player passes.
     */
    public void pass() {
        playMove(-1, -1, _currentPlayer);
    }

    /**
     * The specified player resigns the game.
     */
    public void resign(byte color) {
        char winnerChar = (color == GoBoard.BLACK) ? GoGameResult.WHITE : GoGameResult.BLACK;
        info.result = new GoGameResult(winnerChar, GoGameResult.RESIGN);
    }

    /**
     * Returns true if the game is finished (= a result is set for this game).
     */
    public boolean isFinished() {
        return info.result != null;
    }


    /**
     * Returns the current move number of this game.
     */
    public int getCurrentMoveNumber() {
        return _playedMoves.size();
    }


    /**
     * Places a move on the board, without checking its legality.
     * This move will become the current one.
     */
    public void placeMove(int x, int y, byte color) {
        if (x >= _size) // In some old SGF files, a pass was a move outside of the board
        {
            x = -1;
            y = -1;
        }
        board.setKoCoords(-1, -1);
        List<Coords> prisoners = null;
        if (x >= 0 && y >= 0) {
            prisoners = board.placeMove(x, y, color);
            if (prisoners.size() == 1) {
                // Check if the move produces a ko
                byte[] colors = board.getBoardArray();
                byte[] savedColors = new byte[colors.length];
                System.arraycopy(colors, 0, savedColors, 0, colors.length);

                Coords prisoner = prisoners.get(0);
                List<Coords> newPrisoners = board.placeMove(prisoner.x, prisoner.y, GoBoard.getOppositeColor(color));
                System.arraycopy(savedColors, 0, colors, 0, colors.length);

                if (newPrisoners.size() == 1)
                    board.setKoCoords(prisoner.x, prisoner.y);
            }

            if (color == GoBoard.WHITE)
                _whitePrisoners += prisoners.size();
            else
                _blackPrisoners += prisoners.size();
        }

        _addToTree(x, y, color, prisoners);
        switchCurrentPlayer();
        _setRequestedStones();
        updateMarks();
    }

    /**
     * Places a move on the board of the color of the current player, without checking its legality.
     * This move will become the current one.
     */
    public void placeMove(int x, int y) {
        placeMove(x, y, _currentPlayer);
    }


    /**
     * Cancels the current move, removes it from the game tree and returns it (or null if
     * there was no move to undo). TODO validité d'un undo au milieu d'une variation...?
     */
    public GameNode undo(boolean removeFromTree) {
        GameNode move = _currentNode;
        GameNode parentMove = move.parentNode;
        if (parentMove == null) // Only the base move have parentNode set to null
            return null;

        if (move.x >= 0) {
            // Supprimer la dernière pierre
            board.set(move.x, move.y, GoBoard.EMPTY);
        }

        // Replacer la position précédente sur le goban (prisoniers + commandes SGF type AB[])
        MoveInfo moveInfo = _playedMoves.pop();
        if (moveInfo.prisoners != null && moveInfo.prisoners.size() > 0) {
            for (LightCoords coords : moveInfo.prisoners)
                board.set(coords.x, coords.y, coords.color);

            if (moveInfo.prisoners.get(0).color == GoBoard.WHITE)
                _blackPrisoners -= moveInfo.prisoners.size();
            else
                _whitePrisoners -= moveInfo.prisoners.size();
        }
        if (_currentNode.setStones != null && _currentNode.setStones.size() > 0) {
            for (LightCoords coords : _currentNode.setStones)
                board.set(coords.x, coords.y, GoBoard.EMPTY);
        }
        if (moveInfo.removedStones != null && moveInfo.removedStones.size() > 0) {
            for (LightCoords coords : moveInfo.removedStones)
                board.set(coords.x, coords.y, coords.color);
        }

        if (!_playedMoves.empty()) {
            Coords ko = _playedMoves.peek().ko;
            if (ko != null)
                board.setKoCoords(ko.x, ko.y);
            else
                board.setKoCoords(-1, -1);
        }

        _currentNode = _currentNode.parentNode;
        if (removeFromTree)
            _currentNode.nextNodes.remove(move);

        updateMarks();
        switchCurrentPlayer();
        return move;
    }


    /**
     * Allows to navigate the game history.<br/>
     * If the argument is -1, the last move (if it exists) will become the current move of the game.
     * If it is -2, it will be the next to last, ...
     * If it is 1, it will be the next move (always from the main variation), and so on.
     *
     * @return The real number of moves navigated.
     */
    public int navigate(int amount) {
        int count = 0;

        // Nombre positif
        for (int i = 0; i < amount; i++) {
            int size = _currentNode.nextNodes.size();
            if (size == 0)
                break;

            int lastVariation = _currentNode.lastVariation;
            GameNode move = _currentNode.nextNodes.get(lastVariation < 0 || lastVariation >= size ? 0 : lastVariation);
            placeMove(move.x, move.y, move.color);
            count++;
        }

        // Nombre négatif
        for (int i = 0; i > amount; i--) {
            if (_currentNode.parentNode == null)
                break;
            undo(false);
            count++;
        }

        if (_currentNode.parentNode == null && _currentNode.x >= 0 && _currentNode.y >= 0)
            board.set(_currentNode.x, _currentNode.y, _currentNode.color);
        return count;
    }


    /**
     * Sets the current move to the next variation available (or do nothing if there is none).
     */
    public void gotoNextVariation() {
        _changeVariation(true);
    }

    /**
     * Sets the current move to the previous variation available (or do nothing if there is none).
     */
    public void gotoPreviousVariation() {
        _changeVariation(false);
    }

    /**
     * Sets to true to append the next played move to the current node, even if the game is
     * navigated and the current node changed. Every following move will be appended next
     * to each other.
     */
    public void playAtCurrentNode(boolean enable) {
        _playNode = enable ? _currentNode : null;
    }

    /**
     * Resets the final status of stones.
     */
    public void resetFinalStatus() {
        finalStatus = new GoBoard(_size);
    }


    /**
     * Sets the value of the current move (0 = wrong, 100 = right, -1 = unused).<br/>
     * This can also change the previous move values to show the maximum value
     * which can be reached by playing them. TODO appliquer aux coups suivants? ou interdire si ce n'est pas la fin d'une branche?
     */
    public void setMoveValue(byte value) {
        _currentNode.setMoveValue(value);
    }


    /**
     * Adds a stone of the specified color to the current position (it's not a move). It doesn't capture any stones.
     */
    public void addStone(int x, int y, byte color) {
        _currentNode.setStone(x, y, color);
        board.placeMove(x, y, color, false);
    }

    /**
     * Removes the current move from the game tree, including all of the following moves.
     * If this is called from the base move, the whole tree will be deleted.
     */
    public void deleteMove() {
        if (_currentNode.parentNode == null)
            clear();
        else
            undo(true);
    }

    /**
     * Rotates the board and the variations by 90° CCW
     * (this doesn't rotate added stones and any other SGF property).
     */
    public void rotateCCW() {
        board.rotateCCW();
        _rotateMovesCCW_loop(getBaseNode());
        updateMarks();
    }

    private void _rotateMovesCCW_loop(GameNode move) {
        if (move.x >= 0 && move.y >= 0) {
            byte temp = move.x;
            move.x = move.y;
            move.y = (byte) (_size - temp - 1);
        }

        for (GameNode nextMove : move.nextNodes)
            _rotateMovesCCW_loop(nextMove);
    }

    public void toggleDeadGroup(int x, int y) {
        if (board.isEmpty(x, y))
            return;

        byte color = board.getColor(x, y);
        List<Coords> coords = board.listStonesInGroup(x, y, false);
        for (Coords pt : coords) {
            byte fsColor;
            if (color == GoBoard.BLACK)
                fsColor = finalStatus.getColor(pt.x, pt.y) == GoBoard.DEAD_BLACK_STONE ? GoBoard.EMPTY : GoBoard.DEAD_BLACK_STONE;
            else
                fsColor = finalStatus.getColor(pt.x, pt.y) == GoBoard.DEAD_WHITE_STONE ? GoBoard.EMPTY : GoBoard.DEAD_WHITE_STONE;

            finalStatus.set(pt.x, pt.y, fsColor);
        }
    }

    // TODO implement other rules than Japanese (and move the compute engine in another class)
    public Result computeTerritories() {
        GoBoard tempBoard;
        try {
            tempBoard = (GoBoard) board.clone();
        }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        Result result = new Result();
        result.whitePrisoners = _whitePrisoners;
        result.blackPrisoners = _blackPrisoners;

        for (int x = 0; x < _size; x++) {
            for (int y = 0; y < _size; y++) {
                byte color = finalStatus.getColor(x, y);
                if (color == GoBoard.WHITE_TERRITORY || color == GoBoard.BLACK_TERRITORY) {
                    finalStatus.set(x, y, GoBoard.EMPTY);
                    continue;
                }

                if (color == GoBoard.DEAD_BLACK_STONE || color == GoBoard.DEAD_WHITE_STONE) {
                    List<Coords> removed = tempBoard.removeStones(x, y);
                    if (color == GoBoard.DEAD_BLACK_STONE)
                        result.whitePrisoners += removed.size();
                    else
                        result.blackPrisoners += removed.size();
                }
            }
        }

        _loop_passed = new boolean[_size][_size];
        _loop_localPassed = new boolean[_size][_size];
        for (int x = 0; x < _size; x++) {
            for (int y = 0; y < _size; y++) {
                byte color = tempBoard.getColor(x, y);

                if (_loop_passed[x][y] || color != GoBoard.EMPTY)
                    continue;

                for (int iX = 0; iX < _size; iX++)
                    Arrays.fill(_loop_localPassed[iX], false);
                _loop_markStoneType = 0;
                _loop_count = 0;
                _markPoints_loop(tempBoard, x, y);

                if (_loop_markStoneType == GoBoard.BLACK)
                    result.blackTerritory += _loop_count;
                else
                    result.whiteTerritory += _loop_count;

                if (_loop_markStoneType == GoBoard.BLACK || _loop_markStoneType == GoBoard.WHITE) {
                    for (int iX = 0; iX < _size; iX++) {
                        for (int iY = 0; iY < _size; iY++) {
                            byte inColor = finalStatus.getColor(iX, iY);
                            if (inColor == GoBoard.EMPTY && tempBoard.getColor(iX, iY) == GoBoard.EMPTY) {
                                finalStatus.set(iX, iY, _loop_markStoneType == GoBoard.BLACK
                                        ? GoBoard.BLACK_TERRITORY : GoBoard.WHITE_TERRITORY);
                            }
                        }
                    }
                }
            }
        }

        result.komi = info.komi;
        return result;
    }

    private void _markPoints_loop(GoBoard markBoard, int x, int y) {
        if (_loop_passed[x][y])
            return;

        _loop_passed[x][y] = true;
        _loop_localPassed[x][y] = true;
        _loop_count++;

        if (x + 1 < _size) {
            byte color = markBoard.getColor(x + 1, y);
            if (color == GoBoard.EMPTY)
                _markPoints_loop(markBoard, x + 1, y);
            else
                _loop_markStoneType |= color;
        }
        if (x - 1 >= 0) {
            byte color = markBoard.getColor(x - 1, y);
            if (color == GoBoard.EMPTY)
                _markPoints_loop(markBoard, x - 1, y);
            else
                _loop_markStoneType |= color;
        }
        if (y + 1 < _size) {
            byte color = markBoard.getColor(x, y + 1);
            if (color == GoBoard.EMPTY)
                _markPoints_loop(markBoard, x, y + 1);
            else
                _loop_markStoneType |= color;
        }
        if (y - 1 >= 0) {
            byte color = markBoard.getColor(x, y - 1);
            if (color == GoBoard.EMPTY)
                _markPoints_loop(markBoard, x, y - 1);
            else
                _loop_markStoneType |= color;
        }
    }


    /**
     * Returns the coordinates of each prisoner captured by the current move, or null if there was no.
     */
    public Collection<LightCoords> getLastPrisoners() {
        if (_playedMoves.empty())
            return new ArrayList<>();

        MoveInfo info = _playedMoves.peek();
        return (info.prisoners != null) ? info.prisoners : new ArrayList<LightCoords>();
    }


    /**
     * Returns true if both players just passed successively (this move and the last one).
     */
    public boolean hasTwoPasses() {
        final GameNode parentMove = _currentNode.parentNode;
        return parentMove != null && _currentNode.x == -1 && parentMove.x == -1;
    }

    /**
     * Shows/hides move numbers (displayed as numeric marks on the board).
     */
    public void showMoveNumbers(boolean show) {
        _showMoveNumbers = show;
        updateMarks();
    }

    /**
     * Returns true if playing a stone at the specified coordinates for the current player is legal
     * according to the current position.
     */
    public boolean isLegal(int x, int y) {
        return board.isLegal(x, y, _currentPlayer);
    }

    /**
     * Returns true if playing a stone with the specified color and coordinates is legal
     * according to the current position.
     */
    public boolean isLegal(int x, int y, byte color) {
        return board.isLegal(x, y, color);
    }

    /**
     * Returns the current move.
     */
    public GameNode getCurrentNode() {
        return _currentNode;
    }

    /**
     * Returns the base move of the current game tree.
     */
    public GameNode getBaseNode() {
        return _baseNode;
    }

    /**
     * Sets the base move of the current game tree.
     */
    public void setBaseNode(GameNode baseNode) {
        clear();
        _baseNode = baseNode;
        _currentNode = _baseNode;
        _setRequestedStones();
    }

    /**
     * Switches the next player to play (black becomes white, white becomes black).
     */
    public void switchCurrentPlayer() {
        _currentPlayer = (_currentPlayer == GoBoard.BLACK) ? GoBoard.WHITE : GoBoard.BLACK;
    }

    /**
     * Returns the color of the next player to play.
     */
    public byte getNextPlayer() {
        return _currentPlayer;
    }

    /**
     * Sets the next player to play (use constants of this class : BLACK or WHITE).
     */
    public void setNextPlayer(byte color) {
        _currentPlayer = color;
    }

    /**
     * Returns the komi for this game.
     */
    public double getKomi() {
        return info.komi;
    }

    /**
     * Returns the number of handicap stones used for this game.
     */
    public int getHandicap() {
        return info.handicap;
    }

    /**
     * Returns the total number of black prisoners (captured by black).
     */
    public int getBlackPrisoners() {
        return _blackPrisoners;
    }

    /**
     * Returns the total number of white prisoners (captured by white).
     */
    public int getWhitePrisoners() {
        return _whitePrisoners;
    }

    /**
     * Returns the final status of the specified intersection (BLACK_TERRITORY, ...).
     */
    public byte getFinalStatus(int x, int y) {
        return finalStatus.getColor(x, y);
    }

    /**
     * Sets the final status of the specified intersection (BLACK_TERRITORY, ...).
     */
    public void setFinalStatus(int x, int y, byte status) {
        finalStatus.set(x, y, status);
    }

    /**
     * Completely clears the game tree.
     */
    public void clear() {
        _playedMoves.clear();
        _baseNode = new GameNode(BASE_NODE_COORD, BASE_NODE_COORD, GoBoard.EMPTY);
        _currentNode = _baseNode;
        _currentPlayer = GoBoard.BLACK;
        board.clear();
    }

    /**
     * Navigate to the last move of the current selected variation.
     */
    public void gotoLastMove() {
        navigate(999999);
    }

    /**
     * Navigate to the first move of the game.
     */
    public void gotoFirstMove() {
        navigate(-999999);
    }


    /**
     * Returns the actual representation of the game in a SGF string.
     */
    public String getSgf() {
        return new SgfParser().toSgfString(this, true);
    }

    /**
     * Saves the actual game tree in the specified stream, in SGF format.
     *
     * @throws IOException An error occured during writing.
     */
    public void saveSgf(final OutputStream stream) throws IOException {
        new SgfParser().save(this, stream);
    }

    /**
     * Loads a game in SGF format from the specified stream.
     *
     * @throws IOException An error occured during reading (The SGF may be corrupted).
     */
    public static GoGame[] loadSgf(final InputStream stream) throws IOException {
        return new SgfParser().parse(stream);
    }

    /**
     * Loads a game in SGF format from the specified string.
     *
     * @throws IOException An error occured during reading (The SGF is probably corrupted).
     */
    public static GoGame[] loadSgf(final String sgf) throws IOException {
        InputStream stream = new ByteArrayInputStream(sgf.getBytes());
        GoGame[] games = loadSgf(stream);
        stream.close();
        return games;
    }

    /**
     * Saves the actual game tree in the specified stream, in LRF format (some SGF informations may
     * be lost; look at LRF specifications).
     *
     * @throws IOException An error occured during writing.
     */
    public void saveLrf(final OutputStream stream) throws IOException {
        new LrfParser().save(this, stream);
    }

    /**
     * Loads a game in LRF format from the specified stream.
     *
     * @throws IOException An error occured during reading.
     */
    public static GoGame loadLrf(final InputStream stream) throws IOException {
        return new LrfParser().parse(stream);
    }


    /**
     * Adds a move at the current position (set x to -1 to add a pass) and set it as the current move.
     * If the move already exists, no node is created, it is just set to the corresponding node.
     */
    protected void _addToTree(int x, int y, byte color, List<Coords> prisoners) {
        _currentNode = _currentNode.addNode(x, y, color);
        GameNode parentNode = _currentNode.parentNode;
        if (parentNode != null)
            parentNode.lastVariation = (byte) Math.max(0, parentNode.nextNodes.indexOf(_currentNode));
        _playedMoves.push(new MoveInfo(prisoners, GoBoard.getOppositeColor(color), board.getKoCoords()));
    }


    /**
     * Sets or removes from the board the specified moves by the current node (SGF properties AE/AW/AB).
     */
    protected void _setRequestedStones() {
        if (_currentNode.setStones == null || _currentNode.setStones.size() == 0)
            return;

        ArrayList<LightCoords> removedList = new ArrayList<LightCoords>();
        for (LightCoords coords : _currentNode.setStones) {
            byte prevColor = board.getColor(coords.x, coords.y);
            removedList.add(new LightCoords(coords.x, coords.y, prevColor));
            board.set(coords.x, coords.y, coords.color);
        }
        if (!_playedMoves.empty())
            _playedMoves.peek().setRemovedStones(removedList);
    }


    /**
     * Sets the current move to the next or previous variation available, depending on the parameter.
     */
    protected void _changeVariation(boolean isNext) {
        GameNode parentMove = _currentNode.parentNode;
        if (parentMove == null)
            return;

        int size = parentMove.nextNodes.size();
        if (size <= 1)
            return;

        int index = parentMove.nextNodes.indexOf(_currentNode);
        if (index < 0) {
            System.err.println("Cannot find current node in parent list");
            return;
        }

        if (isNext)
            index = (index == size - 1) ? 0 : index + 1;
        else
            index = (index == 0) ? size - 1 : index - 1;

        GameNode move = parentMove.nextNodes.get(index);
        undo(false);
        placeMove(move.x, move.y, move.color);
    }


    /**
     * Places the specified number of handicap stones according to the japanese rules.<br>
     * It will be white's turn after this method returns.
     * TODO No handicap is set for even boardsizes
     * TODO Handle handicap > 9
     */
    public void placeHandicap(int handicap) {
        if (handicap <= 1 || _size % 2 == 0)
            return;
        if (handicap > 9)
            handicap = 9;

        int shift = (_size > 12) ? 1 : 0;
        int c1 = 2 + shift;
        int c2 = _size / 2;
        int c3 = _size - 3 - shift;

        if (handicap >= 8) {
            addStone(c2, c1, GoBoard.BLACK);
            addStone(c2, c3, GoBoard.BLACK);
        }
        if (handicap >= 6) {
            addStone(c1, c2, GoBoard.BLACK);
            addStone(c3, c2, GoBoard.BLACK);
        }
        if (handicap >= 4) {
            addStone(c3, c3, GoBoard.BLACK);
        }
        if (handicap >= 3) {
            addStone(c1, c1, GoBoard.BLACK);
        }
        if (handicap >= 2) {
            addStone(c1, c3, GoBoard.BLACK);
            addStone(c3, c1, GoBoard.BLACK);
        }

        if (handicap == 5 || handicap == 7 || handicap == 9) {
            addStone(c2, c2, GoBoard.BLACK);
        }

        setNextPlayer(GoBoard.WHITE);
        info.handicap = handicap;
    }


    /**
     * Updates the marks on the board, including the dynamic ones (last played move, move numbers, ko).
     */
    void updateMarks() {
        board.removeMarks();
        // TODO Numéroter les coups de la partie si demandé

        if (_currentNode.x >= 0)
            board.setMark(new BoardMark(_currentNode.x, _currentNode.y, BoardMark.MARK_CIRCLE));

        Coords ko = board.getKoCoords();
        if (ko != null)
            board.setMark(new BoardMark(ko.x, ko.y, BoardMark.MARK_SQUARE));

        if (_currentNode.boardMarks != null) {
            for (BoardMark mark : _currentNode.boardMarks)
                board.setMark(mark);
        }
    }


    /**
     * Contains informations about a move played on the board. It is used to be able to undo
     * any of these moves.
     */
    protected final class MoveInfo {
        /**
         * Contains the coordinates and color of all intersections altered by the last move.
         * This excludes prisoners, but includes any stone removed with an SGF command like AB[]
         * (which can "replace" an empty intersection or a white stone).
         */
        public List<LightCoords> removedStones;

        /**
         * Contains the coordinates and color of all prisoners captured by the last move.
         */
        public List<LightCoords> prisoners;

        /**
         * Contains the coordinates of the ko if there is one, else it is set to null.
         */
        public Coords ko;


        public MoveInfo(Collection<Coords> prisoners, byte prisonersColor, Coords ko) {
            this(prisoners, prisonersColor, ko, null);
        }

        public MoveInfo(Collection<Coords> prisoners, byte prisonersColor, Coords ko, Collection<LightCoords> removedStones) {
            this.ko = ko;
            addPrisoners(prisoners, prisonersColor);
            setRemovedStones(removedStones);
        }

        /**
         * Sets the coordinates of each prisoner captured by the current move.
         */
        public void addPrisoners(Collection<Coords> prisoners, byte prisonersColor) {
            if (prisoners == null)
                return;

            if (this.prisoners == null)
                this.prisoners = new ArrayList<LightCoords>(8);
            for (Coords prisoner : prisoners)
                this.prisoners.add(new LightCoords(prisoner.x, prisoner.y, prisonersColor));
        }

        /**
         * Adds the coordinates and color of an intersection altered by the last move.
         */
        public void addRemovedStone(LightCoords removedStone) {
            if (removedStones == null)
                removedStones = new ArrayList<LightCoords>(8);
            removedStones.add(removedStone);
        }

        /**
         * Adds the coordinates and color of multiple intersections altered by the last move.
         */
        public void setRemovedStones(Collection<LightCoords> removedStones) {
            if (removedStones == null)
                return;

            if (this.removedStones == null)
                this.removedStones = new ArrayList<LightCoords>(8);
            else
                this.removedStones.clear();
            this.removedStones.addAll(removedStones);
        }
    }


    public static final class Result {
        public int whiteTerritory;
        public int blackTerritory;
        public int whitePrisoners;
        public int blackPrisoners;
        public double komi;
    }
}