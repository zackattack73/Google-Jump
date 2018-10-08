package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {
	static final String SERVICE_NAME = "JvnRemoteCoord";

	public static void main(String[] args) {
		try {
            JvnCoordImpl o = new JvnCoordImpl();

            Registry r = LocateRegistry.getRegistry();
            r.rebind(SERVICE_NAME, o);
            
            System.out.println("Waiting for connections");
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	private final Map<String, JvnObject> objectsMap;
	private final Map<Integer, JvnObject> objectsMapId;
	private final Map<Integer, JvnRemoteServer> objectsWriteServer;
	private final Map<Integer, Map<JvnStateLock, JvnRemoteServer>> objectsLocks;
	private Integer currentObjectId = 0;

	/**
	 * Default constructor
	 *
	 **/
	private JvnCoordImpl() throws Exception {
		// to be completed
		this.objectsMap = new HashMap<>();
		this.objectsMapId = new HashMap<>();
		this.objectsWriteServer = new HashMap<>();
		this.objectsLocks = new HashMap<>();
	}

	/**
	 * Allocate a NEW JVN object id (usually allocated to a
	 * newly created JVN object)
	 **/
	public int jvnGetObjectId() throws RemoteException, JvnException {
		// to be completed
		return currentObjectId++;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object
	 * @param js  : the remote reference of the JVNServer
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException {
		// to be completed
		this.objectsMap.put(jon, jo);
		this.objectsWriteServer.put(jo.jvnGetObjectId(), js);
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 **/
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException {
		// to be completed
		return this.objectsMap.get(jon);
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 **/
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		// to be completed
		if (this.objectsWriteServer.get(joi) != null) {
			this.objectsWriteServer.get(joi).jvnInvalidateWriter(joi);
			return this.objectsMapId.get(joi).jvnGetObjectState();
		} else {
			return this.objectsMapId.get(joi).jvnGetObjectState();
		}
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 **/
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		// to be completed
		for (Map.Entry<JvnStateLock, JvnRemoteServer> e : this.objectsLocks.get(joi).entrySet()) {
			// Invalidate all readers
			switch (e.getKey()) {
				case R:
					e.getValue().jvnInvalidateReader(joi);
					break;
				case W:
					try {
						e.getValue().wait();
					} catch(Exception ex) {}

					return e.getValue().jvnInvalidateWriterForReader(joi);
				}
		}
		
		return null;
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {
		// to be completed
	}
}
