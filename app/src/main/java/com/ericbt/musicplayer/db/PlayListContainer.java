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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayListContainer {
    private final static int CAPACITY = 1000;

    private final Map<String, List<PlayListItem>> playlistItems = new HashMap<>(CAPACITY);

    public void clear() {
        playlistItems.clear();
    }

    public void insertPlayListItem(String key, PlayListItem playListItem) {
        List<PlayListItem> items = playlistItems.get(key);

        if (items == null) {
            items = new ArrayList<>();

            playlistItems.put(key, items);
        }

        items.add(playListItem);
    }

    public List<PlayListItem> getPlayListItems(String key) {
        return playlistItems.get(key);
    }
}
