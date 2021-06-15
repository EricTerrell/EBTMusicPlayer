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

package com.ericbt.musicplayer.change_processors;

import android.content.Context;
import android.content.Intent;

import com.ericbt.musicplayer.PlaybackController;
import com.ericbt.musicplayer.utils.Logger;

public abstract class ChangeProcessor {
    private final PlaybackController playbackController;

    private final int connectedStateValue, disconnectedStateValue;

    public ChangeProcessor(PlaybackController playbackController, int connectedStateValue, int disconnectedStateValue) {
        this.playbackController = playbackController;
        this.connectedStateValue = connectedStateValue;
        this.disconnectedStateValue = disconnectedStateValue;
    }

    protected abstract int getCurrentState(Intent intent);

    protected abstract boolean playOnConnect(Context context);

    protected abstract boolean pauseOnDisconnect(Context context);

    public void processChange(Intent intent, Context context) {
        final Logger logger = new Logger(context);

        logger.log(String.format("%s.processChange", this.getClass().getName()));

        final int currentState = getCurrentState(intent);

        if (currentState == connectedStateValue) {
            logger.log("connected");

            if (playOnConnect(context) && !playbackController.isPlaybackFinished()) {
                logger.log("ChangeProcessor.processChange: playing");

                playbackController.play(true);
            }
        } else if (currentState == disconnectedStateValue) {
            logger.log("disconnected");

            if (pauseOnDisconnect(context)) {
                logger.log("ChangeProcessor.processChange: pausing");

                playbackController.pause();

                logger.log("ChangeProcessor.processChange: paused");
            }
        }
    }
}
