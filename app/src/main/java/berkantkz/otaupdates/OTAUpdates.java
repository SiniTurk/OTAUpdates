package berkantkz.otaupdates;

/**
 * Created by berka on 2.02.2017.
 */
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

