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

package com.ericbt.musicplayer.music_library;

import java.text.Collator;
import java.util.Comparator;

public class SearchHit {
    public enum SearchHitType { PLAYLIST, ALBUM, TRACK }

    private final long id;

    private final SearchHitType type;

    private final String text;

    private final Track track;

    public SearchHit(SearchHitType type, String text, long id, Track track) {

        this.type = type;
        this.text = text;
        this.id = id;
        this.track = track;
    }

    public SearchHit(SearchHitType type, String text, long id) {
        this(type, text, id, null);
    }

    public SearchHitType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public long getId() {
        return id;
    }

    public Track getTrack() {
        return track;
    }

    public static class SearchHitComparitor implements Comparator<SearchHit> {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(SearchHit searchHit1, SearchHit searchHit2) {
            if (searchHit1.getType() != searchHit2.getType()) {
                return searchHit1.getType().compareTo(searchHit2.getType());
            } else {
                return collator.compare(searchHit1.getText(), searchHit2.getText());
            }
        }
    }

}
