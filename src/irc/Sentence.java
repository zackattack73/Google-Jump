package irc;

/**
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 */
public class Sentence implements java.io.Serializable {
	String data;
  
	public Sentence() {
		data = "";
	}
	
	public void write(String text) {
		data = text;
	}

	public String read() {
		return data;	
	}
}