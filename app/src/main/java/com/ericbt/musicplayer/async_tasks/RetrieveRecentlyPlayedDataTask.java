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

package com.ericbt.musicplayer.async_tasks;

import android.content.Context;

import com.ericbt.musicplayer.activities.RecentlyPlayedActivity;
import com.ericbt.musicplayer.db.DBUtils;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedData;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedManager;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.Logger;

import java.util.List;

public class RetrieveRecentlyPlayedDataTask implements Runnable
{
    public static final int ROWS = 100;

    private final Context context;

    private final RecentlyPlayedActivity recentlyPlayedActivity;

    private final Logger logger;

    public RetrieveRecentlyPlayedDataTask(Context context, RecentlyPlayedActivity recentlyPlayedActivity, Logger logger) {
        this.context = context;
        this.recentlyPlayedActivity = recentlyPlayedActivity;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            if (DBUtils.databaseExists(context)) {
                final List<RecentlyPlayedData> results = new RecentlyPlayedManager(context, logger).getRecentlyPlayedData(null, ROWS);

                recentlyPlayedActivity.runOnUiThread(() -> {
                    // Refresh list
                    recentlyPlayedActivity.refreshListView(results);
                });
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);
        }
    }
}
