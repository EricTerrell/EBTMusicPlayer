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

package com.ericbt.musicplayer.array_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.music_library.MediaList;

import java.util.ArrayList;
import java.util.List;

public class FilterCategoryValuesArrayAdapter extends ArrayAdapter<String> implements SectionIndexer {
    private Context context;

    private final List<MediaList.PrefixOffset> prefixOffsets = new ArrayList<>();

    private boolean currentCategory;

    private String currentValue;

    public FilterCategoryValuesArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        this.context = context;
    }

    public FilterCategoryValuesArrayAdapter(Context context, List<String> filterCategoryValues, int textViewResourceId, boolean currentCategory) {
        this(context, textViewResourceId);

        if (!filterCategoryValues.isEmpty()) {
            prefixOffsets.add(new MediaList.PrefixOffset(filterCategoryValues.get(0).substring(0, 1).toUpperCase(), 0));

            for (int i = 1; i < filterCategoryValues.size(); i++) {
                final String previous = filterCategoryValues.get(i - 1).substring(0, 1).toUpperCase();
                final String current = filterCategoryValues.get(i).substring(0, 1).toUpperCase();

                if (!previous.equals(current)) {
                    prefixOffsets.add(new MediaList.PrefixOffset(current, i));
                }
            }
        }

        this.context = context;

        this.currentCategory = currentCategory;

        this.currentValue = Preferences.getFilterValue(context);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.filter_item, parent, false);
        }

        final String text = getItem(position);

        final TextView textView = convertView.findViewById(R.id.text);
        textView.setText(text);

        final ImageView check = convertView.findViewById(R.id.check);

        final boolean match = currentCategory && text != null && text.equals(currentValue);

        check.setVisibility(match ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    @Override
    public Object[] getSections() {
        final String[] sections = new String[prefixOffsets.size()];

        for (int i = 0; i < prefixOffsets.size(); i++) {
            sections[i] = prefixOffsets.get(i).getPrefix();
        }

        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return prefixOffsets.get(sectionIndex).getOffset();
    }

    @Override
    public int getSectionForPosition(int position) {
        int section = 0;

        for (int i = 0; i < prefixOffsets.size(); i++) {
            if (position >= prefixOffsets.get(i).getOffset()) {
                section = i;
            } else {
                break;
            }
        }

        return section;
    }
}

