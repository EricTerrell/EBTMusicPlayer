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

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.activities.play_activity.PlayActivity;
import com.ericbt.musicplayer.array_adapters.RecentlyPlayedArrayAdapter;
import com.ericbt.musicplayer.async_tasks.AsyncTask;
import com.ericbt.musicplayer.async_tasks.RetrieveRecentlyPlayedDataTask;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedData;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.IDS;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.POSITION_IN_TRACK;
import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.SELECTED_TRACK;

public class RecentlyPlayedActivity extends Activity {
    private Logger logger;

    private ListView recentlyPlayedListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        logger = new Logger(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recently_played);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        recentlyPlayedListView = (ListView) findViewById(R.id.recentlyPlayedListView);

        recentlyPlayedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecentlyPlayedData recentlyPlayedData = (RecentlyPlayedData) parent.getAdapter().getItem(position);

                RecentlyPlayedActivity.this.finish();

                final Intent intent = new Intent(RecentlyPlayedActivity.this, PlayActivity.class);

                intent
                        .setAction(recentlyPlayedData.getAction())
                        .putExtra(SELECTED_TRACK, recentlyPlayedData.getTrackOrdinalPosition())
                        .putExtra(POSITION_IN_TRACK, recentlyPlayedData.getPositionInTrack());

                final List<String> idList = Arrays.asList(recentlyPlayedData.getIds().split(","));

                final ArrayList idArrayList = new ArrayList<>(idList.size());
                idArrayList.addAll(idList);

                intent.putStringArrayListExtra(IDS, idArrayList);

                startActivity(intent);
            }
        });

        AsyncTask.submit(new RetrieveRecentlyPlayedDataTask(getApplicationContext(), this, logger));
    }

    public void refreshListView(List<RecentlyPlayedData> listContent) {
        logger.log("RecentlyPlayedActivity.refreshListView");

        final RecentlyPlayedArrayAdapter recentlyPlayedArrayAdapter = new RecentlyPlayedArrayAdapter(getApplicationContext(), R.id.Title);
        recentlyPlayedListView.setAdapter(recentlyPlayedArrayAdapter);
        recentlyPlayedArrayAdapter.addAll(listContent);

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

}
