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

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class OTAUpdatesAdapter extends ArrayAdapter<OTAUpdates> {
    private ArrayList<OTAUpdates> otalist;
    private LayoutInflater vi;
    private int Resource;

    OTAUpdatesAdapter(Context context, int resource, ArrayList<OTAUpdates> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        otalist = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // convert view = design
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.ota_filename = (TextView) v.findViewById(R.id.ota_filename);
            holder.ota_version = (TextView) v.findViewById(R.id.ota_version);
            holder.ota_timestamp = (TextView) v.findViewById(R.id.ota_timestamp);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.ota_filename.setText(otalist.get(position).getOta_filename());
        holder.ota_version.setText(otalist.get(position).getOta_version());
        holder.ota_timestamp.setText(otalist.get(position).getOta_timestamp());
        return v;
    }

    private static class ViewHolder {
        TextView ota_filename;
        TextView ota_version;
        TextView ota_timestamp;

    }
}
