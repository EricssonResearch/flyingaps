import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MissionItem {
    private final SimpleStringProperty command;
    private final SimpleFloatProperty latitude;
    private final SimpleFloatProperty longitude;
    private final SimpleFloatProperty altitude;
    private final SimpleIntegerProperty time;
    private final int commandId;

    MissionItem(String command, float latitude, float longitude, float altitude, Integer time, int commandId) {
        this.commandId = commandId;
        this.command = new SimpleStringProperty(command);
        this.latitude = new SimpleFloatProperty(latitude);
        this.longitude = new SimpleFloatProperty(longitude);
        this.altitude = new SimpleFloatProperty(altitude);
        this.time = new SimpleIntegerProperty(time);
    }

    public int getCommandId() { return commandId; }

    public String getCommand() {
        return command.get();
    }

    public void setCommand(String command) {
        this.command.set(command);
    }

    public float getLatitude() {
        return latitude.get();
    }

    public void setLatitude(int latitude) {
        this.latitude.set(latitude);
    }

    public float getLongitude() {
        return longitude.get();
    }

    public void setLongitude(int longitude) {
        this.longitude.set(longitude);
    }

    public float getAltitude() {
        return altitude.get();
    }

    public void setAltitude(int altitude) {
        this.altitude.set(altitude);
    }

    public int getTime() {
        return time.get();
    }

    public void setTime(int time) {
        this.time.set(time);
    }
}
