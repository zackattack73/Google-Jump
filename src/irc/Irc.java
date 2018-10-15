package irc;

import java.awt.*;
import java.awt.event.*;

import jvn.*;

/**
 * Irc class : simple implementation of a chat using JAVANAISE
 */
public class Irc {
	public TextArea	text;
	public TextField data;
	Frame frame;
	JvnObject sentence;
	private static JvnServerImpl js;

	/**
	 * main method
	 * create a JVN object named IRC for representing the Chat application
	 **/
	public static void main(String argv[]) {
		try {
			// initialize JVN
			js = JvnServerImpl.jvnGetServer();

			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject("IRC");

			if (jo == null) {
				jo = js.jvnCreateObject(new Sentence());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}
			// create the graphical part of the Chat application
			new Irc(jo);
		} catch (Exception e) {
			System.out.println("IRC problem: " + e.getMessage());
		}
	}

	/**
	 * IRC Constructor
     * @param jo the JVN object representing the Chat
	 **/
	public Irc(JvnObject jo) {
		sentence = jo;
		frame = new Frame();
		frame.setLayout(new GridLayout(1,1));
		text = new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data = new TextField(40);
		frame.add(data);

		// Read button
		Button readButton = new Button("Read");
		readButton.addActionListener(new ReadListener(this));
		frame.add(readButton);

		// Write button
		Button writeButton = new Button("Write");
		writeButton.addActionListener(new WriteListener(this));
		frame.add(writeButton);

		// Set up frame
		frame.setSize(800,200);
		text.setBackground(Color.black); 
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					Irc.js.jvnTerminate();
				} catch (Exception ignored) { }
				System.exit(0);
			}
		});
	}
}

/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class ReadListener implements ActionListener {
	Irc irc;

	public ReadListener(Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {
			// lock the object in read mode
			irc.sentence.jvnLockRead();

			// invoke the method
			String s = ((Sentence) irc.sentence.jvnGetObjectState()).read();

			// unlock the object
			irc.sentence.jvnUnLock();

			// display the read value
			irc.data.setText(s);
			irc.text.append(s + "\n");
		} catch (JvnException je) {
			System.out.println("IRC problem: " + je.getMessage());
		}
	}
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class WriteListener implements ActionListener {
	Irc irc;

	public WriteListener(Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {	
			// get the value to be written from the buffer
			String s = irc.data.getText();

			// lock the object in write mode
			irc.sentence.jvnLockWrite();

			// invoke the method
			((Sentence) irc.sentence.jvnGetObjectState()).write(s);

			// unlock the object
			irc.sentence.jvnUnLock();
		} catch (JvnException je) {
			System.out.println("IRC problem: " + je.getMessage());
		}
	}
}
