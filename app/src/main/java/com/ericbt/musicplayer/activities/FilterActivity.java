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

import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.array_adapters.FilterCategoryValuesArrayAdapter;
import com.ericbt.musicplayer.async_tasks.AsyncTask;
import com.ericbt.musicplayer.async_tasks.RetrieveAllFilterCategoryValues;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterActivity extends Activity {
    private ListView itemsList;

    private String filterCategory;
    
    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger = new Logger(this);
        
        super.onCreate(savedInstanceState);

        DebugUtils.enableStrictMode(this);

        setContentView(R.layout.activity_filter);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        filterCategory = Preferences.getFilterCategory(this);

        itemsList = (ListView) findViewById(R.id.itemsList);

        itemsList.setOnItemClickListener((parent, view, position, id) -> {
            final String filterText = (String) parent.getAdapter().getItem(position);

            Preferences.putFilterData(FilterActivity.this, filterCategory, filterText);

            finish();
        });

        final Spinner filterSpinner = (Spinner) findViewById(R.id.filterSpinner);

        final String[] filterValues = getResources().getStringArray(R.array.filter_values);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String value = filterValues[position];

                logger.log(String.format("onItemSelected %s", value));

                if (value.length() == 0) {
                    FilterCategoryValuesArrayAdapter filterCategoryValuesArrayAdapter = new FilterCategoryValuesArrayAdapter(getApplicationContext(), R.id.Title);
                    itemsList.setAdapter(filterCategoryValuesArrayAdapter);
                    filterCategoryValuesArrayAdapter.addAll(new ArrayList<String>());

                    Preferences.putFilterData(FilterActivity.this, StringLiterals.EMPTY_STRING, StringLiterals.EMPTY_STRING);
                } else {
                    AsyncTask.submit(new RetrieveAllFilterCategoryValues(FilterActivity.this, value));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                logger.log("onNothingSelected");
            }
        });

        filterSpinner.setSelection(Arrays.asList(filterValues).indexOf(filterCategory));
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

    public void refresh(List<String> filterCategoryValues, String filterCategory) {
        this.filterCategory = filterCategory;

        final boolean currentCategory = filterCategory.equals(Preferences.getFilterCategory(this));

        FilterCategoryValuesArrayAdapter filterCategoryValuesArrayAdapter = new FilterCategoryValuesArrayAdapter(getApplicationContext(), filterCategoryValues, R.id.Title, currentCategory);
        itemsList.setAdapter(filterCategoryValuesArrayAdapter);
        filterCategoryValuesArrayAdapter.addAll(filterCategoryValues);

        if (currentCategory) {
            itemsList.setSelection(filterCategoryValues.indexOf(Preferences.getFilterValue(this)));
        }
    }

}
