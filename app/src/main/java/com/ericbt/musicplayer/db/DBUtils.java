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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.FileUtils;
import com.ericbt.musicplayer.utils.Logger;

import java.io.File;
import java.io.IOException;

import static com.ericbt.musicplayer.StringLiterals.DB_NAME;

public class DBUtils {
    public static SQLiteDatabase createDatabase(Context context) {
        final String dbPath = context.getDatabasePath(StringLiterals.NEW_DB_NAME).getPath();

        final File parentFolder = new File(dbPath).getParentFile();

        if (!parentFolder.exists()) {
            parentFolder.mkdir();
        }

        final File dbFile = new File(dbPath);

        if (dbFile.exists()) {
            new File(dbPath).delete();
        }

        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    public static SQLiteDatabase getDatabase(Context context, boolean existingDatabase) {
        final String dbName = existingDatabase ? DB_NAME : StringLiterals.NEW_DB_NAME;
        final String dbPath = context.getDatabasePath(dbName).getPath();

        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        return getDatabase(context, true);
    }

    public static void updateExistingDatabase(Context context, Logger logger) {
        logger.log("DBUtils.updateExistingDatabase");

        final String dbPath = context.getDatabasePath(DB_NAME).getPath();

        final File dbFile = new File(dbPath);

        if (dbFile.exists()) {
            dbFile.delete();
        }

        final File newDbFile = new File(context.getDatabasePath(StringLiterals.NEW_DB_NAME).getPath());

        boolean renamed = newDbFile.renameTo(dbFile);

        logger.log(String.format("updateExistingDatabase renamed %s to %s result: %b", newDbFile.getAbsolutePath(), dbFile.getAbsolutePath(), renamed));

        if (Preferences.getCopyDatabase(context)) {
            final String copyFilePath = String.format("%s/%s", context.getExternalFilesDir(null), "musicplayer.db.copy");

            logger.log(String.format("Copying %s to %s", dbFile.getAbsolutePath(), copyFilePath));

            try {
                FileUtils.copyFile(dbFile.getAbsolutePath(), copyFilePath);

                FileUtils.scanFile(context, copyFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean databaseExists(Context context) {
        return context.getDatabasePath(DB_NAME).exists();
    }

    public static void deleteNewDb(Context context) {
        try {
            context.getDatabasePath(StringLiterals.NEW_DB_NAME).delete();
        } catch (Throwable ex) {
            ExceptionLogger.logException(ex, context);
        }
    }

    public static long getDatabaseTimeStamp(Context context) {
        final String dbPath = context.getDatabasePath(DB_NAME).getPath();

        final File dbFile = new File(dbPath);

        return dbFile.exists() ? dbFile.lastModified() : 0;
    }

    public static String escape(String value) {
        return value.replaceAll("'", "''");
    }
}
