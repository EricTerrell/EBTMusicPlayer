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

package com.ericbt.musicplayer.async_tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ericbt.musicplayer.db.DBUpgrader;
import com.ericbt.musicplayer.db.DBUtils;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedData;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedManager;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.Logger;

import java.util.List;

import static com.ericbt.musicplayer.async_tasks.RetrieveRecentlyPlayedDataTask.ROWS;

public class DatabaseMaintenanceTask implements Runnable
{
    private final Context context;

    private final Logger logger;

    public DatabaseMaintenanceTask(Context context, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
                new DBUpgrader(logger, db).upgrade();

                final RecentlyPlayedManager recentlyPlayedManager = new RecentlyPlayedManager(context, logger);

                final List<RecentlyPlayedData> recentlyPlayedData = recentlyPlayedManager.getRecentlyPlayedData(null, ROWS + 1);

                if (recentlyPlayedData.size() > ROWS) {
                    recentlyPlayedManager.deleteOldRows(recentlyPlayedData.get(ROWS - 1).getId());
                }
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);
        }
    }
}
