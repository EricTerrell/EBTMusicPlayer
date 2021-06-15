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

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.db.ScanProgressMessage;
import com.ericbt.musicplayer.exceptions.ScanCancelledException;
import com.ericbt.musicplayer.services.scanner_service.ScannerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

public class FileUtils {
	public static String getFileType(File file) {
		return getFileType(file.getName());
	}

	private static String getFileType(String fileName) {
		String fileType = null;

		final int index = fileName.lastIndexOf('.');

		if (index >= 0) {
			fileType = fileName.substring(index + 1);
		}

		return fileType;
	}

	/**
	 * Copies the specified file
	 *
	 * @param srcPath  path of file to be copied
	 * @param destPath path to where file will be copied
	 * @throws IOException if copy fails
	 */
	public static void copyFile(String srcPath, String destPath) throws IOException {
		try (FileInputStream inputStream = new FileInputStream(srcPath);
			 FileOutputStream outputStream = new FileOutputStream(destPath)) {
			final byte[] buffer = new byte[1024];
			int length;

			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}

			outputStream.flush();
		}
	}

	public static String readFile(File file) throws IOException {
		StringBuilder text = new StringBuilder();

		try (FileInputStream fileInputStream = new FileInputStream(file);
			 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
			 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

			boolean finished = false;

			do {
				String line = bufferedReader.readLine();

				if (line == null) {
					finished = true;
				} else {
					text.append(line);
					text.append(StringLiterals.NEWLINE);
				}
			} while (!finished);
		}

		return text.toString();
	}

	public static void search(File root, Set<String> fileTypes, ScannerService musicPlayerService, List<File> files, ScanProgressMessage scanProgressMessage, Context context) throws ScanCancelledException {
		if (musicPlayerService.isScanCancellationRequested()) {
			throw new ScanCancelledException();
		}

		try {
			scanProgressMessage.sendScanProgressMessage(files.size());

			final File[] rootFiles = root.listFiles();

			if (rootFiles != null) {
				for (File currentFile : root.listFiles()) {
					if (currentFile.isFile()) {
						String fileType = FileUtils.getFileType(currentFile);

						if (fileType != null) {
							fileType = FileUtils.getFileType(currentFile).toUpperCase();

							if (fileTypes.contains(fileType)) {
								files.add(currentFile);
								scanProgressMessage.sendScanProgressMessage(files.size());
							}
						}
					} else if (currentFile.isDirectory()) {
						search(currentFile, fileTypes, musicPlayerService, files, scanProgressMessage, context);
					}
				}
			}
		} catch (ScanCancelledException ex) {
			throw ex;
		} catch (Exception ex) {
			Log.e(StringLiterals.LOG_TAG, String.format("Cannot list files for %s. Exception: %s", root.getAbsolutePath(), ex));
			ExceptionLogger.logException(ex, context);
		}
	}

	/**
	 * Ensure that a specified file, in the external folder, is actually accessible.
	 * @param context context
	 * @param filePath filePath
	 */
	public static void scanFile(Context context, String filePath) {
		final Logger logger = new Logger(context);

		MediaScannerConnection.scanFile(context,
				new String[] { filePath }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
						logger.log("FileUtils.canFile: Scanned " + path + ":");
						logger.log("-> uri=" + uri);
					}
				});
	}
}
