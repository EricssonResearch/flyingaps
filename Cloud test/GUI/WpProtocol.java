import org.mavlink.messages.ja4rtor.msg_mission_ack;
import org.mavlink.messages.ja4rtor.msg_mission_request;
import se.kth.mf2063.internetdrone.Message;
import se.kth.mf2063.internetdrone.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class WpProtocol extends Thread {
    int sysId = 255;
    int componentId = 190;
    int target_sysId = 1;
    int target_componentId = 1;
    private LinkedBlockingQueue<byte []> q;
    private Object lock;
    private ObjectOutputStream objectOut;
    private int expSeq = 0;
    private volatile Boolean read = false;
    private volatile Boolean write = false;
    private int count = 0;

    WpProtocol(Object lock, LinkedBlockingQueue<byte []> q) {
        this.lock = lock;
        this.q = q;
    }

    public void setObjectOut(ObjectOutputStream objectOut){
        this.objectOut = objectOut;
    }

    @Override
    public void run() {
        while(true) {
            waitLock();
            startSequence();
        }
    }

    private void waitLock() {
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
