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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


/**
 * Contains the score panel.
 */
public class ScoreView extends FrameLayout {
    private TextView _txtBlackName;
    private TextView _txtWhiteName;
    private TextView _txtBlackTime;
    private TextView _txtWhiteTime;
    private PrisonersView _blackPrisonersView;
    private PrisonersView _whitePrisonersView;

    private String _blackName = "";
    private String _whiteName = "";
    private String _blackRank = "";
    private String _whiteRank = "";
    private int _blackPrisoners = 0;
    private int _whitePrisoners = 0;
    private ForegroundColorSpan _rankSpan;


    public ScoreView(Context context) {
        super(context);
        _init(context);
    }

    public ScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _init(context);
    }

    public ScoreView(Context context, AttributeSet attrs, int styleDef) {
        super(context, attrs, styleDef);
        _init(context);
    }


    private void _init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.score_panel, this, true);
        _rankSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.scoreview_rank));

        _txtBlackName = (TextView) findViewById(R.id.score_black_name);
        _txtWhiteName = (TextView) findViewById(R.id.score_white_name);
        _txtBlackTime = (TextView) findViewById(R.id.score_black_time);
        _txtWhiteTime = (TextView) findViewById(R.id.score_white_time);
        _blackPrisonersView = (PrisonersView) findViewById(R.id.score_black_prisoners);
        _whitePrisonersView = (PrisonersView) findViewById(R.id.score_white_prisoners);
    }


    public void addBlackPrisoners(int amount) {
        setBlackPrisoners(_blackPrisoners + amount);
    }

    public void addWhitePrisoners(int amount) {
        setWhitePrisoners(_whitePrisoners + amount);
    }

    public void setBlackPrisoners(int newAmount) {
        _blackPrisoners = newAmount;
        _blackPrisonersView.setCapturedStones(newAmount);
    }

    public void setWhitePrisoners(int newAmount) {
        _whitePrisoners = newAmount;
        _whitePrisonersView.setCapturedStones(newAmount);
    }

    public void setBlackName(String blackName) {
        _blackName = blackName;
        _refreshBlackName();
    }

    public void setWhiteName(String whiteName) {
        _whiteName = whiteName;
        _refreshWhiteName();
    }

    public void setBlackRank(String blackRank) {
        _blackRank = blackRank;
        _refreshBlackName();
    }

    public void setWhiteRank(String whiteRank) {
        _whiteRank = whiteRank;
        _refreshWhiteName();
    }

    private void _refreshBlackName() {
        _txtBlackName.setText(_spanText(_blackName, _blackRank));
    }

    private void _refreshWhiteName() {
        _txtWhiteName.setText(_spanText(_whiteName, _whiteRank));
    }

    private CharSequence _spanText(String name, String rank) {
        String finalStr = name + " " + rank;
        SpannableString spanStr = new SpannableString(finalStr);
        spanStr.setSpan(_rankSpan, name.length(), finalStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * Change le temps restant pour noir. Passer "null" ou une chaine vide cachera complètement la View.
     */
    public void setBlackTime(String blackTime) {
        if (blackTime != null && blackTime.length() > 0) {
            if (_txtBlackTime.getVisibility() != View.VISIBLE)
                _txtBlackTime.setVisibility(View.VISIBLE);
            _txtBlackTime.setText(blackTime);
        } else {
            _txtBlackTime.setVisibility(View.GONE);
        }
    }

    /**
     * Change le temps restant pour blanc. Passer "null" ou une chaine vide cachera complètement la View.
     */
    public void setWhiteTime(String whiteTime) {
        if (whiteTime != null && whiteTime.length() > 0) {
            if (_txtWhiteTime.getVisibility() != View.VISIBLE)
                _txtWhiteTime.setVisibility(View.VISIBLE);
            _txtWhiteTime.setText(whiteTime);
        } else {
            _txtWhiteTime.setVisibility(View.GONE);
        }
    }
}
