/*
 * This file is part of Elygo-lib.
 * Copyright (C) 2012   Emmanuel Mathis [emmanuel *at* lr-studios.net]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lrstudios.games.ego.lib;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public final class Utils {
    public static final String FILENAME_RESERVED_CHARS = "|\\?*<\":>+[]/'\"";

    private static long startTime = -1;


    /**
     * Converts a collection to a sorted list.
     */
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }


    /**
     * Returns the hexadecimal representation of the specified byte array.
     */
    public static String toHexString(byte[] raw) {
        if (raw == null)
            return null;

        final String HEXES = "0123456789ABCDEF";
        StringBuilder hex = new StringBuilder(2 * raw.length);

        for (byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt(b & 0x0F))
                    .append(' ');
        }

        return hex.toString();
    }

    /**
     * Returns the binary representation of the specified byte array.
     */
    public static String toBinaryString(byte[] arr) {
        StringBuilder str = new StringBuilder();
        for (byte b : arr) {
            StringBuilder conv = new StringBuilder(Integer.toBinaryString(b));
            for (int i = conv.length(); i < 8; i++)
                conv.insert(0, '0');

            str.append(conv)
                    .append(' ');
        }
        return str.toString();
    }

    /**
     * Closes the specified object without throwing any exceptions.
     */
    public static void closeObject(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        }
        catch (Exception ignored) {
        }
    }

    /**
     * Converts a file size to a readable string representation which always use
     * the best suited units (like "4.54 GB", "337 MB").
     */
    public static String formatFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    /**
     * Copy the entire input stream to the specified output stream.
     */
    public static void copyStream(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = input.read(buf, 0, buf.length)) > 0)
            output.write(buf, 0, len);
    }


    /**
     * Deletes all files in the specified directory (this excludes subdirectories).
     */
    public static void deleteAllFiles(File directory) {
        if (!directory.isDirectory())
            return;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                file.delete();
        }
    }


    /**
     * Formats the given variables in a readable string ("val1, val2, val3, ...").
     */
    public static String logVars(Object... vars) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Object o : vars) {
            builder.append(o.toString()).append(", ");
            i++;
        }
        if (i > 0)
            builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    public static void stopwatch_start() {
        startTime = System.nanoTime();
    }

    public static long stopwatch_get() {
        return (System.nanoTime() - startTime) / 1000000;
    }

    public static String komiToString(double komi, char decimalSeparator) {
        return String.format("%d%c%d", (int) (komi), decimalSeparator, ((int) Math.round(komi * 10.0) % 10));
    }

    public static void extractZip(File file) throws IOException {
        final int BUFFER = 2048;
        ZipFile zip = new ZipFile(file);
        String filename = file.getAbsolutePath();
        String newPath = filename.substring(0, filename.lastIndexOf('.'));

        new File(newPath).mkdir();
        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                    dest.write(data, 0, currentByte);
                dest.flush();
                dest.close();
                is.close();
            }
        }
    }
}
