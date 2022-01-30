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

package com.ericbt.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.ericbt.musicplayer.activities.FolderPickerActivity;
import com.ericbt.musicplayer.array_adapters.ScanFoldersArrayAdapter;

import java.util.HashSet;
import java.util.Set;

public class ScanFoldersPreference extends DialogPreference {
    private SharedPreferences sharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private ListView scanFolders;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final Button removeAll = (Button) view.findViewById(R.id.removeAll);

        removeAll.setOnClickListener(v -> {
            final Set<String> scanFolderPaths = new HashSet<>();

            final ScanFoldersArrayAdapter scanFoldersArrayAdapter = new ScanFoldersArrayAdapter(getContext(), R.id.Title);
            scanFolders.setAdapter(scanFoldersArrayAdapter);
            scanFoldersArrayAdapter.addAll(scanFolderPaths);

            Preferences.putScanFolderPaths(getContext(), scanFolderPaths);
        });

        final Button pickFolder = (Button) view.findViewById(R.id.pickFolder);

        pickFolder.setOnClickListener(v -> {
            final Intent intent = new Intent(v.getContext(), FolderPickerActivity.class);
            v.getContext().startActivity(intent);
        });

        scanFolders = (ListView) view.findViewById(R.id.scanFolders);

        updateScanFolders();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        onSharedPreferenceChangeListener = (sharedPreferences, key) -> updateScanFolders();

        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public ScanFoldersPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.scan_folders_preference);

        setNegativeButtonText(null);
    }

    private void updateScanFolders() {
        if (scanFolders != null) {
            final Set<String> scanFolderPaths = Preferences.getScanFolderPaths(getContext());

            final ScanFoldersArrayAdapter scanFoldersArrayAdapter = new ScanFoldersArrayAdapter(getContext(), R.id.Title);
            scanFolders.setAdapter(scanFoldersArrayAdapter);
            scanFoldersArrayAdapter.addAll(scanFolderPaths);
        }

    }
}
