/*
  EBT Music Player
  (C) Copyright 2021, Eric Bergman-Terrell

  This file is part of EBT Music Player.

    EBT Music Player is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBT Music Player is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBT Music Player.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.musicplayer.db;

import android.database.sqlite.SQLiteDatabase;

import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;

import java.util.Date;

public class DBUpgrader {
    private Logger logger;
    private SQLiteDatabase db;

    public DBUpgrader(Logger logger, SQLiteDatabase db) {
        this.logger = logger;
        this.db = db;
    }

    public void upgrade() {
        logger.log("Upgrading database");

        String sqlCommand =
                "CREATE TABLE IF NOT EXISTS RecentlyPlayed(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "action TEXT NOT NULL, " +
                        "ids TEXT NOT NULL, " +
                        "album TEXT NOT NULL, " +
                        "currentTrackName TEXT NOT NULL, " +
                        "trackOrdinalPosition INTEGER NOT NULL, " +
                        "positionInTrack INTEGER NOT NULL)";

        final long startTime = new Date().getTime();

        db.execSQL(sqlCommand);

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "Upgraded database in %d ms", new Date().getTime() - startTime));
    }
}
