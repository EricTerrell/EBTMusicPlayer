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

package com.ericbt.musicplayer.async_tasks;

import com.ericbt.musicplayer.activities.play_activity.PlayActivity;
import com.ericbt.musicplayer.music_library.MusicLibrary;
import com.ericbt.musicplayer.music_library.Track;
import com.ericbt.musicplayer.services.music_player_service.MediaPlaybackData;
import com.ericbt.musicplayer.services.music_player_service.Position;
import com.ericbt.musicplayer.utils.ExceptionLogger;

import java.util.List;

import static com.ericbt.musicplayer.activities.play_activity.PlayActivity.PLAY_TRACK;

public class RetrieveTracksTask implements Runnable
{
    private final PlayActivity playActivity;

    private final List<Long> trackIds;

    private final Position position;

    public RetrieveTracksTask(PlayActivity playActivity, List<Long> trackIds, Position position) {
        this.playActivity = playActivity;
        this.trackIds = trackIds;
        this.position = position;
    }

    @Override
    public void run() {
        try {
            final MediaPlaybackData mediaPlaybackData = new MediaPlaybackData();
            mediaPlaybackData.setAction(PLAY_TRACK);
            mediaPlaybackData.setIds(trackIds);

            final List<Track> trackList = MusicLibrary.retrieveTracks(playActivity, trackIds);
            mediaPlaybackData.setMediaList(trackList);

            if (position.getListIndex() < 0) {
                final Position savedPosition = MusicLibrary.getSavedPosition(playActivity, trackIds);

                position.setListIndex(savedPosition.getListIndex());
                position.setPositionInTrack(savedPosition.getPositionInTrack());
            }

            playActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Refresh list
                    playActivity.refreshTrackList(mediaPlaybackData, position);
                }
            });
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, playActivity);
        }
    }
}
