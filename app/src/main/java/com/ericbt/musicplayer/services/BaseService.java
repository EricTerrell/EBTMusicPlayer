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

package com.ericbt.musicplayer.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.Notification.FLAG_FOREGROUND_SERVICE;
import static android.app.Notification.FLAG_ONGOING_EVENT;

public abstract class BaseService extends Service {
    protected IBinder binder = null;

    public static final String NOTIFICATION_CHANNEL_ID = "ebt_music_player_channel_id";

    public static final String NOTIFICATION_CHANNEL_NAME = "EBT Music Player Notifications";

    protected final static int FOREGROUND_FLAGS = FLAG_ONGOING_EVENT | FLAG_FOREGROUND_SERVICE;

    protected final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    protected Logger logger;

    private final String className;

    private NotificationChannel notificationChannel;

    public BaseService() {
        className = this.getClass().getName();
    }

    @Override
    public void onCreate() {
        logger = new Logger(this);
        
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "%s.onCreate, thread id: %d", className, Thread.currentThread().getId()));

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "%s.onDestroy begin", className));

        super.onDestroy();

        threadPool.shutdownNow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel != null) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);

            notificationChannel = null;
        }

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "%s.onDestroy end", className));
    }

    @Override
    public void onLowMemory() {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "%s.onLowMemory", className));

        super.onLowMemory();
    }

    @Override
    public IBinder onBind(Intent intent) {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "%s.onBind", className));

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "%s.onUnbind", className));

        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "%s.onRebind", className));
    }

    protected void sendMessage(String message) {
        final Intent intent = new Intent();
        intent.setAction(message);

        sendBroadcast(intent);
    }

    protected void createNotificationChannel(String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel == null) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(channelDescription);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
