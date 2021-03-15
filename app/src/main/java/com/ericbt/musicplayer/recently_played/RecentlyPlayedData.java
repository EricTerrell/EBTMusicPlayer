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

package com.ericbt.musicplayer.recently_played;

public class RecentlyPlayedData {
    private int id;

    private String action;

    private String ids;

    private String album;

    private String currentTrackName;

    private int trackOrdinalPosition;

    private int positionInTrack;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCurrentTrackName() {
        return currentTrackName;
    }

    public void setCurrentTrackName(String currentTrackName) {
        this.currentTrackName = currentTrackName;
    }

    public int getTrackOrdinalPosition() {
        return trackOrdinalPosition;
    }

    public void setTrackOrdinalPosition(int trackOrdinalPosition) {
        this.trackOrdinalPosition = trackOrdinalPosition;
    }

    public int getPositionInTrack() {
        return positionInTrack;
    }

    public void setPositionInTrack(int positionInTrack) {
        this.positionInTrack = positionInTrack;
    }
}
