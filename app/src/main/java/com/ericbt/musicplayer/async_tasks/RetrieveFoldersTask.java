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

package com.ericbt.musicplayer.async_tasks;

import android.content.Context;

import com.ericbt.musicplayer.activities.FolderPickerActivity;
import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.Logger;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RetrieveFoldersTask implements Runnable
{
    private final FolderPickerActivity folderPickerActivity;

    private final File folder;

    private final Logger logger;

    public RetrieveFoldersTask(FolderPickerActivity folderPickerActivity, File folder) {
        this.folderPickerActivity = folderPickerActivity;
        this.folder = folder;

        this.logger = new Logger(folderPickerActivity);
    }

    private boolean canListFiles(File file, Context context) {
        boolean result = false;

        try {
            result = file.listFiles() != null;
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);
        }

        return result;
    }

    private static class StringComparitor implements Comparator<String> {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(String string1, String string2) {
            return collator.compare(string1, string2);
        }
    }

    @Override
    public void run() {
        try {
            final List<String> subFolders = new ArrayList<>();

            if (folder.getAbsolutePath().equals(StringLiterals.ROOT_PATH)) {
                for (String filePath : Preferences.getDefaultScanFolders(folderPickerActivity)) {
                    final File file = new File(filePath);

                    if (file.isDirectory() && canListFiles(file, folderPickerActivity)) {
                        subFolders.add(file.getAbsolutePath().substring(1));

                        logger.log(String.format("RetrieveFoldersTask: adding %s", file.getAbsolutePath()));
                    }
                }
            } else {
                for (File file : folder.listFiles()) {
                    if (file.isDirectory() && canListFiles(file, folderPickerActivity)) {
                        subFolders.add(file.getName());

                        logger.log(String.format("RetrieveFoldersTask: adding %s", file.getAbsolutePath()));
                    }
                }
            }

            Collections.sort(subFolders, new StringComparitor());

            folderPickerActivity.runOnUiThread(() -> {
                // Refresh list
                folderPickerActivity.update(folder, subFolders);
            });
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, folderPickerActivity);
        }
    }
}
