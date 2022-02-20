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

package com.ericbt.musicplayer.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.broadcast_receivers.CustomBroadcastReceiver;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.services.scanner_service.ScannerService;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.NavigationUtils;

public class ScanActivity extends Activity {
    private ScannerService scannerService;

    private Button scanButton, cancelButton, settingsButton;

    private TextView statusText;

    private CustomBroadcastReceiver scanBroadcastReceiver;

    private ProgressBar progressBar;

    private Drawable background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        startService();

        progressBar = findViewById(R.id.progressBar);

        settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(v -> startActivity(new Intent(ScanActivity.this, SettingsActivity.class)));

        scanButton = findViewById(R.id.scan);

        scanButton.setOnClickListener(v -> {
            scanButton.setEnabled(false);
            cancelButton.setEnabled(true);
            settingsButton.setEnabled(false);
            progressBar.setEnabled(true);

            scannerService.scan(ScanActivity.this);
        });

        cancelButton = findViewById(R.id.cancel);

        cancelButton.setOnClickListener(v -> {
            cancelButton.setEnabled(false);
            progressBar.setEnabled(false);
            scannerService.requestScanCancellation();
        });

        statusText = findViewById(R.id.statusText);
        background = statusText.getBackground();

        restoreStatusMessage();
    }

    private void setStatusText(String text, boolean error) {
        if (error) {
            statusText.setBackgroundColor(Color.RED);
        } else {
            statusText.setBackground(background);
        }

        statusText.setText(text);
    }

    private void restoreStatusMessage() {
        final String message = Preferences.scanStatus(this);

        setStatusText(message, message.contains(getString(R.string.scan_exception)));

        final boolean isFinished = isFinished(message);

        progressBar.setProgress(0);

        cancelButton.setEnabled(!isFinished);
        scanButton.setEnabled(isFinished);
        settingsButton.setEnabled(isFinished);
    }

    private boolean isFinished(String statusText) {
        return (statusText == null || statusText.trim().length() == 0) ||
                statusText.equals(getString(R.string.scan_cancelled)) ||
                statusText.equals(getString(R.string.scan_complete)) ||
                statusText.contains(getString(R.string.scan_exception));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!isScanInProgress()) {
            scannerService.stopService(this, serviceConnection);
        }

        try {
            unbindService(serviceConnection);
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, this);
        }
    }

    public void updateUIWhenScanCompleteOrCancelled(String message, boolean error) {
        setStatusText(message, error);

        scanButton.setEnabled(true);
        cancelButton.setEnabled(false);
        settingsButton.setEnabled(true);
    }

    public void updateUIWhenScanCompleteOrCancelled(String message) {
        updateUIWhenScanCompleteOrCancelled(message, false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        scanBroadcastReceiver = new CustomBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case CustomBroadcastReceiver.SCAN_PROGRESS_MESSAGE: {
                        setStatusText(intent.getStringExtra(CustomBroadcastReceiver.MESSAGE), false);

                        final int progressPercent = intent.getIntExtra(CustomBroadcastReceiver.PROGRESS_PERCENT, 0);

                        progressBar.setProgress(progressPercent);

                        scanButton.setEnabled(false);
                        cancelButton.setEnabled(true);
                        settingsButton.setEnabled(false);
                    }
                    break;

                    case CustomBroadcastReceiver.SCAN_COMPLETE: {
                        updateUIWhenScanCompleteOrCancelled(ScanActivity.this.getString(R.string.scan_complete));
                    }
                    break;

                    case CustomBroadcastReceiver.SCAN_CANCELLED: {
                        updateUIWhenScanCompleteOrCancelled(ScanActivity.this.getString(R.string.scan_cancelled));
                    }
                    break;
                }
            }
        };

        final IntentFilter intentFilter = new IntentFilter(CustomBroadcastReceiver.SCAN_PROGRESS_MESSAGE);
        intentFilter.addAction(CustomBroadcastReceiver.SCAN_COMPLETE);
        intentFilter.addAction(CustomBroadcastReceiver.SCAN_CANCELLED);

        registerReceiver(scanBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(scanBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        progressBar.setProgress(0);

        restoreStatusMessage();
    }

    private boolean isScanInProgress() {
        return scannerService != null && scannerService.isScanning();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        if (item.getItemId() == android.R.id.home && !isScanInProgress()) {
            NavigationUtils.goBackTo(this, MainActivity.class);

            result = true;
        }

        return result;
    }

    @Override
    public void onBackPressed() {
        if (!scannerService.isScanning()) {
            super.onBackPressed();

            NavigationUtils.goBackTo(this, MainActivity.class);
        }
    }

    private void startService() {
        final Intent intent = new Intent(getApplicationContext(), ScannerService.class);

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startService(intent);
        } else {
            startForegroundService(intent);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScannerService.ScannerServiceBinder scannerServiceBinder = (ScannerService.ScannerServiceBinder) service;
            scannerService = scannerServiceBinder.getService();

            scannerService.startForeground();

            cancelButton.setEnabled(isScanInProgress());
            settingsButton.setEnabled(!isScanInProgress());
            scanButton.setEnabled(!isScanInProgress());
        }
    };
}
