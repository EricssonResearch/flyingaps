/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE SET_WIFI_AP_DISABLED PACKING
package com.MAVLink.quad_java.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Request deactivation of the WiFi Access point.
*/
public class msg_set_wifi_ap_disabled extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_SET_WIFI_AP_DISABLED = 153;
    public static final int MAVLINK_MSG_LENGTH = 0;
    private static final long serialVersionUID = MAVLINK_MSG_ID_SET_WIFI_AP_DISABLED;


    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_SET_WIFI_AP_DISABLED;
        
        return packet;
    }

    /**
    * Decode a set_wifi_ap_disabled message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_set_wifi_ap_disabled(){
        msgid = MAVLINK_MSG_ID_SET_WIFI_AP_DISABLED;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_set_wifi_ap_disabled(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SET_WIFI_AP_DISABLED;
        unpack(mavLinkPacket.payload);        
    }

    
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_SET_WIFI_AP_DISABLED -"+"";
    }
}
        