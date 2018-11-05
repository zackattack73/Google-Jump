package jvn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {
    private static final String SAVE_NAME = "coord.save";
    static final String SERVICE_NAME = "JvnRemoteCoord";
    private Map<String, Integer> nameIds;
    private Map<Integer, JvnObject> idsObjects;
    private Map<Integer, JvnRemoteServer> activeWriter;
    private Map<Integer, Set<JvnRemoteServer>> activeReaders;
    private Integer currentObjectId = 0;

    /**
     * Default constructor
     **/
    private JvnCoordImpl() throws Exception {
        // to be completed
        this.nameIds = new HashMap<>();
        this.idsObjects = new HashMap<>();
        this.activeWriter = new HashMap<>();
        this.activeReaders = new HashMap<>();
		jvnRestoreCoord();
    }

    public static void main(String[] args) {
        try {
            JvnCoordImpl o = new JvnCoordImpl();

            Registry r = LocateRegistry.createRegistry(1333);
            r.rebind(SERVICE_NAME, o);

            System.out.println("Waiting for connections");

            new Thread(() -> {
                try (Scanner s = new Scanner(System.in)) {
                    System.out.println("Press ENTER to quit.");
                    s.nextLine();
                    File f = new File(SAVE_NAME);
                    if (f.exists()) {
                        f.delete();
                    }
                    System.exit(0);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Allocate a NEW JVN object id (usually allocated to a
     * newly created JVN object)
     **/
    public synchronized int jvnGetObjectId() throws RemoteException, JvnException {
        return currentObjectId++;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param js  : the remote reference of the JVNServer
     **/
    public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException {
        Integer oid = jo.jvnGetObjectId();

        this.nameIds.put(jon, oid);
        this.idsObjects.put(oid, jo);
        this.activeWriter.put(oid, js);
        this.activeReaders.put(oid, new HashSet<>());

        System.out.println("jvnRegisterObject: object OK");
    }
	
	/**
     * Save the coord informations in the file "coord.sav"
     * to restore it if needed (crash)
     **/
	public synchronized void jvnSaveCoord() {
		try {
			FileOutputStream saveFile = new FileOutputStream(SAVE_NAME);
			ObjectOutputStream output = new ObjectOutputStream(saveFile);
			
			output.writeObject(this);
			// Flush the stream just in case
			output.flush();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Restore the coord informations from the file "coord.sav"
     * if the file exist
     **/
	public synchronized void jvnRestoreCoord() {
		if (new File(SAVE_NAME).exists()) {
			System.out.println("An old instance of coord has been found and restored");
			try {
				FileInputStream file = new FileInputStream(SAVE_NAME);
				ObjectInputStream input = new ObjectInputStream(file);
				JvnCoordImpl restored = (JvnCoordImpl) input.readObject();
				
				this.idsObjects = restored.idsObjects;
				this.nameIds = restored.nameIds;
				this.activeWriter = restored.activeWriter;
				this.activeReaders = restored.activeReaders;
				this.currentObjectId = restored.currentObjectId;
				input.close();
			} catch (IOException|ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js  : the remote reference of the JVNServer
     **/
    public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException {
        if (this.nameIds.get(jon) != null) {
            // Object exists.
            // We need to invalidate the writer for this object.
            System.out.println("jvnLookupObject: object existed");

            // Get the ID for this name
            Integer id = this.nameIds.get(jon);
            // Lock this object to get back the latest state
            Serializable data = this.jvnLockWrite(id, js);
            // Cast to be able to update the local state of the object
            JvnObjectImpl o = (JvnObjectImpl) this.idsObjects.get(id);
            // Update the state of the local object
            o.updateState(data);
            // Set active writer
            this.activeWriter.put(id, js);

            return o;
        }

        // If we get here, no object exists in cache for this name.
        return null;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     **/
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
        System.out.println("jvnLockRead: " + joi);

        try {
            JvnRemoteServer server = this.activeWriter.get(joi);
            if (server != null) {
                // We have to update state and remove writer
                Serializable lastState = server.jvnInvalidateWriterForReader(joi);
                this.activeWriter.remove(joi); // The object is not in active write anymore
                this.activeReaders.get(joi).add(server);
                System.out.println("jvnLockRead: removed activeWriter");
                ((JvnObjectImpl) this.idsObjects.get(joi)).updateState(lastState); // Update local state
            } else {
                System.out.println("jvnLockRead: no active writer");
            }
        } catch (Exception e) {
            System.out.println("Client crashed.");
            this.activeWriter.put(joi, null); // Last version is on the coordinator
        }

        this.activeReaders.get(joi).add(js);
		
		jvnSaveCoord(); // Save the coord
		
        System.out.println("jvnLockRead: done!");

        return this.idsObjects.get(joi).jvnGetObjectState();
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     **/
    public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
        System.out.println("jvnLockWrite: " + joi);

        // --
        // Remove readers and writers
        // --

        // Remove readers
        Set<JvnRemoteServer> readServers = activeReaders.get(joi);
        if (readServers.size() > 0) {
            System.out.println("jvnLockWrite: will invalidate all readers (" + readServers.size() + ")");
            int i = 0;
            for (JvnRemoteServer s : readServers) {
                if (!s.equals(js)) {
                    System.out.println("jvnLockWrite: " + ++i);
                    try {
                        s.jvnInvalidateReader(joi);
                    } catch (Exception e) { }
                }
            }
            System.out.println("jvnLockWrite: total " + i);
            // Empty list
            readServers.clear();
        } else {
            // Remove writer
            JvnRemoteServer writeServer = activeWriter.get(joi);
            if (writeServer != null && !writeServer.equals(js)) {
                System.out.println("jvnLockWrite: will invalidate writer");
                try {
                    Serializable lastState = writeServer.jvnInvalidateWriter(joi);
                    ((JvnObjectImpl) this.idsObjects.get(joi)).updateState(lastState); // Update local state
                    activeWriter.remove(joi);
                } catch (Exception e) { }
            }
        }

        activeWriter.put(joi, js);

        Serializable s = this.idsObjects.get(joi).jvnGetObjectState();
		
		jvnSaveCoord(); // Save the coord
		
        System.out.println("jvnLockWrite: done!");

        return s;
    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     **/
    public synchronized void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {
        System.out.println("jvnTerminate:");

        // Remove entries where js is a writer (and update local data)
        for (Map.Entry<Integer, JvnRemoteServer> e : this.activeWriter.entrySet()) {
            if (e.getValue().equals(js)) {
                Serializable lastState = e.getValue().jvnInvalidateWriter(e.getKey());
                ((JvnObjectImpl) this.idsObjects.get(e.getKey())).updateState(lastState);
                this.activeWriter.remove(e.getKey());
            }
        }

        // Remove entries where js is a reader
        for (Map.Entry<Integer, Set<JvnRemoteServer>> e : this.activeReaders.entrySet()) {
            for (JvnRemoteServer s : e.getValue()) {
                if (s.equals(js)) {
                    e.getValue().remove(s);
                    break;
                }
            }
        }
    }
}
