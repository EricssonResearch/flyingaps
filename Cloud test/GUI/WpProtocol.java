import se.kth.mf2063.internetdrone.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class WpProtocol extends Thread {
    private LinkedBlockingQueue<Message> q;
    private Object lock;
    private Socket clientSocket;
    private ObjectOutputStream objectOut;
    private int expSeq;

    WpProtocol(Object lock, LinkedBlockingQueue<Message> q) {
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

    public void notifyLock(int seq) {
        if(expSeq == seq | seq == -1 | seq == -2) //-1, q is filled, -2, mission_ack
            synchronized (lock) {
                lock.notify();
            }
        System.out.println("Notified: " + seq);
    }

    private void startSequence() {
        while(!q.isEmpty()) {
            send(q.poll());
            System.out.println("Sent! " + q.size() +" left.");
            System.out.println("Waiting for: " + expSeq);
            waitLock();
            expSeq++;
            }
        expSeq = 0;
        waitLock();
    }

    private void send(Message msg) {
        try {
            objectOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
