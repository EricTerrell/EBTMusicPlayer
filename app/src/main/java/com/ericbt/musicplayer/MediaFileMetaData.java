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

package com.ericbt.musicplayer;

import android.media.MediaMetadataRetriever;

import java.io.File;

public class MediaFileMetaData {
    private String filePath;

    private long timeStamp;

    private long size;

    private String title;

    private String album;

    private String albumArtist;

    private String artist;

    private String bitRate;

    private String trackNumber;

    private String date;

    private String genre;

    private String cdTrackNumber;

    private String discNumber;

    private String duration;

    private String year;

    private String compilation;

    private String composer;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCompilation() {
        return compilation;
    }

    public void setCompilation(String compilation) {
        this.compilation = compilation;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(String discNumber) {
        this.discNumber = discNumber;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return (album != null && !album.isEmpty()) ? album.trim() : "Unknown";
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getBitRate() {
        return bitRate;
    }

    public void setBitRate(String bitRate) {
        this.bitRate = bitRate;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCdTrackNumber() {
        return cdTrackNumber;
    }

    public void setCdTrackNumber(String cdTrackNumber) {
        this.cdTrackNumber = cdTrackNumber;
    }

    public static MediaFileMetaData fromFile(File file) {
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        try {
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());

            MediaFileMetaData mediaFileMetaData = new MediaFileMetaData();

            mediaFileMetaData.setFilePath(file.getAbsolutePath());
            mediaFileMetaData.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            mediaFileMetaData.setAlbumArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            mediaFileMetaData.setAlbumArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
            mediaFileMetaData.setTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            mediaFileMetaData.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            mediaFileMetaData.setBitRate(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            mediaFileMetaData.setCdTrackNumber(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
            mediaFileMetaData.setDate(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
            mediaFileMetaData.setGenre(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            mediaFileMetaData.setCompilation(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION));
            mediaFileMetaData.setComposer(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER));
            mediaFileMetaData.setDuration(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            mediaFileMetaData.setDiscNumber(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER));
            mediaFileMetaData.setYear(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR));

            mediaFileMetaData.setTimeStamp(file.lastModified());
            mediaFileMetaData.setSize(file.length());

            return mediaFileMetaData;
        }
        finally {
            mediaMetadataRetriever.release();
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "MediaFileMetaData{" +
                "filePath='" + filePath + '\'' +
                ", timeStamp=" + timeStamp +
                ", size=" + size +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", albumArtist='" + albumArtist + '\'' +
                ", artist='" + artist + '\'' +
                ", bitRate='" + bitRate + '\'' +
                ", trackNumber='" + trackNumber + '\'' +
                ", date='" + date + '\'' +
                ", genre='" + genre + '\'' +
                ", cdTrackNumber='" + cdTrackNumber + '\'' +
                ", discNumber='" + discNumber + '\'' +
                ", duration='" + duration + '\'' +
                ", year='" + year + '\'' +
                ", complilation='" + compilation + '\'' +
                ", composer='" + composer + '\'' +
                '}';
    }
}
