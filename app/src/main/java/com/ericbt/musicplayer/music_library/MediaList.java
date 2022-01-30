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

import java.util.ArrayList;
import java.util.List;

public class MediaList<T> {
    public static class PrefixOffset {
        private final String prefix;

        private final int offset;

        public PrefixOffset(String prefix, int offset) {
            this.prefix = prefix;
            this.offset = offset;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getOffset() {
            return offset;
        }
    }

    private List<T> media;

    private final List<PrefixOffset> prefixOffsets = new ArrayList<>();

    public List<T> getMedia() {
        return media;
    }

    public void setMedia(List<T> media) {
        this.media = media;
    }

    public List<PrefixOffset> getPrefixOffsets() {
        return prefixOffsets;
    }

    public void updateOffsets(String prefix) {
        if (prefixOffsets.size() == 0 || (!prefixOffsets.get(prefixOffsets.size() - 1).getPrefix().equals(prefix))) {
            prefixOffsets.add(new PrefixOffset(prefix, media.size()));
        }
    }
}
