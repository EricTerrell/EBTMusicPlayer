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

package com.ericbt.musicplayer.db;

public class PlayListItem {
    private final long playListId;
    private final int sequenceNumber;

    public PlayListItem(long playListId, int sequenceNumber) {
        this.playListId = playListId;
        this.sequenceNumber = sequenceNumber;
    }

    public long getPlayListId() {
        return playListId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
