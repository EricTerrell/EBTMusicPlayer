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

import com.ericbt.musicplayer.utils.Logger;

public class CustomBroadcastReceiver extends BroadcastReceiver {
    public static final String SCAN_PROGRESS_MESSAGE = "SCAN_PROGRESS_MESSAGE";
    public static final String MESSAGE = "MESSAGE";
    public static final String PROGRESS_PERCENT = "PROGRESS_PERCENT";
    public static final String SCAN_COMPLETE = "SCAN_COMPLETE";
    public static final String SCAN_CANCELLED = "SCAN_CANCELLED";
    public static final String CURRENT_TRACK = "CURRENT_TRACK";
    public static final String CURRENT_POSITION = "CURRENT_POSITION";
    public static final String TRACK_INDEX = "TRACK_INDEX";
    public static final String LAST_TRACK_PLAYED = "LAST_TRACK_PLAYED";
    public static final String PAUSE = "PAUSE";
    public static final String PLAY_PAUSE = "PLAY_PAUSE";
    public static final String NEXT = "NEXT";
    public static final String PREVIOUS = "PREVIOUS";
    public static final String TICK = "TICK";

    public static final int ABORTED_RESULT_CODE = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        final Logger logger = new Logger(context);

        logger.log(String.format("CustomBroadcastReceiver.onReceive: %s", intent.getAction()));
    }
}
