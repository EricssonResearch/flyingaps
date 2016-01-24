import org.mavlink.IMAVLinkMessage;
import org.mavlink.MAVLinkReader;
import org.mavlink.messages.MAVLinkMessage;

import org.mavlink.messages.ja4rtor.msg_statustext;
import org.mavlink.messages.ja4rtor.msg_sys_status;

import org.mavlink.messages.ja4rtor.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * The class handles messages in the mavlink format. It prints the raw information and interacts with WpProtocol when necessary.
 * Created by eminsaf
 */

class DroneCommunication {
    public static String[] MAVLINK_MESSAGE_TYPES = new String[256];
    public static String[] MAVLINK_SEVERITIES = new String[9];

    DroneCommunication(){}

    static {
        MAVLINK_MESSAGE_TYPES[0] =    "HEARTBEAT";
        MAVLINK_MESSAGE_TYPES[1] =    "SYS_STATUS";
        MAVLINK_MESSAGE_TYPES[2] =    "SYSTEM_TIME";
        MAVLINK_MESSAGE_TYPES[4] =    "PING";
        MAVLINK_MESSAGE_TYPES[5] =    "CHANGE_OPERATOR_CONTROL";
        MAVLINK_MESSAGE_TYPES[6] =    "CHANGE_OPERATOR_CONTROL_ACK";
        MAVLINK_MESSAGE_TYPES[7] =    "AUTH_KEY";
        MAVLINK_MESSAGE_TYPES[11] =   "SET_MODE";
        MAVLINK_MESSAGE_TYPES[20] =   "PARAM_REQUEST_READ";
        MAVLINK_MESSAGE_TYPES[21] =   "PARAM_REQUEST_LIST";
        MAVLINK_MESSAGE_TYPES[22] =   "PARAM_VALUE";
        MAVLINK_MESSAGE_TYPES[23] =   "PARAM_SET";
        MAVLINK_MESSAGE_TYPES[24] =   "GPS_RAW_INT";
        MAVLINK_MESSAGE_TYPES[25] =   "GPS_STATUS";
        MAVLINK_MESSAGE_TYPES[26] =   "SCALED_IMU";
        MAVLINK_MESSAGE_TYPES[27] =   "RAW_IMU";
        MAVLINK_MESSAGE_TYPES[28] =   "RAW_PRESSURE";
        MAVLINK_MESSAGE_TYPES[29] =   "SCALED_PRESSURE";
        MAVLINK_MESSAGE_TYPES[30] =   "ATTITUDE";
        MAVLINK_MESSAGE_TYPES[31] =   "ATTITUDE_QUATERNION";
        MAVLINK_MESSAGE_TYPES[32] =   "LOCAL_POSITION_NED";
        MAVLINK_MESSAGE_TYPES[33] =   "GLOBAL_POSITION_INT";
        MAVLINK_MESSAGE_TYPES[34] =   "RC_CHANNELS_SCALED";
        MAVLINK_MESSAGE_TYPES[35] =   "RC_CHANNELS_RAW";
        MAVLINK_MESSAGE_TYPES[36] =   "SERVO_OUTPUT_RAW";
        MAVLINK_MESSAGE_TYPES[37] =   "MISSION_REQUEST_PARTIAL_LIST";
        MAVLINK_MESSAGE_TYPES[38] =   "MISSION_WRITE_PARTIAL_LIST";
        MAVLINK_MESSAGE_TYPES[39] =   "MISSION_ITEM";
        MAVLINK_MESSAGE_TYPES[40] =   "MISSION_REQUEST";
        MAVLINK_MESSAGE_TYPES[41] =   "MISSION_SET_CURRENT";
        MAVLINK_MESSAGE_TYPES[42] =   "MISSION_CURRENT";
        MAVLINK_MESSAGE_TYPES[43] =   "MISSION_REQUEST_LIST";
        MAVLINK_MESSAGE_TYPES[44] =   "MISSION_COUNT";
        MAVLINK_MESSAGE_TYPES[45] =   "MISSION_CLEAR_ALL";
        MAVLINK_MESSAGE_TYPES[46] =   "MISSION_ITEM_REACHED";
        MAVLINK_MESSAGE_TYPES[47] =   "MISSION_ACK";
        MAVLINK_MESSAGE_TYPES[48] =   "SET_GPS_GLOBAL_ORIGIN";
        MAVLINK_MESSAGE_TYPES[49] =   "GPS_GLOBAL_ORIGIN";
        MAVLINK_MESSAGE_TYPES[50] =   "PARAM_MAP_RC";
        MAVLINK_MESSAGE_TYPES[54] =   "SAFETY_SET_ALLOWED_AREA";
        MAVLINK_MESSAGE_TYPES[55] =   "SAFETY_ALLOWED_AREA";
        MAVLINK_MESSAGE_TYPES[61] =   "ATTITUDE_QUATERNION_COV";
        MAVLINK_MESSAGE_TYPES[62] =   "NAV_CONTROLLER_OUTPUT";
        MAVLINK_MESSAGE_TYPES[63] =   "GLOBAL_POSITION_INT_COV";
        MAVLINK_MESSAGE_TYPES[64] =   "LOCAL_POSITION_NED_COV";
        MAVLINK_MESSAGE_TYPES[65] =   "RC_CHANNELS";
        MAVLINK_MESSAGE_TYPES[66] =   "REQUEST_DATA_STREAM";
        MAVLINK_MESSAGE_TYPES[67] =   "DATA_STREAM";
        MAVLINK_MESSAGE_TYPES[69] =   "MANUAL_CONTROL";
        MAVLINK_MESSAGE_TYPES[70] =   "RC_CHANNELS_OVERRIDE";
        MAVLINK_MESSAGE_TYPES[73] =   "MISSION_ITEM_INT";
        MAVLINK_MESSAGE_TYPES[74] =   "VFR_HUD";
        MAVLINK_MESSAGE_TYPES[75] =   "COMMAND_INT";
        MAVLINK_MESSAGE_TYPES[76] =   "COMMAND_LONG";
        MAVLINK_MESSAGE_TYPES[77] =   "COMMAND_ACK";
        MAVLINK_MESSAGE_TYPES[81] =   "MANUAL_SETPOINT";
        MAVLINK_MESSAGE_TYPES[82] =   "SET_ATTITUDE_TARGET";
        MAVLINK_MESSAGE_TYPES[83] =   "ATTITUDE_TARGET";
        MAVLINK_MESSAGE_TYPES[84] =   "SET_POSITION_TARGET_LOCAL_NED";
        MAVLINK_MESSAGE_TYPES[85] =   "POSITION_TARGET_LOCAL_NED";
        MAVLINK_MESSAGE_TYPES[86] =   "SET_POSITION_TARGET_GLOBAL_INT";
        MAVLINK_MESSAGE_TYPES[87] =   "POSITION_TARGET_GLOBAL_INT";
        MAVLINK_MESSAGE_TYPES[89] =   "LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET";
        MAVLINK_MESSAGE_TYPES[90] =   "HIL_STATE";
        MAVLINK_MESSAGE_TYPES[91] =   "HIL_CONTROLS";
        MAVLINK_MESSAGE_TYPES[92] =   "HIL_RC_INPUTS_RAW";
        MAVLINK_MESSAGE_TYPES[100] =  "OPTICAL_FLOW";
        MAVLINK_MESSAGE_TYPES[101] =  "GLOBAL_VISION_POSITION_ESTIMATE";
        MAVLINK_MESSAGE_TYPES[102] =  "VISION_POSITION_ESTIMATE";
        MAVLINK_MESSAGE_TYPES[103] =  "VISION_SPEED_ESTIMATE";
        MAVLINK_MESSAGE_TYPES[104] =  "VICON_POSITION_ESTIMATE";
        MAVLINK_MESSAGE_TYPES[105] =  "HIGHRES_IMU";
        MAVLINK_MESSAGE_TYPES[106] =  "OPTICAL_FLOW_RAD";
        MAVLINK_MESSAGE_TYPES[107] =  "HIL_SENSOR";
        MAVLINK_MESSAGE_TYPES[110] =  "FILE_TRANSFER_PROTOCOL";
        MAVLINK_MESSAGE_TYPES[111] =  "TIMESYNC";
        MAVLINK_MESSAGE_TYPES[112] =  "CAMERA_TRIGGER";
        MAVLINK_MESSAGE_TYPES[113] =  "HIL_GPS";
        MAVLINK_MESSAGE_TYPES[114] =  "HIL_OPTICAL_FLOW";
        MAVLINK_MESSAGE_TYPES[115] =  "HIL_STATE_QUATERNION";
        MAVLINK_MESSAGE_TYPES[116] =  "SCALED_IMU2";
        MAVLINK_MESSAGE_TYPES[117] =  "LOG_REQUEST_LIST";
        MAVLINK_MESSAGE_TYPES[118] =  "LOG_ENTRY";
        MAVLINK_MESSAGE_TYPES[119] =  "LOG_REQUEST_DATA";
        MAVLINK_MESSAGE_TYPES[120] =  "LOG_DATA";
        MAVLINK_MESSAGE_TYPES[121] =  "LOG_ERASE";
        MAVLINK_MESSAGE_TYPES[122] =  "LOG_REQUEST_END";
        MAVLINK_MESSAGE_TYPES[124] =  "GPS2_RAW";
        MAVLINK_MESSAGE_TYPES[125] =  "POWER_STATUS";
        MAVLINK_MESSAGE_TYPES[127] =  "GPS_RTK";
        MAVLINK_MESSAGE_TYPES[128] =  "GPS2_RTK";
        MAVLINK_MESSAGE_TYPES[129] =  "SCALED_IMU3";
        MAVLINK_MESSAGE_TYPES[130] =  "DATA_TRANSMISSION_HANDSHAKE";
        MAVLINK_MESSAGE_TYPES[131] =  "ENCAPSULATED_DATA";
        MAVLINK_MESSAGE_TYPES[132] =  "DISTANCE_SENSOR";
        MAVLINK_MESSAGE_TYPES[133] =  "TERRAIN_REQUEST";
        MAVLINK_MESSAGE_TYPES[134] =  "TERRAIN_DATA";
        MAVLINK_MESSAGE_TYPES[135] =  "TERRAIN_CHECK";
        MAVLINK_MESSAGE_TYPES[136] =  "TERRAIN_REPORT";
        MAVLINK_MESSAGE_TYPES[137] =  "SCALED_PRESSURE2";
        MAVLINK_MESSAGE_TYPES[138] =  "ATT_POS_MOCAP";
        MAVLINK_MESSAGE_TYPES[139] =  "SET_ACTUATOR_CONTROL_TARGET";
        MAVLINK_MESSAGE_TYPES[140] =  "ACTUATOR_CONTROL_TARGET";
        MAVLINK_MESSAGE_TYPES[141] =  "ALTITUDE";
        MAVLINK_MESSAGE_TYPES[142] =  "RESOURCE_REQUEST";
        MAVLINK_MESSAGE_TYPES[143] =  "SCALED_PRESSURE3";
        MAVLINK_MESSAGE_TYPES[146] =  "CONTROL_SYSTEM_STATE";
        MAVLINK_MESSAGE_TYPES[147] =  "BATTERY_STATUS";
        MAVLINK_MESSAGE_TYPES[148] =  "AUTOPILOT_VERSION";
        MAVLINK_MESSAGE_TYPES[149] =  "LANDING_TARGET";
        MAVLINK_MESSAGE_TYPES[241] =  "VIBRATION";
        MAVLINK_MESSAGE_TYPES[242] =  "HOME_POSITION";
        MAVLINK_MESSAGE_TYPES[243] =  "SET_HOME_POSITION";
        MAVLINK_MESSAGE_TYPES[244] =  "MESSAGE_INTERVAL";
        MAVLINK_MESSAGE_TYPES[245] =  "EXTENDED_SYS_STATE";
        MAVLINK_MESSAGE_TYPES[248] =  "V2_EXTENSION";
        MAVLINK_MESSAGE_TYPES[249] =  "MEMORY_VECT";
        MAVLINK_MESSAGE_TYPES[250] =  "DEBUG_VECT";
        MAVLINK_MESSAGE_TYPES[251] =  "NAMED_VALUE_FLOAT";
        MAVLINK_MESSAGE_TYPES[252] =  "NAMED_VALUE_INT";
        MAVLINK_MESSAGE_TYPES[253] =  "STATUSTEXT";
        MAVLINK_MESSAGE_TYPES[254] =  "DEBUG";

        MAVLINK_SEVERITIES[0] = "MAV_SEVERITY_EMERGENCY";
        MAVLINK_SEVERITIES[1] = "MAV_SEVERITY_ALERT";
        MAVLINK_SEVERITIES[2] = "MAV_SEVERITY_CRITICAL";
        MAVLINK_SEVERITIES[3] = "MAV_SEVERITY_ERROR";
        MAVLINK_SEVERITIES[4] = "MAV_SEVERITY_WARNING";
        MAVLINK_SEVERITIES[5] = "MAV_SEVERITY_NOTICE";
        MAVLINK_SEVERITIES[6] = "MAV_SEVERITY_INFO";
        MAVLINK_SEVERITIES[7] = "MAV_SEVERITY_DEBUG";

    }


    public static String mavlink_decode(byte [] buffer, WpProtocol wpProtocol) {
        DataInputStream dis = null;
        MAVLinkReader reader = null;
        MAVLinkMessage msg = null;
        String log = "";

        dis = new DataInputStream(new ByteArrayInputStream(buffer));
        reader = new MAVLinkReader(dis, IMAVLinkMessage.MAVPROT_PACKET_START_V10);

        do {
            msg = reader.getNextMessageWithoutBlocking();
            if (msg != null) {

                if(msg.messageType == 0)
                    continue;

                log += "#" + msg.sequence + " ";

                switch (msg.messageType) {
                    case 0:
                        /* HEARTBEAT */
                        log += "HEARTBEAT \n";
                        msg_heartbeat heartbeat = (msg_heartbeat)msg;
                        log += ">> "+heartbeat.toString();
                        break;
                    case 1:
                        /* SYS_STATUS */
                        log += "SYS_STATUS \n";
                        msg_sys_status sysStatus = (msg_sys_status)msg;
                        log += ">> "+sysStatus.toString()+"\n";
                        break;
                    case 2:
                        /* SYSTEM_TIME */
                        log += "SYSTEM_TIME \n";
                        msg_system_time systemTime = (msg_system_time)msg;
                        log += ">> "+systemTime.toString();
                        break;
                    case 4:
                        /* PING */
                        log += "PING \n";
                        break;
                    case 5:
                        /* CHANGE_OPERATOR_CONTROL */
                        log += "CHANGE_OPERATOR_CONTROL \n";
                        break;
                    case 6:
                        /* CHANGE_OPERATOR_CONTROL_ACK */
                        log += "CHANGE_OPERATOR_CONTROL_ACK \n";
                        break;
                    case 7:
                        /* AUTH_KEY */
                        log += "AUTH_KEY \n";
                        break;
                    case 11:
                        /* SET_MODE */
                        log += "SET_MODE \n";
                        break;
                    case 20:
                        /* PARAM_REQUEST_READ */
                        log += "PARAM_REQUEST_READ \n";
                        break;
                    case 21:
                        /* PARAM_REQUEST_LIST */
                        log += "PARAM_REQUEST_LIST \n";
                        break;
                    case 22:
                        /* PARAM_VALUE */
                        log += "PARAM_VALUE \n";
                        msg_param_value mpv = (msg_param_value) msg;
                        log += ">> " + mpv.toString() + "\n";
                        break;
                    case 23:
                        /* PARAM_SET */
                        log += "PARAM_SET \n";
                        break;
                    case 24:
                        /* GPS_RAW_INT */
                        log += "GPS_RAW_INT \n";
                        break;
                    case 25:
                        /* GPS_STATUS */
                        log += "GPS_STATUS \n";
                        break;
                    case 26:
                        /* SCALED_IMU */
                        log += "SCALED_IMU \n";
                        break;
                    case 27:
                        /* RAW_IMU */
                        log += "RAW_IMU \n";
                        break;
                    case 28:
                        /* RAW_PRESSURE */
                        log += "RAW_PRESSURE \n";
                        break;
                    case 29:
                        /* SCALED_PRESSURE */
                        log += "SCALED_PRESSURE \n";
                        break;
                    case 30:
                        /* ATTITUDE */
                        log += "ATTITUDE \n";
                        msg_attitude attitude = (msg_attitude)msg;
                        log += ">> "+ attitude.toString();
                        break;
                    case 31:
                        /* ATTITUDE_QUATERNION */
                        log += "ATTITUDE_QUATERNION \n";
                        break;
                    case 32:
                        /* LOCAL_POSITION_NED */
                        log += "LOCAL_POSITION_NED \n";
                        break;
                    case 33:
                        /* GLOBAL_POSITION_INT */
                        log += "GLOBAL_POSITION_INT \n";
                        msg_global_position_int globalPositionInt = (msg_global_position_int)msg;
                        log += ">> "+ globalPositionInt.toString();
                        break;
                    case 34:
                        /* RC_CHANNELS_SCALED */
                        log += "RC_CHANNELS_SCALED \n";
                        break;
                    case 35:
                        /* RC_CHANNELS_RAW */
                        log += "RC_CHANNELS_RAW \n";
                        msg_rc_channels_raw rcChannelsRaw = (msg_rc_channels_raw)msg;
                        log += ">> "+rcChannelsRaw.toString();
                        break;
                    case 36:
                        /* SERVO_OUTPUT_RAW */
                        log += "SERVO_OUTPUT_RAW \n";
                        break;
                    case 37:
                        /* MISSION_REQUEST_PARTIAL_LIST */
                        log += "MISSION_REQUEST_PARTIAL_LIST \n";
                        break;
                    case 38:
                        /* MISSION_WRITE_PARTIAL_LIST */
                        log += "MISSION_WRITE_PARTIAL_LIST \n";
                        break;
                    case 39:
                        /* MISSION_ITEM */
                        log += "MISSION_ITEM \n";
                        msg_mission_item missionItem = (msg_mission_item)msg;
                        log += ">> "+missionItem.toString();
                        break;
                    case 40:
                        /* MISSION_REQUEST */
                        log += "MISSION_REQUEST \n";
                        msg_mission_request missionRequest = (msg_mission_request)msg;
                        log += ">> "+missionRequest.toString();
                        wpProtocol.notifyLock(missionRequest.seq,0);
                        break;
                    case 41:
                        /* MISSION_SET_CURRENT */
                        log += "MISSION_SET_CURRENT \n";
                        break;
                    case 42:
                        /* MISSION_CURRENT */
                        log += "MISSION_CURRENT \n";
                        msg_mission_current missionCurrent = (msg_mission_current)msg;
                        log += ">> "+missionCurrent.toString();
                        break;
                    case 43:
                        /* MISSION_REQUEST_LIST */
                        log += "MISSION_REQUEST_LIST \n";
                        break;
                    case 44:
                        /* MISSION_COUNT */
                        log += "MISSION_COUNT \n";
                        msg_mission_count missionCount = (msg_mission_count)msg;
                        log += ">> "+missionCount.toString();
                        wpProtocol.notifyLock(missionCount.count, 3);
                        break;
                    case 45:
                        /* MISSION_CLEAR_ALL */
                        log += "MISSION_CLEAR_ALL \n";
                        break;
                    case 46:
                        /* MISSION_ITEM_REACHED */
                        log += "MISSION_ITEM_REACHED \n";
                        break;
                    case 47:
                        /* MISSION_ACK */
                        log += "MISSION_ACK \n";
                        msg_mission_ack mma = (msg_mission_ack) msg;
                        log+= ">> " + mma.toString() + "\n";
                        wpProtocol.notifyLock(-1,2);
                        break;
                    case 48:
                        /* SET_GPS_GLOBAL_ORIGIN */
                        log += "SET_GPS_GLOBAL_ORIGIN \n";
                        break;
                    case 49:
                        /* GPS_GLOBAL_ORIGIN */
                        log += "GPS_GLOBAL_ORIGIN \n";
                        break;
                    case 50:
                        /* PARAM_MAP_RC */
                        log += "PARAM_MAP_RC \n";
                        break;
                    case 54:
                        /* SAFETY_SET_ALLOWED_AREA */
                        log += "SAFETY_SET_ALLOWED_AREA \n";
                        break;
                    case 55:
                        /* SAFETY_ALLOWED_AREA */
                        log += "SAFETY_ALLOWED_AREA \n";
                        break;
                    case 61:
                        /* ATTITUDE_QUATERNION_COV */
                        log += "ATTITUDE_QUATERNION_COV \n";
                        break;
                    case 62:
                        /* NAV_CONTROLLER_OUTPUT */
                        log += "NAV_CONTROLLER_OUTPUT \n";
                        msg_nav_controller_output mnco = (msg_nav_controller_output) msg;
                        log+= ">> " + mnco.toString() + "\n";
                        break;
                    case 63:
                        /* GLOBAL_POSITION_INT_COV */
                        log += "GLOBAL_POSITION_INT_COV \n";
                        break;
                    case 64:
                        /* LOCAL_POSITION_NED_COV */
                        log += "LOCAL_POSITION_NED_COV \n";
                        break;
                    case 65:
                        /* RC_CHANNELS */
                        log += "RC_CHANNELS \n";
                        break;
                    case 66:
                        /* REQUEST_DATA_STREAM */
                        log += "REQUEST_DATA_STREAM \n";
                        msg_request_data_stream mrd = (msg_request_data_stream) msg;
                        log+= ">> " + mrd.toString() + "\n";
                        break;
                    case 67:
                        /* DATA_STREAM */
                        log += "DATA_STREAM \n";
                        break;
                    case 69:
                        /* MANUAL_CONTROL */
                        log += "MANUAL_CONTROL \n";
                        break;
                    case 70:
                        /* RC_CHANNELS_OVERRIDE */
                        log += "RC_CHANNELS_OVERRIDE \n";
                        break;
                    case 73:
                        /* MISSION_ITEM_INT */
                        log += "MISSION_ITEM_INT \n";
                        break;
                    case 74:
                        /* VFR_HUD */
                        log += "VFR_HUD \n";
                        break;
                    case 75:
                        /* COMMAND_INT */
                        log += "COMMAND_INT \n";
                        break;
                    case 76:
                        /* COMMAND_LONG */
                        log += "COMMAND_LONG \n";
                        break;
                    case 77:
                        /* COMMAND_ACK */
                        log += "COMMAND_ACK \n";
                        msg_command_ack mca = (msg_command_ack) msg;
                        log += ">> " + mca.toString() + "\n";
                        break;
                    case 81:
                        /* MANUAL_SETPOINT */
                        log += "MANUAL_SETPOINT \n";
                        break;
                    case 82:
                        /* SET_ATTITUDE_TARGET */
                        log += "SET_ATTITUDE_TARGET \n";
                        break;
                    case 83:
                        /* ATTITUDE_TARGET */
                        log += "ATTITUDE_TARGET \n";
                        break;
                    case 84:
                        /* SET_POSITION_TARGET_LOCAL_NED */
                        log += "SET_POSITION_TARGET_LOCAL_NED \n";
                        break;
                    case 85:
                        /* POSITION_TARGET_LOCAL_NED */
                        log += "POSITION_TARGET_LOCAL_NED \n";
                        break;
                    case 86:
                        /* SET_POSITION_TARGET_GLOBAL_INT */
                        log += "SET_POSITION_TARGET_GLOBAL_INT \n";
                        break;
                    case 87:
                        /* POSITION_TARGET_GLOBAL_INT */
                        log += "POSITION_TARGET_GLOBAL_INT \n";
                        break;
                    case 89:
                        /* LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET */
                        log += "LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET \n";
                        break;
                    case 90:
                        /* HIL_STATE */
                        log += "HIL_STATE \n";
                        break;
                    case 91:
                        /* HIL_CONTROLS */
                        log += "HIL_CONTROLS \n";
                        break;
                    case 92:
                        /* HIL_RC_INPUTS_RAW */
                        log += "HIL_RC_INPUTS_RAW \n";
                        break;
                    case 100:
                        /* OPTICAL_FLOW */
                        log += "OPTICAL_FLOW \n";
                        break;
                    case 101:
                        /* GLOBAL_VISION_POSITION_ESTIMATE */
                        log += "GLOBAL_VISION_POSITION_ESTIMATE \n";
                        break;
                    case 102:
                        /* VISION_POSITION_ESTIMATE */
                        log += "VISION_POSITION_ESTIMATE \n";
                        break;
                    case 103:
                        /* VISION_SPEED_ESTIMATE */
                        log += "VISION_SPEED_ESTIMATE \n";
                        break;
                    case 104:
                        /* VICON_POSITION_ESTIMATE */
                        log += "VICON_POSITION_ESTIMATE \n";
                        break;
                    case 105:
                        /* HIGHRES_IMU */
                        log += "HIGHRES_IMU \n";
                        break;
                    case 106:
                        /* OPTICAL_FLOW_RAD */
                        log += "OPTICAL_FLOW_RAD \n";
                        break;
                    case 107:
                        /* HIL_SENSOR */
                        log += "HIL_SENSOR \n";
                        break;
                    case 110:
                        /* FILE_TRANSFER_PROTOCOL */
                        log += "FILE_TRANSFER_PROTOCOL \n";
                        break;
                    case 111:
                        /* TIMESYNC */
                        log += "TIMESYNC \n";
                        break;
                    case 112:
                        /* CAMERA_TRIGGER */
                        log += "CAMERA_TRIGGER \n";
                        break;
                    case 113:
                        /* HIL_GPS */
                        log += "HIL_GPS \n";
                        break;
                    case 114:
                        /* HIL_OPTICAL_FLOW */
                        log += "HIL_OPTICAL_FLOW \n";
                        break;
                    case 115:
                        /* HIL_STATE_QUATERNION */
                        log += "HIL_STATE_QUATERNION \n";
                        break;
                    case 116:
                        /* SCALED_IMU2 */
                        log += "SCALED_IMU2 \n";
                        break;
                    case 117:
                        /* LOG_REQUEST_LIST */
                        log += "LOG_REQUEST_LIST \n";
                        break;
                    case 118:
                        /* LOG_ENTRY */
                        log += "LOG_ENTRY \n";
                        break;
                    case 119:
                        /* LOG_REQUEST_DATA */
                        log += "LOG_REQUEST_DATA \n";
                        break;
                    case 120:
                        /* LOG_DATA */
                        log += "LOG_DATA \n";
                        break;
                    case 121:
                        /* LOG_ERASE */
                        log += "LOG_ERASE \n";
                        break;
                    case 122:
                        /* LOG_REQUEST_END */
                        log += "LOG_REQUEST_END \n";
                        break;
                    case 124:
                        /* GPS2_RAW */
                        log += "GPS2_RAW \n";
                        break;
                    case 125:
                        /* POWER_STATUS */
                        log += "POWER_STATUS \n";
                        msg_power_status mps = (msg_power_status) msg;
                        log += ">> " + mps.toString() + "\n";
                        break;
                    case 127:
                        /* GPS_RTK */
                        log += "GPS_RTK \n";
                        break;
                    case 128:
                        /* GPS2_RTK */
                        log += "GPS2_RTK \n";
                        break;
                    case 129:
                        /* SCALED_IMU3 */
                        log += "SCALED_IMU3 \n";
                        break;
                    case 130:
                        /* DATA_TRANSMISSION_HANDSHAKE */
                        log += "DATA_TRANSMISSION_HANDSHAKE \n";
                        break;
                    case 131:
                        /* ENCAPSULATED_DATA */
                        log += "ENCAPSULATED_DATA \n";
                        break;
                    case 132:
                        /* DISTANCE_SENSOR */
                        log += "DISTANCE_SENSOR \n";
                        break;
                    case 133:
                        /* TERRAIN_REQUEST */
                        log += "TERRAIN_REQUEST \n";
                        break;
                    case 134:
                        /* TERRAIN_DATA */
                        log += "TERRAIN_DATA \n";
                        break;
                    case 135:
                        /* TERRAIN_CHECK */
                        log += "TERRAIN_CHECK \n";
                        break;
                    case 136:
                        /* TERRAIN_REPORT */
                        log += "TERRAIN_REPORT \n";
                        break;
                    case 137:
                        /* SCALED_PRESSURE2 */
                        log += "SCALED_PRESSURE2 \n";
                        break;
                    case 138:
                        /* ATT_POS_MOCAP */
                        log += "ATT_POS_MOCAP \n";
                        break;
                    case 139:
                        /* SET_ACTUATOR_CONTROL_TARGET */
                        log += "SET_ACTUATOR_CONTROL_TARGET \n";
                        break;
                    case 140:
                        /* ACTUATOR_CONTROL_TARGET */
                        log += "ACTUATOR_CONTROL_TARGET \n";
                        break;
                    case 141:
                        /* ALTITUDE */
                        log += "ALTITUDE \n";
                        break;
                    case 142:
                        /* RESOURCE_REQUEST */
                        log += "RESOURCE_REQUEST \n";
                        break;
                    case 143:
                        /* SCALED_PRESSURE3 */
                        log += "SCALED_PRESSURE3 \n";
                        break;
                    case 146:
                        /* CONTROL_SYSTEM_STATE */
                        log += "CONTROL_SYSTEM_STATE \n";
                        break;
                    case 147:
                        /* BATTERY_STATUS */
                        log += "BATTERY_STATUS \n";
                        msg_battery_status mbs = (msg_battery_status) msg;
                        log += ">> " + mbs.toString() + "\n";
                        break;
                    case 148:
                        /* AUTOPILOT_VERSION */
                        log += "AUTOPILOT_VERSION \n";
                        break;
                    case 149:
                        /* LANDING_TARGET */
                        log += "LANDING_TARGET \n";
                        break;
                    case 241:
                        /* VIBRATION */
                        log += "VIBRATION \n";
                        break;
                    case 242:
                        /* HOME_POSITION */
                        log += "HOME_POSITION \n";
                        msg_home_position hp = (msg_home_position) msg;
                        log += ">> " + hp.toString() + "\n";
                        break;
                    case 243:
                        /* SET_HOME_POSITION */
                        log += "SET_HOME_POSITION \n";
                        break;
                    case 244:
                        /* MESSAGE_INTERVAL */
                        log += "MESSAGE_INTERVAL \n";
                        break;
                    case 245:
                        /* EXTENDED_SYS_STATE */
                        log += "EXTENDED_SYS_STATE \n";
                        break;
                    case 248:
                        /* V2_EXTENSION */
                        log += "V2_EXTENSION \n";
                        break;
                    case 249:
                        /* MEMORY_VECT */
                        log += "MEMORY_VECT \n";
                        break;
                    case 250:
                        /* DEBUG_VECT */
                        log += "DEBUG_VECT \n";
                        break;
                    case 251:
                        /* NAMED_VALUE_FLOAT */
                        log += "NAMED_VALUE_FLOAT \n";
                        break;
                    case 252:
                        /* NAMED_VALUE_INT */
                        log += "NAMED_VALUE_INT \n";
                        break;
                    case 253:
                        /* STATUSTEXT */
                        log += "STATUSTEXT \n";
                        msg_statustext msgStatustext = (msg_statustext)msg;
                        log += ">> Text: "+msgStatustext.getText() + " Sev: "+msgStatustext.severity +"\n";
                        break;
                    case 254:
                        /* DEBUG */
                        log += "DEBUG \n";
                        break;
                    default:
                        /* Unknown message */
                        log += "Unknown "+msg.messageType+" !! \n";
                        break;
                }
            }
        } while( msg != null);

        try {
            dis.close();
        } catch (IOException e) {

        }
        return log;
    }
}

