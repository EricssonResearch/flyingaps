/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE GET_NETWORK_STATS PACKING
package com.MAVLink.packer\common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Request statistical data of the network usage and drone status.
*/
public class msg_get_network_stats extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_GET_NETWORK_STATS = 156;
    public static final int MAVLINK_MSG_LENGTH = 249;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GET_NETWORK_STATS;


      
    /**
    * Serialized data structure containing network usage information
    */
    public byte data[] = new byte[249];
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GET_NETWORK_STATS;
              
        
        for (int i = 0; i < data.length; i++) {
            packet.payload.putByte(data[i]);
        }
                    
        
        return packet;
    }

    /**
    * Decode a get_network_stats message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
         
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = payload.getByte();
        }
                
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_get_network_stats(){
        msgid = MAVLINK_MSG_ID_GET_NETWORK_STATS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_get_network_stats(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GET_NETWORK_STATS;
        unpack(mavLinkPacket.payload);        
    }

     
    /**
    * Sets the buffer of this message with a string, adds the necessary padding
    */
    public void setData(String str) {
        int len = Math.min(str.length(), 249);
        for (int i=0; i<len; i++) {
            data[i] = (byte) str.charAt(i);
        }

        for (int i=len; i<249; i++) {            // padding for the rest of the buffer
            data[i] = 0;
        }
    }

    /**
    * Gets the message, formated as a string
    */
    public String getData() {
        String result = "";
        for (int i = 0; i < 249; i++) {
            if (data[i] != 0)
                result = result + (char) data[i];
            else
                break;
        }
        return result;

    }
                         
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GET_NETWORK_STATS -"+" data:"+data+"";
    }
}
        