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

public class OTAUpdates {
    private String ota_filename;
    private String ota_timestamp;
    private String ota_version;
    private String ota_md5;

    OTAUpdates() {

    }

    //  GETTERS & SETTERS

    //ota_filename
    String getOta_filename() {
        return ota_filename;
    }

    void setOta_filename(String ota_filename) {
        this.ota_filename = ota_filename;
    }

    //ota_timestamp
    String getOta_timestamp() {
        return ota_timestamp;
    }

    void setOta_timestamp(String ota_timestamp) {
        this.ota_timestamp = ota_timestamp;
    }

    //ota_version
    String getOta_version() {
        return ota_version;
    }

    void setOta_version(String ota_version) {
        this.ota_version = ota_version;
    }

    //ota_md5
    String getOta_md5() {
        return ota_md5;
    }
    void setOta_md5(String ota_md5) {
        this.ota_md5 = ota_md5;
    }

}

