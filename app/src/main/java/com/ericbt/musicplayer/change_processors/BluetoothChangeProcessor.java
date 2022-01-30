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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.ericbt.musicplayer.PlaybackController;
import com.ericbt.musicplayer.Preferences;

public class BluetoothChangeProcessor extends ChangeProcessor {
    public BluetoothChangeProcessor(PlaybackController playbackController) {
        super(playbackController, BluetoothAdapter.STATE_CONNECTED, BluetoothAdapter.STATE_DISCONNECTED);
    }

    @Override
    protected int getCurrentState(Intent intent) {
        return intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
    }

    @Override
    protected boolean playOnConnect(Context context) {
        return Preferences.bluetoothPlayOnConnect(context);
    }

    @Override
    protected boolean pauseOnDisconnect(Context context) {
        return Preferences.bluetoothPauseOnDisconnect(context);
    }
}
