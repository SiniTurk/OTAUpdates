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

public class OTAUpdates {
    private String ota_filename;
    private String ota_timestamp;
    private String ota_version;
    private String ota_channel;
    public OTAUpdates() {

    }

    //  GETTERS & SETTERS

    //ota_filename
    public String getOta_filename() {
        return ota_filename;
    }
    public void setOta_filename(String ota_filename) {
        this.ota_filename = ota_filename;
    }

    //ota_timestamp
    public String getOta_timestamp() {
        return ota_timestamp;
    }
    public void setOta_timestamp(String ota_timestamp) {
        this.ota_timestamp = ota_timestamp;
    }

    //ota_version
    public String getOta_version() {
        return ota_version;
    }
    public void setOta_version(String ota_version) {
        this.ota_version = ota_version;
    }

    //ota_channel
    public  String getOta_channel() {
        return ota_channel;
    }
    public void setOta_channel(String ota_channel) {
        this.ota_channel = ota_channel;
    }
}

