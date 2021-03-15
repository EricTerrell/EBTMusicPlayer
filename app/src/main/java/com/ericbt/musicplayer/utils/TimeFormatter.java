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

package com.ericbt.musicplayer.utils;

public class TimeFormatter {
    private final static long MS_PER_HOUR = 1000 * 60 * 60;

    public static String toHHMMSS(long milliseconds) {
        final long hours = milliseconds / MS_PER_HOUR;

        milliseconds -= hours * MS_PER_HOUR;

        final long msPerMinute = 1000 * 60;

        final long minutes = milliseconds / msPerMinute;

        milliseconds -= minutes * msPerMinute;

        final long msPerSecond = 1000;

        final long seconds = milliseconds / msPerSecond;

        return hours > 0 ?
                String.format(LocaleUtils.getDefaultLocale(), "%02d:%02d:%02d", hours, minutes, seconds) :
                String.format(LocaleUtils.getDefaultLocale(), "%02d:%02d", minutes, seconds);
    }

    public static String toDaysHHMMSS(long milliseconds) {
        final long msPerDay = 24 * MS_PER_HOUR;

        final long days = milliseconds / msPerDay;

        milliseconds -= days * msPerDay;

        final String hhmmss = toHHMMSS(milliseconds);

        return days > 0 ?
                String.format(LocaleUtils.getDefaultLocale(), "%d days %s", days, hhmmss) :
                hhmmss;
    }
}
