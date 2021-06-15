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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedData;

public class RecentlyPlayedArrayAdapter extends ArrayAdapter<RecentlyPlayedData> {
    private final Context context;

    public RecentlyPlayedArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        this.context = context;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.recently_played_item, parent, false);
        }

        final RecentlyPlayedData recentlyPlayedData = getItem(position);

        final TextView albumAndTrack = (TextView) convertView.findViewById(R.id.AlbumAndTrack);
        albumAndTrack.setText(String.format("%s/%s", recentlyPlayedData.getAlbum(), recentlyPlayedData.getCurrentTrackName()));

        return convertView;
    }
}

