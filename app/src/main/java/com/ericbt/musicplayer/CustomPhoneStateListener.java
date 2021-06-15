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
import android.telephony.PhoneStateListener;

import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;


public class CustomPhoneStateListener extends PhoneStateListener {
    private final PlaybackController playbackController;

    private final Logger logger;

    public CustomPhoneStateListener(Context context, PlaybackController playbackController) {
        this.playbackController = playbackController;

        this.logger = new Logger(context);
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "onCallStateChanged: state: %d", state));

        super.onCallStateChanged(state, incomingNumber);

        switch (state) {
            case CALL_STATE_RINGING:
            case CALL_STATE_OFFHOOK: {
                if (!playbackController.isProgramaticallyPaused()) {
                    final boolean paused = playbackController.pause();

                    playbackController.setIsProgramaticallyPaused(paused);
                }
            }
            break;

            case CALL_STATE_IDLE: {
                if (playbackController.isProgramaticallyPaused()) {
                    playbackController.play(true);
                }
            }
            break;
        }
    }
}
