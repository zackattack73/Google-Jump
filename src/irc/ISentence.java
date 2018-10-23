package irc;

import jvn.JvnLockReadAnnotation;
import jvn.JvnLockWriteAnnotation;

public interface ISentence {
    @JvnLockWriteAnnotation
    void write(String text);

    @JvnLockReadAnnotation
    String read();
}
