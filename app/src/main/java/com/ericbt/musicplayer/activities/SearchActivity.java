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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.activities.play_activity.PlayActivity;
import com.ericbt.musicplayer.async_tasks.AsyncTask;
import com.ericbt.musicplayer.async_tasks.SearchTask;
import com.ericbt.musicplayer.array_adapters.SearchHitArrayAdapter;
import com.ericbt.musicplayer.music_library.SearchHit;
import com.ericbt.musicplayer.utils.DebugUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.IDS;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.PLAY_ALBUM;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.PLAY_PLAYLIST;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.PLAY_TRACK;

public class SearchActivity extends Activity {
    private static final String SEARCH_IN_PROGRESS = "SEARCH_IN_PROGRESS";

    private Button search;

    private EditText searchText;

    private ListView searchHitsListView;

    private boolean isSearchInProgress = false;

    private TextView noResultsFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        searchText = (EditText) findViewById(R.id.searchText);

        noResultsFound = (TextView) findViewById(R.id.noResultsFound);

        search = (Button) findViewById(R.id.search);

        search.setOnClickListener(v -> {
            isSearchInProgress = true;

            final SearchHitArrayAdapter searchHitArrayAdapter = new SearchHitArrayAdapter(getApplicationContext(), R.id.Title, SearchActivity.this);
            searchHitsListView.setAdapter(searchHitArrayAdapter);
            searchHitArrayAdapter.addAll(new ArrayList<SearchHit>());

            enable(false);

            AsyncTask.submit(new SearchTask(SearchActivity.this, SearchActivity.this, searchText.getText().toString().trim().toUpperCase()));
        });

        search.setEnabled(!searchText.getEditableText().toString().trim().isEmpty());

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search.setEnabled(!s.toString().trim().isEmpty());
            }
        });
        searchHitsListView = (ListView) findViewById(R.id.searchHitsListView);

        searchHitsListView.setOnItemClickListener((parent, view, position, id) -> {
            SearchHit searchHit = (SearchHit) parent.getItemAtPosition(position);

            ArrayList<String> ids = new ArrayList<>();
            ids.add(String.valueOf(searchHit.getId()));

            switch(searchHit.getType()) {
                case ALBUM: {
                    final Intent intent = new Intent(SearchActivity.this, PlayActivity.class);
                    intent.setAction(PLAY_ALBUM);
                    intent.putExtra(IDS, ids);
                    startActivity(intent);
                }
                break;

                case PLAYLIST: {
                    final Intent intent = new Intent(SearchActivity.this, PlayActivity.class);
                    intent.setAction(PLAY_PLAYLIST);
                    intent.putExtra(IDS, ids);
                    startActivity(intent);
                }
                break;

                case TRACK: {
                    final Intent intent = new Intent(SearchActivity.this, PlayActivity.class);
                    intent.setAction(PLAY_TRACK);
                    intent.putExtra(IDS, ids);
                    startActivity(intent);
                }
                break;
            }
        });

        if (savedInstanceState != null) {
            isSearchInProgress = savedInstanceState.getBoolean(SEARCH_IN_PROGRESS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SEARCH_IN_PROGRESS, isSearchInProgress);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        if (item.getItemId() == android.R.id.home && !isSearchInProgress) {
            finish();

            result = true;
        }

        return result;
    }

    @Override
    public void onBackPressed() {
        if (!isSearchInProgress) {
            super.onBackPressed();
        }
    }

    public void enable(boolean enabled) {
        searchHitsListView.setEnabled(enabled);
        search.setEnabled(enabled);
        searchText.setEnabled(enabled);

        if (enabled) {
            isSearchInProgress = false;
        }
    }

    public void refreshSearchHits(List<SearchHit> searchHits) {
        final SearchHitArrayAdapter searchHitArrayAdapter = new SearchHitArrayAdapter(getApplicationContext(), R.id.Title, this);
        searchHitsListView.setAdapter(searchHitArrayAdapter);
        searchHitArrayAdapter.addAll(searchHits);

        noResultsFound.setVisibility(searchHits.isEmpty() ? View.VISIBLE : View.GONE);
    }

}
