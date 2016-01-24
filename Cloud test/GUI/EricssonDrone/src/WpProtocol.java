import org.mavlink.messages.ja4rtor.msg_mission_ack;
import org.mavlink.messages.ja4rtor.msg_mission_request;
import se.kth.mf2063.internetdrone.Message;
import se.kth.mf2063.internetdrone.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class to handle the waypoint protocol. In need of being rewritten.
 */
public class WpProtocol extends Thread {
    /**
     * The System ID of the Ground Control Station.
     */
    int sysId = 255;
    /**
     * The Component ID of the Ground Control Station.
     */
    int componentId = 190;
    /**
     * The System ID of the drone.
     */
    int target_sysId = 1;
    /**
     * The Component ID of the drone.
     */
    int target_componentId = 1;
    /**
     * The queue with messages to be sent.
     */
    private LinkedBlockingQueue<byte []> q;
    /**
     * Shared lock between the threads for synchronization.
     */
    private Object lock;
    /**
     * The output stream to send messages.
     */
    private ObjectOutputStream objectOut;
    /**
     * Expected sequence to be requested by the drone. Duplicate requests can and will occur which needs to be handled.
     */
    private int expSeq = 0;
    /**
     * Tells if it is in read mode.
     */
    private volatile Boolean read = false;
    /**
     * Tells if it is in write mode.
     */
    private volatile Boolean write = false;
    /**
     * Keeps track of the number of messages.
     */
    private int count = 0;

    /**
     * Creates the waypoint protocol object and sets the lock and q variables.
     */
    WpProtocol(Object lock, LinkedBlockingQueue<byte []> q) {
        this.lock = lock;
        this.q = q;
    }

    /**
     * Sets the output stream.
     * @param  objectOut  The output streem.
     */
    public void setObjectOut(ObjectOutputStream objectOut){
        this.objectOut = objectOut;
    }

    /**
     * The run method for the thread with an infinite loop.
     */
    @Override
    public void run() {
        while(true) {
            waitLock();
            startSequence();
        }
    }

    /**
     * Wait for another thread/the drone before the next step.
     */
    private void waitLock() {
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by the receiving server when a message in the waypoint protocol is received.
     * @param  seq  The sequence number in the waypoint protocol
     * @param  mode The mode for the waypoint protocol, read/write/done with read/write
     */
    public void notifyLock(int seq, int mode) {
        if((expSeq == seq) | (mode != 0)) { //1, write mission from q, 2, mission_ack, 3, mission count, 4, read mission from drone
            if(mode == 3)
                count = seq;
            if(mode == 4)
                read = true;
            if(mode == 1)
                write = true;
            synchronized (lock) {
                lock.notify();
            }
            System.out.println("Notified: " + seq);
        }
    }

    /**
     * Called by the infinite loop when a read or a write is initiated. Prepares the needed messages to be sent.
     */
    private void startSequence() {
        if(write) {
            expSeq = 0;
            while (q.size() > 1) {
                send(MessageType.MAVLINK, q.poll());
                System.out.println("Sent! " + q.size() + " left.");
                System.out.println("Waiting for: " + expSeq);
                waitLock();
                expSeq++;
            }
            send(MessageType.MAVLINK, q.poll());//Last mission_item
        }
        else if(read) {
            byte[] mavLinkByteArray = null;
            send(MessageType.MAVLINK, q.poll());
            waitLock();
            expSeq = 0;
            while(count != 0) {
                msg_mission_request mmr = new msg_mission_request();
                mmr.target_system = target_sysId;
                mmr.target_component = target_componentId;
                mmr.seq = expSeq;
                try {
                    mavLinkByteArray = mmr.encode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                send(MessageType.MAVLINK, mavLinkByteArray);
                count--;

                waitLock();
                expSeq++;
            }
            msg_mission_ack mi = new msg_mission_ack(sysId, componentId);
            mi.target_system = target_sysId;
            mi.target_component = target_componentId;
            mi.type = 0;

            try {
                mavLinkByteArray = mi.encode();
            } catch (IOException e) {
                e.printStackTrace();
            }
            send(MessageType.MAVLINK, mavLinkByteArray);
        }
        read = false;
        write = false;
        expSeq = 0;
    }

    /**
     * Sends the message needed to comply with the waypoint protocol.
     * @param  messageType  The type of the medssage to be sent(either Mavlink to the drone or a command to the phone)
     * @param  mavLinkByteArray The Mavlink message byte array if messageType is Mavlink
     * @see    MessageType
     */
    private void send(MessageType messageType, byte[] mavLinkByteArray) {
        Message msg = new Message();
        msg.setByteArray(mavLinkByteArray);
        msg.setMessageType(messageType);

        try {
            synchronized(objectOut) {
                objectOut.writeObject(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
