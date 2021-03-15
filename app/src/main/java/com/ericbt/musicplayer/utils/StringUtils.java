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

import com.ericbt.musicplayer.StringLiterals;

public class StringUtils {
    public static String trimAtFirstNull(String string) {
        final int firstNullPos = string.indexOf('\0');

        if (firstNullPos >= 0) {
            return string.substring(0, firstNullPos);
        } else {
            return string;
        }
    }

    public static String trim(String str) {
        return str != null ? str.trim() : StringLiterals.EMPTY_STRING;
    }

    public static String initialLetterUpperCase(String str) {
        str = StringUtils.trim(str);

        return (str != null & str.length() > 0) ? str.substring(0, 1).toUpperCase() + str.substring(1, str.length()) : StringLiterals.EMPTY_STRING;
    }

    public static String zeroPad(String str, int maxLength) {
        if (str == null) {
            str = "0";
        } else {
            str = extractInteger(str);

            if (str == null || str.isEmpty()) {
                str = "0";
            }
        }

        final String format = "%0" + String.valueOf(maxLength) + "d";

        return String.format(LocaleUtils.getDefaultLocale(), format, Integer.valueOf(str));
    }

    private static String extractInteger(String string) {
        final StringBuilder result = new StringBuilder();

        while (!string.isEmpty() && !Character.isDigit(string.charAt(0))) {
            string = string.substring(1);
        }

        while (!string.isEmpty() && Character.isDigit(string.charAt(0))) {
            result.append(string.charAt(0));
            string = string.substring(1);
        }

        return result.toString();
    }

    /**
     * Case-insensitive contains
     * @param string - original string
     * @param substring - substring, assumed to be upper case
     * @return true if substring found in original string
     */
    public static boolean contains(String string, String substring) {
        if (string == null || substring == null || substring.length() > string.length()) {
            return false;
        } else {
            return string.toUpperCase().contains(substring);
        }
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
