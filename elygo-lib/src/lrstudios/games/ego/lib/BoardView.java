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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import lrstudios.games.ego.lib.themes.BlackWhiteTheme;
import lrstudios.games.ego.lib.themes.DarkBoardTheme;
import lrstudios.games.ego.lib.themes.StandardTheme;
import lrstudios.games.ego.lib.themes.Theme;
import lrstudios.util.android.AndroidUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Displays a go board.<p>
 * <p/>
 * There are no functions to manipulate the go board in the view. It's only a visual
 * representation of a board and any board manipulation is done outside of this class.
 */
public final class BoardView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "BoardView";

    private static final double ANIM_CAPTURE_DURATION = 380.0;


    // Values from preferences
    private Theme _theme;
    private int _zoom_margin;
    private boolean _requiresValidation;
    private float _setting_offsetY;
    private boolean _offsetLarge;
    private int _gridLineSize;
    private int _stonesPadding;

    // Internal variables
    private BoardListener _listener;
    private InitListener _initListener;
    private GestureDetector _gestureDetector;
    private RefreshHandler _refreshHandler = new RefreshHandler();

    private int _surfaceWidth;
    private int _surfaceHeight;
    private int _finalWidth;
    private int _finalHeight;
    private int _surfaceSmallestSize;
    private int _surfaceLargestSize;
    private int _leftMargin;
    private float _answerCircleRadius;
    private float _baseGridInterval;
    private float _offsetY;
    private boolean _isZoom;
    private float _zoomFactor;
    private int _stoneSize;
    private Point _crossCursor = new Point(-1, -1);
    private Point _moveValidated;
    private Rect _baseBounds;
    private Rect _clipBounds;
    private boolean _isMoveLegal;
    private boolean _forceRequiresValidation;
    private boolean _allowIllegalMoves;
    private boolean _playLock;
    private boolean _showAnswers;
    private boolean _showVariations;
    private boolean _showFinalStatus;
    private boolean _reverseColors;
    private boolean _monocolor;

    private List<BoardAnimation> _anim_prisonersList = new ArrayList<BoardAnimation>();
    private Drawable _anim_captureBlackDrawable;
    private Drawable _anim_captureWhiteDrawable;

    private Point _pt_coord2point = new Point(-1, -1);
    private Paint _stdBitmapPaint;
    private Drawable _cursorDrawableBlack;
    private Drawable _cursorDrawableWhite;

    private GoGame _game;
    private int _size;


    // Gesture detector callbacks
    private final class BoardGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            if (!_playLock) {
                float x = event.getX();
                float y = event.getY();

                if (_requiresValidation) {
                    final Point coords = _cache_getBoardCoordsAtLocation(x, y);
                    if (_crossCursor.x >= 0 && _isMoveLegal && coords.equals(_crossCursor.x, _crossCursor.y))
                        _moveValidated = new Point(coords);
                    else
                        _moveValidated = null;
                }
                if (_moveValidated == null) {
                    final Point coords = _cache_getBoardCoordsAtLocation(x, y - _offsetY);
                    moveCrossCursor(coords);
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (_requiresValidation && !_playLock && _moveValidated != null) {
                if (_listener != null)
                    _listener.onPress(_moveValidated.x, _moveValidated.y);
                moveCrossCursor(null);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
        }
    }

    private boolean onUp(MotionEvent event) {
        if (!_requiresValidation && !_playLock) {
            final Point coords = _cache_getBoardCoordsAtLocation(event.getX(), event.getY() - _offsetY);

            if (_crossCursor.x >= 0 && _listener != null && _isInBounds(coords) &&
                    (_allowIllegalMoves || _game.isLegal(coords.x, coords.y)))
            {
                _listener.onPress(coords.x, coords.y);
            }
            moveCrossCursor(null);
        }
        else if (_requiresValidation && !_isMoveLegal) {
            moveCrossCursor(null);
        }

        return true;
    }

    private boolean onMove(MotionEvent event) {
        if (!_playLock && _moveValidated == null) {
            final Point coords = _cache_getBoardCoordsAtLocation(event.getX(), event.getY() - _offsetY);
            moveCrossCursor(coords);
        }

        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            // We don't use the onScroll event of the GestureDetector because it stops sending events after a long press
            case MotionEvent.ACTION_MOVE:
                onMove(e);
                break;
            case MotionEvent.ACTION_UP:
                onUp(e);
                break;
        }
        return _gestureDetector.onTouchEvent(e);
    }


    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readPreferences();

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(this);

        _gestureDetector = new GestureDetector(context, new BoardGestureListener());
        _stdBitmapPaint = new Paint();
        _answerCircleRadius = getResources().getDimension(R.dimen.boardview_answer_circle_radius);

        lockPlaying(); // To prevent errors if the user try to play before the surface is created
    }


    public void readPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        _zoom_margin = Integer.parseInt(prefs.getString("tsumegoMarginPref", "3"));
        _stonesPadding = Integer.parseInt(prefs.getString("stonePaddingPref", "1"));
        _gridLineSize = Integer.parseInt(prefs.getString("gridLinesSizePref", "1"));
        _requiresValidation = _forceRequiresValidation || prefs.getBoolean("requiresValidationPref", false);
        String skin = prefs.getString("themePref", "standard");
        String inputType = prefs.getString("inputType", "0");

        if (skin.equals("blackwhite")) {
            if (!(_theme instanceof BlackWhiteTheme))
                _theme = new BlackWhiteTheme(getContext());
        }
        else if (skin.equals("darkwood")) {
            if (!(_theme instanceof DarkBoardTheme))
                _theme = new DarkBoardTheme(getContext());
        }
        else {
            if (!(_theme instanceof StandardTheme))
                _theme = new StandardTheme(getContext());
        }

        _setting_offsetY = inputType.startsWith("offset") ? getResources().getDimension(R.dimen.stone_input_offset) : 0;
        _offsetLarge = inputType.equals("offsetLarge");
    }


    /**
     * Changes the current board shown by this view.
     */
    public void changeGame(GoGame game, boolean allowZoom) {
        _isZoom = allowZoom;
        _game = game;

        _size = _game.board.getSize();
        _showAnswers = false;
        _baseBounds = null;
        recreateGraphics();
    }

    public void setZoomMargin(int margin) {
        if (margin < 0)
            margin = 0;
        else if (margin > _size)
            margin = _size;

        _zoom_margin = margin;
        recreateGraphics();
    }

    public int getZoomMargin() {
        return _zoom_margin;
    }

    /**
     * Defines whether playing moves needs validation or not (this overrides the preference).
     */
    public void setMoveValidation(boolean needValidation) {
        _forceRequiresValidation = needValidation;
    }

    /**
     * Allows or prevents the view to fire onPress events for illegal moves.
     */
    public void allowIllegalMoves(boolean allow) {
        _allowIllegalMoves = allow;
    }

    /**
     * Prevents the user to play a move.
     */
    public void lockPlaying() {
        _playLock = true;
        moveCrossCursor(null);
    }

    /**
     * Allows the user to play a move.
     */
    public void unlockPlaying() {
        _playLock = false;
    }

    public void showVariations(boolean show) {
        _showVariations = show;
    }


    /**
     * Shows or hides the good and wrong variations of the current problem by drawing marks on the board.
     */
    public void showAnswers(boolean show) {
        _showAnswers = show;
        invalidate();
    }

    /**
     * If the answers are already shown, they will become hidden and vice versa.
     */
    public void toggleAnswers() {
        showAnswers(!_showAnswers);
    }

    /**
     * Reverses (or not) all colors of the current game (only visually, it doesn't alter the game).
     */
    public void setReverseColors(boolean reverse) {
        _reverseColors = reverse;
    }

    /**
     * If set to true, all black stones will become white.
     */
    public void setMonocolor(boolean enable) {
        _monocolor = enable;
        invalidate();
    }

    /**
     * Returns the current visual theme of this board.
     */
    public Theme getCurrentTheme() {
        return _theme;
    }


    /**
     * Draws the cross cursor to the specified location.
     */
    private void moveCrossCursor(Point coords) {
        if (_isInBounds(coords)) {
            _crossCursor.set(coords.x, coords.y);
            _isMoveLegal = _allowIllegalMoves || _game.isLegal(coords.x, coords.y);
        }
        else {
            _crossCursor.set(-1, -1);
        }
        invalidate(); // TODO repaint cross cursor only
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Log.v(TAG, "surfaceChanged() width=" + width + ", height=" + height);

        final boolean isLandscape = width > height;
        _surfaceWidth = width;
        _surfaceHeight = height;
        _surfaceSmallestSize = (isLandscape) ? height : width;
        _surfaceLargestSize = (isLandscape) ? width : height;
        recreateGraphics();
    }

    /**
     * Recreates some graphical objects when the zoom, board or screen size is changed.
     * This also invalidates the view.
     */
    public void recreateGraphics() {
        if (_surfaceWidth <= 0 || _surfaceHeight <= 0)
            return;

        _baseGridInterval = _surfaceSmallestSize / (float) _size;

        _computeDimensions(_isZoom && _baseBounds == null);

        if (!_offsetLarge || (_clipBounds.width() > 15 && _clipBounds.height() > 15))
            _offsetY = _setting_offsetY;
        else
            _offsetY = 0;

        _leftMargin = (_surfaceWidth - _finalWidth) / 2;
        _zoomFactor = _stoneSize / _baseGridInterval;
        _crossCursor.set(-1, -1);

        Theme.Config config = new Theme.Config(
                _surfaceWidth, _surfaceHeight, _stoneSize, _size, _gridLineSize, _stonesPadding);
        _theme.init(config);
        if (_initListener != null)
            _initListener.onThemeLoaded(_theme);

        Resources res = getContext().getResources();
        _cursorDrawableBlack = new BitmapDrawable(res, _theme.blackStoneBitmap);
        _cursorDrawableBlack.setAlpha(98);
        _cursorDrawableWhite = new BitmapDrawable(res, _theme.whiteStoneBitmap);
        _cursorDrawableWhite.setAlpha(98);

        _anim_captureBlackDrawable = new BitmapDrawable(res, _theme.blackStoneBitmap);
        _anim_captureWhiteDrawable = new BitmapDrawable(res, _theme.whiteStoneBitmap);
        invalidate();
    }


    private void _computeDimensions(boolean allowRotation) {
        final Rect maxBounds = new Rect(0, 0, _size - 1, _size - 1);
        _clipBounds = (_baseBounds == null) ? _game.board.getBounds() : _baseBounds;

        // _baseBounds avoids rotating/zooming the same problem multiple times (this may
        // happen especially when the user go to the preferences screen during playing
        // and go back).
        _baseBounds = new Rect(_clipBounds);

        if (!_isZoom || _clipBounds.right < 0 || _clipBounds.bottom < 0) {
            _clipBounds.set(0, 0, _size - 1, _size - 1);
            allowRotation = false;
        }
        else {
            AndroidUtils.Rect_addMargin(_clipBounds, (_size < 19 ? 99 : _zoom_margin), maxBounds);
        }

        int hSize = _clipBounds.width() + 1;
        int vSize = _clipBounds.height() + 1;
        int zoom_largestBoardSize = (hSize > vSize) ? hSize : vSize;
        int zoom_smallestBoardSize = (hSize > vSize) ? vSize : hSize;

        int largestSide_maxStoneSize = _surfaceLargestSize / zoom_largestBoardSize;
        int smallestSide_maxStoneSize = _surfaceSmallestSize / zoom_smallestBoardSize;

        _stoneSize = (largestSide_maxStoneSize < smallestSide_maxStoneSize) ? largestSide_maxStoneSize : smallestSide_maxStoneSize;

        int highestSize = _stoneSize * zoom_largestBoardSize;
        int lowestSize = _stoneSize * zoom_smallestBoardSize;

        _finalWidth = (_surfaceWidth > _surfaceHeight) ? highestSize : lowestSize;
        _finalHeight = (_surfaceWidth > _surfaceHeight) ? lowestSize : highestSize;

        if (allowRotation
                && ((hSize > vSize && _finalWidth < _finalHeight)
                || (hSize < vSize && _finalWidth > _finalHeight)))
        {
            _game.rotateCCW();
            _baseBounds = null;
            _computeDimensions(false);
        }
        else {
            // If an entire side of the board is shown, there may be some space left on this side. We remove it
            int spaceWidth = (_surfaceWidth - _finalWidth) / _stoneSize;

            _finalWidth += spaceWidth * _stoneSize;

            int leftWidth = spaceWidth - _clipBounds.left;
            if (leftWidth < 0)
                leftWidth = 0;
            _clipBounds.left -= spaceWidth - leftWidth;
            _clipBounds.right += leftWidth;

            int rightSpace = (_surfaceWidth - (_clipBounds.right - _clipBounds.left + 1) * _stoneSize) / _stoneSize;
            if (rightSpace + _clipBounds.width() >= _size)
                rightSpace = _size - 1 - _clipBounds.width();
            int bottomSpace = (_surfaceHeight - (_clipBounds.bottom - _clipBounds.top + 1) * _stoneSize) / _stoneSize;
            if (bottomSpace + _clipBounds.height() >= _size)
                bottomSpace = _size - 1 - _clipBounds.height();

            if (rightSpace > 0) {
                _clipBounds.right += rightSpace;
                _finalWidth += _stoneSize * rightSpace;
            }
            if (bottomSpace > 0) {
                _clipBounds.bottom += bottomSpace;
                _finalHeight += _stoneSize * bottomSpace;
            }

            AndroidUtils.Rect_crop(_clipBounds, maxBounds);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (_game == null) {
            _game = new GoGame(9, 6.5, 0);
            changeGame(_game, false);
        }
        setWillNotDraw(false); // Necessary for `onDraw()` to be called
        unlockPlaying();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        lockPlaying();
    }


    @Override
    public void onDraw(Canvas canvas) {
        // Background
        _theme.drawBackground(canvas, 0, 0, _surfaceWidth, _surfaceHeight);
        canvas.translate(_leftMargin, 0);

        // Grid
        canvas.save();
        canvas.clipRect(0, 0, _finalWidth, _finalHeight);
        canvas.scale(_zoomFactor, _zoomFactor);
        canvas.translate(-Math.round(_clipBounds.left * _baseGridInterval), -Math.round(_clipBounds.top * _baseGridInterval));

        _theme.drawGrid(canvas);

        canvas.restore();

        // Stones
        final GoBoard board = _game.board;
        final int boardSize = board.getSize();
        final byte[] colors = board.getBoardArray();
        final int stoneSize = _stoneSize;

        for (int curX = _clipBounds.left; curX <= _clipBounds.right; curX++) {
            for (int curY = _clipBounds.top; curY <= _clipBounds.bottom; curY++) {
                int x = curX - _clipBounds.left;
                int y = curY - _clipBounds.top;

                boolean showStatus = _showFinalStatus;
                if (showStatus) {
                    byte color = _game.getFinalStatus(curX, curY);
                    if (_reverseColors) {
                        switch (color) {
                            case GoBoard.BLACK_TERRITORY:
                                color = GoBoard.WHITE_TERRITORY;
                                break;
                            case GoBoard.WHITE_TERRITORY:
                                color = GoBoard.BLACK_TERRITORY;
                                break;
                            case GoBoard.DEAD_BLACK_STONE:
                                color = GoBoard.DEAD_WHITE_STONE;
                                break;
                            case GoBoard.DEAD_WHITE_STONE:
                                color = GoBoard.DEAD_BLACK_STONE;
                                break;
                        }
                    }
                    if (color == GoBoard.BLACK_TERRITORY) {
                        _theme.blackTerritory.setBounds(
                                stoneSize * x, stoneSize * y,
                                stoneSize * (x + 1), stoneSize * (y + 1));
                        _theme.blackTerritory.draw(canvas);
                    }
                    else if (color == GoBoard.WHITE_TERRITORY) {
                        _theme.whiteTerritory.setBounds(
                                stoneSize * x, stoneSize * y,
                                stoneSize * (x + 1), stoneSize * (y + 1));
                        _theme.whiteTerritory.draw(canvas);
                    }
                    else if (color == GoBoard.DEAD_BLACK_STONE) {
                        _theme.drawDeadBlackStone(canvas, stoneSize * x, stoneSize * y, stoneSize);
                    }
                    else if (color == GoBoard.DEAD_WHITE_STONE) {
                        _theme.drawDeadWhiteStone(canvas, stoneSize * x, stoneSize * y, stoneSize);
                    }
                    else {
                        showStatus = false;
                    }
                }

                if (!showStatus) {
                    byte color = colors[curY * boardSize + curX];
                    if (_reverseColors && color != GoBoard.EMPTY)
                        color = GoBoard.getOppositeColor(color);
                    if (color == GoBoard.BLACK && !_monocolor)
                        canvas.drawBitmap(_theme.blackStoneBitmap, stoneSize * x + _stonesPadding, stoneSize * y + _stonesPadding, _stdBitmapPaint);
                    else if (color == GoBoard.WHITE || (color == GoBoard.BLACK && _monocolor))
                        canvas.drawBitmap(_theme.whiteStoneBitmap, stoneSize * x + _stonesPadding, stoneSize * y + _stonesPadding, _stdBitmapPaint);
                    else if (color == GoBoard.ANY)
                        _theme.drawAnyStone(canvas, stoneSize * x, stoneSize * y, stoneSize);
                }
            }
        }

        // Variations
        if (_showVariations && !_showFinalStatus) {
            GameNode parentMove = _game.getCurrentNode().parentNode;
            if (parentMove != null) {
                for (GameNode move : parentMove.nextNodes) {
                    if (_isInBounds(move.x, move.y) && _game.board.getColor(move.x, move.y) == GoBoard.EMPTY) {
                        byte color = move.color;
                        if (_reverseColors && color != GoBoard.EMPTY)
                            color = GoBoard.getOppositeColor(color);

                        if (color == GoBoard.BLACK)
                            _theme.drawBlackVariation(canvas, stoneSize * move.x, stoneSize * move.y, stoneSize);
                        else if (color == GoBoard.WHITE)
                            _theme.drawWhiteVariation(canvas, stoneSize * move.x, stoneSize * move.y, stoneSize);
                    }
                }
            }
        }

        // Marks
        final float MARK_PADDING = 4.5f + ((_size < 10) ? 0.5f : 0f);
        for (BoardMark mark : board.getMarks()) {
            int x = mark.x - _clipBounds.left;
            int y = mark.y - _clipBounds.top;
            byte color = colors[mark.y * boardSize + mark.x];
            if (_reverseColors && color != GoBoard.EMPTY)
                color = GoBoard.getOppositeColor(color);

            // Letters and digits
            if (mark.type == BoardMark.MARK_LABEL) {
                Paint paint = (color == GoBoard.BLACK) ? _theme.blackLabelPaint : // TODO should be in _theme (no public Paint at all would be better)
                        (color == GoBoard.WHITE) ? _theme.whiteLabelPaint : _theme.boardLabelPaint;

                String markText = Character.toString(mark.getLabel()).toLowerCase();
                Rect bounds = new Rect();
                paint.getTextBounds(markText, 0, markText.length(), bounds);
                canvas.drawText(markText,
                        stoneSize * x + (stoneSize / 2.0f) - AndroidUtils.getTextWidth(markText, paint) / 2.0f, // getTextWidth() is more accurate than getBounds()
                        stoneSize * y + (stoneSize / 2.0f) + bounds.height() / 2.0f,
                        paint);
            }
            // Shapes
            else {
                Paint paint = (color == GoBoard.BLACK) ? _theme.blackMarkPaint :
                        (color == GoBoard.WHITE) ? _theme.whiteMarkPaint : _theme.boardMarkPaint;

                ShapeDrawable markShape = null;
                switch (mark.type) {
                    case BoardMark.MARK_CIRCLE:
                        markShape = _theme.circleMark;
                        break;
                    case BoardMark.MARK_CROSS:
                        markShape = _theme.crossMark;
                        break;
                    case BoardMark.MARK_SQUARE:
                        markShape = _theme.squareMark;
                        break;
                    case BoardMark.MARK_TRIANGLE:
                        markShape = _theme.triangleMark;
                        break;
                }

                if (markShape != null) {
                    markShape.getPaint().set(paint);
                    markShape.setBounds(
                            Math.round(stoneSize * x + stoneSize / MARK_PADDING),
                            Math.round(stoneSize * y + stoneSize / MARK_PADDING),
                            Math.round(stoneSize * x + stoneSize - stoneSize / MARK_PADDING),
                            Math.round(stoneSize * y + stoneSize - stoneSize / MARK_PADDING));
                    markShape.draw(canvas);
                }
            }
        }

        // Problem solution
        if (_showAnswers) {
            for (GameNode node : _game.getCurrentNode().nextNodes) {
                if (node.value < 0)
                    continue;

                int x = node.x - _clipBounds.left;
                int y = node.y - _clipBounds.top;
                canvas.drawCircle(
                        stoneSize * x + stoneSize / 2,
                        stoneSize * y + stoneSize / 2,
                        _answerCircleRadius, (node.value > 0) ? _theme.goodVariationPaint : _theme.badVariationPaint);
            }
        }

        // Cross cursor
        if (_crossCursor.x >= 0) {
            int x = _crossCursor.x - _clipBounds.left;
            int y = _crossCursor.y - _clipBounds.top;

            Paint paint = _isMoveLegal ? _theme.crossCursorPaint : _theme.illegalCrossCursorPaint;
            canvas.drawLine(
                    stoneSize * x + stoneSize / 2f, 0,
                    stoneSize * x + stoneSize / 2f, _surfaceHeight, paint);
            canvas.drawLine(
                    -_leftMargin, stoneSize * y + stoneSize / 2f,
                    _surfaceWidth - _leftMargin, stoneSize * y + stoneSize / 2f, paint);

            if (_isMoveLegal) {
                Drawable cursor;
                byte nextPlayer = _game.getNextPlayer();
                if (nextPlayer == GoBoard.ANY)
                    cursor = _theme.anyStoneDrawable;
                else if (nextPlayer == GoBoard.EMPTY)
                    cursor = _theme.blackTerritory;
                else if (!_monocolor && ((nextPlayer == GoBoard.BLACK && !_reverseColors) || (nextPlayer == GoBoard.WHITE && _reverseColors)))
                    cursor = _cursorDrawableBlack;
                else
                    cursor = _cursorDrawableWhite;
                cursor.setBounds(
                        stoneSize * x + _stonesPadding, stoneSize * y + _stonesPadding,
                        stoneSize * x - _stonesPadding + _stoneSize, stoneSize * y - _stonesPadding + _stoneSize);
                cursor.draw(canvas);
            }
        }

        // Animations
        if (_anim_prisonersList.size() > 0) {
            for (BoardAnimation anim : _anim_prisonersList)
                anim.draw(canvas);
        }
    }


    public void addPrisoners(Iterable<LightCoords> prisoners) {
        if (prisoners == null)
            return;

        for (LightCoords coords : prisoners) {
            int x = (coords.x - _clipBounds.left) * _stoneSize;
            int y = (coords.y - _clipBounds.top) * _stoneSize;

            Drawable anim;
            if (!_monocolor && ((coords.color == GoBoard.BLACK && !_reverseColors) || (coords.color == GoBoard.WHITE && _reverseColors)))
                anim = _anim_captureBlackDrawable;
            else
                anim = _anim_captureWhiteDrawable;
            _anim_prisonersList.add(new BoardAnimation(anim,
                    new Rect(x + _stonesPadding, y + _stonesPadding, x + _stoneSize - _stonesPadding, y + _stoneSize - _stonesPadding),
                    new Rect(x + _stoneSize / 2, y + _stoneSize / 2, x + _stoneSize / 2, y + _stoneSize / 2),
                    255, 85));
        }

        _updateAnimations();
    }


    public void setBoardListener(BoardListener listener) {
        _listener = listener;
    }

    public void setInitListener(InitListener listener) {
        _initListener = listener;
    }

    /**
     * Shows (or not) the final status of the current game.
     */
    public void showFinalStatus(boolean show) {
        _showFinalStatus = show;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!_isZoom && _finalHeight > 0) // TODO this won't work properly if a board is zoomed and set to "wrap_content" (add a _fullscreen field?)
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec));
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private boolean _isInBounds(Point coords) {
        return coords != null && _isInBounds(coords.x, coords.y);
    }

    private boolean _isInBounds(int x, int y) {
        return x >= _clipBounds.left && x <= _clipBounds.right
                && y >= _clipBounds.top && y <= _clipBounds.bottom;
    }


    /**
     * Updates all animations on the board.
     */
    private void _updateAnimations() {
        final double SLEEP_INTERVAL = 1000.0 / 50.0;

        if (_anim_prisonersList.size() > 0) {
            Iterator<BoardAnimation> it = _anim_prisonersList.iterator();
            while (it.hasNext()) {
                BoardAnimation anim = it.next();
                anim.update(SLEEP_INTERVAL / ANIM_CAPTURE_DURATION);

                if (anim.isFinished())
                    it.remove();

                invalidate(
                        _leftMargin + anim.startX, anim.startY,
                        _leftMargin + anim.startX + anim.startWidth, anim.startY + anim.startHeight);
            }

            _refreshHandler.postUpdate((long) SLEEP_INTERVAL);
        }
    }


    /**
     * Returns the board coordinates located at the specified (x, y) point on the surface.
     * WARNING : for performance issues, the reference returned will always be the same.
     */
    private Point _cache_getBoardCoordsAtLocation(float x, float y) {
        int finalX = (int) (x / _stoneSize) + _clipBounds.left;
        int finalY = (int) (y / _stoneSize) + _clipBounds.top;
        _pt_coord2point.set(finalX, finalY);
        return _pt_coord2point;
    }


    // Handle view animations
    private final class RefreshHandler extends Handler {
        static final int _MSG_REPAINT = 5555;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == _MSG_REPAINT)
                BoardView.this._updateAnimations();
        }

        /**
         * Updates all animations, then refresh the View.
         */
        public void postUpdate(long delayMillis) {
            this.removeMessages(_MSG_REPAINT);
            sendMessageDelayed(obtainMessage(_MSG_REPAINT), delayMillis);
        }
    }


    public interface BoardListener {
        /**
         * Called when the user clicks on an intersection of the board.
         */
        void onPress(int x, int y);
    }

    public interface InitListener {
        void onThemeLoaded(Theme theme);
    }
}
