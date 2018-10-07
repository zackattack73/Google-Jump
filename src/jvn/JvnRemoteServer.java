package jvn;

import java.rmi.*;
import java.io.*;

/**
 * Remote interface of a JVN server (used by a remote JvnCoord)
 */
public interface JvnRemoteServer extends Remote {
	/**
	 * Invalidate the Read lock of a JVN object
	 * @param joi : the JVN object id
	 **/
	void jvnInvalidateReader(int joi) throws RemoteException, JvnException;

	/**
	 * Invalidate the Write lock of a JVN object
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 **/
	Serializable jvnInvalidateWriter(int joi) throws RemoteException, JvnException;

	/**
	 * Reduce the Write lock of a JVN object
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 **/
	Serializable jvnInvalidateWriterForReader(int joi) throws RemoteException, JvnException;
}
