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

package com.ericbt.musicplayer.services.scanner_service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import androidx.core.app.NotificationCompat;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.activities.ScanActivity;
import com.ericbt.musicplayer.broadcast_receivers.CustomBroadcastReceiver;
import com.ericbt.musicplayer.services.BaseService;
import com.ericbt.musicplayer.utils.ExceptionLogger;

public class ScannerService extends BaseService {
    private static final int NOTIFICATION_ID = 1001;

    private boolean isScanCancellationRequested, isScanning;

    public ScannerService() {
        // Binder given to clients
        binder = new ScannerServiceBinder();
    }

    public void startForeground() {
        logger.log("ScannerService.startForeground");

        startForeground(NOTIFICATION_ID, createScanNotification(StringLiterals.EMPTY_STRING));
    }

    public void stopService(Context context, ServiceConnection serviceConnection) {
        logger.log("ScannerService.stopService");

        try {
            context.unbindService(serviceConnection);
        } catch (Throwable ex) {
            ExceptionLogger.logException(ex, this);
        }

        this.stopForeground(true);
        this.stopSelf();
    }

    private Notification createScanNotification(String message) {
        logger.log(String.format("ScannerService.createScanNotification %s", message));

        final Intent notificationIntent = new Intent(this, ScanActivity.class);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createNotificationChannel("Scanning");

        final Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(StringLiterals.APP_NAME)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .build();

        notification.flags |= FOREGROUND_FLAGS;

        return notification;
    }

    public void updateScanNotification(String message) {
        logger.log(String.format("updateScanNotification %s", message));

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, createScanNotification(message));
    }

    public void sendScanProgressMessage(String message, int progressPercent) {
        final Intent intent = new Intent()
                .setAction(CustomBroadcastReceiver.SCAN_PROGRESS_MESSAGE)
                .putExtra(CustomBroadcastReceiver.MESSAGE, message)
                .putExtra(CustomBroadcastReceiver.PROGRESS_PERCENT, progressPercent);

        sendBroadcast(intent);

        updateScanNotification(message);
    }

    public void sendScanProgressMessage(String message) {
        sendScanProgressMessage(message, 0);
    }

    public void sendScanCompleteMessage() {
        sendMessage(CustomBroadcastReceiver.SCAN_COMPLETE);

        updateScanNotification(getString(R.string.scan_complete));
    }

    public void sendScanCancelledMessage() {
        sendMessage(CustomBroadcastReceiver.SCAN_CANCELLED);

        updateScanNotification(getString(R.string.scan_cancelled));
    }

    public void scan(ScanActivity scanActivity) {
        isScanCancellationRequested = false;

        threadPool.submit(new ScanTask(this, scanActivity));
    }

    public boolean isScanCancellationRequested() {
        return isScanCancellationRequested;
    }

    public boolean isScanning() { return isScanning; }

    public void setIsScanning(boolean isScanning) { this.isScanning = isScanning; }

    public void requestScanCancellation() {
        isScanCancellationRequested = true;
    }

    public class ScannerServiceBinder extends Binder {
        public ScannerService getService() {
            return ScannerService.this;
        }
    }

}
