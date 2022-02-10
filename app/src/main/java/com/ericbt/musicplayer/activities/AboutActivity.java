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

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.async_tasks.AsyncTask;
import com.ericbt.musicplayer.async_tasks.RetrieveMusicLibraryCountsTask;
import com.ericbt.musicplayer.music_library.MusicLibraryCounts;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;
import com.ericbt.musicplayer.utils.TimeFormatter;
import com.ericbt.musicplayer.utils.Version;

public class AboutActivity extends Activity {
    private LinearLayout statistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Button clearLogFile = findViewById(R.id.clearLogFile);

        clearLogFile.setOnClickListener(v -> {
            try {
                Logger.getLogFilePath(AboutActivity.this).delete();
            } catch (Throwable ex) {
                ExceptionLogger.logException(ex, AboutActivity.this);
            }
        });

        final Button licenseTerms = findViewById(R.id.licenseTerms);

        licenseTerms.setOnClickListener(v -> {
            final Intent intent = new Intent(AboutActivity.this, LicenseTermsActivity.class);
            intent.putExtra(StringLiterals.ALLOW_CANCEL, true);

            startActivity(intent);
        });

        statistics = findViewById(R.id.statistics);

        final TextView version = findViewById(R.id.version);
        version.setText(Version.getVersionName());

        final TextView folder = findViewById(R.id.folder);
        folder.setText(Logger.getLogFolder(this).toString());

        AsyncTask.submit(new RetrieveMusicLibraryCountsTask(this, this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        if (item.getItemId() == android.R.id.home) {
            finish();

            result = true;
        }

        return result;
    }

    public void updateCounts(MusicLibraryCounts musicLibraryCounts) {
        statistics.setVisibility(View.VISIBLE);

        final TextView albums = findViewById(R.id.albums);

        albums.setText(String.format(LocaleUtils.getDefaultLocale(), "Albums: %,d", musicLibraryCounts.getAlbums()));

        final TextView playLists = findViewById(R.id.playlists);

        playLists.setText(String.format(LocaleUtils.getDefaultLocale(), "Playlists: %,d", musicLibraryCounts.getPlayLists()));

        final TextView tracks = findViewById(R.id.tracks);

        tracks.setText(String.format(LocaleUtils.getDefaultLocale(), "Tracks: %,d", musicLibraryCounts.getTracks()));

        final TextView fileSize = findViewById(R.id.fileSize);

        fileSize.setText(String.format(LocaleUtils.getDefaultLocale(), "File Size: %,d", musicLibraryCounts.getFileSize()));

        final TextView duration = findViewById(R.id.duration);
        duration.setText(String.format(LocaleUtils.getDefaultLocale(), "Duration: %s", TimeFormatter.toDaysHHMMSS(musicLibraryCounts.getDuration())));
    }
}
