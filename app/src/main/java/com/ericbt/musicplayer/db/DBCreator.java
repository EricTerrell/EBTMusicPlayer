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

import com.ericbt.musicplayer.services.scanner_service.ScannerService;
import com.ericbt.musicplayer.utils.Logger;

import java.util.Locale;

public class DBCreator extends DBBuilder {
    private Logger logger;

    public DBCreator(ScannerService scannerService, Logger logger) {
        super(scannerService);

        this.logger = logger;
    }

    public void create() throws Exception {
        logger.log("Creating database");

        try (SQLiteDatabase db = DBUtils.createDatabase(scannerService)) {
            final Locale locale = Locale.getDefault();

            String sqlCommand = String.format("INSERT INTO \"android_metadata\" VALUES ('%s_%s')", locale.getLanguage(), locale.getCountry());

            execSQL(db, sqlCommand);
            
            sqlCommand =
                    "CREATE TABLE MediaFileMetaData(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "filePath TEXT NOT NULL," +
                        "timeStamp INTEGER NOT NULL," +
                        "size INTEGER NOT NULL," +
                        "title TEXT NULL," +
                        "album TEXT NULL," +
                        "albumArtist TEXT NULL," +
                        "artist TEXT NULL," +
                        "bitRate TEXT NULL," +
                        "trackNumber TEXT NULL," +
                        "date TEXT NULL," +
                        "genre TEXT NULL," +
                        "cdTrackNumber TEXT NULL," +
                        "discNumber TEXT NULL," +
                        "duration TEXT NULL," +
                        "year TEXT NULL," +
                        "compilation TEXT NULL," +
                        "composer TEXT NULL)";

            execSQL(db, sqlCommand);

            sqlCommand =
                    "CREATE TABLE Album(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "album TEXT NOT NULL)";

            execSQL(db, sqlCommand);

            sqlCommand =
                    "CREATE TABLE PlayList(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "fileName TEXT NOT NULL," +
                            "timeStamp INTEGER NOT NULL," +
                            "size INTEGER NOT NULL)";

            execSQL(db, sqlCommand);

            sqlCommand =
                    "CREATE TABLE Album2MediaFileMetaData(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "albumId INTEGER," +
                            "mediaFileMetaDataId INTEGER," +
                            "FOREIGN KEY(albumId) REFERENCES Album(id)" +
                            "FOREIGN KEY(mediaFileMetaDataId) REFERENCES MediaFileMetaData(id))";

            execSQL(db, sqlCommand);
            sqlCommand =
                    "CREATE TABLE PlayList2MediaFileMetaData(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "playListId INTEGER," +
                            "sequenceNumber INTEGER," +
                            "mediaFileMetaDataId INTEGER," +
                            "FOREIGN KEY(playListId) REFERENCES PlayList(id)," +
                            "FOREIGN KEY(mediaFileMetaDataId) REFERENCES MediaFileMetaData(id))";

            execSQL(db, sqlCommand);
        }
    }
}
