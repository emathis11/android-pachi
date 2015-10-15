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
 * Contains the result of a game in the SGF Format (RE[] property).<p>
 * <p/>
 * "0" (zero) or "Draw" for a draw (jigo)<br/>
 * "B+" ["score"] for a black win<br/>
 * "W+" ["score"] for a white win<br/>
 * Score is optional (some games don't have a score e.g. chess).<br/>
 * If the score is given it has to be given as a real value, e.g. "B+0.5", "W+64", "B+12.5"<br/>
 * Use "B+R" or "B+Resign" and "W+R" or "W+Resign" for a win by resignation.
 * Applications must not write "Black resigns".<br/>
 * Use "B+T" or "B+Time" and "W+T" or "W+Time" for a win on time, "B+F" or "B+Forfeit" and "W+F"
 * or "W+Forfeit" for a win by forfeit<br/>
 * "Void" for no result or suspended play<br/>
 * "?" for an unknown result.
 */
public class GoGameResult {
    public static final char
            JIGO = '0',
            BLACK = 'B',
            WHITE = 'W',
            VOID = 'V',
            UNKNOWN_WINNER = '?';

    public static final double
            RESIGN = -1,
            TIME = -2,
            FORFEIT = -3,
            UNKNOWN_AMOUNT = -9;

    private char winner;
    private double score;


    public GoGameResult(char winner, double score) {
        this.winner = winner;
        this.score = score;
    }

    private GoGameResult(String result) {
        char c = result.charAt(0);

        if (c == '0' || c == 'D' || c == 'J') {
            winner = JIGO;
        }
        else if (c == 'V') {
            winner = VOID;
        }
        else if (c == '?') {
            winner = UNKNOWN_WINNER;
        }
        else {
            if (c == 'B')
                winner = BLACK;
            else if (c == 'W')
                winner = WHITE;
            else
                winner = UNKNOWN_WINNER;

            score = UNKNOWN_AMOUNT;
            if (result.length() > 2) {
                c = result.charAt(2);
                if (c == 'R')
                    score = RESIGN;
                else if (c == 'T')
                    score = TIME;
                else if (c == 'F')
                    score = FORFEIT;
                else if (Character.isDigit(c)) {
                    try {
                        score = SgfParser.parseKomi(result.substring(2), '.');
                    }
                    catch (NumberFormatException ignored) {

                    }
                }
            }
        }
    }


    /**
     * Returns the result in a valid SGF property format (without the RE[] part).
     */
    @Override
    public String toString() {
        if (winner == JIGO)
            return "0";
        else if (winner == VOID)
            return "Void";
        else if (winner == UNKNOWN_WINNER)
            return "?";

        String result;
        if (score > 0)
            result = ((int) score) + "." + ((int) Math.round(score * 10) % 10);
        else if (score == RESIGN)
            result = "R";
        else if (score == TIME)
            result = "T";
        else if (score == FORFEIT)
            result = "F";
        else
            result = "";

        return winner + "+" + result;
    }


    /**
     * Returns the winner represented by this instance. It can be one of the following values (constants) :<br/>
     * JIGO, BLACK, WHITE, VOID, UNKNOWN_WINNER
     */
    public char getWinner() {
        return winner;
    }

    /**
     * Returns the score represented by this instance. It can be one of the following values (constants) :<br/>
     * RESIGN, TIME, FORFEIT, UNKNOWN_AMOUNT, or the result as a number.
     */
    public double getScore() {
        return score;
    }


    /**
     * Tries to parse the given SGF result. Returns null if it failed.
     */
    public static GoGameResult tryParse(String sgfResult) {
        try {
            return new GoGameResult(sgfResult);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
