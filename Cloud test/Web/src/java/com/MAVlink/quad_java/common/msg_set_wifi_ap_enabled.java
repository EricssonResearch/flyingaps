/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE SET_WIFI_AP_ENABLED PACKING
package com.MAVLink.quad_java.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Request activation of the WiFi Access point with the provided settings.
*/
public class msg_set_wifi_ap_enabled extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_SET_WIFI_AP_ENABLED = 152;
    public static final int MAVLINK_MSG_LENGTH = 61;
    private static final long serialVersionUID = MAVLINK_MSG_ID_SET_WIFI_AP_ENABLED;


      
    /**
    * Network Service Set Identifier
    */
    public byte ssid[] = new byte[50];
      
    /**
    * MAC Address of the Wireless Access Point (In case we ever have more than one access point)
    */
    public byte bssid[] = new byte[6];
      
    /**
    * Authentication algorithm: Open = 0|Shared = 1|Leap = 2
    */
    public short auth_algorithm;
      
    /**
    * Allowed protocols: WPA = 0|RSN = 1|Both = 2
    */
    public short allowed_protocols;
      
    /**
    * Recognized key management schemes: NONE = 0|WPA_PSK = 1|WPA_EAP = 2|IEEE8021X = 3|WPA2_PSK = 4
    */
    public short key_mgmt;
      
    /**
    * Pairwise ciphers for WPA: NONE = 0|TKIP = 1|CCMP = 2
    */
    public short pairwise_cipher;
      
    /**
    * Group ciphers for WPA: WEP40 = 0|WEP104 = 1|TKIP = 2|CCMP = 3
    */
    public short group_cipher;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_SET_WIFI_AP_ENABLED;
              
        
        for (int i = 0; i < ssid.length; i++) {
            packet.payload.putByte(ssid[i]);
        }
                    
              
        
        for (int i = 0; i < bssid.length; i++) {
            packet.payload.putByte(bssid[i]);
        }
                    
              
        packet.payload.putUnsignedByte(auth_algorithm);
              
        packet.payload.putUnsignedByte(allowed_protocols);
              
        packet.payload.putUnsignedByte(key_mgmt);
              
        packet.payload.putUnsignedByte(pairwise_cipher);
              
        packet.payload.putUnsignedByte(group_cipher);
        
        return packet;
    }

    /**
    * Decode a set_wifi_ap_enabled message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
         
        for (int i = 0; i < this.ssid.length; i++) {
            this.ssid[i] = payload.getByte();
        }
                
              
         
        for (int i = 0; i < this.bssid.length; i++) {
            this.bssid[i] = payload.getByte();
        }
                
              
        this.auth_algorithm = payload.getUnsignedByte();
              
        this.allowed_protocols = payload.getUnsignedByte();
              
        this.key_mgmt = payload.getUnsignedByte();
              
        this.pairwise_cipher = payload.getUnsignedByte();
              
        this.group_cipher = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_set_wifi_ap_enabled(){
        msgid = MAVLINK_MSG_ID_SET_WIFI_AP_ENABLED;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_set_wifi_ap_enabled(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SET_WIFI_AP_ENABLED;
        unpack(mavLinkPacket.payload);        
    }

     
    /**
    * Sets the buffer of this message with a string, adds the necessary padding
    */
    public void setSsid(String str) {
        int len = Math.min(str.length(), 50);
        for (int i=0; i<len; i++) {
            ssid[i] = (byte) str.charAt(i);
        }

        for (int i=len; i<50; i++) {            // padding for the rest of the buffer
            ssid[i] = 0;
        }
    }

    /**
    * Gets the message, formated as a string
    */
    public String getSsid() {
        String result = "";
        for (int i = 0; i < 50; i++) {
            if (ssid[i] != 0)
                result = result + (char) ssid[i];
            else
                break;
        }
        return result;

    }
                          
    /**
    * Sets the buffer of this message with a string, adds the necessary padding
    */
    public void setBssid(String str) {
        int len = Math.min(str.length(), 6);
        for (int i=0; i<len; i++) {
            bssid[i] = (byte) str.charAt(i);
        }

        for (int i=len; i<6; i++) {            // padding for the rest of the buffer
            bssid[i] = 0;
        }
    }

    /**
    * Gets the message, formated as a string
    */
    public String getBssid() {
        String result = "";
        for (int i = 0; i < 6; i++) {
            if (bssid[i] != 0)
                result = result + (char) bssid[i];
            else
                break;
        }
        return result;

    }
                                   
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_SET_WIFI_AP_ENABLED -"+" ssid:"+ssid+" bssid:"+bssid+" auth_algorithm:"+auth_algorithm+" allowed_protocols:"+allowed_protocols+" key_mgmt:"+key_mgmt+" pairwise_cipher:"+pairwise_cipher+" group_cipher:"+group_cipher+"";
    }
}
        