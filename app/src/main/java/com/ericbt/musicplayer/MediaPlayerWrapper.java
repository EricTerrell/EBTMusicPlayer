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

package com.ericbt.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;

import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;

public class MediaPlayerWrapper {
    private MediaPlayer mediaPlayer, nextMediaPlayer;
    
    private final Logger logger;

    public MediaPlayerWrapper(Context context, MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        
        this.logger = new Logger(context);
    }

    public void setNextMediaPlayer(MediaPlayer nextMediaPlayer) {
        this.nextMediaPlayer = nextMediaPlayer;

        mediaPlayer.setNextMediaPlayer(nextMediaPlayer);
    }

    public MediaPlayer getNextMediaPlayer() {
        return nextMediaPlayer;
    }

    public void release() {
        if (mediaPlayer != null) {
            logger.log(String.format(LocaleUtils.getDefaultLocale(), "Releasing MediaPlayer duration: %d", mediaPlayer.getDuration()));

            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (nextMediaPlayer != null) {
            logger.log(String.format(LocaleUtils.getDefaultLocale(), "Releasing MediaPlayer duration: %d", nextMediaPlayer.getDuration()));

            nextMediaPlayer.release();
            nextMediaPlayer = null;
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public void start() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
}
