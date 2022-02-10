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

package com.ericbt.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preferences {
	private static final String SCAN_FOLDERS_KEY             = "SCAN_FOLDERS_KEY";
	private static final String USER_ACCEPTED_TERMS_KEY      = "UserAcceptedTerms";
	private static final String COPY_DATABASE_KEY            = "copy_database_key";
	private static final String FILTER_CATEGORY              = "filter_category_key";
	private static final String FILTER_VALUE                 = "filter_value_key";
	private static final String CURRENT_TAB_KEY              = "CURRENT_TAB_KEY";
    private static final String FIRST_VISIBLE_ITEM_KEY       = "FIRST_VISIBLE_ITEM_KEY";
    private static final String SCAN_STATUS_KEY              = "SCAN_STATUS_KEY";

	private static final Pattern SDCARD_PATH_REGEX = Pattern.compile("/storage/\\S\\S\\S\\S-\\S\\S\\S\\S/");

	public static Set<String> getDefaultScanFolders(Context context) {
		final Set<String> results = new HashSet<>();

		final String sdCardPath = getSDCardPath(context);

		if (sdCardPath != null) {
			results.add(sdCardPath);
		}

		results.add(Environment.getExternalStorageDirectory().getAbsolutePath());
		results.add(Environment.getDataDirectory().getAbsolutePath());
		results.add(Environment.getDownloadCacheDirectory().getAbsolutePath());
		results.add(Environment.getRootDirectory().getAbsolutePath());
		results.add(Environment.getDownloadCacheDirectory().getAbsolutePath());

		final String topLevelStorageFolderName = getRootExternalStorageFolder();

		if (topLevelStorageFolderName != null) {
			results.add(topLevelStorageFolderName);
		}

		final String [] folderNames = new String[] {
				Environment.DIRECTORY_DOCUMENTS,
				Environment.DIRECTORY_MUSIC,
				Environment.DIRECTORY_PODCASTS,
				Environment.DIRECTORY_DOWNLOADS,
				Environment.DIRECTORY_MOVIES,
				Environment.DIRECTORY_PICTURES,
				Environment.DIRECTORY_ALARMS,
				Environment.DIRECTORY_NOTIFICATIONS,
				Environment.DIRECTORY_DCIM,
				Environment.DIRECTORY_RINGTONES
		};

		for (final String folderName : folderNames) {
			results.add(Environment.getExternalStoragePublicDirectory(folderName).toString());
		}

		return results;
	}

	private static String getSDCardPath(Context context) {
		String path = null;

		for (File mediaDir : context.getExternalMediaDirs()) {
			final Matcher matcher = SDCARD_PATH_REGEX.matcher(mediaDir.getAbsolutePath());

			if (matcher.find()) {
				path = matcher.group(0);
			}
		}

		return path;
	}

	private static String getRootExternalStorageFolder() {
		String result = null;

		String topLevelStorageFolder = Environment.getExternalStorageDirectory().getAbsolutePath().substring(1);

		int endOfFolderName = topLevelStorageFolder.indexOf("/");

		if (endOfFolderName >= 0) {
			result = "/" + topLevelStorageFolder.substring(0, endOfFolderName);
		}

		return result;
	}

	public static Set<String> getScanFolderPaths(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getStringSet(SCAN_FOLDERS_KEY, new HashSet<String>());
	}

	public static void putScanFolderPaths(Context context, Set<String> scanFolderPaths) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putStringSet(SCAN_FOLDERS_KEY, scanFolderPaths);

		editor.apply();
	}

	public static int getScrollPrefixLength(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return Integer.parseInt(sharedPreferences.getString("scroll_prefix_length", "2"));
	}

	public static boolean isAlbumsTabVisible(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("albums_tab", true);
	}

	public static boolean isPlaylistsTabVisible(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("playlists_tab", true);
	}

	public static boolean isTracksTabVisible(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("tracks_tab", true);
	}

	public static boolean isExceptionLoggingActive(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("log_exceptions_key", false);
	}

	public static boolean userAcceptedTerms(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean(USER_ACCEPTED_TERMS_KEY, false);
	}

	public static void putUserAcceptedTerms(Context context, boolean userAcceptedTerms) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(USER_ACCEPTED_TERMS_KEY, userAcceptedTerms);

		editor.apply();
	}

	public static String scanStatus(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getString(SCAN_STATUS_KEY, StringLiterals.EMPTY_STRING);
	}

	public static void putScanStatus(Context context, String scanStatus) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SCAN_STATUS_KEY, scanStatus);

		editor.apply();
	}

	public static boolean headphonePlayOnConnect(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("headphone_play_on_connect", true);
	}

	public static boolean headphonePauseOnDisconnect(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("headphone_pause_on_disconnect", true);
	}

	public static boolean bluetoothPlayOnConnect(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("bluetooth_play_on_connect", true);
	}

	public static boolean bluetoothPauseOnDisconnect(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("bluetooth_pause_on_disconnect", true);
	}

	public static boolean isFullLoggingActive(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean("full_logging_key", false);
	}

	public static boolean getCopyDatabase(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getBoolean(COPY_DATABASE_KEY, false);
	}

	public static String getFilterCategory(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getString(FILTER_CATEGORY, StringLiterals.EMPTY_STRING);
	}

	public static void putFilterData(Context context, String filterCategory, String filterValue) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(FILTER_CATEGORY, filterCategory);
		editor.putString(FILTER_VALUE, filterValue);

		editor.apply();
	}

	public static String getFilterValue(Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPreferences.getString(FILTER_VALUE, null);
	}

    public static int getCurrentTab(Context context) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getInt(CURRENT_TAB_KEY, 0);
    }

    public static void putCurrentTab(Context context, int currentTab) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(CURRENT_TAB_KEY, currentTab);

        editor.apply();
    }

    public static int getFirstVisibleItem(Context context) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getInt(FIRST_VISIBLE_ITEM_KEY, 0);
    }

    public static void putFirstVisibleItem(Context context, int firstVisibleItem) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(FIRST_VISIBLE_ITEM_KEY, firstVisibleItem);

        editor.apply();
    }

}
