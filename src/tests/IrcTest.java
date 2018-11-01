package tests;

import jvn.JvnCoordImpl;

public class IrcTest {
    private static final Integer NB_THREADS = 6;

    public static void main(String[] args) {
        new Thread(() -> JvnCoordImpl.main(args)).start();

        Thread[] threads = new Thread[NB_THREADS];
        for (int i = 0; i < NB_THREADS; i++) {
            threads[i] = new Thread(BurstRunnable.getNewInstance(i));
            threads[i].start();
        }

        for (int i = 0; i < NB_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }
}
