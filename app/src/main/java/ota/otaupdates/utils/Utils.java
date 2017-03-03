/**
 * Project: OTAUpdates
 *
 * @author berkantkz, TimSchumi
 * License: GNU General Public License, Version 3
 * <p>
 * Copyright 2017 Berkant Korkmaz, Tim Schumacher
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Only the Main Program is covered by this License. Other modules/items which
 * are included/used may have other Licenses
 */

package ota.otaupdates.utils;

import android.os.Looper;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Utils implements Constants {
    /**
     * Credit goes to Matthew Booth (http://www.github.com/MatthewBooth) for this function
     * @param propName The prop to be checked
     * @return boolean If the prop exists
     */
    public static Boolean doesPropExist(String propName) {
        boolean valid = false;

        try {
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if (line.contains("[" + propName +"]")) {
                    valid = true;
                }
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return valid;
    }

    /**
     * Credit goes to Matthew Booth (http://www.github.com/MatthewBooth) for this function
     * @param propName The prop to be checked
     * @return String The value of the prop
     */
    public static String getProp(String propName) {
        Process p = null;
        String result = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line=br.readLine()) != null) {
                result = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void DownloadFromUrl(String download_url, String fileName)
    {
        final String TAG = "Downloader";

        if (!isMainThread()) {
            try {
                URL url = new URL(download_url);

                if (!new File(DL_PATH).isDirectory()) {
                    if (!new File(DL_PATH).mkdirs()) {
                        Log.e(TAG, "Creating the directory " + DL_PATH + "failed");
                    }
                }

                File file = new File(DL_PATH + fileName);

                long startTine = System.currentTimeMillis();
                Log.d(TAG, "Beginning download of " + url.getPath() + " to " + DL_PATH + fileName);

                /*
                 * Open a connection and define Streams
                 */
                URLConnection ucon = url.openConnection();
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                /*
                 * Read bytes until there is nothing left
                 */
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                /* Convert Bytes to a String */
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baf.toByteArray());
                fos.close();

                Log.d(TAG, "Download finished in " + ((System.currentTimeMillis() - startTine) / 1000) + " seconds");
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e);
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Tried to run in Main Thread. Aborting...");
        }
    }

    public static Boolean isMainThread()
    {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
