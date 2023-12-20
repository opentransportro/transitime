/* (C)2023 */
package org.transitclock.utils.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * For debugging. Should be a unit test.
 *
 * @author SkiBu Smith
 */
public class ThreadTest implements Runnable {

    private String name;

    public ThreadTest(int i) {
        name = Integer.toString(i);
    }

    @Override
    public void run() {
        // Simply continue to do things
        while (true) {
            // Surround thread with a try/catch to catch
            // all exceptions and loop forever. This way
            // don't have to worry about a thread dying.
            try {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                System.err.println("Name=" + name);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        NamedThreadFactory namedFactory = new NamedThreadFactory("testName");
        int NTHREDS = 5;
        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS, namedFactory);

        for (int i = 0; i < NTHREDS + 1; i++) {
            Runnable worker = new ThreadTest(i);
            executor.execute(worker);
        }
    }
}
