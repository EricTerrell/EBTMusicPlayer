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

import com.ericbt.musicplayer.activities.FilterActivity;
import com.ericbt.musicplayer.music_library.MusicLibrary;
import com.ericbt.musicplayer.utils.ExceptionLogger;

import java.util.List;

public class RetrieveAllFilterCategoryValues implements Runnable
{
    private final FilterActivity filterActivity;

    private final String filterCategory;

    public RetrieveAllFilterCategoryValues(FilterActivity filterActivity, String filterCategory) {
        this.filterActivity = filterActivity;
        this.filterCategory = filterCategory;
    }

    @Override
    public void run() {
        try {
            final List<String> filterCategoryValues = MusicLibrary.retrieveAllFilterCategoryValues(filterActivity, filterCategory);

            filterActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Refresh list
                    filterActivity.refresh(filterCategoryValues, filterCategory);
                }
            });
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, filterActivity);
        }
    }
}
