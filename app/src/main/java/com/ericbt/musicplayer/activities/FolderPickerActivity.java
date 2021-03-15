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
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ericbt.musicplayer.Permissions;
import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.array_adapters.FolderPickerArrayAdapter;
import com.ericbt.musicplayer.async_tasks.RetrieveFoldersTask;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.ExceptionLogger;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class FolderPickerActivity extends Activity {
    private TextView currentFolder;

    private ListView folderListView;

    private Button up, selectCurrentFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_folder_picker);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        folderListView = (ListView) findViewById(R.id.folderListView);

        currentFolder = (TextView) findViewById(R.id.currentFolder);

        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                enable(false);

                final String itemText = (String) parent.getItemAtPosition(position);
                final String filePath = currentFolder.getText().toString() + itemText;

                try {
                    update(new File(filePath));

                    currentFolder.setText(filePath + StringLiterals.ROOT_PATH);
                } catch (Exception ex) {
                    ExceptionLogger.logException(ex, FolderPickerActivity.this);
                }
            }
        });

        up = (Button) findViewById(R.id.up);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(currentFolder.getText().toString());

                try {
                    update(file.getParentFile());
                    currentFolder.setText((file.getParentFile().getAbsolutePath() + StringLiterals.ROOT_PATH).replace("//", StringLiterals.ROOT_PATH));
                } catch (Exception ex) {
                    ExceptionLogger.logException(ex, FolderPickerActivity.this);
                }
            }
        });

        selectCurrentFolder = (Button) findViewById(R.id.selectCurrentFolder);

        selectCurrentFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Set<String> scanFolderPaths = Preferences.getScanFolderPaths(getApplicationContext());

                final Set<String> newSet = new HashSet<>(scanFolderPaths);
                newSet.add(currentFolder.getText().toString());

                Preferences.putScanFolderPaths(getApplicationContext(), newSet);

                finish();
            }
        });

        update(new File(currentFolder.getText().toString()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Permissions.requestReadExternalStoragePermission(this);
    }

    private void update(File folder) {
        Executors.newFixedThreadPool(1).submit(new RetrieveFoldersTask(this, folder));
    }

    public void update(File folder, List<String> subFolders) {
        final FolderPickerArrayAdapter folderPickerArrayAdapter = new FolderPickerArrayAdapter(getApplicationContext(), R.id.Title);
        folderListView.setAdapter(folderPickerArrayAdapter);
        folderPickerArrayAdapter.addAll(subFolders);

        enable(true);

        up.setEnabled(!folder.getAbsolutePath().equals(StringLiterals.ROOT_PATH));
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

    public void enable(boolean enabled) {
        up.setEnabled(enabled);
        selectCurrentFolder.setEnabled(enabled);
        folderListView.setEnabled(enabled);
    }

}
