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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericbt.musicplayer.activities.MediaFileMetadataActivity;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.music_library.Track;

public class MediaFileArrayAdapter extends ArrayAdapter<Track> {
    private final Context context;

    private final Activity activity;

    public MediaFileArrayAdapter(Context context, int textViewResourceId, Activity activity) {
        super(context, textViewResourceId);

        this.context = context;
        this.activity = activity;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.media_list_item, parent, false);
        }

        final Track mediaFileDataForPlayList = getItem(position);

        final TextView textView = (TextView) convertView.findViewById(R.id.Title);
        textView.setText(mediaFileDataForPlayList.getTitle());

        final ImageView info = (ImageView) convertView.findViewById(R.id.info);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MediaFileMetadataActivity.class);
                intent.putExtras(mediaFileDataForPlayList.toBundle());
                activity.startActivity(intent);
            }
        });

        return convertView;
    }
}

