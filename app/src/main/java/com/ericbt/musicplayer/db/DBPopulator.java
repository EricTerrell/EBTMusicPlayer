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

package com.ericbt.musicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ericbt.musicplayer.services.scanner_service.ScannerService;
import com.ericbt.musicplayer.utils.FileUtils;
import com.ericbt.musicplayer.utils.LocaleUtils;
import com.ericbt.musicplayer.utils.M3UUtils;
import com.ericbt.musicplayer.MediaFileMetaData;
import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.exceptions.MusicPlayerException;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.utils.Logger;
import com.ericbt.musicplayer.utils.StringUtils;
import com.ericbt.musicplayer.exceptions.ScanCancelledException;
import com.ericbt.musicplayer.utils.TimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBPopulator extends DBBuilder {
    private final static String M3U = "M3U";

    private final static String MEDIA_FILE_TYPES = "MP3,FLAC,AAC,OGG";

    private final static int CAPACITY = 1000;

    private final Map<String, Long> albums = new HashMap<>(CAPACITY);
    private final PlayListContainer playlists = new PlayListContainer();

    private static final int NUMERICAL_FIELD_LENGTH = 3;

    private final Logger logger;

    public DBPopulator(ScannerService musicPlayerService, Logger logger) {
        super(musicPlayerService);

        this.logger = logger;
    }

    private class ScanProgressMessageImpl implements ScanProgressMessage {
        @Override
        public void sendScanProgressMessage(int numberOfFilesScanned) {
            final int filesProcessedPerStatusUpdate = 100;

            if (numberOfFilesScanned == 0 || numberOfFilesScanned % filesProcessedPerStatusUpdate == 0) {
                final String progressMessage = String.format(LocaleUtils.getDefaultLocale(), "Scanned %,d files", numberOfFilesScanned);
                scannerService.sendScanProgressMessage(progressMessage, 0);
            }
        }
    }

    public void scan() throws ScanCancelledException {
        final String logFilePath = String.format("%s/%s", scannerService.getExternalFilesDir(null), "ebt_music_player_scan_log.txt");

        if (Preferences.isFullLoggingActive(scannerService)) {
            new File(logFilePath).delete();

            logger.log(String.format("Logging file scan details to %s", logFilePath));
        }

        logger.log("Inserting data into database");

        final long startTime = System.currentTimeMillis();

        try (SQLiteDatabase db = DBUtils.getDatabase(scannerService.getApplicationContext(), false)) {
            final List<File> directories = getRootDirectories(scannerService);

            final Set<String> mediaFileTypes = new HashSet<>();

            for (final String fileType : MEDIA_FILE_TYPES.split(",")) {
                logger.log(String.format("Support file type %s", fileType));

                mediaFileTypes.add(fileType);
            }

            final Set<String> playListFileTypes = new HashSet<>();
            playListFileTypes.add(M3U);

            final List<File> files = new ArrayList<>();

            final Set<String> allFileTypes = new HashSet<>();
            allFileTypes.addAll(mediaFileTypes);
            allFileTypes.addAll(playListFileTypes);

            for (File directory : directories) {
                FileUtils.search(directory, allFileTypes, scannerService, files, new ScanProgressMessageImpl(), scannerService);
            }

            final List<File> playlists = new ArrayList<>(CAPACITY);
            final List<File> mediaFiles = new ArrayList<>(CAPACITY);

            final Set<String> processedFilePaths = new HashSet<>(files.size());

            for (File file : files) {
                if (!processedFilePaths.contains(file.getAbsolutePath())) {
                    processedFilePaths.add(file.getAbsolutePath());

                    final String fileType = FileUtils.getFileType(file).toUpperCase();

                    if (playListFileTypes.contains(fileType)) {
                        playlists.add(file);
                    } else if (mediaFileTypes.contains(fileType)) {
                        mediaFiles.add(file);
                    }
                }
            }

            final int totalFilesToProcess = playlists.size() + mediaFiles.size();
            final int filesProcessedPerStatusUpdate = 25;

            int filesProcessed = 0;
            for (File file : playlists) {
                try {
                    processPlayListFile(db, file);
                    updateStatusMessage(++filesProcessed, totalFilesToProcess, filesProcessedPerStatusUpdate);
                } catch (ScanCancelledException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    logger.log(ex.getMessage(), ex);
                }
            }

            playlists.clear();

            for (File file : mediaFiles) {
                try {
                    processMediaFile(db, file);
                    updateStatusMessage(++filesProcessed, totalFilesToProcess, filesProcessedPerStatusUpdate);
                } catch (ScanCancelledException ex)
                {
                    throw ex;
                }
                catch (Exception ex) {
                    logger.log(ex.getMessage(), ex);
                }
            }

            mediaFiles.clear();
        } finally {
            albums.clear();
            playlists.clear();

            final String message = String.format("Elapsed time: %s", TimeFormatter.toHHMMSS((int) (System.currentTimeMillis() - startTime)));
            scannerService.sendScanProgressMessage(message, 100);

            logger.log(message);
        }
    }

    private void updateStatusMessage(int filesProcessed, int totalFilesToProcess, int filesProcessedPerStatusUpdate) {
        if (filesProcessed % filesProcessedPerStatusUpdate == 0) {
            final int progressPercent = (filesProcessed * 100) / totalFilesToProcess;

            final String message = String.format(LocaleUtils.getDefaultLocale(), "Processed file %,d/%,d %d%%", filesProcessed, totalFilesToProcess, progressPercent);

            scannerService.sendScanProgressMessage(message, progressPercent);
        }
    }

    private List<File> getRootDirectories(Context context) {
        final List<File> rootPaths = new ArrayList<>();

        for (String filePath : Preferences.getScanFolderPaths(context)) {
            rootPaths.add(new File(filePath));
        }

        return rootPaths;
    }

    /**
     * Some devices (e.g. ZTE Blade V8 Pro) cannot extract the disc number from a media file (even when it's there).
     * In this case, substitute the playlist sequence number of the file, if it was included in a playlist.
     * @param mediaFileMetaData media file metadata
     * @param file media file
     */
    private void fixupDiscNumber(MediaFileMetaData mediaFileMetaData, File file) {
        if (StringUtils.isBlank(mediaFileMetaData.getDiscNumber())) {
            try {
                final String folderAndFilename = M3UUtils.extractFolderAndFilename(file.getAbsolutePath());

                final List<PlayListItem> playListItems = playlists.getPlayListItems(folderAndFilename);

                if (playListItems != null) {
                    for (PlayListItem playListItem : playListItems) {
                        mediaFileMetaData.setDiscNumber(String.valueOf(playListItem.getSequenceNumber()));
                    }
                }
            } catch (Throwable ex) {
                logger.log(ex.getMessage(), ex);
            }
        }
    }

    private void processMediaFile(SQLiteDatabase db, File file) throws MusicPlayerException, ScanCancelledException {
        try {
            final MediaFileMetaData mediaFileMetaData = MediaFileMetaData.fromFile(file);
            fixupDiscNumber(mediaFileMetaData, file);

            logger.log(String.format("MP3: %s%sMetadata: %s", file.getAbsolutePath(), StringLiterals.NEWLINE, mediaFileMetaData.toString()));

            final String message = String.format("MP3: path: %s album: %s", file.getAbsolutePath(), mediaFileMetaData.getAlbum());
            logger.log(message);

            insertMediaFileMetaData(db, mediaFileMetaData, file);
        } catch (ScanCancelledException ex) { throw ex; }
        catch (Exception ex) {
            final String message = String.format("Cannot extract metadata from file %s Exception: %s", file.getAbsolutePath(), ex);

            logger.log(message, ex);

            throw new MusicPlayerException(message);
        }
    }

    private long insertMediaFileMetaData(SQLiteDatabase db, MediaFileMetaData mediaFileMetaData, File file) throws MusicPlayerException, ScanCancelledException {
        final ContentValues columnValues = new ContentValues();

        columnValues.put("filePath", mediaFileMetaData.getFilePath());
        columnValues.put("timeStamp", file.lastModified());
        columnValues.put("size", file.length());
        columnValues.put("title", StringUtils.initialLetterUpperCase(mediaFileMetaData.getTitle()));
        columnValues.put("album", mediaFileMetaData.getAlbum());
        columnValues.put("albumArtist", mediaFileMetaData.getAlbumArtist());
        columnValues.put("artist", mediaFileMetaData.getArtist());
        columnValues.put("bitRate", mediaFileMetaData.getBitRate());
        columnValues.put("trackNumber", StringUtils.zeroPad(mediaFileMetaData.getTrackNumber(), NUMERICAL_FIELD_LENGTH));
        columnValues.put("date", mediaFileMetaData.getDate());
        columnValues.put("genre", mediaFileMetaData.getGenre());
        columnValues.put("cdTrackNumber", StringUtils.zeroPad(mediaFileMetaData.getCdTrackNumber(), NUMERICAL_FIELD_LENGTH));
        columnValues.put("discNumber", StringUtils.zeroPad(mediaFileMetaData.getDiscNumber(), NUMERICAL_FIELD_LENGTH));
        columnValues.put("duration", mediaFileMetaData.getDuration());
        columnValues.put("year", mediaFileMetaData.getYear());
        columnValues.put("compilation", mediaFileMetaData.getCompilation());
        columnValues.put("composer", mediaFileMetaData.getComposer());

        final long mediaFileMetaDataId = insert(db, "MediaFileMetaData", columnValues);

        if (mediaFileMetaDataId == -1) {
            final String message = String.format("Cannot insert media file info: %s", mediaFileMetaData.getFilePath());

            logger.log(message);

            throw new MusicPlayerException(message);
        }

        if (!albums.containsKey(mediaFileMetaData.getAlbum())) {
            final long albumId = insertAlbum(db, mediaFileMetaData);

            albums.put(mediaFileMetaData.getAlbum(), albumId);
        }

        Long albumId = albums.get(mediaFileMetaData.getAlbum());

        if (albumId != null) {
            insertAlbum2MediaFileMetaData(db, albumId, mediaFileMetaDataId);
        }

        final String folderAndFilename = M3UUtils.extractFolderAndFilename(mediaFileMetaData.getFilePath()).toUpperCase();

        final List<PlayListItem> playListItems = playlists.getPlayListItems(folderAndFilename);

        if (playListItems != null) {
            for (PlayListItem playListItem : playListItems) {
                final Long playListId = playListItem.getPlayListId();

                if (playListId != null) {
                    columnValues.clear();

                    columnValues.put("playListId", playListId);
                    columnValues.put("sequenceNumber", playListItem.getSequenceNumber());
                    columnValues.put("mediaFileMetaDataId", mediaFileMetaDataId);

                    final long playList2MediaFileMetaDataId = insert(db, "PlayList2MediaFileMetaData", columnValues);

                    if (playList2MediaFileMetaDataId == -1) {
                        logger.log(String.format(LocaleUtils.getDefaultLocale(), "Cannot insert PlayList2MediaFileMetaData, mediaFileMetaDataId: %d", mediaFileMetaDataId));
                    }
                }
            }
        }

        return mediaFileMetaDataId;
    }

    private long insertAlbum(SQLiteDatabase db, MediaFileMetaData mediaFileMetaData) throws ScanCancelledException, MusicPlayerException {
        if (mediaFileMetaData.getAlbum().trim().length() > 0) {
            final ContentValues columnValues = new ContentValues();

            columnValues.put("album", StringUtils.initialLetterUpperCase(mediaFileMetaData.getAlbum()));

            final long albumId = insert(db, "Album", columnValues);

            final String message = String.format("inserted album: %s", columnValues.get("album"));
            logger.log(message);

            if (albumId == -1) {
                final String exceptionMessage = String.format("Cannot insert album info: %s", mediaFileMetaData.getFilePath());

                logger.log(exceptionMessage);

                throw new MusicPlayerException(exceptionMessage);
            } else {
                albums.put(mediaFileMetaData.getAlbum(), albumId);

                return albumId;
            }
        } else {
            final String message = "Cannot insert album info: album is null or empty";

            logger.log(message);

            throw new IllegalArgumentException(message);
        }
    }

    private long insertAlbum2MediaFileMetaData(SQLiteDatabase db, long albumId, long mediaFileMetaDataId) throws ScanCancelledException, MusicPlayerException {
        final ContentValues columnValues = new ContentValues();

        columnValues.put("albumId", albumId);
        columnValues.put("mediaFileMetaDataId", mediaFileMetaDataId);

        long album2MediaFileMetaDataId = insert(db, "Album2MediaFileMetaData", columnValues);

        if (album2MediaFileMetaDataId == -1) {
            final String message = String.format(LocaleUtils.getDefaultLocale(), "Cannot insert album to media file metadata info: albumId: %d mediaFileMetaDataId: %d", albumId, mediaFileMetaDataId);

            logger.log(message);

            throw new MusicPlayerException(message);
        }

        return album2MediaFileMetaDataId;
    }

    private long processPlayListFile(SQLiteDatabase db, File file) throws ScanCancelledException, MusicPlayerException, IOException {
        final String fileContent = FileUtils.readFile(file);

        final String message = String.format("Playlist: %s Contents:\r\n%s", file.getAbsolutePath(), fileContent);
        logger.log(message);

        return insertPlaylist(db, file.getName().substring(0, file.getName().length() - M3U.length() - 1), fileContent, file);
    }

    private long insertPlaylist(SQLiteDatabase db, String fileName, String fileContent, File file) throws ScanCancelledException, MusicPlayerException {
        final ContentValues columnValues = new ContentValues();

        columnValues.put("fileName", StringUtils.initialLetterUpperCase(fileName));
        columnValues.put("timeStamp", file.lastModified());
        columnValues.put("size", file.length());

        long playListId = insert(db, "Playlist", columnValues);

        if (playListId == -1) {
            final String message = String.format("Cannot insert playlist: fileName: %s", fileName);

            logger.log(message);

            throw new MusicPlayerException(message);
        } else {
            final String[] lines = fileContent.split(System.getProperty("line.separator"));

            int sequenceNumber = 0;

            for (final String line : lines) {
                playlists.insertPlayListItem(M3UUtils.extractFolderAndFilename(line), new PlayListItem(playListId, sequenceNumber++));
            }
        }

        return playListId;
    }
}
