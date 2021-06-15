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

import com.ericbt.musicplayer.activities.SearchActivity;
import com.ericbt.musicplayer.music_library.MusicLibrary;
import com.ericbt.musicplayer.music_library.SearchHit;
import com.ericbt.musicplayer.utils.ExceptionLogger;

import java.util.List;

public class SearchTask implements Runnable
{
    private final Context context;

    private final SearchActivity searchActivity;

    private final String searchText;

    public SearchTask(Context context, SearchActivity searchActivity, String searchText) {
        this.context = context;
        this.searchActivity = searchActivity;
        this.searchText = searchText;
    }

    @Override
    public void run() {
        try {
            final List<SearchHit> searchHits = MusicLibrary.retrieveSearchHits(context, searchText);

            searchActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Refresh list
                    searchActivity.enable(true);
                    searchActivity.refreshSearchHits(searchHits);
                }
            });
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);
        }
    }
}
