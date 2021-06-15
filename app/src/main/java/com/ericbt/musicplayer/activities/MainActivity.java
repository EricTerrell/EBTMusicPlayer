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

package com.ericbt.musicplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;

import com.ericbt.musicplayer.CustomUncaughtExceptionHandler;
import com.ericbt.musicplayer.Permissions;
import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.activities.play_activity.PlayActivity;
import com.ericbt.musicplayer.async_tasks.AsyncTask;
import com.ericbt.musicplayer.async_tasks.RetrieveAllAlbumsTask;
import com.ericbt.musicplayer.async_tasks.RetrieveAllPlaylistsTask;
import com.ericbt.musicplayer.async_tasks.RetrieveAllTracksTask;
import com.ericbt.musicplayer.async_tasks.DatabaseMaintenanceTask;
import com.ericbt.musicplayer.db.DBUtils;
import com.ericbt.musicplayer.array_adapters.AlbumArrayAdapter;
import com.ericbt.musicplayer.array_adapters.PlayListArrayAdapter;
import com.ericbt.musicplayer.array_adapters.TrackArrayAdapter;
import com.ericbt.musicplayer.music_library.Album;
import com.ericbt.musicplayer.music_library.Media;
import com.ericbt.musicplayer.music_library.MediaList;
import com.ericbt.musicplayer.music_library.PlayList;
import com.ericbt.musicplayer.music_library.Track;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.Logger;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.view.View.GONE;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.IDS;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.PLAY_ALBUM;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.PLAY_PLAYLIST;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.PLAY_TRACK;

public class MainActivity extends Activity {
    private static final String ALBUMS_INDICATOR = "albums";
    private static final String PLAYLISTS_INDICATOR = "playlists";
    private static final String TRACKS_INDICATOR = "tracks";

    private static final int FILTER = 1;

    private static final int SCAN_ACTIVITY = 1000;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private ListView mediaListView;

    private TabHost tabHost;

    private TabHost.TabSpec albumsTabSpec, playListsTabSpec, tracksTabSpec;

    private Button search, clear, play, filter;

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger = new Logger(this);

        logger.log("MainActivity.onCreate");

        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        final CustomUncaughtExceptionHandler customUncaughtExceptionHandler = new CustomUncaughtExceptionHandler(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(customUncaughtExceptionHandler);

        setContentView(R.layout.activity_main);

        if (getIntent().getBooleanExtra(StringLiterals.EXIT, false)) {
            logger.log("MainActivity: exit");

            finish();
        } else {
            setupTabs();

            search = (Button) findViewById(R.id.search);

            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                }
            });

            mediaListView = (ListView) findViewById(R.id.mediaListView);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    logger = new Logger(MainActivity.this);

                    if (key.endsWith("_tab")) {
                        tabHost.clearAllTabs();
                        tabHost.setup();

                        addVisibleTabs();

                        refreshMediaListView();
                    } else if (key.equals("scroll_prefix_length")) {
                        refreshMediaListView();
                    }
                }
            };

            if (Preferences.getScanFolderPaths(this).size() == 0) {
                Preferences.putScanFolderPaths(this, Preferences.getDefaultScanFolders(this));
            }

            clear = (Button) findViewById(R.id.clear);

            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshMediaListView();
                }
            });

            play = (Button) findViewById(R.id.play);

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ArrayList<String> ids = new ArrayList<>();

                    for (int i = 0; i < mediaListView.getAdapter().getCount(); i++) {
                        Media item = (Media) mediaListView.getAdapter().getItem(i);

                        if (item.isChecked()) {
                            ids.add(String.valueOf(item.getId()));
                        }
                    }

                    if (!ids.isEmpty()) {
                        final String tag = tabHost.getCurrentTabTag();

                        switch (tag) {
                            case ALBUMS_INDICATOR: {
                                startPlayActivity(PLAY_ALBUM, ids);
                            }
                            break;

                            case PLAYLISTS_INDICATOR: {
                                startPlayActivity(PLAY_PLAYLIST, ids);
                            }
                            break;

                            case TRACKS_INDICATOR: {
                                startPlayActivity(PLAY_TRACK, ids);
                            }
                            break;
                        }
                    } else {
                        startActivity(new Intent(MainActivity.this, PlayTipsActivity.class));
                    }
                }
            });

            filter = (Button) findViewById(R.id.filter);

            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(MainActivity.this, FilterActivity.class), FILTER);
                }
            });

            sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

            maintainDatabase();

            refreshMediaListView();

            promptUserToAcceptLicenseTerms();
        }
    }

    private void setupTabs() {
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        albumsTabSpec = tabHost.newTabSpec(ALBUMS_INDICATOR);
        albumsTabSpec.setContent(R.id.tab1);
        albumsTabSpec.setIndicator("Albums");

        playListsTabSpec = tabHost.newTabSpec(PLAYLISTS_INDICATOR);
        playListsTabSpec.setIndicator("Playlists");
        playListsTabSpec.setContent(R.id.tab2);

        tracksTabSpec = tabHost.newTabSpec(TRACKS_INDICATOR);
        tracksTabSpec.setIndicator("Tracks");
        tracksTabSpec.setContent(R.id.tab3);

        addVisibleTabs();

        final int currentTab = Preferences.getCurrentTab(this);

        if (currentTab >= 0 && currentTab < tabHost.getTabWidget().getTabCount()) {
            tabHost.setCurrentTab(currentTab);
        }

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Preferences.putCurrentTab(MainActivity.this, tabHost.getCurrentTab());
                Preferences.putFirstVisibleItem(MainActivity.this, 0);

                refreshMediaListView();
            }
        });
    }

    private void addVisibleTabs() {
        logger.log("MainActivity.addVisibleTabs");

        if (Preferences.isPlaylistsTabVisible(MainActivity.this)) {
            tabHost.addTab(playListsTabSpec);
        }

        if (Preferences.isAlbumsTabVisible(MainActivity.this)) {
            tabHost.addTab(albumsTabSpec);
        }

        if (Preferences.isTracksTabVisible(MainActivity.this)) {
            tabHost.addTab(tracksTabSpec);
        }
    }

    private void promptUserToAcceptLicenseTerms() {
        logger.log("MainActivity.promptUserToAcceptLicenseTerms");

        // Prompt user to accept license terms if they have not been previously accepted.
        if (!Preferences.userAcceptedTerms(this)) {
            Intent licenseTermsIntent = new Intent(this, LicenseTermsActivity.class);
            licenseTermsIntent.putExtra(StringLiterals.ALLOW_CANCEL, false);
            startActivity(licenseTermsIntent);
        }
    }

    private void setupScanButton() {
        logger.log("MainActivity.setupScanButton");

        final Button scan = (Button) findViewById(R.id.scan);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);

                startActivityForResult(intent, SCAN_ACTIVITY);
            }
        });

        scan.setVisibility(DBUtils.databaseExists(this) ? GONE : View.VISIBLE);
    }

    private boolean userLaunchedAppFromIcon() {
        logger.log("MainActivity.userLaunchedFromIcon");

        final String action = getIntent().getAction();

        return !isTaskRoot() && action != null && action.equals(Intent.ACTION_MAIN) && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER);
    }

    @Override
    protected void onStart() {
        logger.log("MainActivity.onStart");

        super.onStart();

        /*
        See http://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes
         */
        if (userLaunchedAppFromIcon()) {
            logger.log("MainActivity: finish() because PlayActivity or ScanActivity are running");

            finish();
        }

        Permissions.requestPermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.log("MainActivity.onResume");
    }

    @Override
    protected void onDestroy() {
        logger.log("MainActivity.onDestroy start");

        if (onSharedPreferenceChangeListener != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }

        super.onDestroy();

        logger.log("MainActivity.onDestroy end");
    }

    public void enable(boolean enabled) {
        logger.log("MainActivity.enable");

        final View[] views = new View[]{
                tabHost,
                tabHost.getTabWidget(),
                mediaListView,
                search,
                clear,
                play,
                filter
        };

        for (View view : views) {
            if (view != null) {
                view.setEnabled(enabled);
            }
        }
    }

    private void maintainDatabase() {
        AsyncTask.submit(new DatabaseMaintenanceTask(this, logger));
    }

    private void processTabChange() {
        logger.log("MainActivity.processTabChange");

        if (mediaListView.isEnabled()) {
            mediaListView.setAdapter(null);

            final String tag = tabHost.getCurrentTabTag();

            if (tag != null) {
                switch (tabHost.getCurrentTabTag()) {
                    case ALBUMS_INDICATOR: {
                        enable(false);
                        AsyncTask.submit(new RetrieveAllAlbumsTask(getApplicationContext(), this));
                    }
                    break;

                    case PLAYLISTS_INDICATOR: {
                        enable(false);
                        AsyncTask.submit(new RetrieveAllPlaylistsTask(getApplicationContext(), this));
                    }
                    break;

                    case TRACKS_INDICATOR: {
                        enable(false);
                        AsyncTask.submit(new RetrieveAllTracksTask(getApplicationContext(), this));
                    }
                    break;
                }
            }
        }
    }

    public void refreshAlbums(MediaList<Album> albums) {
        logger.log("MainActivity.refreshAlbums");

        final AlbumArrayAdapter albumArrayAdapter = new AlbumArrayAdapter(getApplicationContext(), R.id.Title, albums.getPrefixOffsets());
        mediaListView.setAdapter(albumArrayAdapter);
        albumArrayAdapter.addAll(albums.getMedia());

        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> ids = new ArrayList<>();
                ids.add(String.valueOf(id));

                startPlayActivity(PLAY_ALBUM, ids);
            }
        });

        mediaListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Media item = (Media) mediaListView.getItemAtPosition(position);

                item.setChecked(!item.isChecked());

                albumArrayAdapter.notifyDataSetChanged();

                return true;
            }
        });

        recordScrollPosition();
        restoreScrollPosition();
    }

    public void refreshPlayLists(MediaList<PlayList> playLists) {
        logger.log("MainActivity.refreshPlayLists");

        final PlayListArrayAdapter playListArrayAdapter = new PlayListArrayAdapter(getApplicationContext(), R.id.Title, playLists.getPrefixOffsets());
        mediaListView.setAdapter(playListArrayAdapter);
        playListArrayAdapter.addAll(playLists.getMedia());

        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> ids = new ArrayList<>();
                ids.add(String.valueOf(id));

                startPlayActivity(PLAY_PLAYLIST, ids);
            }
        });

        mediaListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Media item = (Media) mediaListView.getItemAtPosition(position);

                item.setChecked(!item.isChecked());

                playListArrayAdapter.notifyDataSetChanged();

                return true;
            }
        });

        recordScrollPosition();
        restoreScrollPosition();
    }

    public void refreshTracks(MediaList<Track> tracks) {
        logger.log("MainActivity.refreshTracks");

        final TrackArrayAdapter trackArrayAdapter = new TrackArrayAdapter(getApplicationContext(), R.id.Title, tracks.getPrefixOffsets());
        mediaListView.setAdapter(trackArrayAdapter);
        trackArrayAdapter.addAll(tracks.getMedia());

        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> ids = new ArrayList<>();
                ids.add(String.valueOf(id));

                startPlayActivity(PLAY_TRACK, ids);
            }
        });

        mediaListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Media item = (Media) mediaListView.getItemAtPosition(position);

                item.setChecked(!item.isChecked());

                trackArrayAdapter.notifyDataSetChanged();

                return true;
            }
        });

        recordScrollPosition();
        restoreScrollPosition();
    }

    private void recordScrollPosition() {
        mediaListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Preferences.putFirstVisibleItem(MainActivity.this, mediaListView.getFirstVisiblePosition());
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // EMPTY
            }
        });
    }

    private void restoreScrollPosition() {
        final int firstVisibleItem = Preferences.getFirstVisibleItem(this);

        if (firstVisibleItem >= 0 && firstVisibleItem < mediaListView.getCount()) {
            mediaListView.setSelection(firstVisibleItem);
        }
    }

    private void startPlayActivity(String action, ArrayList<String> ids) {
        logger.log("MainActivity.startPlayActivity");

        final Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.setAction(action);
        intent.putStringArrayListExtra(IDS, ids);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.log("MainActivity.onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public void refreshMediaListView() {
        logger.log("MainActivity.refreshMediaListView");

        processTabChange();

        setupScanButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger.log("MainActivity.onOptionsItemsSelected");

        boolean result = false;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.scan: {
                startActivityForResult(new Intent(this, ScanActivity.class), SCAN_ACTIVITY);
                result = true;
            }
            break;

            case R.id.recentlyPlayed: {
                startActivity(new Intent(this, RecentlyPlayedActivity.class));
                result = true;
            }
            break;

            case R.id.settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                result = true;
            }
            break;

            case R.id.about: {
                startActivity(new Intent(this, AboutActivity.class));
                result = true;
            }
            break;
        }

        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.log("MainActivity.onActivityResult");

        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case FILTER: {
                refreshMediaListView();
            }
            break;

            case SCAN_ACTIVITY: {
                enable(true);
                refreshMediaListView();
            }
            break;
        }
    }
}
