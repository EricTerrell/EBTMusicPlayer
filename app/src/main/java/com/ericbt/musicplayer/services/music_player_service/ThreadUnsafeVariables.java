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

package com.ericbt.musicplayer.services.music_player_service;

import com.ericbt.musicplayer.MediaPlayerWrapper;
import com.ericbt.musicplayer.services.music_player_service.Position;

class ThreadUnsafeVariables {
    private Position position;

    private MediaPlayerWrapper mediaPlayerWrapper;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public MediaPlayerWrapper getMediaPlayerWrapper() {
        return mediaPlayerWrapper;
    }

    public void setMediaPlayerWrapper(MediaPlayerWrapper mediaPlayerWrapper) {
        this.mediaPlayerWrapper = mediaPlayerWrapper;
    }
}
