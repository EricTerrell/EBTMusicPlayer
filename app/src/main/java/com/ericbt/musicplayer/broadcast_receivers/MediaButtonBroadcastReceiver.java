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

package com.ericbt.musicplayer.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.ericbt.musicplayer.broadcast_receivers.CustomBroadcastReceiver;
import com.ericbt.musicplayer.utils.Logger;

import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;

public class MediaButtonBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final Logger logger = new Logger(context);

        logger.log(String.format("MediaButtonBroadcastReceiver: onReceive: %s", intent.getAction()));

        final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch(event.getKeyCode()) {
                case KEYCODE_MEDIA_PLAY_PAUSE: {
                    context.sendBroadcast(new Intent(CustomBroadcastReceiver.PLAY_PAUSE));
                }
                break;

                case KEYCODE_MEDIA_NEXT: {
                    context.sendBroadcast(new Intent(CustomBroadcastReceiver.NEXT));
                }
                break;

                case KEYCODE_MEDIA_PREVIOUS: {
                    context.sendBroadcast(new Intent(CustomBroadcastReceiver.PREVIOUS));
                }
                break;
            }
        }
    }
}
