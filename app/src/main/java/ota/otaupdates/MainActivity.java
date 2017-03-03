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
package ota.otaupdates;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

import ota.otaupdates.utils.Constants;
import ota.otaupdates.utils.MD5;
import ota.otaupdates.utils.Utils;
import eu.chainfire.libsuperuser.Shell;

import static ota.otaupdates.utils.Constants.DL_PATH;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int RESULT_SETTINGS = 1;
    final StringBuilder build_url = new StringBuilder();
    final StringBuilder build_dl_url = new StringBuilder();
    final StringBuilder delta_url = new StringBuilder();
    final StringBuilder delta_dl_url = new StringBuilder();
    static ArrayList<OTAUpdates> otaList;
    OTAUpdatesAdapter adapter;
    Snackbar sb_network;
    Snackbar sb_no_su;
    static SharedPreferences sharedPreferences;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean("force_english", false)) {
            Locale myLocale = new Locale("en");
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
        }

        if (sharedPreferences.getBoolean("apptheme_light", false))
            setTheme(R.style.AppTheme_Light);
        else
            setTheme(R.style.AppTheme_Dark);

        setContentView(R.layout.activity_main);

        build_url.append((Utils.doesPropExist(Constants.URL_PROP)) ? Utils.getProp(Constants.URL_PROP) : getString(R.string.download_url))
                .append("/api/")
                .append(Build.DEVICE).append("/")
                .append(Build.TIME / 1000);

        build_dl_url.append((Utils.doesPropExist(Constants.URL_PROP)) ? Utils.getProp(Constants.URL_PROP) : getString(R.string.download_url))
                .append("/builds/");

        delta_url.append((Utils.doesPropExist(Constants.URL_PROP) ? Utils.getProp(Constants.URL_PROP) : getString(R.string.download_url)))
                .append("/delta/")
                .append(Build.VERSION.INCREMENTAL);

        delta_dl_url.append((Utils.doesPropExist(Constants.URL_PROP)) ? Utils.getProp(Constants.URL_PROP) : getString(R.string.download_url))
                .append("/deltas/");

        otaList = new ArrayList<>();
        get_builds();
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);

        final ListView ota_list = (ListView) findViewById(R.id.ota_list);

        adapter = new OTAUpdatesAdapter(getApplicationContext(), R.layout.row, otaList);
        ota_list.setAdapter(adapter);

        final CoordinatorLayout coordinator_root = (CoordinatorLayout) findViewById(R.id.coordinator_root);
        ota_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {
                final String url = build_dl_url.toString() + otaList.get(position).getOta_filename();

                if (Build.VERSION.SDK_INT >= 23 && !checkPermission())
                    allow_write_sd();
                else if (sharedPreferences.getBoolean("disable_mobile", true) && isMobileDataEnabled()) {
                        sb_network = Snackbar.make(coordinator_root, getString(R.string.disable_mobile_message), Snackbar.LENGTH_SHORT);
                        sb_network.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                        sb_network.show();
                    }
                else {
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    create_notification(1, getString(R.string.app_name), getString(R.string.downloader_notification, otaList.get(position).getOta_filename()));
                                    Utils.DownloadFromUrl(url, otaList.get(position).getOta_filename());
                                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (MD5.checkMD5(otaList.get(position).getOta_md5(), new File(DL_PATH + otaList.get(position).getOta_filename())) || !sharedPreferences.getBoolean("md5_checking", true))
                                                trigger_autoinstall(DL_PATH + otaList.get(position).getOta_filename());
                                            else {
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle(getString(R.string.md5_title))
                                                        .setMessage(getString(R.string.md5_message))
                                                        .setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                            }
                                                        })
                                                        .show();
                                            }
                                        }
                                    });
                                }
                            }).start();
                }
            }
        });

    }

    private void get_builds() {
        otaList.clear();
        new JSONAsyncTask().execute(delta_url.toString());
        new JSONAsyncTask().execute(build_url.toString());
    }

    private void allow_write_sd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.write_access));
        builder.setMessage(getString(R.string.write_access_message));
        builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
                    // If user hasn't allowed yet, request the permission.
                    requestPermission();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        if (!checkPermission()) {
            // If user hasn't allowed yet, show requester dialog.
            alert.show();
        }
    }

    private void create_notification(int id, String title, String content) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_50)
                .setContentTitle(title)
                .setContentText(content);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }

    private void trigger_autoinstall(final String file_path) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final CoordinatorLayout coordinator_root = (CoordinatorLayout) findViewById(R.id.coordinator_root);
        if (sharedPreferences.getBoolean("enable_auto_install", true) ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.auto_install_title));
            builder.setMessage(getString(R.string.auto_install_message));
            builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (Shell.SU.available()) {
                        Shell.SU.run("rm -rf /cache/recovery/openrecoveryscript");
                        Shell.SU.run("echo \"install " + file_path + "\" >> /cache/recovery/openrecoveryscript");

                        if (sharedPreferences.getBoolean("wipe_cache", true))
                            Shell.SU.run("echo \"wipe cache\" >> /cache/recovery/openrecoveryscript");

                        if (sharedPreferences.getBoolean("wipe_dalvik", true))
                            Shell.SU.run("echo \"wipe dalvik\" >> /cache/recovery/openrecoveryscript");

                        if (sharedPreferences.getBoolean("auto_reboot", true))
                            Shell.SU.run("reboot recovery");
                    } else {
                        sb_no_su = Snackbar.make(coordinator_root, "SU access is not available", Snackbar.LENGTH_SHORT);
                        sb_no_su.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                        sb_no_su.show();
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alert = builder.create();
            alert.setCancelable(false);
            alert.show();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @return null if unconfirmed
     */
    public Boolean isMobileDataEnabled(){
        Object connectivityService = getSystemService(CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) connectivityService;

        try {
            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean)m.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(getString(R.string.app_name), "Permission granted. Files can be saved");
                } else {
                    Log.e(getString(R.string.app_name), "Permission denied. The App won\'t work");
                    finish();
                }
                break;
        }
    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        CoordinatorLayout coordinator_root;
        Snackbar sb;
        ListView ota_list;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            coordinator_root = (CoordinatorLayout) findViewById(R.id.coordinator_root);
            sb = Snackbar.make(coordinator_root, getString(R.string.loading), Snackbar.LENGTH_SHORT);
            sb.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
            sb.show();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                HttpGet httppost = new HttpGet(urls[0]);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httppost);

                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray jarray = new JSONArray(data);

                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);

                        OTAUpdates dls = new OTAUpdates();

                        dls.setOta_filename(object.getString("filename"));
                        dls.setOta_timestamp(object.getString("timestamp"));
                        dls.setOta_md5(object.getString("md5sum"));

                        if (object.isNull("old_incremental")) {
                            dls.setOta_version(object.getString("version"));
                        } else {
                            dls.setOta_old_incremental(object.getString("old_incremental"));
                            dls.setDelta(true);
                        }

                        otaList.add(dls);

                    }
                    return true;
                }

            } catch (UnknownHostException e) {
                Log.e(getString(R.string.app_name), "No Network Connection");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar mSnackbar = Snackbar.make(coordinator_root, getString(R.string.loading_failed), Snackbar.LENGTH_SHORT);
                        mSnackbar.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                        mSnackbar.show();
                    }
                });
            } catch (IOException | ParseException | JSONException e) {
                e.printStackTrace();
            }
            return false;

        }

        protected void onPostExecute(Boolean result) {
            adapter.notifyDataSetChanged();
            pb.setVisibility(View.INVISIBLE);
            ota_list = (ListView) findViewById(R.id.ota_list);
            ota_list.setVisibility((adapter.isEmpty())?View.GONE:View.VISIBLE);
            sb.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
            sb.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // show menu when menu button is pressed
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            get_builds();
            pb.setVisibility(View.VISIBLE);
        }
        else if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(MainActivity.this, Settings.class);
            startActivityForResult(settings, RESULT_SETTINGS);
        }

        return true;
    }

}
