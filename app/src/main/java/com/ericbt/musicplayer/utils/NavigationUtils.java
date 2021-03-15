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

package com.ericbt.musicplayer.utils;

import android.app.Activity;
import android.content.Intent;

public class NavigationUtils {
    /**
     * Go to the specified activity, and clear any other activities off of the back stack.
     *
     * Why this is necessary:
     *
     * Music is playing, and PlayActivity is destroyed. User navigates to PlayActivity via the
     * notification. By default, when the user clicks "back", the user will see another instance
     * of the PlayActivity, and will have to click "back" a second time.
     *
     * @param currentActivity current activity
     * @param goBackToActivityClass class of activity to go back to
     */
    public static void goBackTo(Activity currentActivity, Class goBackToActivityClass) {
        final Intent intent = new Intent(currentActivity, goBackToActivityClass)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        currentActivity.startActivity(intent);

        currentActivity.finish();
    }
}
