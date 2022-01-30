/*
  EBT Music Player
  (C) Copyright 2022, Eric Bergman-Terrell

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

public class DBIndexer extends DBBuilder {
    private final Logger logger;

    public DBIndexer(ScannerService musicPlayerService, Logger logger) {
        super(musicPlayerService);

        this.logger = logger;
    }

    public void addIndexes() throws Exception {
        logger.log("Adding indexes to database");

        try (SQLiteDatabase db = DBUtils.getDatabase(scannerService, false)) {
            final String[] indexCommands = new String[] {
                    "CREATE INDEX idx_FilePath ON MediaFileMetaData(filePath);",
                    "CREATE INDEX idx_Album ON Album(album);",
                    "CREATE INDEX idx_fileName ON PlayList(fileName);",
                    "CREATE INDEX idx_playListId ON PlayList2MediaFileMetaData(playListId);",
                    "CREATE INDEX idx_albumId ON Album2MediaFileMetaData(albumId);",
                    "CREATE INDEX idx_genre ON MediaFileMetaData(genre);",
                    "CREATE INDEX idx_artist ON MediaFileMetaData(artist);",
                    "CREATE INDEX idx_albumArtist ON MediaFileMetaData(albumArtist);",
                    "CREATE INDEX idx_composer ON MediaFileMetaData(composer);",
                    "CREATE INDEX idx_compilation ON MediaFileMetaData(compilation);",
                    "CREATE INDEX idx_year ON MediaFileMetaData(year);"
            };

            for (String indexCommand : indexCommands) {
                execSQL(db, indexCommand);
            }
        }
    }
}
