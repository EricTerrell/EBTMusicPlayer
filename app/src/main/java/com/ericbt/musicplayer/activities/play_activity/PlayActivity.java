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

package com.ericbt.musicplayer.activities.play_activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ericbt.musicplayer.activities.MainActivity;
import com.ericbt.musicplayer.change_processors.AudioFocusChangeProcessor;
import com.ericbt.musicplayer.broadcast_receivers.CustomBroadcastReceiver;
import com.ericbt.musicplayer.CustomPhoneStateListener;
import com.ericbt.musicplayer.PlaybackController;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.async_tasks.AsyncTask;
import com.ericbt.musicplayer.async_tasks.RetrieveAlbumsTask;
import com.ericbt.musicplayer.async_tasks.RetrievePlayListsTask;
import com.ericbt.musicplayer.async_tasks.RetrieveTracksTask;
import com.ericbt.musicplayer.change_processors.BluetoothChangeProcessor;
import com.ericbt.musicplayer.change_processors.HeadphonePlugChangeProcessor;
import com.ericbt.musicplayer.array_adapters.MediaFileArrayAdapter;
import com.ericbt.musicplayer.music_library.Track;
import com.ericbt.musicplayer.services.music_player_service.MediaPlaybackData;
import com.ericbt.musicplayer.services.music_player_service.MusicPlayerService;
import com.ericbt.musicplayer.services.music_player_service.Position;
import com.ericbt.musicplayer.services.music_player_service.TrackProgress;
import com.ericbt.musicplayer.utils.AudioUtils;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;
import com.ericbt.musicplayer.utils.NavigationUtils;
import com.ericbt.musicplayer.utils.TimeFormatter;

import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothAdapter.EXTRA_CONNECTION_STATE;
import static android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE;

import androidx.core.app.ActivityCompat;

public class PlayActivity extends Activity implements PlaybackController {
    public static final String PLAY_ALBUM = "PLAY_ALBUM";
    public static final String PLAY_PLAYLIST = "PLAY_PLAYLIST";
    public static final String PLAY_TRACK = "PLAY_TRACK";
    public static final String SELECTED_TRACK = "SELECTED_TRACK";
    private static final String LIST_INDEX = "LIST_INDEX";
    public static final String POSITION_IN_TRACK = "POSITION_IN_TRACK";
    private static final String ALBUM = "ALBUM";
    private static final String TRACK_TITLE = "TRACK_TITLE";
    private static final String TRACK_DURATION = "TRACK_DURATION";
    private static final String TRACK_ARTIST = "TRACK_ARTIST";
    private static final String IS_PLAYBACK_FINISHED = "IS_PLAYBACK_FINISHED";
    private static final String IS_PROGRAMATICALLY_PAUSED = "IS_PROGRAMATICALLY_PAUSED";

    private enum PlayPauseButtonState {PLAY, PAUSE}

    public static final String IDS = "IDS";

    private final ThreadUnsafeVariables threadUnsafeVariables = new ThreadUnsafeVariables();

    private ImageButton play, pause, next, previous;

    private ListView trackListView;

    private MediaPlaybackData mediaPlaybackData;

    private Position position = new Position(0, 0);

    private Position pausePosition;

    private CustomBroadcastReceiver customBroadcastReceiver, connectDisconnectBroadcastReceiver;

    private int selectedTrack = -1;

    private enum Action {PLAY, NEXT, PREVIOUS, RECONNECT}

    private Action action;

    private TextView currentAlbum, currentTrackName, currentTrackArtist,
            currentTrackCurrentPosition, currentTrackDuration;

    private SeekBar currentTrackSeekBar;

    private CustomPhoneStateListener customPhoneStateListener;

    private HeadphonePlugChangeProcessor headphonePlugChangeProcessor;

    private BluetoothChangeProcessor bluetoothChangeProcessor;

    private AudioManager audioManager;

    private AudioFocusChangeProcessor audioFocusChangeProcessor;

    private boolean isPlaybackFinished, isProgramaticallyPaused;

    private TelephonyManager telephonyManager;

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger = new Logger(this);

        logger.log("PlayActivity.onCreate");

        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        audioFocusChangeProcessor = new AudioFocusChangeProcessor(this, this);
        customPhoneStateListener = new CustomPhoneStateListener(this, this);

        setContentView(R.layout.activity_play);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        trackListView = findViewById(R.id.trackListView);

        play = findViewById(R.id.play);

        play.setOnClickListener(v -> play(true));

        pause = findViewById(R.id.pause);

        pause.setOnClickListener(v -> pause());

        next = findViewById(R.id.next);

        next.setOnClickListener(v -> {
            if (AudioUtils.requestAudioFocus(audioManager, audioFocusChangeProcessor, logger) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startService(Action.NEXT);
                enablePlayPauseButton(PlayPauseButtonState.PAUSE);
            }
        });

        previous = findViewById(R.id.previous);

        previous.setOnClickListener(v -> {
            if (AudioUtils.requestAudioFocus(audioManager, audioFocusChangeProcessor, logger) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startService(Action.PREVIOUS);
                enablePlayPauseButton(PlayPauseButtonState.PAUSE);
            }
        });

        currentAlbum = findViewById(R.id.currentAlbum);
        currentTrackName = findViewById(R.id.currentTrackName);
        currentTrackArtist = findViewById(R.id.currentTrackArtist);

        currentTrackCurrentPosition = findViewById(R.id.currentTrackCurrentPosition);
        currentTrackDuration = findViewById(R.id.currentTrackDuration);
        currentTrackSeekBar = findViewById(R.id.currentTrackSeekBar);

        logger.log(String.format("PlayActivity.onCreate: have savedInstanceState: %b", savedInstanceState != null));

        if (savedInstanceState != null) {
            selectedTrack = savedInstanceState.getInt(SELECTED_TRACK);

            position.setListIndex(savedInstanceState.getInt(LIST_INDEX));
            position.setPositionInTrack(savedInstanceState.getInt(POSITION_IN_TRACK));

            if (position.getListIndex() >= 0) {
                pausePosition = position;
            }

            logger.log(String.format(LocaleUtils.getDefaultLocale(), "PlayActivity.onCreate: position: %s pausePosition: %s", position, pausePosition));

            currentTrackSeekBar.setEnabled(false);

            currentAlbum.setText(savedInstanceState.getString(ALBUM));
            currentTrackName.setText(savedInstanceState.getString(TRACK_TITLE));
            currentTrackArtist.setText(savedInstanceState.getString(TRACK_ARTIST));

            final int trackDuration = savedInstanceState.getInt(TRACK_DURATION);
            currentTrackSeekBar.setMax(trackDuration);
            currentTrackDuration.setText(TimeFormatter.toHHMMSS(trackDuration));
            currentTrackCurrentPosition.setText(TimeFormatter.toHHMMSS(position.getPositionInTrack()));

            isPlaybackFinished = savedInstanceState.getBoolean(IS_PLAYBACK_FINISHED);

            isProgramaticallyPaused = savedInstanceState.getBoolean(IS_PROGRAMATICALLY_PAUSED);
        } else {
            selectedTrack = getIntent().getIntExtra(SELECTED_TRACK, -1);

            logger.log(String.format(LocaleUtils.getDefaultLocale(), "Retrieved SELECTED_TRACK from indent: %d", selectedTrack));

            position.setListIndex(selectedTrack);
            position.setPositionInTrack(getIntent().getIntExtra(POSITION_IN_TRACK, 0));

            enablePlayPauseButton(PlayPauseButtonState.PLAY);

            next.setEnabled(false);
            previous.setEnabled(false);
        }

        populateTrackList();

        currentTrackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                logger.log("PlayActivity.onStopTrackingTouch");

                synchronized (threadUnsafeVariables) {
                    logger.log("PlayActivity.onStopTrackingTouch inside synchronized block");

                    if (threadUnsafeVariables.getMusicPlayerService() != null) {
                        threadUnsafeVariables.getMusicPlayerService().seekTo(seekBar.getProgress());
                    }
                }
            }
        });

        headphonePlugChangeProcessor = new HeadphonePlugChangeProcessor(this, this);
        bluetoothChangeProcessor = new BluetoothChangeProcessor(this);

        customBroadcastReceiver = createCustomBroadcastReceiver();
        connectDisconnectBroadcastReceiver = createConnectDisconnectBroadcastReceiver();

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        AudioUtils.requestAudioFocus(audioManager, audioFocusChangeProcessor, logger);

        startService(Action.RECONNECT);
    }

    private void enablePlayPauseButton(PlayPauseButtonState playPauseButtonStateState) {
        final boolean playActive = playPauseButtonStateState == PlayPauseButtonState.PLAY;

        play.setEnabled(playActive);
        play.setVisibility(playActive ? View.VISIBLE : View.GONE);

        final boolean pauseActive = playPauseButtonStateState == PlayPauseButtonState.PAUSE;

        pause.setEnabled(pauseActive);
        pause.setVisibility(pauseActive ? View.VISIBLE : View.GONE);
    }

    private boolean isEnabled(PlayPauseButtonState playPauseButtonState) {
        return playPauseButtonState == PlayPauseButtonState.PLAY ? play.isEnabled() : pause.isEnabled();
    }

    @Override
    public void onStart() {
        super.onStart();

        logger.log("PlayActivity.onStart");
    }

    @Override
    public void onDestroy() {
        logger.log("PlayActivity.onDestroy");

        super.onDestroy();

        unregisterReceiver(customBroadcastReceiver);
        unregisterReceiver(connectDisconnectBroadcastReceiver);

        telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        audioManager.abandonAudioFocus(audioFocusChangeProcessor.getOnAudioFocusChangeListener());

        try {
            unbindService(serviceConnection);
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, this);
        }
    }

    private void resumePlaying() {
        logger.log("PlayActivity.resumePlaying start");

        synchronized (threadUnsafeVariables) {
            logger.log("PlayActivity.resumePlaying inside synchronized block");

            if (threadUnsafeVariables.getMusicPlayerService() != null && threadUnsafeVariables.getMusicPlayerService().isPlaying()) {
                logger.log("PlayActivity.resumePlaying isPlaying");

                final TrackProgress trackProgress = threadUnsafeVariables.getMusicPlayerService().getTrackProgress();
                final Position position = threadUnsafeVariables.getMusicPlayerService().getPosition();

                mediaPlaybackData = threadUnsafeVariables.getMusicPlayerService().getMediaPlaybackData();

                currentTrackName.setText(mediaPlaybackData.getMediaList().get(position.getListIndex()).getTitle());
                currentTrackArtist.setText(mediaPlaybackData.getMediaList().get(position.getListIndex()).getArtist());

                currentTrackSeekBar.setMax(trackProgress.getDuration());
                currentTrackSeekBar.setProgress(trackProgress.getCurrentPosition());

                currentTrackDuration.setText(TimeFormatter.toHHMMSS(trackProgress.getDuration()));

                selectedTrack = position.getListIndex();
                logger.log(String.format(LocaleUtils.getDefaultLocale(), "SELECTED_TRACK = %d", selectedTrack));

                if (selectedTrack >= 0) {
                    trackListView.setItemChecked(selectedTrack, true);

                    trackListView.smoothScrollToPosition(selectedTrack);
                }
            } else if (selectedTrack < 0) {
                clearCurrentSong();
            }
        }

        logger.log("PlayActivity.resumePlaying end");
    }

    private void clearCurrentSong() {
        logger.log("PlayActivity.clearCurrentSong");

        enablePlayPauseButton(PlayPauseButtonState.PLAY);

        next.setEnabled(false);
        previous.setEnabled(false);

        final TextView[] textViews = new TextView[]{
                currentAlbum, currentTrackName, currentTrackArtist, currentTrackDuration, currentTrackCurrentPosition
        };

        for (TextView textView : textViews) {
            textView.setText(StringLiterals.EMPTY_STRING);
        }

        currentTrackSeekBar.setProgress(0);

        position.setListIndex(0);

        for (int i = 0; i < trackListView.getCount(); i++) {
            trackListView.setItemChecked(i, false);
        }

        trackListView.smoothScrollToPosition(0);
    }

    @Override
    public boolean isProgramaticallyPaused() {
        return isProgramaticallyPaused;
    }

    @Override
    public void setIsProgramaticallyPaused(boolean isProgramaticallyPaused) {
        this.isProgramaticallyPaused = isProgramaticallyPaused;
    }

    @Override
    public boolean play(boolean requestAudioFocus) {
        logger.log(String.format("PlayActivity.play requestAudioFocus: %b", requestAudioFocus));

        boolean result = false;

        if (isEnabled(PlayPauseButtonState.PLAY)) {
            // We only want to request audio focus when the user has initiated the action (by
            // pressing the play, next, or previous buttons. Reason: on bluetooth this can cause
            // the volume to be automatically lowered as each track starts playing.
            if (!requestAudioFocus || AudioUtils.requestAudioFocus(audioManager, audioFocusChangeProcessor, logger) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                enablePlayPauseButton(PlayPauseButtonState.PAUSE);

                next.setEnabled(true);
                previous.setEnabled(true);

                startService(Action.PLAY);

                result = true;
            }
        }

        return result;
    }

    @Override
    public boolean pause() {
        logger.log("PlayActivity.pause");

        if (isEnabled(PlayPauseButtonState.PAUSE)) {
            enablePlayPauseButton(PlayPauseButtonState.PLAY);

            currentTrackSeekBar.setEnabled(false);

            pauseService();

            return true;
        } else {
            return false;
        }
    }

    private boolean playOrPause() {
        logger.log("PlayActivity.playOrPause");

        if (isEnabled(PlayPauseButtonState.PAUSE)) {
            return pause();
        } else if (isEnabled(PlayPauseButtonState.PLAY)) {
            return play(true);
        } else {
            return false;
        }
    }

    @Override
    public boolean isPlaybackFinished() {
        return isPlaybackFinished;
    }

    private boolean next() {
        logger.log("PlayActivity.next");

        if (AudioUtils.requestAudioFocus(audioManager, audioFocusChangeProcessor, logger) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (next.isEnabled()) {
                startService(Action.NEXT);

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean previous() {
        logger.log("PlayActivity.previous");

        if (AudioUtils.requestAudioFocus(audioManager, audioFocusChangeProcessor, logger) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (previous.isEnabled()) {
                startService(Action.PREVIOUS);

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private CustomBroadcastReceiver createCustomBroadcastReceiver() {
        logger.log("PlayActivity.createCustomBroadcastReceiver");

        final CustomBroadcastReceiver customBroadcastReceiver = new CustomBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                logger.log(String.format("PlayActivity onReceive action: %s isInitialStickyBroadcast: %b", intent.getAction(), isInitialStickyBroadcast()));

                if (!isInitialStickyBroadcast()) {
                    switch (intent.getAction()) {
                        case CustomBroadcastReceiver.CURRENT_TRACK: {
                            processCurrentTrack(intent);
                        }
                        break;

                        case CustomBroadcastReceiver.LAST_TRACK_PLAYED: {
                            processLastTrackPlayed();

                            // Abort the broadcast. The MusicPlayerService will determine
                            // if the broadcast was aborted. And if not, it will kill the
                            // service itself.
                            setResultCode(CustomBroadcastReceiver.ABORTED_RESULT_CODE);
                            abortBroadcast();
                        }
                        break;

                        case CustomBroadcastReceiver.PAUSE: {
                            pause();
                        }
                        break;

                        case CustomBroadcastReceiver.PLAY_PAUSE: {
                            playOrPause();
                        }
                        break;

                        case CustomBroadcastReceiver.NEXT: {
                            next();
                        }
                        break;

                        case CustomBroadcastReceiver.PREVIOUS: {
                            previous();
                        }
                        break;

                        case CustomBroadcastReceiver.TICK: {
                            final int currentPosition = intent.getIntExtra(CustomBroadcastReceiver.CURRENT_POSITION, 0);

                            currentTrackSeekBar.setProgress(currentPosition);
                            currentTrackCurrentPosition.setText(TimeFormatter.toHHMMSS(currentPosition));
                        }
                        break;
                    }
                }

                logger.log("PlayActivity onReceive end");
            }
        };

        final IntentFilter intentFilter = new IntentFilter(CustomBroadcastReceiver.CURRENT_TRACK);
        intentFilter.addAction(CustomBroadcastReceiver.LAST_TRACK_PLAYED);
        intentFilter.addAction(CustomBroadcastReceiver.PLAY_PAUSE);
        intentFilter.addAction(CustomBroadcastReceiver.NEXT);
        intentFilter.addAction(CustomBroadcastReceiver.PREVIOUS);
        intentFilter.addAction(CustomBroadcastReceiver.PAUSE);
        intentFilter.addAction(CustomBroadcastReceiver.TICK);

        registerReceiver(customBroadcastReceiver, intentFilter);

        return customBroadcastReceiver;
    }

    private CustomBroadcastReceiver createConnectDisconnectBroadcastReceiver() {
        logger.log("PlayActivity.createConnectDisconnectBroadcastReceiver");

        final CustomBroadcastReceiver connectDisconnectBroadcastReceiver = new CustomBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                logger.log(String.format("PlayActivity onReceive action: %s isInitialStickyBroadcast: %b", intent.getAction(), isInitialStickyBroadcast()));

                if (!isInitialStickyBroadcast()) {
                    switch (intent.getAction()) {
                        case Intent.ACTION_HEADSET_PLUG: {
                            headphonePlugChangeProcessor.processChange(intent, PlayActivity.this);
                        }
                        break;

                        case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                            final int connectionState = intent.getIntExtra(EXTRA_CONNECTION_STATE, -1);
                            final int prevConnectionState = intent.getIntExtra(EXTRA_PREVIOUS_CONNECTION_STATE, -1);
                            final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                            String bluetoothDeviceName = StringLiterals.EMPTY_STRING;

                            if (ActivityCompat.checkSelfPermission(PlayActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                bluetoothDeviceName = bluetoothDevice.getName();
                            }

                            logger.log(String.format(LocaleUtils.getDefaultLocale(),
                                    "BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED connectionState: %d prevConnectionState: %d bluetoothDevice name: %s address: %s bond state: %d",
                                    connectionState,
                                    prevConnectionState,
                                    bluetoothDeviceName,
                                    bluetoothDevice.getAddress(),
                                    bluetoothDevice.getBondState()
                            ));

                            bluetoothChangeProcessor.processChange(intent, PlayActivity.this);
                        }
                        break;

                        case AudioManager.ACTION_AUDIO_BECOMING_NOISY: {
                            pause();
                        }
                        break;
                    }
                }

                logger.log("PlayActivity onReceive end");
            }
        };

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        registerReceiver(connectDisconnectBroadcastReceiver, intentFilter);

        return connectDisconnectBroadcastReceiver;
    }

    private void processCurrentTrack(Intent intent) {
        logger.log("PlayActivity.processCurrentTrack");

        pausePosition = null;

        final int currentTrack = intent.getIntExtra(CustomBroadcastReceiver.TRACK_INDEX, 0);

        final Track track = mediaPlaybackData.getMediaList().get(currentTrack);

        currentAlbum.setText(track.getAlbum());
        currentTrackName.setText(track.getTitle());
        currentTrackArtist.setText(track.getArtist());

        currentTrackSeekBar.setEnabled(true);

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "PlayActivity.processCurrentTrack track: %d currentAlbum: %s", currentTrack, mediaPlaybackData.getMediaList().get(currentTrack).getTitle()));

        synchronized (threadUnsafeVariables) {
            logger.log("PlayActivity.processCurrentTrack: in synchronized block");

            if (threadUnsafeVariables.getMusicPlayerService() != null) {
                final TrackProgress trackProgress = threadUnsafeVariables.getMusicPlayerService().getTrackProgress();

                currentTrackSeekBar.setMax(trackProgress.getDuration());

                currentTrackDuration.setText(TimeFormatter.toHHMMSS(trackProgress.getDuration()));
            }
        }

        position.setListIndex(currentTrack);

        enablePlayPauseButton(PlayPauseButtonState.PAUSE);

        next.setEnabled(position.getListIndex() < mediaPlaybackData.getMediaList().size() - 1);
        previous.setEnabled(position.getListIndex() > 0);

        trackListView.setItemChecked(position.getListIndex(), true);
        trackListView.smoothScrollToPosition(currentTrack);

        logger.log("PlayActivity.processCurrentTrack finished");
    }

    private void processLastTrackPlayed() {
        logger.log("PlayActivity.processLastTrackPlayed");

        isPlaybackFinished = true;

        stopService();

        enablePlayPauseButton(PlayPauseButtonState.PLAY);

        next.setEnabled(false);

        position = new Position(0, 0);

        clearCurrentSong();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        logger.log("PlayActivity.onSaveInstanceState begin");

        logger.log(String.format("PlayActivity.onSaveInstanceState: have pausePosition: %b", pausePosition != null));

        if (pausePosition != null) {
            logger.log(String.format("PlayActivity.onSaveInstanceState: pausePosition: %s", pausePosition));

            outState.putInt(LIST_INDEX, pausePosition.getListIndex());
            outState.putInt(POSITION_IN_TRACK, pausePosition.getPositionInTrack());
            outState.putInt(SELECTED_TRACK, pausePosition.getListIndex());
            outState.putInt(TRACK_DURATION, currentTrackSeekBar.getMax());
            outState.putString(ALBUM, currentAlbum.getText().toString());
            outState.putString(TRACK_TITLE, currentTrackName.getText().toString());
            outState.putString(TRACK_ARTIST, currentTrackArtist.getText().toString());
        } else {
            outState.putInt(LIST_INDEX, -1);
            outState.putInt(POSITION_IN_TRACK, 0);
            outState.putInt(SELECTED_TRACK, -1);
        }

        outState.putBoolean(IS_PLAYBACK_FINISHED, isPlaybackFinished);
        outState.putBoolean(IS_PROGRAMATICALLY_PAUSED, isProgramaticallyPaused);

        super.onSaveInstanceState(outState);

        logger.log("PlayActivity.onSaveInstanceState end");
    }

    private void pauseService() {
        logger.log("PlayActivity.pauseService");

        final Position stopPosition = stopService();

        if (stopPosition != null) {
            pausePosition = position = stopPosition;
        } else
        {
            logger.log(String.format("Service was previously paused, using old position: %s", position));

            // Service may have already been stopped. If so, use the old stop position.
            pausePosition = position;
        }

        synchronized (threadUnsafeVariables) {
            logger.log("PlayActivity.pauseService: in synchronized block");

            threadUnsafeVariables.setMusicPlayerService(null);
        }
    }

    private void startService(Action action) {
        logger.log(String.format("PlayActivity.startService Action: %s", action));

        this.action = action;

        final Intent intent = new Intent(getApplicationContext(), MusicPlayerService.class);

        synchronized (threadUnsafeVariables) {
            logger.log("PlayActivity.startService: in synchronized block");

            if (threadUnsafeVariables.getMusicPlayerService() != null) {
                if (action == Action.NEXT || action == Action.PREVIOUS) {
                    pauseService();
                }

                if (action == Action.PLAY) {
                    stopService();
                }
            }
        }

        logger.log("PlayActivity.startService bindService and startService");

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startService(intent);
        } else {
            startForegroundService(intent);
        }
    }

    private void populateTrackList() {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "PlayActivity.populateTrackList SELECTED_TRACK: %d", selectedTrack));

        final ArrayList<String> idsList = getIntent().getStringArrayListExtra(IDS);

        final List<Long> ids = new ArrayList<>();

        for (String id : idsList) {
            ids.add(Long.valueOf(id));
        }

        switch(getIntent().getAction()) {
            case PLAY_ALBUM: {
                AsyncTask.submit(new RetrieveAlbumsTask(this, ids, position));
            }
            break;

            case PLAY_PLAYLIST: {
                AsyncTask.submit(new RetrievePlayListsTask(this, ids, position));
            }
            break;

            case PLAY_TRACK: {
                AsyncTask.submit(new RetrieveTracksTask(this, ids, position));
            }
            break;
        }
    }

    @Override
    public void onBackPressed() {
        logger.log("PlayActivity.onBackPressed");

        super.onBackPressed();

        pauseService();

        NavigationUtils.goBackTo(this, MainActivity.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger.log("PlayActivity.onOptionsSelected");

        boolean result = false;

        if (item.getItemId() == android.R.id.home) {
            pauseService();

            NavigationUtils.goBackTo(this, MainActivity.class);

            result = true;
        }

        return result;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.log("PlayActivity.onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.log(String.format("PlayActivity.onServiceConnected action=%s", action));

            final MusicPlayerService.MusicPlayerServiceBinder musicPlayerServiceBinder = (MusicPlayerService.MusicPlayerServiceBinder) service;

            synchronized (threadUnsafeVariables) {
                logger.log("PlayActivity.onServiceConnected: inside synchronized block");

                threadUnsafeVariables.setMusicPlayerService(musicPlayerServiceBinder.getService());

                threadUnsafeVariables.getMusicPlayerService().startForeground(PlayActivity.this.getIntent());

                if (action == Action.RECONNECT) {
                    if (threadUnsafeVariables.getMusicPlayerService().isPlaying()) {
                        enablePlayPauseButton(PlayPauseButtonState.PAUSE);

                        threadUnsafeVariables.getMusicPlayerService().sendCurrentTrackMessage();
                    } else {
                        stopService();

                        enablePlayPauseButton(PlayPauseButtonState.PLAY);

                        next.setEnabled(false);
                        previous.setEnabled(false);

                        return;
                    }
                }

                final MediaSession mediaSession = new MediaSession(getApplicationContext(), "tag");

                mediaSession.setCallback(new MediaSession.Callback() {
                    @Override
                    public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                        if (StringLiterals.MEDIA_BUTTON.equals(mediaButtonIntent.getAction())) {
                            logger.log(String.format("onMediaButtonEvent action: %s", mediaButtonIntent.getAction()));

                            KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                            logger.log(String.format("event action: %d", event.getAction()));

                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                final int keyCode = event.getKeyCode();
                                logger.log(String.format("keyCode: %d", keyCode));

                                switch(keyCode) {
                                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                    case KeyEvent.KEYCODE_MEDIA_PLAY: {
                                        playOrPause();

                                        return true;
                                    }

                                    case KeyEvent.KEYCODE_MEDIA_STOP: {
                                        pause();

                                        return true;
                                    }

                                    case KeyEvent.KEYCODE_MEDIA_NEXT: {
                                        next();

                                        return true;
                                    }

                                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
                                        previous();

                                        return true;
                                    }
                                }
                            }
                        }

                        return super.onMediaButtonEvent(mediaButtonIntent);
                    }
                });

                mediaSession.setActive(true);

                final PlaybackState.Builder stateBuilder = new PlaybackState.Builder();

                mediaSession.setPlaybackState(stateBuilder.build());
                stateBuilder.setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0.0f);

                if (mediaPlaybackData != null) {
                    final Track track = mediaPlaybackData.getMediaList().get(position.getListIndex());

                    threadUnsafeVariables.getMusicPlayerService().startForeground(mediaSession, track, PlayActivity.this.getIntent());

                    threadUnsafeVariables.getMusicPlayerService().setMediaPlaybackData(mediaPlaybackData, position);
                }

                switch (action) {
                    case PLAY: {
                        isPlaybackFinished = false;
                        isProgramaticallyPaused = false;
                        threadUnsafeVariables.getMusicPlayerService().play();

                        currentTrackSeekBar.setEnabled(true);
                    }
                    break;

                    case NEXT: {
                        isPlaybackFinished = false;
                        isProgramaticallyPaused = false;

                        enablePlayPauseButton(PlayPauseButtonState.PAUSE);

                        threadUnsafeVariables.getMusicPlayerService().next();

                        currentTrackSeekBar.setEnabled(true);
                    }
                    break;

                    case PREVIOUS: {
                        isPlaybackFinished = false;
                        isProgramaticallyPaused = false;

                        enablePlayPauseButton(PlayPauseButtonState.PAUSE);

                        threadUnsafeVariables.getMusicPlayerService().previous();

                        currentTrackSeekBar.setEnabled(true);
                    }
                    break;

                    case RECONNECT: {
                        logger.log("RECONNECT to service");

                        if (!threadUnsafeVariables.getMusicPlayerService().isPlaying()) {
                            processLastTrackPlayed();
                        } else {
                            isProgramaticallyPaused = false;
                            resumePlaying();
                        }
                    }
                }
            }
        }
    };

    private Position stopService() {
        logger.log("PlayActivity.stopService begin");

        Position position = null;

        synchronized (threadUnsafeVariables) {
            logger.log("PlayActivity.stopService: inside synchronized block");

            if (threadUnsafeVariables.getMusicPlayerService() != null) {
                position = threadUnsafeVariables.getMusicPlayerService().stopService(PlayActivity.this, serviceConnection);
            } else {
                logger.log("musicPlayerService is null");

                threadUnsafeVariables.setMusicPlayerService(null);
            }
        }

        logger.log("PlayActivity.stopService end");

        return position;
    }

    public void refreshTrackList(final MediaPlaybackData mediaPlaybackData, Position position) {
        logger.log(String.format(LocaleUtils.getDefaultLocale(), "PlayActivity.refreshTrackList position = %s", position));

        selectedTrack = position.getListIndex();

        if (mediaPlaybackData.getMediaList().isEmpty()) {
            Toast.makeText(this, "No tracks found", Toast.LENGTH_SHORT).show();

            finish();
        } else {
            this.mediaPlaybackData = mediaPlaybackData;

            MediaFileArrayAdapter trackArrayAdapter = new MediaFileArrayAdapter(getApplicationContext(), R.id.Title, this);
            trackListView.setAdapter(trackArrayAdapter);
            trackArrayAdapter.addAll(this.mediaPlaybackData.getMediaList());

            trackListView.setOnItemClickListener((parent, view, position1, id) -> {
                if (AudioUtils.requestAudioFocus(audioManager, audioFocusChangeProcessor, logger) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    PlayActivity.this.position = new Position(position1, 0);

                    enablePlayPauseButton(PlayPauseButtonState.PAUSE);

                    next.setEnabled(true);
                    previous.setEnabled(true);

                    startService(Action.PLAY);

                    next.setEnabled(position1 < mediaPlaybackData.getMediaList().size() - 1);
                    previous.setEnabled(position1 > 0);
                }
            });

            if (selectedTrack >= 0) {
                final Track track = mediaPlaybackData.getMediaList().get(selectedTrack);

                currentAlbum.setText(track.getAlbum());
                currentTrackArtist.setText(track.getArtist());
                currentTrackName.setText(track.getTitle());

                final int trackDuration = Integer.parseInt(track.getDuration());
                currentTrackSeekBar.setMax(trackDuration);
                currentTrackSeekBar.setProgress(position.getPositionInTrack());
                currentTrackSeekBar.setEnabled(false);
                currentTrackDuration.setText(TimeFormatter.toHHMMSS(trackDuration));
                currentTrackCurrentPosition.setText(TimeFormatter.toHHMMSS(position.getPositionInTrack()));

                trackListView.setItemChecked(selectedTrack, true);

                trackListView.smoothScrollToPosition(selectedTrack);

                this.position = this.pausePosition = position;
            }

            resumePlaying();
        }
    }
}
