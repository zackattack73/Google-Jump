package jvn;

import java.io.Serializable;

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
        switch (lock) {
            // STOPSHIP: 08/10/18 OUI NON? stop your sheep
            case RC:
                lock = JvnStateLock.R;
                break;
            case WC:
                lock = JvnStateLock.RWC;
                break;
            case N: JvnServerImpl.jvnGetServer().jvnLockRead(id); lock = JvnStateLock.R;
        }
        //  RC: R, WC: RWC, NL:
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        switch (lock) {
            case R: lock = JvnStateLock.W;
            case RC: lock = JvnStateLock.R; break;
            case WC: lock = JvnStateLock.W; break;
            case N: JvnServerImpl.jvnGetServer().jvnLockWrite(id); lock = JvnStateLock.W; break;
        }
        // STOPSHIP: 08/10/18 STOP SHI
    }

    @Override
    public void jvnUnLock() throws JvnException {
        switch (lock) {
            case R: lock = JvnStateLock.RC; notify();
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
        switch (lock) {
            case R:
                try {
                    wait();
                } catch (InterruptedException e) { }
            case RC:
                // STOPSHIP: 08/10/18 OUI
                // STOPSHIP: 08/10/18 NON
                lock = JvnStateLock.N;
                break;
        }

        // R: wait, RC: NL
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
    	switch (lock) {
            case W:
                try {
                    wait();
                } catch (InterruptedException e) { }
            case WC:
                this.lock = JvnStateLock.N;
                return data;
        }

        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        switch (lock) {
            case W:
                try {
                    wait();
                } catch (InterruptedException e) { }
            case WC:
                this.lock = JvnStateLock.RC;
                return this.data;
                // STOPSHIP: 08/10/18 <3
            case RWC:
                this.lock = JvnStateLock.R;
        }

        return null;
    }
}
