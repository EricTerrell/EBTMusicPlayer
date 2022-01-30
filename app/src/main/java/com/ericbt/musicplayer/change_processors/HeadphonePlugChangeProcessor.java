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

package com.ericbt.musicplayer.change_processors;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.ericbt.musicplayer.PlaybackController;
import com.ericbt.musicplayer.Preferences;

public class HeadphonePlugChangeProcessor extends ChangeProcessor {
    private final Context context;

    public HeadphonePlugChangeProcessor(PlaybackController playbackController, Context context) {
        super(playbackController, 1, 0);

        this.context = context;
    }

    @Override
    protected int getCurrentState(Intent intent) {
        return intent.getIntExtra("state", -1);
    }

    @Override
    protected boolean playOnConnect(Context context) {
        final boolean playOnConnect = Preferences.headphonePlayOnConnect(context);

        if (playOnConnect) {
            reduceVolume();
        }

        return playOnConnect;
    }

    @Override
    protected boolean pauseOnDisconnect(Context context) {
        return Preferences.headphonePauseOnDisconnect(context);
    }

    private void reduceVolume() {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final float percent = 0.25f;
        final int reducedVolume = (int) (maxVolume * percent);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, reducedVolume, 0);
    }

}
