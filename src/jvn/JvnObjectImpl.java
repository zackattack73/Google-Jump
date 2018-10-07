package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;

public class JvnObjectImpl implements JvnObject {
    private Serializable data;
    private JvnStateLock lock;
    private int id;

    public JvnObjectImpl(Serializable data, int id) {
        this.data = data;
        this.lock = JvnStateLock.R;
        this.id = id;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        JvnServerImpl.jvnGetServer().jvnLockRead(id);
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        JvnServerImpl.jvnGetServer().jvnLockWrite(id);
    }

    @Override
    public void jvnUnLock() throws JvnException {
        switch (lock) {
            case R: lock = JvnStateLock.RC;
            case W: lock = JvnStateLock.WC;
            case RWC: lock = JvnStateLock.WC;
        }
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }

    @Override
    public Serializable jvnGetObjectState() throws JvnException {
        return data;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {
        try {
            JvnServerImpl.jvnGetServer().jvnInvalidateReader(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        try {
            return JvnServerImpl.jvnGetServer().jvnInvalidateWriter(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        try {
            return JvnServerImpl.jvnGetServer().jvnInvalidateWriterForReader(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }
}
