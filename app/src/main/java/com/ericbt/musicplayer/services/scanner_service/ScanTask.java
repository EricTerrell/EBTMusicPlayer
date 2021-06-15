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

package com.ericbt.musicplayer.services.scanner_service;

import android.database.sqlite.SQLiteDatabase;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.activities.ScanActivity;
import com.ericbt.musicplayer.db.DBCreator;
import com.ericbt.musicplayer.db.DBIndexer;
import com.ericbt.musicplayer.db.DBPopulator;
import com.ericbt.musicplayer.db.DBUpgrader;
import com.ericbt.musicplayer.db.DBUtils;
import com.ericbt.musicplayer.exceptions.ScanCancelledException;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.Logger;

public class ScanTask implements Runnable
{
    private final ScannerService scannerService;

    private final ScanActivity scanActivity;

    public ScanTask(ScannerService scannerService, ScanActivity scanActivity) {
        this.scannerService = scannerService;
        this.scanActivity = scanActivity;
    }

    @Override
    public void run() {
        final Logger logger = new Logger(scannerService);

        try  {
            scannerService.setIsScanning(true);

            scannerService.sendScanProgressMessage(scanActivity.getString(R.string.creating_database));
            new DBCreator(scannerService, logger).create();

            scannerService.sendScanProgressMessage(scanActivity.getString(R.string.creating_database));

            try (SQLiteDatabase db = DBUtils.getDatabase(scannerService.getApplicationContext(), false)) {
                new DBUpgrader(logger, db).upgrade();
            }

            scannerService.sendScanProgressMessage(scanActivity.getString(R.string.adding_media_metadata));
            new DBPopulator(scannerService, logger).scan();

            scannerService.sendScanProgressMessage(scanActivity.getString(R.string.indexing_database));
            new DBIndexer(scannerService, logger).addIndexes();

            // Replace old database with newly-created database.
            DBUtils.updateExistingDatabase(scannerService, logger);

            scannerService.sendScanProgressMessage(scanActivity.getString(R.string.database_created));

            Preferences.putFirstVisibleItem(scannerService, 0);

            scannerService.sendScanCompleteMessage();
        } catch (ScanCancelledException ex) {
            DBUtils.deleteNewDb(scanActivity);

            scannerService.sendScanCancelledMessage();
        }
        catch (Throwable throwable) {
            DBUtils.deleteNewDb(scanActivity);

            throwable.printStackTrace();
            ExceptionLogger.logException(throwable, scanActivity);
        }
        finally {
            scannerService.setIsScanning(false);

            scanActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scanActivity.updateUIWhenScanCompleteOrCancelled(scanActivity.getString(R.string.scan_complete));
                }
            });
        }
    }
}
