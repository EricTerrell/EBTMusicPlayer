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

package com.ericbt.musicplayer.music_library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedData;
import com.ericbt.musicplayer.recently_played.RecentlyPlayedManager;
import com.ericbt.musicplayer.services.music_player_service.Position;
import com.ericbt.musicplayer.utils.ExceptionLogger;
import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.Logger;
import com.ericbt.musicplayer.utils.StringUtils;
import com.ericbt.musicplayer.db.DBUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ericbt.musicplayer.async_tasks.RetrieveRecentlyPlayedDataTask.ROWS;

public class MusicLibrary {
    private final static int CAPACITY = 1000;

    private final static int FILTER_ITEM_CAPACITY = 100;

    public static MusicLibraryCounts retrieveCounts(Context context) {
        MusicLibraryCounts musicLibraryCounts = new MusicLibraryCounts();

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String unionAll = "UNION ALL ";

            final String query =
                    "SELECT COUNT(*) FROM PlayList " +
                    unionAll +
                    "SELECT COUNT(*) FROM Album " +
                    unionAll +
                    "SELECT COUNT(*) FROM MediaFileMetaData " +
                    unionAll +
                    "SELECT SUM(duration) FROM MediaFileMetaData "
                    + unionAll +
                    "SELECT SUM(size) FROM MediaFileMetaData;";

            try (Cursor cursor = db.rawQuery(query, new String[]{})) {
                cursor.moveToNext();

                musicLibraryCounts.setPlayLists(cursor.getLong(0));
                cursor.moveToNext();

                musicLibraryCounts.setAlbums(cursor.getLong(0));
                cursor.moveToNext();

                musicLibraryCounts.setTracks(cursor.getLong(0));
                cursor.moveToNext();

                musicLibraryCounts.setDuration(cursor.getLong(0));
                cursor.moveToNext();

                musicLibraryCounts.setFileSize(cursor.getLong(0));
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);

            throw ex;
        }

        return musicLibraryCounts;
    }

    public static List<SearchHit> retrieveSearchHits(Context context, String searchText) {
        final List<SearchHit> searchHits = new ArrayList<>(CAPACITY);

        // Want to GC albums as soon as possible.
        {
            final List<Album> albums = MusicLibrary.retrieveAlbums(context, true).getMedia();

            for (Album album : albums) {
                if (StringUtils.contains(album.getAlbum(), searchText)) {
                    searchHits.add(new SearchHit(SearchHit.SearchHitType.ALBUM, album.getAlbum(), album.getId()));
                }
            }
        }

        // Want to GC playLists as soon as possible.
        {
            final List<PlayList> playLists = MusicLibrary.retrievePlayLists(context, true).getMedia();

            for (PlayList playList : playLists) {
                if (StringUtils.contains(playList.getFileName(), searchText)) {
                    searchHits.add(new SearchHit(SearchHit.SearchHitType.PLAYLIST, playList.getFileName(), playList.getId()));
                }
            }
        }

        // Want to GC tracks as soon as possible.
        {
            final List<Track> tracks = MusicLibrary.retrieveTracks(context, true).getMedia();

            for (Track track : tracks) {
                if (StringUtils.contains(track.getAlbumArtist(), searchText) ||
                    StringUtils.contains(track.getAlbum(), searchText) ||
                    StringUtils.contains(track.getTitle(), searchText) ||
                    StringUtils.contains(track.getCompilation(), searchText) ||
                    StringUtils.contains(track.getComposer(), searchText) ||
                    StringUtils.contains(track.getFilePath(), searchText) ||
                    StringUtils.contains(track.getYear(), searchText)) {
                    searchHits.add(new SearchHit(SearchHit.SearchHitType.TRACK, track.getTitle(), track.getId(), track));
                }
            }
        }

        Collections.sort(searchHits, new SearchHit.SearchHitComparitor());

        return searchHits;
    }

    private static String getFilteredSubquery(Context context, String queryFormat) {
        String subquery;

        final String filterCategory = Preferences.getFilterCategory(context);

        if (StringUtils.isBlank(filterCategory)) {
            subquery = StringLiterals.EMPTY_STRING;
        } else {
            subquery = String.format(queryFormat, filterCategory, DBUtils.escape(Preferences.getFilterValue(context)));
        }

        return subquery;
    }

    private static String getPlayListsQuery(Context context, boolean retrieveAll) {
        final String subQueryFormat =
                "WHERE EXISTS " +
                        "( " +
                        "  SELECT PL2.id " +
                        "  FROM PlayList2MediaFileMetaData PL2 " +
                        "  INNER JOIN MediaFileMetaData MFMD ON MFMD.id = PL2.mediaFileMetaDataId " +
                        "  WHERE PL2.playListId = PL.id " +
                        "  AND MFMD.%s = '%s' " +
                        "  LIMIT 1 " +
                        ") ";

        final String subQuery = retrieveAll ? "" : getFilteredSubquery(context, subQueryFormat);

        final String queryFormat =
                "SELECT PL.id, PL.fileName " +
                "FROM PlayList PL " +
                "%s " +
                "ORDER BY fileName COLLATE NOCASE;";

        return String.format(queryFormat, subQuery);
    }

    public static MediaList<PlayList> retrievePlayLists(Context context, boolean retrieveAll) {
        final Logger logger = new Logger(context);

        final MediaList<PlayList> mediaList = new MediaList<>();

        final List<PlayList> playLists = new ArrayList<>(CAPACITY);
        mediaList.setMedia(playLists);

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String query = getPlayListsQuery(context, retrieveAll);

            try (Cursor cursor = db.rawQuery(query, new String[] { })) {
                while (cursor.moveToNext()) {
                    PlayList playList = new PlayList();

                    playList.setId(cursor.getLong(0));
                    playList.setFileName(StringUtils.trim(cursor.getString(1)));

                    if (playList.getFileName().length() > 0) {
                        final String prefix = playList.getFileName().substring(0, Math.min(playList.getFileName().length(), Preferences.getScrollPrefixLength(context)));

                        mediaList.updateOffsets(prefix);

                        playLists.add(playList);
                    }
                }
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);

            throw ex;
        }

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "retrievePlayLists retrieved %d items", mediaList.getMedia().size()));

        return mediaList;
    }

    private static String getAlbumsQuery(Context context, boolean retrieveAll) {
        final String subQueryFormat =
                "WHERE EXISTS " +
                        "( " +
                        "  SELECT A2.id " +
                        "  FROM Album2MediaFileMetaData A2 " +
                        "  INNER JOIN MediaFileMetaData MFMD ON MFMD.id = A2.mediaFileMetaDataId " +
                        "  WHERE A2.albumId = A.id " +
                        "  AND MFMD.%s = '%s' " +
                        "  LIMIT 1 " +
                        ") ";

        final String subQuery = retrieveAll ? "" : getFilteredSubquery(context, subQueryFormat);

        final String queryFormat =
                "SELECT A.id, A.album " +
                "FROM Album A " +
                "%s " +
                "ORDER BY A.album COLLATE NOCASE";

        return String.format(queryFormat, subQuery);
    }

    public static MediaList<Album> retrieveAlbums(Context context, boolean retrieveAll) {
        final Logger logger = new Logger(context);

        final MediaList<Album> mediaList = new MediaList<>();

        final List<Album> albums = new ArrayList<>(CAPACITY);
        mediaList.setMedia(albums);

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String query = getAlbumsQuery(context, retrieveAll);

            try (Cursor cursor = db.rawQuery(query, new String[] {})) {
                while (cursor.moveToNext()) {
                    Album album = new Album();

                    album.setId(cursor.getLong(0));
                    album.setAlbum(cursor.getString(1));

                    if (album.getAlbum().length() > 0) {
                        final String prefix = album.getAlbum().substring(0, Math.min(album.getAlbum().length(), Preferences.getScrollPrefixLength(context)));

                        mediaList.updateOffsets(prefix);

                        albums.add(album);
                    }
                }
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);

            throw ex;
        }

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "retrieveAlbums retrieved %d items", mediaList.getMedia().size()));

        return mediaList;
    }

    private static String getTracksQuery(Context context, boolean retrieveAll) {
        final String subQuery = retrieveAll ? "" : getFilteredSubquery(context, "WHERE M.%s = '%s' ");

        return
                "SELECT M.id, M.filePath, M.timeStamp, M.size, M.title, M.album, M.albumArtist, M.artist, M.bitRate, M.trackNumber, M.date, M.genre, M.cdTrackNumber, " +
                "M.discNumber, M.duration, M.year, M.compilation, M.composer " +
                "FROM MediaFileMetaData M " +
                subQuery +
                "ORDER BY M.title COLLATE NOCASE, M.album COLLATE NOCASE, CAST(M.discNumber AS INT), CAST(M.trackNumber AS INT), CAST(M.cdTrackNumber AS INT);";
    }

    public static MediaList<Track> retrieveTracks(Context context, boolean retrieveAll) {
        final Logger logger = new Logger(context);

        final MediaList<Track> mediaList = new MediaList<>();

        final List<Track> tracks = new ArrayList<>(CAPACITY);
        mediaList.setMedia(tracks);

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String query = getTracksQuery(context, retrieveAll);

            try (Cursor cursor = db.rawQuery(query, new String[] {})) {
                while (cursor.moveToNext()) {
                    Track track = retrieveTracks(cursor);

                    if (track.getTitle().length() > 0) {
                        final String prefix = track.getTitle().substring(0, Math.min(track.getTitle().length(), Preferences.getScrollPrefixLength(context)));

                        mediaList.updateOffsets(prefix);

                        tracks.add(track);
                    }
                }
            }
        } catch (Exception ex) {
        ExceptionLogger.logException(ex, context);

        throw ex;
        }

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "retrieveTracks retrieved %d items", mediaList.getMedia().size()));

        return mediaList;
    }

    private static Track retrieveTracks(Cursor cursor) {
        Track track = new Track();

        track.setId(cursor.getLong(0));
        track.setFilePath(cursor.getString(1));
        track.setTimeStamp(cursor.getLong(2));
        track.setSize(cursor.getLong(3));
        track.setTitle(cursor.getString(4));
        track.setAlbum(cursor.getString(5));
        track.setAlbumArtist(cursor.getString(6));
        track.setArtist(cursor.getString(7));
        track.setBitRate(cursor.getString(8));
        track.setTrackNumber(cursor.getString(9));
        track.setDate(cursor.getString(10));
        track.setGenre(cursor.getString(11));
        track.setCdTrackNumber(cursor.getString(12));
        track.setDiscNumber(cursor.getString(13));
        track.setDuration(cursor.getString(14));
        track.setYear(cursor.getString(15));
        track.setCompilation(cursor.getString(16));
        track.setComposer(cursor.getString(17));

        return track;
    }

    public static List<Track> retrieveMediaFileDataForPlayLists(Context context, List<Long> playListIds) {
        final String playListIdsText = TextUtils.join(",", playListIds);

        final List<Track> results = new ArrayList<>(CAPACITY);

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String query =
                    String.format(
                    "SELECT M.id, M.filePath, M.timeStamp, M.size, M.title, M.album, M.albumArtist, M.artist, M.bitRate, M.trackNumber, M.date, M.genre, M.cdTrackNumber, M.discNumber, M.duration, M.year, M.compilation, M.composer " +
                            "FROM PlayList2MediaFileMetaData P " +
                            "INNER JOIN MediaFileMetaData M ON M.id = P.mediaFileMetaDataId " +
                            "INNER JOIN PlayList PL ON PL.id = P.playListId " +
                            "WHERE P.playListId IN (%s) " +
                            "ORDER BY PL.fileName COLLATE NOCASE, P.sequenceNumber;", playListIdsText);

            try (Cursor cursor = db.rawQuery(query, new String[] { })) {
                while (cursor.moveToNext()) {
                    results.add(retrieveTracks(cursor));
                }
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);

            throw ex;
        }

        return results;
    }

    public static Position getSavedPosition(Context context, List<Long> ids) {
        final Logger logger = new Logger(context);

        final String idsText = TextUtils.join(",", ids);

        logger.log(String.format("MusicLibrary.getSavedPosition ids: %s", idsText));

        final List<RecentlyPlayedData> results = new RecentlyPlayedManager(context, logger).getRecentlyPlayedData(null, ROWS);

        logger.log(String.format(LocaleUtils.getDefaultLocale(), "Retrieved %d results", results.size()));

        final Position position = new Position(0, 0);

        for (RecentlyPlayedData recentlyPlayedData : results) {
            if (recentlyPlayedData.getIds().equals(idsText)) {
                position.setListIndex(recentlyPlayedData.getTrackOrdinalPosition());
                position.setPositionInTrack(recentlyPlayedData.getPositionInTrack());

                logger.log(String.format("MusicLibrary.getSavedPosition: setting position to %s", position));

                break;
            }
        }

        return position;
    }

    public static List<Track> retrieveTracks(Context context, List<Long> trackIds) {
        final List<Track> results = new ArrayList<>(CAPACITY);

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String trackIdsText = TextUtils.join(",", trackIds);

            final String query =
                    String.format(
                        "SELECT M.id, M.filePath, M.timeStamp, M.size, M.title, M.album, M.albumArtist, M.artist, M.bitRate, M.trackNumber, M.date, M.genre, M.cdTrackNumber, M.discNumber, M.duration, M.year, M.compilation, M.composer " +
                        "FROM MediaFileMetaData M " +
                        "WHERE M.id IN(%s) " +
                        "ORDER BY M.discNumber, M.trackNumber, M.cdTrackNumber;", trackIdsText);

            try (Cursor cursor = db.rawQuery(query, new String[] { })) {
                while (cursor.moveToNext()) {
                    results.add(retrieveTracks(cursor));
                }
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);

            throw ex;
        }

        return results;
    }

    public static List<Track> retrieveTracksForAlbums(Context context, List<Long> albumIds) {
        final List<Track> results = new ArrayList<>(CAPACITY);

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String albumIdsString = TextUtils.join(",", albumIds);

            final String query =
                    String.format(
                            "SELECT  M.id, M.filePath, M.timeStamp, M.size, M.title, M.album, M.albumArtist, M.artist, M.bitRate, M.trackNumber, M.date, M.genre, M.cdTrackNumber, M.discNumber, M.duration, M.year, M.compilation, M.composer " +
                            "FROM Album2MediaFileMetaData A2MFMD " +
                            "INNER JOIN MediaFileMetaData M ON M.id = A2MFMD.mediaFileMetaDataId " +
                            "INNER JOIN Album A ON A.id = A2MFMD.albumId " +
                            "WHERE A.id IN(%s) " +
                            "ORDER BY A.album, CAST(M.discNumber AS INT), CAST (M.trackNumber AS INT), CAST(M.cdTrackNumber AS INT);", albumIdsString);

            try (Cursor cursor = db.rawQuery(query, new String[] { })) {
                while (cursor.moveToNext()) {
                    results.add(retrieveTracks(cursor));
                }
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);

            throw ex;
        }

        return results;
    }

    public static List<String> retrieveAllFilterCategoryValues(Context context, String column) {
        final List<String> results = new ArrayList<>(FILTER_ITEM_CAPACITY);

        try (SQLiteDatabase db = DBUtils.getDatabase(context)) {
            final String query =
                    String.format(
                            "SELECT DISTINCT MFMD.%s " +
                            "FROM MediaFileMetaData MFMD " +
                            "WHERE MFMD.%s IS NOT NULL AND MFMD.%s <> '' " +
                            "ORDER BY MFMD.%s COLLATE NOCASE;", column, column, column, column);

            try (Cursor cursor = db.rawQuery(query, new String[] { })) {
                while (cursor.moveToNext()) {
                    final String filterItem = cursor.getString(0);

                    if (!StringUtils.isBlank(filterItem)) {
                        results.add(filterItem.trim());
                    }
                }
            }
        } catch (Exception ex) {
            ExceptionLogger.logException(ex, context);

            throw ex;
        }

        return results;
    }

}