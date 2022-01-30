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

import com.ericbt.musicplayer.activities.MainActivity;
import com.ericbt.musicplayer.db.DBUtils;
import com.ericbt.musicplayer.music_library.Album;
import com.ericbt.musicplayer.music_library.MediaList;
import com.ericbt.musicplayer.music_library.MusicLibrary;
import com.ericbt.musicplayer.utils.ExceptionLogger;

public class RetrieveAllAlbumsTask implements Runnable
{
    private final Context context;

    private final MainActivity mainActivity;

    public RetrieveAllAlbumsTask(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        try {
            if (DBUtils.databaseExists(context)) {
                final MediaList<Album> mediaList = MusicLibrary.retrieveAlbums(context, false);

                mainActivity.runOnUiThread(() -> {
                    // Refresh list
                    mainActivity.enable(true);
                    mainActivity.refreshAlbums(mediaList);
                });
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);
        }
    }
}
