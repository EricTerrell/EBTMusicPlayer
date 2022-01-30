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

import android.content.Context;
import android.util.Log;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.StringLiterals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Logger {
    private final Context context;

    private final String logFilePath;

    private final boolean isLoggingActive;

    private final static Set<String> logFilesToPublish = new HashSet<>();

    public Logger(Context context, boolean isLoggingActive) {
        this.context = context;

        this.logFilePath = getLogFilePath(context).getAbsolutePath();

        this.isLoggingActive = isLoggingActive;
    }

    public Logger(Context context) {
        this(context, Preferences.isFullLoggingActive(context));
    }

    public synchronized void log(String message, Throwable ex) {
        if (isLoggingActive) {
            if (ex != null) {
                Log.i(StringLiterals.LOG_TAG, message, ex);
            } else {
                Log.i(StringLiterals.LOG_TAG, message);
            }

            if (isLoggingActive) {
                try (PrintWriter printWriter = createPrintWriter()) {
                    final String exText = ex != null ? String.format("%s\r\n", ex) : StringLiterals.EMPTY_STRING;

                    printWriter.print(String.format("%s: %s\r\n%s", new Date(), message, exText));
                } catch (Exception exWriteToLog) {
                    Log.e(StringLiterals.LOG_TAG, String.format("Cannot write to log file %s", logFilePath), exWriteToLog);
                }

                if (!logFilesToPublish.contains(logFilePath)) {
                    FileUtils.scanFile(context, logFilePath);

                    logFilesToPublish.add(logFilePath);
                }
            }
        }
    }

    public synchronized void log(String message) {
        log(message, null);
    }

    private PrintWriter createPrintWriter() throws IOException {
        return new PrintWriter(new FileWriter(logFilePath, true));
    }

    public static File getLogFolder(Context context) {
        return context.getExternalFilesDir(null);
    }

    public static File getLogFilePath(Context context) {
        return new File(String.format("%s/%s", getLogFolder(context), StringLiterals.LOG_FILE_NAME));
    }
}
