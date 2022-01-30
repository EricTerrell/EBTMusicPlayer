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

package com.ericbt.musicplayer.utils;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.ericbt.musicplayer.change_processors.AudioFocusChangeProcessor;

public class AudioUtils {
    public static int requestAudioFocus(AudioManager audioManager, AudioFocusChangeProcessor audioFocusChangeProcessor, Logger logger) {
        int audioFocusStatus;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            audioFocusStatus =
                    audioManager.requestAudioFocus(audioFocusChangeProcessor.getOnAudioFocusChangeListener(),
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);
        } else {
            final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            final AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeProcessor.getOnAudioFocusChangeListener())
                    .build();

            audioFocusStatus = audioManager.requestAudioFocus(audioFocusRequest);
        }

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "PlayActivity.play audioFocusStatus = %d", audioFocusStatus));

        return audioFocusStatus;
    }
}
