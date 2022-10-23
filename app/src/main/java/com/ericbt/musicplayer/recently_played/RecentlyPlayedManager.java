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

package com.ericbt.musicplayer.recently_played;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ericbt.musicplayer.db.DBUtils;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecentlyPlayedManager {
    private final String TABLE = "RecentlyPlayed";

    private final Context context;
    private final Logger logger;

    public RecentlyPlayedManager(Context context, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    public List<RecentlyPlayedData> getRecentlyPlayedData(String action, int threshold) {
        final List<RecentlyPlayedData> results = new ArrayList<>(threshold);

        try (final SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String whereClause = action != null ? "WHERE action = ? " : "";

            final String query =
                    "SELECT id, album, currentTrackName, ids, action, trackOrdinalPosition, positionInTrack " +
                            "FROM RecentlyPlayed " +
                            whereClause +
                            "ORDER BY id DESC " +
                            "LIMIT ?;";

            final String[] selectionArgs = action != null ? new String[] { action, String.valueOf(threshold) } : new String[] { String.valueOf(threshold) };

            final long startTime = new Date().getTime();

            try (final Cursor cursor = db.rawQuery(query, selectionArgs)) {
                logger.log(String.format("retrieve query: %s", query));

                while (cursor.moveToNext()) {
                    RecentlyPlayedData result = new RecentlyPlayedData();
                    result.setId(cursor.getInt(0));
                    result.setAlbum(cursor.getString(1));
                    result.setCurrentTrackName(cursor.getString(2));
                    result.setIds(cursor.getString(3));
                    result.setAction(cursor.getString(4));
                    result.setTrackOrdinalPosition(cursor.getInt(5));
                    result.setPositionInTrack(cursor.getInt(6));

                    logger.log(String.format(LocaleUtils.getDefaultLocale(), "Retrieved id: %d", result.getId()));

                    results.add(result);
                }
            }

            logger.log(String.format(LocaleUtils.getDefaultLocale(), "Retrieved %d recently played data rows in %d ms", results.size(), new Date().getTime() - startTime));
        }

        return results;
    }

    public void deleteOldRows(int minKeepRowId) {
        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String whereClause = "id < ?";
            final String[] whereArgs = new String[] { String.valueOf(minKeepRowId) };

            final long startTime = new Date().getTime();

            final int deletedRows = db.delete(TABLE, whereClause, whereArgs);

            logger.log(String.format(LocaleUtils.getDefaultLocale(), "Deleted %d recently played data rows in %d ms", deletedRows, new Date().getTime() - startTime));
        }
    }

    public void updateRecentlyPlayedData(String action, List<Long> ids, String album, String trackName, int currentTrack, int positionInTrack) {
        try {
            final List<RecentlyPlayedData> recentlyPlayedData = getRecentlyPlayedData(action, 1);

            final ContentValues contentValues = new ContentValues();
            contentValues.put("action", action);
            contentValues.put("ids", TextUtils.join(",", ids));
            contentValues.put("album", album);
            contentValues.put("currentTrackName", trackName);
            contentValues.put("trackOrdinalPosition", currentTrack);
            contentValues.put("positionInTrack", positionInTrack);

            final long startTime = new Date().getTime();

            try (final SQLiteDatabase db = DBUtils.getDatabase(context)) {
                if (recentlyPlayedData.size() == 1) {
                    if (album.equals(recentlyPlayedData.get(0).getAlbum())) {
                        final String[] whereValues = new String[]{String.valueOf(recentlyPlayedData.get(0).getId())};

                        final int rowCount = db.update(TABLE, contentValues, "id = ?", whereValues);

                        logger.log(String.format(LocaleUtils.getDefaultLocale(), "Updated %d rows", rowCount));
                    } else {
                        final long rowId = db.insert(TABLE, null, contentValues);

                        logger.log(String.format(LocaleUtils.getDefaultLocale(), "Inserted rowId %d", rowId));
                    }
                } else {
                    final long rowId = db.insert(TABLE, null, contentValues);

                    logger.log(String.format(LocaleUtils.getDefaultLocale(), "Inserted rowId %d", rowId));
                }
            } catch (Exception ex) {
                ExceptionLogger.logException(ex, context);
            }

            logger.log(String.format(LocaleUtils.getDefaultLocale(), "Updated recently played data in %d ms", new Date().getTime() - startTime));
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);
        }
    }

}
