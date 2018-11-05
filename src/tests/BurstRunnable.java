package tests;

import irc.ISentence;
import irc.Sentence;
import jvn.JvnException;
import jvn.JvnObjectInvocationHandler;
import jvn.JvnServerImpl;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BurstRunnable implements Runnable {
    private static final String OBJECT_NAME = "BurstTestSentence";
    private static final Integer NB_TESTS = 100;
    private static final Integer MIN_SLEEP = 2;
    private static final Integer MAX_SLEEP = 10;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final Integer RANDOM_STRING_LENGTH = 10;

    private static final Level levelLog = Level.INFO;

    private static final Logger logger = Logger.getLogger(BurstRunnable.class.getName());

    private final Random randomGenerator;
    private final Integer nb;
    private final JvnServerImpl js;
    private ISentence sentence;

    private BurstRunnable(Integer nb, ISentence sentence) {
        this.nb = nb;
        this.sentence = sentence;
        this.randomGenerator = new Random();
        this.js = JvnServerImpl.jvnGetServer();
    }

    static synchronized BurstRunnable getNewInstance(Integer nb) {
        ISentence sentence = (ISentence) JvnObjectInvocationHandler.lookup(OBJECT_NAME);

        if (sentence == null) {
            sentence = (ISentence) JvnObjectInvocationHandler.create(new Sentence(), OBJECT_NAME);
        }

        return new BurstRunnable(nb, sentence);
    }

    @Override
    public void run() {
        logger.log(levelLog, "Thread {0} will start now.", nb);
        for (int i = 0; i < NB_TESTS; i++) {
            int isRead = randomGenerator.nextInt(2);
            if (isRead == 0) {
                logger.log(levelLog, "Thread {0} will now read.", nb);
                sentence.read();
            } else {
                String toWrite = randomString(RANDOM_STRING_LENGTH);
                logger.log(levelLog, "Thread {0} will now write: {1}.", new Object[]{nb, toWrite});
                sentence.write(toWrite);
            }

            int sleepFor = randomGenerator.nextInt(MAX_SLEEP - MIN_SLEEP) + MIN_SLEEP;
            try {
                Thread.sleep(sleepFor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            js.jvnTerminate();
        } catch (JvnException e) {
            e.printStackTrace();
        }
    }

    private String randomString(int length) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = randomGenerator.nextInt(ALPHABET.length());
            b.append(ALPHABET, index, index + 1);
        }
        return b.toString();
    }
}
