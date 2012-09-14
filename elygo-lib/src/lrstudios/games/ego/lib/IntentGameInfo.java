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

import android.os.Parcel;
import android.os.Parcelable;


/**
 * This parcelable class is passed to a BoardActivity to start a new game
 * with the specified parameters.
 */
public final class IntentGameInfo implements Parcelable
{
    public int boardSize;
    public int handicap;
    public byte color;
    public double komi;
    public int botLevel;
    public String rules = "";


    public IntentGameInfo()
    {
    }

    public IntentGameInfo(Parcel in)
    {
        boardSize = in.readInt();
        handicap = in.readInt();
        color = in.readByte();
        komi = in.readDouble();
        botLevel = in.readInt();
        rules = in.readString();
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeInt(boardSize);
        parcel.writeInt(handicap);
        parcel.writeByte(color);
        parcel.writeDouble(komi);
        parcel.writeInt(botLevel);
        parcel.writeString(rules);
    }

    public static final Parcelable.Creator<IntentGameInfo> CREATOR = new Parcelable.Creator<IntentGameInfo>()
    {
        @Override
        public IntentGameInfo createFromParcel(Parcel source)
        {
            return new IntentGameInfo(source);
        }

        @Override
        public IntentGameInfo[] newArray(int size)
        {
            return new IntentGameInfo[size];
        }
    };
}
