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

package com.ericbt.musicplayer.services.music_player_service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import com.ericbt.musicplayer.MediaPlayerWrapper;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.broadcast_receivers.CustomBroadcastReceiver;
import com.ericbt.musicplayer.music_library.Track;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedManager;
import com.ericbt.musicplayer.services.BaseService;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.LocaleUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MusicPlayerService extends BaseService {
    private final static String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    private final static String ACTION_NEXT = "ACTION_NEXT";
    private final static String ACTION_PAUSE = "ACTION_PAUSE";

    private final static String PLAYBACK = "Playback";

    private MediaSession mediaSession;

    private MediaPlaybackData mediaPlaybackData;

    private Intent cachedIntent;

    private static final int NOTIFICATION_ID = 1000;

    private Timer timer;

    private final AtomicInteger timerCount = new AtomicInteger(1);

    private final ThreadUnsafeVariables threadUnsafeVariables = new ThreadUnsafeVariables();
    
    public MusicPlayerService() {
        // Binder given to clients
        binder = new MusicPlayerServiceBinder();
    }

    public void startForeground(Intent intent) {
        logger.log("MusicPlayerService.startForeground");

        this.cachedIntent = intent;

        startForeground(NOTIFICATION_ID, createDefaultNotification());
    }

    public void startForeground(MediaSession mediaSession, Track track, Intent intent) {
        logger.log(String.format("MusicPlayerService.startForeground %s", track.getTitle()));

        this.mediaSession = mediaSession;
        this.cachedIntent = intent;

        timer = createTimer();

        startForeground(NOTIFICATION_ID, createPlayNotification(track));
    }

    public void setMediaPlaybackData(MediaPlaybackData mediaPlaybackData, Position position) {
        this.mediaPlaybackData = mediaPlaybackData;

        logger.log("MusicPlayerService.setPlayListData");

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.setPlayListData - in synchronized block");

            threadUnsafeVariables.setPosition(position);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();

            if (action != null) {
                logger.log(String.format("MusicPlayerService.onStartCommand action: %s", action));

                switch (intent.getAction()) {
                    case ACTION_PAUSE: {
                        sendMessage(CustomBroadcastReceiver.PAUSE);
                    }
                    break;

                    case ACTION_NEXT: {
                        sendMessage(CustomBroadcastReceiver.NEXT);
                    }
                    break;

                    case ACTION_PREVIOUS: {
                        sendMessage(CustomBroadcastReceiver.PREVIOUS);
                    }
                    break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private NotificationCompat.Action generateAction(int iconId, String title, String intentAction) {
        final Intent intent = new Intent(getApplicationContext(), MusicPlayerService.class);
        intent.setAction(intentAction);

        final PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        return new NotificationCompat.Action.Builder(iconId, title, pendingIntent).build();
    }

    private Notification createDefaultNotification() {
        createNotificationChannel(PLAYBACK);

        final Notification notification = new NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        notification.flags |= FOREGROUND_FLAGS;

        return notification;
    }

    private Notification createPlayNotification(Track track) {
        logger.log(String.format("MusicPlayerService.createPlayNotification %s", track.getTitle()));

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, cachedIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        createNotificationChannel(PLAYBACK);

        return createNotification(pendingIntent, track);
    }

    private Notification createNotification(PendingIntent pendingIntent, Track track) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return createLegacyNotification(pendingIntent, track);
        } else {
            return createNewNotification(pendingIntent, track);
        }
    }

    private Notification createLegacyNotification(PendingIntent pendingIntent, Track track) {
        final Notification notification = new NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(track.getAlbum())
                .setContentText(track.getTitle())
                .setContentIntent(pendingIntent)
                .addAction(generateAction(R.drawable.lock_screen_previous, getString(R.string.previous), ACTION_PREVIOUS))
                .addAction(generateAction(R.drawable.lock_screen_pause, getString(R.string.pause), ACTION_PAUSE))
                .addAction(generateAction(R.drawable.lock_screen_next, getString(R.string.next), ACTION_NEXT))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                        .setShowActionsInCompactView(0, 1, 2)
                ).build();

        notification.flags |= FOREGROUND_FLAGS;

        return notification;
    }

    // TODO: Trying to get the media player notification UI to work. Until then, display a
    // minimal UI that at least brings up the play activity.
    //
    // https://android-developers.googleblog.com/2020/08/playing-nicely-with-media-controls.html
    private Notification createNewNotification(PendingIntent pendingIntent, Track track) {
        final String message = String.format("%s: %s", track.getAlbum(), track.getTitle());

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

    public void updatePlayNotification(Track track) {
        logger.log(String.format("updatePlayNotification track: %s", track.getTitle()));

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, createPlayNotification(track));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseMediaPlayer();

        if (timer != null) {
            logger.log("MusicPlayerService.onDestroy cancel timer");

            timer.cancel();

            timer = null;
        }

        logger.log("MusicPlayerService.onDestroy end");
    }

    private void displayErrorMessage(String mediaFilePath) {
        final String message = String.format("Cannot find music file: %s", mediaFilePath);

        Toast.makeText(MusicPlayerService.this, message, Toast.LENGTH_LONG).show();

        logger.log(String.format("MusicPlayerService.displayErrorMessage %s", message));
    }

    private void setCompletionListener(final MediaPlayer mediaPlayer) {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "MusicPlayerService.setOnCompletionListener thread id: %d", Thread.currentThread().getId()));

        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> threadPool.submit(() -> {
            logger.log(String.format(LocaleUtils.getDefaultLocale(), "MusicPlayerService.setOnCompletionListener run() thread id: %d", Thread.currentThread().getId()));

            synchronized (threadUnsafeVariables) {
                logger.log("MusicPlayerService.setOnCompletionListener run() - inside of synchronized block");

                if (threadUnsafeVariables.getPosition().getListIndex() + 1 < mediaPlaybackData.getMediaList().size()) {
                    threadUnsafeVariables.getPosition().setListIndex(threadUnsafeVariables.getPosition().getListIndex() + 1);

                    threadUnsafeVariables.setMediaPlayerWrapper(new MediaPlayerWrapper(MusicPlayerService.this, threadUnsafeVariables.getMediaPlayerWrapper().getNextMediaPlayer()));

                    try {
                        updatePlayNotification(mediaPlaybackData.getMediaList().get(threadUnsafeVariables.getPosition().getListIndex()));

                        logger.log("Releasing MediaPlayer");
                        mediaPlayer1.release();

                        setupNextTrack();

                        sendCurrentTrackMessage();
                    } catch (Exception ex) {
                        ExceptionLogger.logException(ex, MusicPlayerService.this);
                    }
                } else {
                    releaseMediaPlayer();

                    sendLastTrackCompletedMessage();
                }
            }
        }));
    }

    private void setupNextTrack() {
        logger.log("MusicPlayerService.setupNextTrack");

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.setupNextTrack - inside synchronized block");

            if (threadUnsafeVariables.getPosition().getListIndex() + 1 < mediaPlaybackData.getMediaList().size()) {
                final String mediaFilePath = mediaPlaybackData.getMediaList().get(threadUnsafeVariables.getPosition().getListIndex() + 1).getFilePath();

                final MediaPlayer nextTrackPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(mediaFilePath));

                if (nextTrackPlayer != null) {
                    logger.log(String.format(LocaleUtils.getDefaultLocale(), "Creating MediaPlayer duration %d %s", nextTrackPlayer.getDuration(), mediaFilePath));

                    setCompletionListener(nextTrackPlayer);
                    setOnErrorListener(nextTrackPlayer);
                    setOnPreparedListener(nextTrackPlayer);

                    threadUnsafeVariables.getMediaPlayerWrapper().setNextMediaPlayer(nextTrackPlayer);
                } else {
                    displayErrorMessage(mediaFilePath);
                }
            }
        }
    }

    private void setOnErrorListener(final MediaPlayer mediaPlayer) {
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e(StringLiterals.LOG_TAG, String.format("MediaPlayer onError what: %d extra: %d", what, extra));

            return false;
        });
    }

    private void setOnPreparedListener(final MediaPlayer mediaPlayer) {
        mediaPlayer.setOnPreparedListener(mp -> logger.log(String.format("OnPreparedListener: %s", mediaPlayer)));
    }

    public void sendCurrentTrackMessage() {
        logger.log("MusicPlayerService.sendCurrentTrackMessage");

        final Intent intent = new Intent();
        intent.setAction(CustomBroadcastReceiver.CURRENT_TRACK);

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.sendCurrentTrackMessage - inside synchronized block");

            intent.putExtra(CustomBroadcastReceiver.TRACK_INDEX, threadUnsafeVariables.getPosition().getListIndex());

            logger.log(String.format(LocaleUtils.getDefaultLocale(), "sendCurrentTrackMessage Track: %d", threadUnsafeVariables.getPosition().getListIndex()));
        }

        localBroadcastManager.sendBroadcast(intent);
    }

    private void sendLastTrackCompletedMessage() {
        final Intent intent = new Intent();
        intent.setAction(CustomBroadcastReceiver.LAST_TRACK_PLAYED);

        sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() != CustomBroadcastReceiver.ABORTED_RESULT_CODE) {
                    // Broadcast was not aborted by PlayActivity. Need to destroy the service
                    // because PlayActivity didn't.

                    stopService(null, null);
                }
            }
        }, null, Activity.RESULT_OK, null, null);
    }

    public void next() {
        logger.log("MusicPlayerService.next");

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.next - inside synchronized block");

            if (threadUnsafeVariables.getPosition().getListIndex() < mediaPlaybackData.getMediaList().size() - 1) {
                threadUnsafeVariables.getPosition().setListIndex(threadUnsafeVariables.getPosition().getListIndex() + 1);
                play();
            }
        }
    }

    public void previous() {
        logger.log("MusicPlayerService.previous");

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.previous - inside synchronized block");

            if (threadUnsafeVariables.getPosition().getListIndex() > 0) {
                threadUnsafeVariables.getPosition().setListIndex(threadUnsafeVariables.getPosition().getListIndex() - 1);
                play();
            }
        }
    }

    private void releaseMediaPlayer() {
        logger.log("MusicPlayerService.releaseMediaPlayer");

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.releaseMediaPlayer inside synchronized block");

            try {
                if (threadUnsafeVariables.getMediaPlayerWrapper() != null) {
                    threadUnsafeVariables.getMediaPlayerWrapper().release();
                }
            } catch (Exception ex) {
                ExceptionLogger.logException(ex, MusicPlayerService.this);
            } finally {
                threadUnsafeVariables.setMediaPlayerWrapper(null);
            }
        }
    }

    public TrackProgress getTrackProgress() {
        logger.log("MusicPlayerService.getTrackProgress");

        TrackProgress trackProgress;

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.getTrackProgress - inside synchronized block");

            trackProgress = threadUnsafeVariables.getMediaPlayerWrapper() != null ? new TrackProgress(threadUnsafeVariables.getMediaPlayerWrapper().getCurrentPosition(), threadUnsafeVariables.getMediaPlayerWrapper().getDuration()) : new TrackProgress(0, 0);

            logger.log(String.format("trackProgress: %s", trackProgress));
        }

        return trackProgress;
    }

    public void play() {
        threadPool.submit(() -> {
            logger.log("MusicPlayerService.play run()");

            synchronized (threadUnsafeVariables) {
                logger.log("MusicPlayerService.play run() inside synchronized block");

                logger.log(String.format(LocaleUtils.getDefaultLocale(), "MusicPlayerService.play thread id: %d", Thread.currentThread().getId()));

                releaseMediaPlayer();

                final String mediaFilePath = mediaPlaybackData.getMediaList().get(threadUnsafeVariables.getPosition().getListIndex()).getFilePath();

                final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(mediaFilePath));

                if (mediaPlayer != null) {
                    threadUnsafeVariables.setMediaPlayerWrapper(new MediaPlayerWrapper(MusicPlayerService.this, mediaPlayer));

                    logger.log(String.format(LocaleUtils.getDefaultLocale(), "Creating MediaPlayer duration: %d %s", threadUnsafeVariables.getMediaPlayerWrapper().getDuration(), mediaFilePath));

                    setCompletionListener(threadUnsafeVariables.getMediaPlayerWrapper().getMediaPlayer());
                    setOnErrorListener(threadUnsafeVariables.getMediaPlayerWrapper().getMediaPlayer());
                    setOnPreparedListener(threadUnsafeVariables.getMediaPlayerWrapper().getMediaPlayer());

                    setupNextTrack();

                    threadUnsafeVariables.getMediaPlayerWrapper().seekTo(threadUnsafeVariables.getPosition().getPositionInTrack());
                    threadUnsafeVariables.getMediaPlayerWrapper().start();

                    sendCurrentTrackMessage();

                    updatePlayNotification(mediaPlaybackData.getMediaList().get(threadUnsafeVariables.getPosition().getListIndex()));
                } else {
                    displayErrorMessage(mediaFilePath);
                }
            }
        });
    }

    public void seekTo(final int newPosition) {
        threadPool.submit(() -> {
            logger.log("MusicPlayerService.seekTo");

            synchronized (threadUnsafeVariables) {
                if (threadUnsafeVariables.getMediaPlayerWrapper() != null) {
                    threadUnsafeVariables.getMediaPlayerWrapper().pause();
                    threadUnsafeVariables.getMediaPlayerWrapper().seekTo(newPosition);
                    threadUnsafeVariables.getMediaPlayerWrapper().start();
                }
            }
        });
    }

    public class MusicPlayerServiceBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    public Position stopService(Context context, ServiceConnection serviceConnection) {
        logger.log("MusicPlayerService.stopService");

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.stopService inside synchronized block");

            if (threadUnsafeVariables.getMediaPlayerWrapper() != null) {
                threadUnsafeVariables.getPosition().setPositionInTrack(threadUnsafeVariables.getMediaPlayerWrapper().getCurrentPosition());
            }

            releaseMediaPlayer();

            if (context != null && serviceConnection != null) {
                try {
                    context.unbindService(serviceConnection);
                } catch (Throwable ex) {
                    ExceptionLogger.logException(ex, this);
                }
            }

            this.stopForeground(true);
            this.stopSelf();

            return threadUnsafeVariables.getPosition();
        }
    }

    public Position getPosition() {
        logger.log("MusicPlayerService.getPosition");

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.getPosition - inside synchronized block");

            return new Position(threadUnsafeVariables.getPosition());
        }
    }

    public MediaPlaybackData getMediaPlaybackData() {
        return mediaPlaybackData;
    }

    public boolean isPlaying() {
        logger.log("MusicPlayerService.isPlaying");

        boolean isPlaying = false;

        synchronized (threadUnsafeVariables) {
            logger.log("MusicPlayerService.isPlaying - inside synchronized block");

            try {
                isPlaying = threadUnsafeVariables.getMediaPlayerWrapper() != null && threadUnsafeVariables.getMediaPlayerWrapper().isPlaying();
            } catch (Throwable th) {
                // nothing
            }
        }

        return isPlaying;
    }

    private Timer createTimer() {
        logger.log("MusicPlayerService.createTimer");

        if (timer != null) {
            logger.log("MusicPlayerService.createTimer: timer exists!");

            timer.cancel();
        }

        final UUID timerTaskUUID = UUID.randomUUID();

        logger.log(String.format("MusicPlayerService.createTimer: %s", timerTaskUUID));

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (threadUnsafeVariables) {
                    final Position position = threadUnsafeVariables.getPosition();

                    if (position != null) {
                        final Intent intent = new Intent();
                        intent.setAction(CustomBroadcastReceiver.TICK);

                        intent.putExtra(CustomBroadcastReceiver.CURRENT_TRACK, position.getListIndex());

                        final TrackProgress trackProgress = threadUnsafeVariables.getMediaPlayerWrapper() != null ? new TrackProgress(threadUnsafeVariables.getMediaPlayerWrapper().getCurrentPosition(), threadUnsafeVariables.getMediaPlayerWrapper().getDuration()) : new TrackProgress(0, 0);

                        intent.putExtra(CustomBroadcastReceiver.CURRENT_POSITION, trackProgress.getCurrentPosition());

                        localBroadcastManager.sendBroadcast(intent);

                        if (timerCount.getAndIncrement() % 15 == 0) {
                            final RecentlyPlayedManager recentlyPlayedManager = new RecentlyPlayedManager(MusicPlayerService.this, logger);

                            recentlyPlayedManager.updateRecentlyPlayedData(
                                    mediaPlaybackData.getAction(),
                                    mediaPlaybackData.getIds(),
                                    mediaPlaybackData.getMediaList().get(position.getListIndex()).getAlbum(),
                                    mediaPlaybackData.getMediaList().get(position.getListIndex()).getTitle(),
                                    position.getListIndex(),
                                    trackProgress.getCurrentPosition());
                        }
                    }
                }
            }
        }, 0, 1000);

        return timer;
    }

}
