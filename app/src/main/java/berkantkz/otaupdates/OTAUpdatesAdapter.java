package berkantkz.otaupdates;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by berka on 2.02.2017.
 */
public class OTAUpdatesAdapter extends ArrayAdapter<OTAUpdates> {
    ArrayList<OTAUpdates> otalist;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public OTAUpdatesAdapter(Context context, int resource, ArrayList<OTAUpdates> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        otalist = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.ota_channel = (ImageView) v.findViewById(R.id.ota_channel);
            holder.ota_filename = (TextView) v.findViewById(R.id.ota_filename);
            holder.ota_version = (TextView) v.findViewById(R.id.ota_version);
            holder.ota_timestamp = (TextView) v.findViewById(R.id.ota_timestamp);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.ota_channel.setImageResource(R.mipmap.ic_launcher);
        //new DownloadImageTask(holder.ota_channel).execute(otalist.get(position).getOta_channel());
        holder.ota_filename.setText(otalist.get(position).getOta_filename());
        holder.ota_version.setText(otalist.get(position).getOta_version());
        holder.ota_timestamp.setText(otalist.get(position).getOta_timestamp());
        return v;
    }

    static class ViewHolder {
        public ImageView ota_channel;
        public TextView ota_filename;
        public TextView ota_version;
        public TextView ota_timestamp;

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
