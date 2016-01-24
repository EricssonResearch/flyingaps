import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * A class to store information for a mission item. It is mapped to the list of mission items in the GUI.
 */
public class MissionItem {
    /**
     * The mission item type.
     */
    private final SimpleStringProperty command;
    /**
     * The latitude in decimal WGS84.
     */
    private final SimpleFloatProperty latitude;
    /**
     * The longitude in decimal WGS84.
     */
    private final SimpleFloatProperty longitude;
    /**
     * The altitude in decimal WGS84.
     */
    private final SimpleFloatProperty altitude;
    /**
     * The hold time for 'Fly To' mission item.
     */
    private final SimpleIntegerProperty time;
    /**
     * The Mavlink ID for the command.
     */
    private final int commandId;

    /**
     * The constructor sets all the fields of the class. There are no setters in this class.
     *
     * @param  commandId    The Mavlink ID for the command.
     * @param  command      The mission item type.
     * @param  latitude     The latitude in decimal WGS84.
     * @param  longitude    The longitude in decimal WGS84.
     * @param  altitude     The altitude in decimal WGS84.
     * @param  time         The hold time for 'Fly To' mission item.
     * @param  commandId    The Mavlink ID for the command.
     */
    MissionItem(String command, float latitude, float longitude, float altitude, Integer time, int commandId) {
        this.commandId = commandId;
        this.command = new SimpleStringProperty(command);
        this.latitude = new SimpleFloatProperty(latitude);
        this.longitude = new SimpleFloatProperty(longitude);
        this.altitude = new SimpleFloatProperty(altitude);
        this.time = new SimpleIntegerProperty(time);
    }

    /**
     *  Returns the Mavlink ID for the command.
     *
     * @return      the Mavlink ID for the command
     */
    public int getCommandId() { return commandId; }

    /**
     * Returns a string containing the name of the command.
     *
     * @return      a string containing the name of the command
     */
    public String getCommand() {
        return command.get();
    }

    /**
     * Returns the latitude in decimal WGS84 as a string.
     *
     * @return      the latitude in decimal WGS84 as a string
     */
    public float getLatitude() {
        return latitude.get();
    }

    /**
     * Returns the longitude in decimal WGS84 as a string.
     *
     * @return      the longitude in decimal WGS84 as a string
     */
    public float getLongitude() {
        return longitude.get();
    }

    /**
     * Returns the altitude in decimal WGS84 as a string.
     *
     * @return      the altitude in decimal WGS84 as a string
     */
    public float getAltitude() {
        return altitude.get();
    }

    /**
     * Returns the hold time
     *
     * @return      the hold time
     */
    public int getTime() {
        return time.get();
    }
}
