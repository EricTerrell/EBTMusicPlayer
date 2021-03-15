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

package com.ericbt.musicplayer.music_library;

public class MusicLibraryCounts {
    private long playLists;

    private long albums;

    private long tracks;

    private long duration;

    private long fileSize;

    public long getPlayLists() {
        return playLists;
    }

    public void setPlayLists(long playLists) {
        this.playLists = playLists;
    }

    public long getAlbums() {
        return albums;
    }

    public void setAlbums(long albums) {
        this.albums = albums;
    }

    public long getTracks() {
        return tracks;
    }

    public void setTracks(long tracks) {
        this.tracks = tracks;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
