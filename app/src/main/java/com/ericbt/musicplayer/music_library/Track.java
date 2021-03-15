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

package com.ericbt.musicplayer.music_library;

import android.os.Bundle;

import com.ericbt.musicplayer.utils.LocaleUtils;

import java.util.Date;

public class Track extends Media {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
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

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getCdTrackNumber() {
        return cdTrackNumber;
    }

    public void setCdTrackNumber(String cdTrackNumber) {
        this.cdTrackNumber = cdTrackNumber;
    }

    public String getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(String discNumber) {
        this.discNumber = discNumber;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", filePath='" + filePath + '\'' +
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
                ", compilation='" + compilation + '\'' +
                ", composer='" + composer + '\'' +
                '}';
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putString("01_Title:", title);
        bundle.putString("02_Album:", album);
        bundle.putString("03_Artist:", artist);
        bundle.putString("04_Album Artist:", albumArtist);
        bundle.putString("05_Compilation:", compilation);
        bundle.putString("06_Composer:", composer);
        bundle.putString("07_Genre:", genre);
        bundle.putString("08_Date:", date);
        bundle.putString("09_Year:", year);
        bundle.putString("10_Disc Number:", discNumber);
        bundle.putString("11_Track Number:", trackNumber);
        bundle.putString("12_CD Track Number:", cdTrackNumber);
        bundle.putString("13_File Path:", filePath);
        bundle.putString("14_Bit Rate:", bitRate);
        bundle.putString("15_Duration:", duration);
        bundle.putString("16_Time Stamp:", new Date(timeStamp).toString());
        bundle.putString("17_Size:", String.format(LocaleUtils.getDefaultLocale(), "%,d", size));

        return bundle;
    }
}