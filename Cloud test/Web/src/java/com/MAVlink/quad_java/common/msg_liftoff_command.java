/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE LIFTOFF_COMMAND PACKING
package com.MAVLink.quad_java.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Request liftoff from the current location after a delay in ms. May be denied depending on the current state
*/
public class msg_liftoff_command extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_LIFTOFF_COMMAND = 158;
    public static final int MAVLINK_MSG_LENGTH = 4;
    private static final long serialVersionUID = MAVLINK_MSG_ID_LIFTOFF_COMMAND;


      
    /**
    * Delay
    */
    public long delay;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_LIFTOFF_COMMAND;
              
        packet.payload.putUnsignedInt(delay);
        
        return packet;
    }

    /**
    * Decode a liftoff_command message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.delay = payload.getUnsignedInt();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_liftoff_command(){
        msgid = MAVLINK_MSG_ID_LIFTOFF_COMMAND;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_liftoff_command(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LIFTOFF_COMMAND;
        unpack(mavLinkPacket.payload);        
    }

      
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_LIFTOFF_COMMAND -"+" delay:"+delay+"";
    }
}
        