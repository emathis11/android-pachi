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


public class GameInfo
{
    public double komi;
    public int handicap;
    public int boardSize;


    /** Provides the name of the black player. */
    public String blackName = "";
    /** Provides the name of the white player. */
    public String whiteName = "";

    /** Provides the rank of the black player. */
    public String blackRank = "";
    /** Provides the rank of the white player. */
    public String whiteRank = "";

    /** Provides the name of the black team, if game was part of a team-match (e.g. China-Japan Supermatch). */
    public String blackTeam;
    /** Provides the name of the white team, if game was part of a team-match (e.g. China-Japan Supermatch). */
    public String whiteTeam;

    /** Provides the name of the person, who made the annotations to the game. */
    public String annotationsBy;

    /** Any copyright information (e.g. for the annotations) should be included here. */
    public String copyrightInfo;

    /** Provides the name of the application used to create this gametree.<br/>
        The name should be unique and must not be changed for different versions of the same program. */
    public String applicationName;

    /** The version number itself may be of any kind, but the format used must ensure that by using an ordinary
        string-compare, one is able to tell if the version is lower or higher than another version number. */
    public String applicationVersion;

    /** The game result (can be null). */
    public GoGameResult result;

    /**
     * Provides the date when the game was played.<br/>
     * It is MANDATORY to use the ISO-standard format for DT.<br/>
     * Note: ISO format implies usage of the Gregorian calendar.<br/><br/>
     *
     * Syntax:<br/>
     * "YYYY-MM-DD" year (4 digits), month (2 digits), day (2 digits)<br/>
     * Do not use other separators such as "/", " ", "," or ".".<br/>
     * Partial dates are allowed:<br/>
     * "YYYY" - game was played in YYYY<br/>
     * "YYYY-MM" - game was played in YYYY, month MM<br/>
     * For games that last more than one day: separate other dates<br/>
     * by a comma (no spaces!); following shortcuts may be used:<br/>
     * "MM-DD" - if preceded by YYYY-MM-DD, YYYY-MM, MM-DD, MM or DD<br/>
     * "MM" - if preceded by YYYY-MM or MM<br/>
     * "DD" - if preceded by YYYY-MM-DD, MM-DD or DD<br/>
     * Shortcuts acquire the last preceding YYYY and MM (if necessary).<br/>
     * Note: interpretation is done from left to right.<br/><br/>
     *
     * Examples:<br/>
     *     1996-05,06 = played in May,June 1996<br/>
     *     1996-05-06,07,08 = played on 6th,7th,8th May 1996<br/>
     *     1996,1997 = played in 1996 and 1997<br/>
     *     1996-12-27,28,1997-01-03,04 = played on 27th,28th of December 1996 and on 3rd,4th January 1997<br/><br/>
     *
     * Note: it's recommended to use shortcuts whenever possible,<br/>
     * e.g. 1997-05-05,06 instead of 1997-05-05,1997-05-06
     */
    public String gameDate;

    /** Provides the name of the event (e.g. tournament). Additional information (e.g. final, playoff, ..)
        should be put in the round information. */
    public String eventName;

    /** Provides a name for the game. The name is used to easily find games within a collection.
        The name should therefore contain some helpful information for identifying the game. */
    public String gameName;

    /** Provides some extra information about the following game. The intend of GC is to provide some
        background information and/or to summarize the game itself. */
    public String gameComment;

    /** Provides some information about the opening played (e.g. san-ren-sei, Chinese fuseki, etc.). */
    public String opening;

    /** Describes the method used for overtime (byo-yomi). Examples: "5 mins Japanese style, 1 move / min",
        "25 moves / 10 min". */
    public String overtimeMethod;

    /** SGF property PL[] to specify the first player to play. */
    public String firstPlayer;

    /** Provides the place where the game was played. */
    public String place;

    /** Provides round-number and type of round. It should be written in the following way: xx (tt),
        where xx is the number of the round and (tt) the type: final, playoff, league, ... */
    public String round;

    /** Provides the used rules for this game. Because there are many different rules, It requires
     mandatory names only for a small set of well known rule sets :<br/>
     "AGA" (rules of the American Go Association)<br/>
     "GOE" (the Ing rules of Goe)<br/>
     "Japanese" (the Nihon-Kiin rule set)<br/>
     "NZ" (New Zealand rules). */
    public String rules;

    /** Provides the name of the source (e.g. book, journal, ...). */
    public String source;

    /** Provides the time limits of the game. The time limit is given in seconds. */
    public Integer timeLimit;

    /** Provides the name of the user (or program), who entered the game. */
    public String scribe;

    /** This can be null, but if it's not it contains the SGF data used to load the game. */
    public String originalSgf;



    public GameInfo()
    {
    }
}
