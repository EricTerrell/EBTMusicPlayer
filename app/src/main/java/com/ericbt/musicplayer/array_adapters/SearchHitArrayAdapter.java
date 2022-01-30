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
import com.ericbt.musicplayer.music_library.SearchHit;

public class SearchHitArrayAdapter extends ArrayAdapter<SearchHit> {
    private final Context context;

    private final Activity activity;

    public SearchHitArrayAdapter(Context context, int textViewResourceId, Activity activity) {
        super(context, textViewResourceId);

        this.context = context;
        this.activity = activity;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.search_hit_item, parent, false);
        }

        final SearchHit searchHit = getItem(position);

        TextView textView = (TextView) convertView.findViewById(R.id.Title);
        textView.setText(searchHit.getText());

        final ImageView album = (ImageView) convertView.findViewById(R.id.album);
        final ImageView playlist = (ImageView) convertView.findViewById(R.id.playlist);
        final ImageView trackImageView = (ImageView) convertView.findViewById(R.id.track);

        album.setVisibility(View.GONE);
        playlist.setVisibility(View.GONE);
        trackImageView.setVisibility(View.GONE);

        switch(searchHit.getType()) {
            case ALBUM: {
                album.setVisibility(View.VISIBLE);
            }
            break;

            case PLAYLIST: {
                playlist.setVisibility(View.VISIBLE);
            }
            break;

            case TRACK: {
                trackImageView.setVisibility(View.VISIBLE);
            }
            break;
        }

        final ImageView info = (ImageView) convertView.findViewById(R.id.info);

        info.setVisibility(searchHit.getType().equals(SearchHit.SearchHitType.TRACK) ? View.VISIBLE : View.INVISIBLE);

        info.setOnClickListener(v -> {
            Intent intent = new Intent(context, MediaFileMetadataActivity.class);
            intent.putExtras(searchHit.getTrack().toBundle());
            activity.startActivity(intent);
        });

        return convertView;
    }
}

