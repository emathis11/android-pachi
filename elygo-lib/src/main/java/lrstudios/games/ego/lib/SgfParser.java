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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


/**
 * Parses and saves SGF files from/to instances of {@link GoGame}.
 */
public final class SgfParser {
    private static final String[] _properties;

    // SGF properties constants : these are used to speed up the parsing/saving process
    public static final byte
            PROP_UNDEFINED = -1,
            PROP_ADD_BLACK = 0, PROP_ADD_EMPTY = 1, PROP_ANNOTATION = 2, PROP_APPLICATION_NAME = 3, PROP_ARROW = 4, PROP_ADD_WHITE = 5,
            PROP_BLACK = 6, PROP_BLACK_TIME_LEFT = 7, PROP_BAD_MOVE = 8, PROP_BLACK_RANK = 9, PROP_BLACK_TEAM = 10,
            PROP_COMMENT = 11, PROP_CHARSET = 12, PROP_COPYRIGHT = 13, PROP_CIRCLE = 14,
            PROP_DIM_POINTS = 15, PROP_EVEN_POSITION = 16, PROP_DOUBTFUL = 17, PROP_DATE = 18,
            PROP_EVENT = 19,
            PROP_SGF_VERSION = 20, PROP_FIGURE = 21,
            PROP_GOOD_FOR_BLACK = 22, PROP_GAME_COMMENT = 23, PROP_GAME = 24, PROP_GAME_NAME = 25, PROP_GOOD_FOR_WHITE = 26,
            PROP_HANDICAP = 27, PROP_HOTSPOT = 28,
            PROP_INTERESTING = 29,
            PROP_KOMI = 30, PROP_KO = 31,
            PROP_LABEL = 32, PROP_LINE = 33,
            PROP_MARK_WITH_X = 34, PROP_SET_MOVE_NUMBER = 35,
            PROP_NODE_NAME = 36,
            PROP_OT_STONES_BLACK = 37, PROP_OPENING = 38, PROP_OVERTIME = 39, PROP_OT_STONES_WHITE = 40,
            PROP_PLAYER_BLACK = 41, PROP_PLACE = 42, PROP_PLAYER_TO_PLAY = 43, PROP_PRINT_MOVE_MODE = 44, PROP_PLAYER_WHITE = 45,
            PROP_RESULT = 46, PROP_ROUND = 47, PROP_RULES = 48,
            PROP_SELECTED = 49, PROP_SOURCE = 50, PROP_SQUARE = 51, PROP_STYLE = 52, PROP_SIZE = 53,
            PROP_TERRITORY_BLACK = 54, PROP_TESUJI = 55, PROP_TIME_LIMIT = 56, PROP_TRIANGLE = 57, PROP_TERRITORY_WHITE = 58,
            PROP_UNCLEAR_POS = 59, PROP_USER = 60,
            PROP_VALUE = 61, PROP_VIEW = 62,
            PROP_WHITE = 63, PROP_WHITE_TIME_LEFT = 64, PROP_WHITE_RANK = 65, PROP_WHITE_TEAM = 66,
            PROP_MAX_VALUE = 66;

    private PushbackReader _reader;
    private ParseOptions _parseOptions;
    private ArrayList<GameNode> _baseNodes;
    private ArrayList<GameInfo> _gameInfos;
    private Writer _writer;
    private GoGame _game;
    private IOException _exception;
    private GameInfo _gameInfo;
    private boolean _optimized;
    private boolean _infoOnly;
    private Coords _cachedCoords = new Coords();
    private StringBuilder _property = new StringBuilder(2);
    private StringBuilder _value = new StringBuilder(16);


    public SgfParser() {
        this(null);
    }

    public SgfParser(ParseOptions options) {
        _parseOptions = (options == null) ? new ParseOptions() : options;
    }

    /**
     * Parses the specified SGF file (a single file can contain several SGF trees, which will be returned
     * as different instances of GoGame).
     *
     * @throws IOException An error occurred during reading (the SGF file may be corrupted).
     */
    public GoGame[] parse(InputStream stream) throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(stream), 1024);
        _reader = _infoOnly ? new CopyPushbackReader(reader) : new PushbackReader(reader);
        _baseNodes = new ArrayList<>(1);
        _gameInfos = new ArrayList<>(1);

        _parse_loop(null);
        if (_infoOnly && _gameInfo != null)
            _gameInfo.originalSgf = ((CopyPushbackReader) _reader).getString();
        //System.out.println("Found " + _baseNodes.size() + " games with a total of " + _nodeCount + " SGF nodes.");

        int nodes = _baseNodes.size();
        GoGame[] games = new GoGame[nodes];
        for (int i = 0; i < nodes; i++)
            games[i] = new GoGame(_gameInfos.get(i), _baseNodes.get(i));

        // Parse as problems :
        // Try to know which problem format is used to set the move values properly
        if (_parseOptions._parseAsProblems) {
            ArrayList<GoGame> newGames = new ArrayList<>();
            for (GoGame game : games) {
                GameNode baseNode = game.getBaseNode();
                // If the board is empty at the beginning, it means that the problems are stored
                // in the variations
                if (baseNode.setStones == null || baseNode.setStones.size() == 0) {
                    for (GameNode nextNode : baseNode.nextNodes) {
                        GameNode parentNode = nextNode.parentNode;
                        nextNode.parentNode = null;
                        game.setBaseNode(nextNode);
                        newGames.add(GoGame.loadSgf(game.getSgf())[0]);
                        game.setBaseNode(baseNode);
                        nextNode.parentNode = parentNode;
                    }
                }
            }
            if (newGames.size() > 0) {
                Collections.addAll(newGames, games);
                games = newGames.toArray(new GoGame[newGames.size()]);
            }

            for (GoGame game : games)
                _setMoveValues(game);
        }
        return games;
    }


    // This method could use recursive functions but android stack size is very limited, so it should be avoided
    private void _setMoveValues(GoGame game) {
        GameNode baseNode = game.getBaseNode();

        // Try to find move comments indicating good and bad variations
        Stack<GameNode> stack = new Stack<>();
        stack.push(baseNode);
        while (!stack.empty()) {
            GameNode node = stack.pop();
            String comment = node.getComment();

            if (node.nextNodes.size() > 0) {
                stack.addAll(node.nextNodes);
            }
            else if (comment.contains("RIGHT")) {
                node.setMoveValue(100);

                if (comment.endsWith("RIGHT"))
                    node.setComment(comment.substring(0, comment.length() - 5));
                else if (comment.startsWith("RIGHT"))
                    node.setComment(comment.substring(5));
            }
            else {
                node.setMoveValue(0);
            }
        }

        if (baseNode.value < 100) {
            // No solution found, we assume all are valid
            stack = new Stack<>();
            stack.add(baseNode);
            while (!stack.empty()) {
                GameNode node = stack.pop();
                node.setMoveValue(100);
                stack.addAll(node.nextNodes);
            }
        }
    }

    /**
     * This parsees only the SGF header(s) contained in the specified stream.
     *
     * @throws IOException An error occurred during reading (the SGF file may be corrupted).
     */
    public GoGame[] parseInfo(InputStream stream) throws IOException {
        _infoOnly = true;
        return parse(stream);
    }

    // Recursive parsing function
    private void _parse_loop(GameNode curNode) throws IOException {
        int ch;
        while ((ch = _reader.read()) != -1) {
            switch (ch) {
                case ';':
                    GameNode node = _parseNode();
                    if (node != null) {
                        if (curNode == null) {
                            _baseNodes.add(node);
                        }
                        else {
                            node.parentNode = curNode;
                            curNode.nextNodes.add(node);
                        }
                        curNode = node;
                    }
                    break;

                case '(':
                    if (curNode == null) {
                        if (_infoOnly && _gameInfo != null) {
                            _reader.unread(ch);
                            _gameInfo.originalSgf = ((CopyPushbackReader) _reader).getString();
                            _reader.read();
                        }
                        _gameInfo = new GameInfo();
                        _gameInfo.komi = 6.5;
                        _gameInfo.boardSize = 19;
                        _gameInfo.handicap = 0;
                        _gameInfos.add(_gameInfo);
                    }
                    _parse_loop(curNode);
                    if (curNode != null)
                        curNode.nextNodes.trimToSize();
                    break;

                case ')':
                    if (curNode != null)
                        curNode.nextNodes.trimToSize();
                    return;
            }
        }
    }

    private GameNode _parseNode() throws IOException {
        byte propertyType = -2;
        boolean readingValue = false;
        boolean propertyFound = false;
        GameNode node = new GameNode();
        _property.setLength(0);
        _value.setLength(0);

        int iCh;
        while ((iCh = _reader.read()) != -1) {
            char ch = (char) iCh;
            if (ch == ']' && readingValue && (_value.length() == 0 || _value.charAt(_value.length() - 1) != '\\')) {
                _addProperty(node, propertyType, _value.toString());
                propertyFound = true;
                readingValue = false;
            }
            else if (!readingValue) {
                if (ch == '[') {
                    propertyType = parsePropertyType(_property.toString());
                    readingValue = true;
                    _value.setLength(0);
                }
                else if (ch == ';' || ch == '(' || ch == ')') {
                    _reader.unread(iCh);
                    break;
                }
                else if (ch != ' ' && ch != '\n' && ch != '\r' && ch != '\t') {
                    if (propertyType >= -1) {
                        _property.setLength(0);
                        propertyType = -2;
                    }
                    _property.append(ch);
                }
            }
            else {
                _value.append(ch);
            }
        }
        if (!propertyFound) // Empty node
            return null;

        if (node.setStones != null)
            node.setStones.trimToSize();
        if (node.boardMarks != null)
            node.boardMarks.trimToSize();
        return node;
    }

    private void _addProperty(GameNode node, int propertyType, String value) {
        switch (propertyType) {
            case PROP_BLACK:
                _cacheStringToCoords(value);
                node.x = (byte) _cachedCoords.x;
                node.y = (byte) _cachedCoords.y;
                node.color = GoBoard.BLACK;
                break;
            case PROP_WHITE:
                _cacheStringToCoords(value);
                node.x = (byte) _cachedCoords.x;
                node.y = (byte) _cachedCoords.y;
                node.color = GoBoard.WHITE;
                break;

            case PROP_ADD_BLACK:
                _cacheStringToCoords(value);
                node.setStone(_cachedCoords.x, _cachedCoords.y, GoBoard.BLACK);
                break;
            case PROP_ADD_WHITE:
                _cacheStringToCoords(value);
                node.setStone(_cachedCoords.x, _cachedCoords.y, GoBoard.WHITE);
                break;
            case PROP_ADD_EMPTY:
                _cacheStringToCoords(value);
                node.setStone(_cachedCoords.x, _cachedCoords.y, GoBoard.EMPTY);
                break;

            case PROP_COMMENT:
                node.setComment(value);
                break;

            case PROP_TRIANGLE:
                _cacheStringToCoords(value);
                node.addMark(new BoardMark(_cachedCoords.x, _cachedCoords.y, BoardMark.MARK_TRIANGLE));
                break;
            case PROP_CIRCLE:
                _cacheStringToCoords(value);
                node.addMark(new BoardMark(_cachedCoords.x, _cachedCoords.y, BoardMark.MARK_CIRCLE));
                break;
            case PROP_SQUARE:
                _cacheStringToCoords(value);
                node.addMark(new BoardMark(_cachedCoords.x, _cachedCoords.y, BoardMark.MARK_SQUARE));
                break;
            case PROP_LABEL:
                _cacheStringToCoords(value);
                String label = value.substring(value.indexOf(':') + 1);
                if (label.length() > 0)
                    node.addMark(new BoardLabelMark(_cachedCoords.x, _cachedCoords.y, label.charAt(0)));
                break;
            case PROP_MARK_WITH_X:
                _cacheStringToCoords(value);
                node.addMark(new BoardMark(_cachedCoords.x, _cachedCoords.y, BoardMark.MARK_CROSS));
                break;

            case PROP_TERRITORY_BLACK:
                _cacheStringToCoords(value);
                node.addMark(new BoardMark(_cachedCoords.x, _cachedCoords.y, BoardMark.MARK_BLACK_TERRITORY));
                break;
            case PROP_TERRITORY_WHITE:
                _cacheStringToCoords(value);
                node.addMark(new BoardMark(_cachedCoords.x, _cachedCoords.y, BoardMark.MARK_WHITE_TERRITORY));
                break;

            case PROP_SIZE:
                _gameInfo.boardSize = Integer.parseInt(value);
                break;
            case PROP_KOMI:
                _gameInfo.komi = parseKomi(value, '.');
                break;
            case PROP_HANDICAP:
                _gameInfo.handicap = Integer.parseInt(value);
                break;
            case PROP_RULES:
                _gameInfo.rules = value.trim();
                break;
            case PROP_PLAYER_BLACK:
                _gameInfo.blackName = value.trim();
                break;
            case PROP_PLAYER_WHITE:
                _gameInfo.whiteName = value.trim();
                break;
            case PROP_WHITE_RANK:
                _gameInfo.whiteRank = value.trim();
                break;
            case PROP_BLACK_RANK:
                _gameInfo.blackRank = value.trim();
                break;
            case PROP_RESULT:
                _gameInfo.result = GoGameResult.tryParse(value);
                break;
            case PROP_PLAYER_TO_PLAY:
                _gameInfo.firstPlayer = value;
                break;
            case PROP_EVENT:
                _gameInfo.eventName = value;
                break;
            case PROP_DATE:
                _gameInfo.gameDate = value;
                break;
            case PROP_ROUND:
                _gameInfo.round = value;
                break;

            case PROP_UNDEFINED:
                //System.err.println("SgfParser warning: No SGF property found (value : \"" + value + "\")");
        }
    }


    /**
     * Saves a {@link GoGame} in the specified {@link OutputStream}.
     *
     * @throws IOException An error occurred during writing.
     */
    public void save(GoGame game, OutputStream stream) throws IOException {
        _writer = new BufferedWriter(new PrintWriter(stream), 1024);
        _gameInfo = game.info;
        _game = game;
        _exception = null;

        // Saves the file from another thread to have a larger stack size for recursive function calls
        // (Android stack size is limited to 8KB, which throws a StackOverflowError on large SGF files).
        Thread thread = new Thread(new ThreadGroup("TROLOLO"), save_loop, "SgfParser", 256 * 1024);
        thread.start();
        try {
            thread.join();
        }
        catch (InterruptedException ignored) {
        }
        if (_exception != null)
            throw _exception;
    }

    /**
     * Saves a game in the specified stream. Optimizations are done to reduce the file size :
     * no spaces, no new line characters, and some SGF attributes will be missing, like GM, FF, ST, CA, AP,
     * BR/WR/PW/PB, RU).
     *
     * @throws IOException An error occurred during writing.
     */
    public void saveOptimized(GoGame game, OutputStream stream) throws IOException {
        _optimized = true;
        save(game, stream);
    }

    private Runnable save_loop = new Runnable() {
        @Override
        public void run() {
            try {
                _save_loop(_game.getBaseNode(), true);
                _writer.flush();
            }
            catch (IOException e) {
                _exception = e;
            }
        }

        private void _save_loop(GameNode curNode, boolean writeParentheses) throws IOException {
            if (!_optimized)
                _writer.write("\n");
            if (writeParentheses)
                _writer.write('(');
            _writeNode(curNode);

            List<GameNode> nextNodesList = curNode.nextNodes;
            for (GameNode nextNode : nextNodesList) {
                _save_loop(nextNode, nextNodesList.size() > 1);
            }
            if (writeParentheses)
                _writer.write(')');
        }
    };

    private void _writeNode(GameNode node) throws IOException {
        _writer.write(";");

        // Premier coup de la partie, y ajouter les infos
        if (node.parentNode == null) {
            if (!_optimized) {
                _writer.write("GM[1]FF[4]CA[UTF-8]");
                _writeProperty("AP", _gameInfo.applicationName);
            }
            _writeProperty("RU", _gameInfo.rules == null ? "Japanese" : _gameInfo.rules);
            _writeProperty("PW", _gameInfo.whiteName);
            _writeProperty("PB", _gameInfo.blackName);
            _writeProperty("WR", _gameInfo.whiteRank);
            _writeProperty("BR", _gameInfo.blackRank);

            _writeProperty("SZ", _gameInfo.boardSize);
            _writeProperty("KM", _komiToSgfString(_gameInfo.komi));
            _writeProperty("HA", _gameInfo.handicap);
            if (_gameInfo.result != null)
                _writeProperty("RE", _gameInfo.result.toString());
            _writeProperty("PL", _gameInfo.firstPlayer);
            _writeProperty("EV", _gameInfo.eventName);
            _writeProperty("DT", _gameInfo.gameDate);
            _writeProperty("RO", _gameInfo.round);
        }

        if (node.color != GoBoard.EMPTY) {
            char colorChar = node.color == GoBoard.WHITE ? 'W' : 'B';
            if (node.x == -1 && node.y == -1)
                _writer.write(colorChar + "[]");
            else if (node.x >= 0 && node.y >= 0)
                _writer.write(colorChar + "[" + coordsToString(node.x, node.y) + "]");
        }
        if (node.setStones != null && node.setStones.size() > 0) {
            List<LightCoords> whiteList = new ArrayList<>();
            List<LightCoords> blackList = new ArrayList<>();
            List<LightCoords> emptyList = new ArrayList<>();

            for (LightCoords coords : node.setStones) {
                if (coords.color == GoBoard.WHITE)
                    whiteList.add(coords);
                else if (coords.color == GoBoard.BLACK)
                    blackList.add(coords);
                else
                    emptyList.add(coords);
            }

            if (whiteList.size() > 0) {
                _writer.write("AW");
                for (LightCoords coords : whiteList)
                    _writer.write("[" + coordsToString(coords.x, coords.y) + "]");
            }
            if (blackList.size() > 0) {
                _writer.write("AB");
                for (LightCoords coords : blackList)
                    _writer.write("[" + coordsToString(coords.x, coords.y) + "]");
            }
            if (emptyList.size() > 0) {
                _writer.write("AE");
                for (LightCoords coords : emptyList)
                    _writer.write("[" + coordsToString(coords.x, coords.y) + "]");
            }
        }

        // Marks
        if (node.boardMarks != null) {
            // TODO optimize
            ArrayList<BoardMark> triangles = new ArrayList<>();
            ArrayList<BoardMark> circles = new ArrayList<>();
            ArrayList<BoardMark> squares = new ArrayList<>();
            ArrayList<BoardMark> cross = new ArrayList<>();
            ArrayList<BoardMark> labels = new ArrayList<>();
            for (BoardMark mark : node.boardMarks) {
                switch (mark.type) {
                    case BoardMark.MARK_TRIANGLE:
                        triangles.add(mark);
                        break;
                    case BoardMark.MARK_CIRCLE:
                        circles.add(mark);
                        break;
                    case BoardMark.MARK_SQUARE:
                        squares.add(mark);
                        break;
                    case BoardMark.MARK_CROSS:
                        cross.add(mark);
                        break;
                    case BoardMark.MARK_LABEL:
                        labels.add(mark);
                        break;
                }
            }
            if (triangles.size() > 0) {
                _writer.write("TR");
                for (BoardMark mark : triangles)
                    _writer.write("[" + coordsToString(mark.x, mark.y) + "]");
            }
            if (circles.size() > 0) {
                _writer.write("CR");
                for (BoardMark mark : circles)
                    _writer.write("[" + coordsToString(mark.x, mark.y) + "]");
            }
            if (squares.size() > 0) {
                _writer.write("SQ");
                for (BoardMark mark : squares)
                    _writer.write("[" + coordsToString(mark.x, mark.y) + "]");
            }
            if (cross.size() > 0) {
                _writer.write("MA");
                for (BoardMark mark : squares)
                    _writer.write("[" + coordsToString(mark.x, mark.y) + "]");
            }
            if (labels.size() > 0) {
                _writer.write("LB");
                for (BoardMark mark : labels)
                    _writer.write("[" + coordsToString(mark.x, mark.y) + ":" + mark.getLabel() + "]");
            }
        }

        String comment = node.getComment();
        if (comment.length() > 0)
            _writeProperty("C", comment.replace("]", "\\]"));
    }

    private void _writeProperty(String property, int value) throws IOException {
        _writer.write(String.format("%s[%d]", property, value));
    }

    private void _writeProperty(String property, String value) throws IOException {
        if (value != null && value.length() > 0)
            _writer.write(String.format("%s[%s]", property, value));
    }


    /**
     * Converts the specified {@link GoGame} into a SGF[4] string.
     */
    public String toSgfString(GoGame game, boolean optimize) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(128);
        try {
            if (optimize)
                saveOptimized(game, outputStream);
            else
                save(game, outputStream);
            String sgf = outputStream.toString();
            outputStream.close();
            return sgf;
        }
        catch (IOException ignored) {
            return "";
        }
    }


    /**
     * Caching the parsed SGF coords considerably reduces the SGF parsing time on Android.
     */
    private void _cacheStringToCoords(String sgfCoords) {
        if (sgfCoords.length() == 0) {
            _cachedCoords.x = -1;
            _cachedCoords.y = -1;
        }
        else {
            _cachedCoords.x = sgfCoords.charAt(0) - 'a';
            _cachedCoords.y = sgfCoords.charAt(1) - 'a';
        }
    }

    private String _komiToSgfString(double komi) {
        return String.format("%d.%d", (int) (komi), Math.abs((int) Math.round(komi * 10.0) % 10));
    }


    /**
     * Returns the SGF representation of the specified property (PROP_ADD_WHITE returns "AW", for example).
     */
    public static String propertyToString(int propertyType) {
        return _properties[propertyType];
    }

    /**
     * Returns the SGF representation of the specified coordinates.
     * TODO : handle coordinates greater than 26
     */
    public static String coordsToString(int x, int y) {
        return Character.toString((char) ('a' + x)) + Character.toString((char) ('a' + y));
    }

    /**
     * Returns the coordinates represented by the specified SGF coordinates.
     * TODO : handle coordinates greater than 26
     */
    public static Coords stringToCoords(String sgfCoords) {
        return sgfCoords.length() == 0 ? new Coords(-1, -1) : new Coords(sgfCoords.charAt(0) - 'a', sgfCoords.charAt(1) - 'a');
    }

    /**
     * Returns the constant representing the specified SGF property (or -1 if the property is unknown).
     */
    public static byte parsePropertyType(String type) {
        int property = Arrays.binarySearch(_properties, type);
        return property >= 0 ? (byte) property : -1;
    }

    public static float parseKomi(String number, char decimalSeparator) {
        int dotPos = number.indexOf(decimalSeparator);
        if (dotPos < 0)
            return Integer.parseInt(number);
        int integerPart = Integer.parseInt(number.substring(0, dotPos));
        int decimalPart = Integer.parseInt(number.substring(dotPos + 1, dotPos + 2));
        return integerPart + (integerPart >= 0 ? 1f : -1f) * (decimalPart / 10f);
    }

    public static GoGame[] parse(File file) throws IOException {
        return parse(file, null);
    }

    public static GoGame[] parse(File file, ParseOptions options) throws IOException {
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
            return new SgfParser(options).parse(stream);
        }
        finally {
            Utils.closeObject(stream);
        }
    }


    public static final class ParseOptions {
        private boolean _parseAsProblems;

        public ParseOptions parseAsProblems(boolean parse) {
            _parseAsProblems = parse;
            return this;
        }
    }


    private static final class CopyPushbackReader extends PushbackReader {
        StringBuilder str = new StringBuilder(1024);

        public CopyPushbackReader(Reader reader) {
            super(reader);
        }

        @Override
        public int read() throws IOException {
            int ch = super.read();
            if (ch != -1)
                str.append((char) ch);
            return ch;
        }

        @Override
        public void unread(int oneChar) throws IOException {
            str.setLength(str.length() - 1);
            super.unread(oneChar);
        }

        public String getString() {
            String retStr = str.toString();
            str.setLength(0);
            return retStr;
        }
    }

    /*
    ***** SGF properties *****

    AB  1234  Add Black       setup            list of stone
    AE  1234  Add Empty       setup            list of point
    AN  --34  Annotation      game-info        simpletext
    AP  ---4  Application     root             composed simpletext : simpletext
    AR  ---4  Arrow           -                list of composed point : point
    AW  1234  Add White       setup            list of stone
    B   1234  Black           move             move
    BL  1234  Black time left move             real
    BM  1234  Bad move        move             double
    BR  1234  Black rank      game-info        simpletext
    BT  --34  Black team      game-info        simpletext
    C   1234  Comment         -                text
    CA  ---4  Charset         root             simpletext
    CP  --34  Copyright       game-info        simpletext
    CR  --34  Circle          -                list of point
    DD  ---4  Dim points      - (inherit)      list of point
    DM  --34  Even position   -                double
    DO  --34  Doubtful        move             none
    DT  1234  Date            game-info        simpletext
    EV  1234  Event           game-info        simpletext
    FF  -234  Fileformat      root             number (range: 1-4)
    FG  1234  Figure          -                none | composed number : simpletext
    GB  1234  Good for Black  -                double
    GC  1234  Game comment    game-info        text
    GM  1234  Game            root             number (range: 1-5,7-16)
    GN  1234  Game name       game-info        simpletext
    GW  1234  Good for White  -                double
    HA  1234  Handicap        game-info (Go)   number
    HO  --34  Hotspot         -                double
    IT  --34  Interesting     move             none
    KM  1234  Komi            game-info (Go)   real
    KO  --34  Ko              move             none
    LB  --34  Label           -                list of composed point : simpletext
    LN  --34  Line            -                list of composed point : point
    MA  --34  Mark with X     -                list of point
    MN  --34  Set move number move             number
    N   1234  Nodename        -                simpletext
    OB  --34  OtStones Black  move             number
    ON  --34  Opening         game-info        simpletext
    OT  ---4  Overtime        game-info        simpletext
    OW  --34  OtStones White  move             number
    PB  1234  Player Black    game-info        simpletext
    PC  1234  Place           game-info        simpletext
    PL  1234  Player to play  setup            color
    PM  ---4  Print move mode - (inherit)      number
    PW  1234  Player White    game-info        simpletext
    RE  1234  Result          game-info        simpletext
    RO  1234  Round           game-info        simpletext
    RU  --34  Rules           game-info        simpletext
    SL  1234  Selected        -                list of point
    SO  1234  Source          game-info        simpletext
    SQ  ---4  Square          -                list of point
    ST  ---4  Style           root             number (range: 0-3)
    SZ  1234  Size            root             (number | composed number : number)
    TB  1234  Territory Black - (Go)           elist of point
    TE  1234  Tesuji          move             double
    TM  1234  Timelimit       game-info        real
    TR  --34  Triangle        -                list of point
    TW  1234  Territory White - (Go)           elist of point
    UC  --34  Unclear pos     -                double
    US  1234  User            game-info        simpletext
    V   1234  Value (score)   -                real
    VW  1234  View            - (inherit)      elist of point
    W   1234  White           move             move
    WL  1234  White time left move             real
    WR  1234  White rank      game-info        simpletext
    WT  --34  White team      game-info        simpletext
    */

    static {
        _properties = new String[PROP_MAX_VALUE + 1];

        _properties[PROP_ADD_BLACK] = "AB";
        _properties[PROP_ADD_EMPTY] = "AE";
        _properties[PROP_ANNOTATION] = "AN";
        _properties[PROP_APPLICATION_NAME] = "AP";
        _properties[PROP_ARROW] = "AR";
        _properties[PROP_ADD_WHITE] = "AW";

        _properties[PROP_BLACK] = "B";
        _properties[PROP_BLACK_TIME_LEFT] = "BL";
        _properties[PROP_BAD_MOVE] = "BM";
        _properties[PROP_BLACK_RANK] = "BR";
        _properties[PROP_BLACK_TEAM] = "BT";

        _properties[PROP_COMMENT] = "C";
        _properties[PROP_CHARSET] = "CA";
        _properties[PROP_COPYRIGHT] = "CP";
        _properties[PROP_CIRCLE] = "CR";

        _properties[PROP_DIM_POINTS] = "DD";
        _properties[PROP_EVEN_POSITION] = "DM";
        _properties[PROP_DOUBTFUL] = "DO";
        _properties[PROP_DATE] = "DT";

        _properties[PROP_EVENT] = "EV";

        _properties[PROP_SGF_VERSION] = "FF";
        _properties[PROP_FIGURE] = "FG";

        _properties[PROP_GOOD_FOR_BLACK] = "GB";
        _properties[PROP_GAME_COMMENT] = "GC";
        _properties[PROP_GAME] = "GM";
        _properties[PROP_GAME_NAME] = "GN";
        _properties[PROP_GOOD_FOR_WHITE] = "GW";

        _properties[PROP_HANDICAP] = "HA";
        _properties[PROP_HOTSPOT] = "HO";

        _properties[PROP_INTERESTING] = "IT";

        _properties[PROP_KOMI] = "KM";
        _properties[PROP_KO] = "KO";

        _properties[PROP_LABEL] = "LB";
        _properties[PROP_LINE] = "LN";

        _properties[PROP_MARK_WITH_X] = "MA";
        _properties[PROP_SET_MOVE_NUMBER] = "MN";

        _properties[PROP_NODE_NAME] = "N";

        _properties[PROP_OT_STONES_BLACK] = "OB";
        _properties[PROP_OPENING] = "ON";
        _properties[PROP_OVERTIME] = "OT";
        _properties[PROP_OT_STONES_WHITE] = "OW";

        _properties[PROP_PLAYER_BLACK] = "PB";
        _properties[PROP_PLACE] = "PC";
        _properties[PROP_PLAYER_TO_PLAY] = "PL";
        _properties[PROP_PRINT_MOVE_MODE] = "PM";
        _properties[PROP_PLAYER_WHITE] = "PW";

        _properties[PROP_RESULT] = "RE";
        _properties[PROP_ROUND] = "RO";
        _properties[PROP_RULES] = "RU";

        _properties[PROP_SELECTED] = "SL";
        _properties[PROP_SOURCE] = "SO";
        _properties[PROP_SQUARE] = "SQ";
        _properties[PROP_STYLE] = "ST";
        _properties[PROP_SIZE] = "SZ";

        _properties[PROP_TERRITORY_BLACK] = "TB";
        _properties[PROP_TESUJI] = "TE";
        _properties[PROP_TIME_LIMIT] = "TM";
        _properties[PROP_TRIANGLE] = "TR";
        _properties[PROP_TERRITORY_WHITE] = "TW";

        _properties[PROP_UNCLEAR_POS] = "UC";
        _properties[PROP_USER] = "US";

        _properties[PROP_VALUE] = "V";
        _properties[PROP_VIEW] = "VW";

        _properties[PROP_WHITE] = "W";
        _properties[PROP_WHITE_TIME_LEFT] = "WL";
        _properties[PROP_WHITE_RANK] = "WR";
        _properties[PROP_WHITE_TEAM] = "WT";
    }
}
