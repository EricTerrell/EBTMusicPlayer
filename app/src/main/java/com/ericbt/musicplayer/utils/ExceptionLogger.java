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

import android.content.Context;
import android.util.Log;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.StringLiterals;

import java.util.Date;

public class ExceptionLogger {
    public static void logException(Throwable tr, Context context) {
        final String logMessage1 = String.format(LocaleUtils.getDefaultLocale(), "CustomUncaughtExceptionHandler.uncaughtException: Thread %d Message %s", Thread.currentThread().getId(), tr.getMessage());

        final boolean isExceptionLoggingActive = Preferences.isExceptionLoggingActive(context);

        if (isExceptionLoggingActive) {
            Log.e(StringLiterals.LOG_TAG, logMessage1);
        }

        final String logMessage2 = String.format(LocaleUtils.getDefaultLocale(), "%s\r\n\r\nThread: %d\r\n\r\nMessage:\r\n\r\n%s\r\n\r\nStack Trace:\r\n\r\n%s",
                new Date(),
                Thread.currentThread().getId(),
                tr.getMessage(),
                Log.getStackTraceString(tr));

        if (isExceptionLoggingActive) {
            Log.e(StringLiterals.LOG_TAG, logMessage2);
            tr.printStackTrace();
        }

        final Logger logger = new Logger(context, isExceptionLoggingActive);

        logger.log(logMessage1);
        logger.log(logMessage2, tr);
    }
}
