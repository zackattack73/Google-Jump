package jvn;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {
	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;
	
	private Map<Integer, JvnObject> mapId;

	/**
	 * Default constructor
	 **/
	private JvnServerImpl() throws Exception {
		super();
		this.mapId = new HashMap<>();
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
		System.exit(0);
	} 

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws JvnException {
		// to be completed
		return null; 
	}

	/**
	 *  Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo : the JVN object
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException {
		// to be completed
		// this.mapName.put(jon, jo);
		this.mapId.put(jo.jvnGetObjectId(), jo);
	}

	/**
	 * Provide the reference of a JVN object being given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object
	 **/
	public JvnObject jvnLookupObject(String jon) throws JvnException {
		// to be completed
		return null;
	}	

	/**
	 * Get a Read lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		// to be completed
		// JvnObject object;
		return null;
	}

	/**
	 * Get a Write lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		// to be completed
		return null;
	}	

	/**
	 * Invalidate the Read lock of the JVN object identified by id 
	 * called by the JvnCoord
	 * @param joi the JVN object id
	 **/
	public void jvnInvalidateReader(int joi) throws RemoteException, JvnException {
		// to be completed
		JvnObject o = this.mapId.get(joi);
		o.jvnInvalidateReader();
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws RemoteException, JvnException {
		// to be completed
		JvnObject o = this.mapId.get(joi);
		o.jvnInvalidateWriter();
		return o;
	}

	/**
	 * Reduce the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws RemoteException, JvnException {
		// to be completed
		JvnObject o = this.mapId.get(joi);
		o.jvnInvalidateWriterForReader();
		return o;
	}
}
