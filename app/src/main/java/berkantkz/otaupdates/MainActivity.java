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

            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;

        }

        protected void onPostExecute(Boolean result) {
            adapter.notifyDataSetChanged();
            if (result == false)
                Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
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

            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;

        }

        protected void onPostExecute(Boolean result) {
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
            if (result == false)
                Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
        }
    }
}
