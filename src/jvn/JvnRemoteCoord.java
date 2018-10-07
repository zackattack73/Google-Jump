package jvn;

import java.rmi.*;
import java.io.*;

/**
 * Remote Interface of the JVN Coordinator  
 */
public interface JvnRemoteCoord extends Remote {
	/**
	 *  Allocate a NEW JVN object id (usually allocated to a
	 *  newly created JVN object)
	 **/
	int jvnGetObjectId() throws RemoteException, JvnException;

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object
	 * @param js  : the remote reference of the JVNServer
	 **/
	void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException;

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 **/
	JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException;

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 **/
	Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException;

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 **/
	Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException;

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 **/
	void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException;
}
