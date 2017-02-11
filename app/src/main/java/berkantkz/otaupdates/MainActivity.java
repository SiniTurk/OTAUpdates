/**
 * Project: OTAUpdates
 *
 * @author berkantkz, TimSchumi
 * License: GNU General Public License, Version 3
 */
/**
 * Copyright 2017 Berkant Korkmaz, Tim Schumacher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package berkantkz.otaupdates;

import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<OTAUpdates> otaList;
    OTAUpdatesAdapter adapter;
    private PullRefreshLayout refreshLayout;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        final StringBuilder build_device = new StringBuilder();
        // TODO: Retrieve server address from build.prop
        build_device.append("http://timschumi.16mb.com/ota/api/").append(Build.DEVICE);

        otaList = new ArrayList<OTAUpdates>();
        new JSONAsyncTask().execute(build_device.toString());

        final ListView listview = (ListView) findViewById(R.id.ota_list);
        adapter = new OTAUpdatesAdapter(getApplicationContext(), R.layout.row, otaList);
        listview.setAdapter(adapter);

        refreshLayout = (PullRefreshLayout) findViewById(R.id.app_swipe_refresh);
            refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
            refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new JSONAsyncRefreshTask().execute(build_device.toString());
            }
        });

        ImageView btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONAsyncRefreshTask().execute(build_device.toString());
            }
        });

    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        CoordinatorLayout coordinator_root;
        Snackbar sb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            coordinator_root = (CoordinatorLayout) findViewById(R.id.coordinator_root);
            sb = Snackbar.make(coordinator_root, "Loading", Snackbar.LENGTH_LONG);
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

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    JSONObject jsono = new JSONObject(data);
                    JSONArray jarray = jsono.getJSONArray("result");

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
            if (!result)
                sb.make(coordinator_root, "Failed to load! Check your connection first.", Snackbar.LENGTH_LONG);
                sb.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                sb.show();
        }
    }

    class JSONAsyncRefreshTask extends AsyncTask<String, Void, Boolean> {

        CoordinatorLayout coordinator_root;
        Snackbar sb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            coordinator_root = (CoordinatorLayout) findViewById(R.id.coordinator_root);
            sb = Snackbar.make(coordinator_root, "Refreshing...", Snackbar.LENGTH_LONG);
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

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
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
                sb.make(coordinator_root, "Failed to load! Check your connection first.", Snackbar.LENGTH_LONG);
                sb.getView().setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecond));
                sb.show();
                refreshLayout.setRefreshing(false);
        }
    }
}
