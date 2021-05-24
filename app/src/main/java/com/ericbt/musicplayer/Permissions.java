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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions {
    public static final int PERMISSION_READ_EXTERNAL_STORAGE = 1000;
    public static final int PERMISSION_BLUETOOTH             = 1001;
    public static final int PERMISSION_PHONE_STATE           = 1002;

    private static void requestPermission(String permission, int callbackCode, Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, new String[] { permission }, callbackCode);
                // PERMISSION_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private static void requestReadExternalStoragePermission(Activity activity) {
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE, activity);
    }

    private static void requestBluetoothPermission(Activity activity) {
        requestPermission(Manifest.permission.BLUETOOTH, PERMISSION_BLUETOOTH, activity);
    }

    private static void requestPhoneStatePermission(Activity activity) {
        requestPermission(Manifest.permission.READ_PHONE_STATE, PERMISSION_PHONE_STATE, activity);
    }

    public static void requestPermissions(Activity activity) {
        requestReadExternalStoragePermission(activity);
        requestBluetoothPermission(activity);
        requestPhoneStatePermission(activity);
    }
}
