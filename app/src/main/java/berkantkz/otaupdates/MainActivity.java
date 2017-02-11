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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

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

        final StringBuilder sb = new StringBuilder();
        // TODO: Retrieve server address from build.prop
        sb.append("http://timschumi.16mb.com/ota/api/").append(Build.DEVICE);

        otaList = new ArrayList<OTAUpdates>();
        new JSONAsyncTask().execute(sb.toString());

        final ListView listview = (ListView) findViewById(R.id.ota_list);
        adapter = new OTAUpdatesAdapter(getApplicationContext(), R.layout.row, otaList);
        listview.setAdapter(adapter);

        refreshLayout = (PullRefreshLayout) findViewById(R.id.app_swipe_refresh);
            refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
            refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new JSONAsyncRefreshTask().execute(sb.toString());
            }
        });

    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    class JSONAsyncRefreshTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
        }
    }
}
