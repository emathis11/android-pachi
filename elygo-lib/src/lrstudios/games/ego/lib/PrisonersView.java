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
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;


public class PrisonersView extends View
{
    private static final String TAG = "PrisonersView";

    private int _stoneSize;
    private ShapeDrawable _stone;
    private Paint _textPaint;
    private String _prisoners;
    private boolean _isWhiteStone = false;


    public PrisonersView(Context context)
    {
        super(context);
        _handleAttributes(context, null);
        _recreateGraphics();
    }

    public PrisonersView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _handleAttributes(context, attrs);
        _recreateGraphics();
    }

    public PrisonersView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        _handleAttributes(context, attrs);
        _recreateGraphics();
    }


    private void _handleAttributes(Context context, AttributeSet attrs)
    {
        if (attrs != null)
        {
            TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PrisonersView);
            _isWhiteStone = arr.getBoolean(R.styleable.PrisonersView_showWhiteStone, false);
            arr.recycle();
        }
        _textPaint = new Paint();
        _textPaint.setColor(_isWhiteStone ? Color.BLACK : Color.WHITE);
        _textPaint.setAntiAlias(true);
    }

    /**
     * Changes the displayed number of prisoners.
     */
    public void setCapturedStones(int newAmount)
    {
        String temp = Integer.toString(newAmount);
        if (temp.equals(_prisoners))
            return;
        _prisoners = temp;
        int len = _prisoners.length() - 1;
        _textPaint.setTextSize((0.7f - 0.1f * len) * _stoneSize);
        invalidate();
    }


    private void _recreateGraphics()
    {
        _stoneSize = Math.round(getResources().getDimension(R.dimen.prisoners_view_stone_size));
        if (_isWhiteStone)
        {
            Paint whiteStonePaint = new Paint();
            whiteStonePaint.setAntiAlias(true);
            whiteStonePaint.setShader(new LinearGradient(
                    (int)(_stoneSize * 0.33), 0,
                _stoneSize, _stoneSize,
                    Color.rgb(255, 255, 255), Color.rgb(142, 142, 142), Shader.TileMode.CLAMP));

            _stone = new ShapeDrawable(new OvalShape());
            _stone.getPaint().set(whiteStonePaint);
            _stone.setBounds(0, 0, _stoneSize, _stoneSize);
        }
        else
        {
            Paint blackStonePaint = new Paint();
            blackStonePaint.setAntiAlias(true);
            blackStonePaint.setShader(new RadialGradient(
                _stoneSize / 3.0f, _stoneSize / 10.0f, _stoneSize / 2.1f,
                    Color.rgb(70, 70, 70), Color.BLACK, Shader.TileMode.CLAMP));

            _stone = new ShapeDrawable(new OvalShape());
            _stone.getPaint().set(blackStonePaint);
            _stone.setBounds(0, 0, _stoneSize, _stoneSize);
        }
        setCapturedStones(0);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        _stone.draw(canvas);

        Rect rect = new Rect();
        _textPaint.getTextBounds(_prisoners, 0, _prisoners.length(), rect);
        canvas.drawText(
                _prisoners,
                (_stoneSize - Utils.getTextWidth(_prisoners, _textPaint)) / 2f,
                (_stoneSize - rect.height()) / 2f + rect.height(),
                _textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(_stoneSize, _stoneSize);
/*        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        if(parentWidth < parentHeight)
            this.setMeasuredDimension(parentWidth, (int)(parentWidth*(float)factor));
        else
            this.setMeasuredDimension((int)(parentHeight/(float)factor), parentHeight);*/
    }
}
