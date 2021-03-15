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

package com.ericbt.musicplayer.array_adapters;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.music_library.MediaList;

import java.util.ArrayList;
import java.util.List;

public class BaseArrayAdapter<T> extends ArrayAdapter<T> implements SectionIndexer {
    protected Context context;
    protected int textViewResourceId;

    private List<MediaList.PrefixOffset> prefixOffsets;

    private Object[] prefixes;

    public BaseArrayAdapter(Context context, int textViewResourceId, List<MediaList.PrefixOffset> prefixOffsets) {
        super(context, textViewResourceId);

        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.prefixOffsets = prefixOffsets;

        List<String> prefixList = new ArrayList<>();

        if (prefixOffsets != null) {
            for (MediaList.PrefixOffset prefixOffset : prefixOffsets) {
                prefixList.add(prefixOffset.getPrefix());
            }

            prefixes = prefixList.toArray();
        } else {
            prefixes = new Object[0];
        }
    }

    @Override
    public Object[] getSections() {
        return prefixes;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        int position = 0;

        if (sectionIndex < prefixOffsets.size()) {
            position = prefixOffsets.get(sectionIndex).getOffset();
        } else {
            Log.w(StringLiterals.LOG_TAG, String.format("getPositionForSection sectionIndex: %d size %d", sectionIndex, prefixOffsets.size()));
        }

        return position;
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

