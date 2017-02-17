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
 */
package berkantkz.otaupdates;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.baoyz.widget.PullRefreshLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import berkantkz.otaupdates.utils.Constants;
import berkantkz.otaupdates.utils.Utils;
import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int RESULT_SETTINGS = 1;
    public static Activity mActivity;
    final StringBuilder build_device = new StringBuilder();
    final StringBuilder build_dl_url = new StringBuilder();
    ArrayList<OTAUpdates> otaList;
    OTAUpdatesAdapter adapter;
    DownloadManager manager;
    DownloadManager.Request request;
    Snackbar sb_network;
    static SharedPreferences sharedPreferences;

    private PullRefreshLayout refreshLayout;
    BroadcastReceiver dlcomplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                // Trigger Installation if Root was detected
                String filename = null;

                Bundle extras = intent.getExtras();
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
                Cursor c = manager.query(q);

                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    }
                }
                c.close();

                trigger_autoinstall(filename);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = MainActivity.this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        build_device.append((Utils.doesPropExist(Constants.URL_PROP)) ? Utils.getProp(Constants.URL_PROP) : getString(R.string.download_url))
                .append("/api/")
                .append(Build.DEVICE).append("/")
                .append(Build.TIME / 1000);

        build_dl_url.append((Utils.doesPropExist(Constants.URL_PROP)) ? Utils.getProp(Constants.URL_PROP) : getString(R.string.download_url))
                .append("/builds/");

        registerReceiver(dlcomplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        otaList = new ArrayList<>();
        get_ota_builds();

        final ListView ota_list = (ListView) findViewById(R.id.ota_list);

        adapter = new OTAUpdatesAdapter(getApplicationContext(), R.layout.row, otaList);
        ota_list.setAdapter(adapter);

        refreshLayout = (PullRefreshLayout) findViewById(R.id.app_swipe_refresh);
        refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_ota_builds();
            }
        });

        ImageView btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_ota_builds();
            }
        });

        ImageView btn_settings = (ImageView) findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settings = new Intent(MainActivity.this, Settings.class);
                startActivityForResult(settings, RESULT_SETTINGS);
            }
        });

        ConnectivityManager cm = (ConnectivityManager) MainActivity.this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final CoordinatorLayout coordinator_root = (CoordinatorLayout) findViewById(R.id.coordinator_root);
        ota_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {
                String url = build_dl_url.toString() + otaList.get(position).getOta_filename();
                request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription(otaList.get(position).getOta_version() + " " + "-" + " " + otaList.get(position).getOta_timestamp());
                request.setTitle(otaList.get(position).getOta_filename());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, otaList.get(position).getOta_filename());
                // get download service and enqueue file
                manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if (Build.VERSION.SDK_INT >= 23 && !checkPermission())
                    allow_write_sd();
                else if (sharedPreferences.getBoolean("disable_mobile", true)) {
                    if (isMobileDataEnabled()) {
                        sb_network = Snackbar.make(coordinator_root, getString(R.string.disable_mobile_message), Snackbar.LENGTH_SHORT);
                        sb_network.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                        sb_network.show();
                    } else {
                        manager.enqueue(request);
                    }
                } else {
                    manager.enqueue(request);
                }
            }
        });

        if (sharedPreferences.getBoolean("force_english", true)) {
            setEnglish();
        } if (sharedPreferences.getBoolean("force_english", false)) {
            setLocale();
        }

    }

    private void get_ota_builds() {
        new JSONAsyncTask().execute(build_device.toString());
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

    private void trigger_autoinstall(final String file_path) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Shell.SU.available() && sharedPreferences.getBoolean("enable_auto_install", true) ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.auto_install_title));
            builder.setMessage(getString(R.string.auto_install_message));
            builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Shell.SU.run("rm -rf /cache/recovery/openrecoveryscript");
                    Shell.SU.run("echo \"install "+file_path+"\" >> /cache/recovery/openrecoveryscript");

                    if (sharedPreferences.getBoolean("wipe_cache", true))
                        Shell.SU.run("echo \"wipe cache\" >> /cache/recovery/openrecoveryscript");

                    if (sharedPreferences.getBoolean("wipe_dalvik", true))
                        Shell.SU.run("echo \"wipe dalvik\" >> /cache/recovery/openrecoveryscript");

                    if (sharedPreferences.getBoolean("auto_reboot", true))
                        Shell.SU.run("reboot recovery");
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
                    Log.d(getString(R.string.app_name), getString(R.string.permissions_granted));
                } else {
                    Log.e(getString(R.string.app_name), getString(R.string.permissions_denied));
                    finish();
                }
                break;
        }
    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        CoordinatorLayout coordinator_root;
        Snackbar sb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            coordinator_root = (CoordinatorLayout) findViewById(R.id.coordinator_root);
            sb = Snackbar.make(coordinator_root, getString(R.string.loading), Snackbar.LENGTH_LONG);
            sb.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
            sb.show();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {

                //------------------>>
                HttpGet httppost = new HttpGet(urls[0]);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httppost);

                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    JSONObject jsono = new JSONObject(data);
                    JSONArray jarray = jsono.getJSONArray("result");
                    otaList.clear();

                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);

                        OTAUpdates dls = new OTAUpdates();

                        dls.setOta_filename(object.getString("filename"));
                        dls.setOta_version(object.getString("version"));
                        dls.setOta_timestamp(object.getString("timestamp"));
                        //dls.setOta_channel(object.getString("channel"));

                        otaList.add(dls);

                    }
                    return true;
                }

                //------------------>>

            } catch (ParseException | IOException | JSONException e) {
                e.printStackTrace();
            }
            return false;

        }

        protected void onPostExecute(Boolean result) {
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
            if (!result)
                Snackbar.make(coordinator_root, getString(R.string.loading_failed), Snackbar.LENGTH_LONG);
            sb.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
            sb.show();
            refreshLayout.setRefreshing(false);
        }
    }

    public void setEnglish() {
            Locale myLocale = new Locale("en");
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
    }

    public void setLocale() {
        String current = Locale.getDefault().getDisplayLanguage();
        Locale myLocale = new Locale(current);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}
