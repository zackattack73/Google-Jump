package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
    private Serializable state;
    private JvnStateLock lock;
    private int id;

    public JvnObjectImpl(Serializable state, int id) {
        this.state = state;
        this.lock = JvnStateLock.W;
        this.id = id;
    }

    @Override
    public synchronized void jvnLockRead() throws JvnException {
        switch (lock) {
            case RC:
                changeLock(JvnStateLock.R);
                break;
            case WC:
                changeLock(JvnStateLock.RWC);
                break;
            case W:
                changeLock(JvnStateLock.R);
                break;
            case N:
                JvnServerImpl.jvnGetServer().jvnLockRead(id);
                changeLock(JvnStateLock.R);
                break;
            case R:
            case RWC:
                // Nothing to do...
                break;
        }
    }

    @Override
    public synchronized void jvnLockWrite() throws JvnException {
        switch (lock) {
            case WC:
                changeLock(JvnStateLock.W);
                break;
            case RWC:
                changeLock(JvnStateLock.W);
            case R:
            case RC:
            case N:
                JvnServerImpl.jvnGetServer().jvnLockWrite(id);
                changeLock(JvnStateLock.W);
                break;
            case W:
                // Nothing to do...
                break;
        }
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        switch (lock) {
            case R:
                changeLock(JvnStateLock.RC);
                break;
            case W:
                changeLock(JvnStateLock.WC);
                break;
            case RWC:
                changeLock(JvnStateLock.WC);
                break;
        }

        notifyLock();
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }

    @Override
    public Serializable jvnGetObjectState() throws JvnException {
        return state;
    }

    public void updateState(Serializable newState) {
        this.state = newState;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        switch (lock) {
            case R:
                waitLock();
                changeLock(JvnStateLock.N);
                break;
            case RC:
                changeLock(JvnStateLock.N);
                break;
        }
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
    	switch (lock) {
            case W:
                waitLock();
                changeLock(JvnStateLock.N);
                return state;
            case WC:
                changeLock(JvnStateLock.N);
                return state;
            case RWC:
                waitLock();
                changeLock(JvnStateLock.N);
                return state;
            default:
                System.out.println("WAS STATE " + lock);
        }

        return state;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        switch (lock) {
            case W:
                waitLock();
                changeLock(JvnStateLock.RC);
                return state;
            case WC:
                changeLock(JvnStateLock.RC);
                return state;
            case RWC:
                waitLock();
                changeLock(JvnStateLock.RC);
                return state;
            default:
                System.out.println("WAS STATE " + lock);
        }

        return state;
    }

    private void changeLock(JvnStateLock to) throws JvnException {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackElement = stackTraceElements[2];

        //System.out.println("[" + jvnGetObjectId() + "] " + stackElement.getMethodName() + ": " + lock + "->" + to);

        lock = to;
    }

    private void waitLock() {
        try {
            //System.out.println("Waiting for unlock...");
            wait();
        } catch (InterruptedException e) { }
    }

    private void notifyLock() {
        //System.out.println("Notifying unlock...");
        notify();
    }
}
