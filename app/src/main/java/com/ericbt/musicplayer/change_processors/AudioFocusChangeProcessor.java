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
import android.media.AudioManager;

import com.ericbt.musicplayer.PlaybackController;
import com.ericbt.musicplayer.utils.Logger;

import java.util.Locale;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

public class AudioFocusChangeProcessor {
    private final PlaybackController playbackController;

    private final Logger logger;

    public AudioManager.OnAudioFocusChangeListener getOnAudioFocusChangeListener() {
        return onAudioFocusChangeListener;
    }

    private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    public AudioFocusChangeProcessor(Context context, PlaybackController playbackController) {
        this.playbackController = playbackController;
        this.onAudioFocusChangeListener = createOnAudioFocusChangedListener();

        logger = new Logger(context);
    }

    private AudioManager.OnAudioFocusChangeListener createOnAudioFocusChangedListener() {
        return focusChange -> {
            logger.log(String.format(Locale.US, "onAudioFocusChange focusChange = %d isPaused = %b", focusChange, playbackController.isProgramaticallyPaused()));

            switch(focusChange) {
                case AUDIOFOCUS_LOSS:
                case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                case AUDIOFOCUS_LOSS_TRANSIENT: {
                    if (!playbackController.isProgramaticallyPaused()) {
                        boolean isPaused = playbackController.pause();
                        playbackController.setIsProgramaticallyPaused(isPaused);
                    }
                }
                break;

                case AUDIOFOCUS_GAIN: {
                    if (playbackController.isProgramaticallyPaused()) {
                        playbackController.play(false);
                    }
                }
                break;
            }
        };
    }

}
