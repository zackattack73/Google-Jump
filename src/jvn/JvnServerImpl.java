package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {
	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private JvnRemoteCoord remoteCoord;
	private Map<Integer, JvnObject> mapId;

	/**
	 * Default constructor
	 **/
	private JvnServerImpl() throws Exception {
		super();

		this.mapId = new HashMap<>();

        Registry r = LocateRegistry.getRegistry("localhost");
        remoteCoord = (JvnRemoteCoord) r.lookup(JvnCoordImpl.SERVICE_NAME);
	}

	/**
	 * Static method allowing an application to get a reference to
	 * a JVN server instance
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				System.err.println("Impossible d'instancier le serveur.");
				return null;
			}
		}

		return js;
	}

	/**
	 * The JVN service is not used anymore
	 **/
	public void jvnTerminate() throws JvnException {
		// to be completed
		try {
			remoteCoord.jvnTerminate(this);
		} catch (RemoteException e) { }
	} 

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws JvnException {
		// to be completed
        try {
            int nid = remoteCoord.jvnGetObjectId();
            return new JvnObjectImpl(o, nid);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

		return null;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo : the JVN object
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException {
		// to be completed
		this.mapId.put(jo.jvnGetObjectId(), jo);

		try {
            remoteCoord.jvnRegisterObject(jon, jo, this);
        } catch (RemoteException e) {
		    e.printStackTrace();
        }
	}

	/**
	 * Provide the reference of a JVN object being given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object
	 **/
	public JvnObject jvnLookupObject(String jon) throws JvnException {
		// to be completed
        try {
        	JvnObject o = remoteCoord.jvnLookupObject(jon, this);

        	if (o != null) {
				this.mapId.put(o.jvnGetObjectId(), o);
				return o;
			}

			return null;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

		return null;
	}	

	/**
	 * Get a Read lock on a JVN object
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		// to be completed
		try {
			Serializable state = remoteCoord.jvnLockRead(joi, this);
			((JvnObjectImpl) this.mapId.get(joi)).updateState(state);
		} catch (final RemoteException re) {
			re.printStackTrace();
		}

		return null;
	}

	/**
	 * Get a Write lock on a JVN object
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		// to be completed
		try {
			Serializable state = remoteCoord.jvnLockWrite(joi, this);
			((JvnObjectImpl) this.mapId.get(joi)).updateState(state);
		} catch (final RemoteException re) {
			re.printStackTrace();
		}

		return null;
	}	

	/**
	 * Invalidate the Read lock of the JVN object identified by id
	 * called by the JvnCoord
	 * @param joi the JVN object id
	 **/
	public synchronized void jvnInvalidateReader(int joi) throws RemoteException, JvnException {
		// to be completed
		this.mapId.get(joi).jvnInvalidateReader();
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 **/
	public synchronized Serializable jvnInvalidateWriter(int joi) throws RemoteException, JvnException {
		// to be completed
		return this.mapId.get(joi).jvnInvalidateWriter();
	}

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 **/
	public synchronized Serializable jvnInvalidateWriterForReader(int joi) throws RemoteException, JvnException {
		// to be completed
		return this.mapId.get(joi).jvnInvalidateWriterForReader();
	}
}
