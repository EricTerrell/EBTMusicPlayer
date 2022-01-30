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
import android.widget.ImageView;
import android.widget.TextView;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.music_library.Album;
import com.ericbt.musicplayer.music_library.MediaList;

import java.util.List;

public class AlbumArrayAdapter extends BaseArrayAdapter<Album> {
    public AlbumArrayAdapter(Context context, int textViewResourceId, List<MediaList.PrefixOffset> prefixOffsets) {
        super(context, textViewResourceId, prefixOffsets);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.library_item, parent, false);
        }

        final Album album = getItem(position);

        TextView textView = (TextView) convertView.findViewById(R.id.Title);
        textView.setText(album.getAlbum());

        ImageView check = (ImageView) convertView.findViewById(R.id.check);

        check.setVisibility(album.isChecked() ? View.VISIBLE : View.GONE);

        return convertView;
    }
}

