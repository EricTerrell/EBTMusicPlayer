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

import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.utils.DebugUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaFileMetadataActivity extends Activity {
    public static final String METADATA_BUNDLE = "METADATA_BUNDLE";
    private static final int PREFIX_LENGTH = 2;

    private static class NameValuePair {
        private String name;

        private String value;

        public NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    private static class NameValueComparitor implements Comparator<NameValuePair> {
        private Collator collator = Collator.getInstance();

        @Override
        public int compare(NameValuePair nameValuePair1, NameValuePair nameValuePair2) {
            return nameValuePair1.getName().substring(0, PREFIX_LENGTH + 1).compareTo(nameValuePair2.getName().substring(0, PREFIX_LENGTH + 1));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_file_metadata);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final TableLayout tableLayout = (TableLayout) findViewById(R.id.table);

        final Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            for (String key : bundle.keySet()) {
                String value = (String) bundle.get(key);

                nameValuePairs.add(new NameValuePair(key, value));
            }

            Collections.sort(nameValuePairs, new NameValueComparitor());

            for (NameValuePair nameValuePair : nameValuePairs) {
                TableRow tableRow = new TableRow(this);

                TextView textView = new TextView(this);
                textView.setText(nameValuePair.getName().substring(PREFIX_LENGTH + 1));
                tableRow.addView(textView);

                TextView margin = new TextView(this);
                margin.setWidth(20);
                tableRow.addView(margin);

                textView = new TextView(this);
                textView.setSingleLine(false);
                textView.setText(nameValuePair.getValue());

                tableRow.addView(textView);

                TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams();
                layoutParams.setMargins(5, 5, 5, 5);
                tableRow.setLayoutParams(layoutParams);

                tableLayout.addView(tableRow);
            }
        }
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
