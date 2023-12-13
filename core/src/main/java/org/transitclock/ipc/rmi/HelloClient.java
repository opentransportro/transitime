/* (C)2023 */
package org.transitclock.ipc.rmi;

import java.rmi.RemoteException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.transitclock.utils.Time;
import org.transitclock.utils.threading.NamedThreadFactory;

/**
 * Sample test program to show how a client can do RMI calls.
 *
 * @author SkiBu Smith
 */
public class HelloClient {
    private static Hello hello;

    /** Does a test of the Hello.concat() RMI call. */
    private static void test() {
        try {
            System.out.println("running...");
            String result = hello.concat("s1", "s2");
            System.out.println("result=" + result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String projectId = args.length > 0 ? args[0] : "testProjectId";

        hello = ClientFactory.getInstance(projectId, Hello.class);

        // Access the class a bunch of times simultaneous using multiple
        // threads. This will cause some of the calls to fail
        NamedThreadFactory threadFactory = new NamedThreadFactory("testThreadName");
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                RmiCallInvocationHandler.getMaxConcurrentCallsPerProject() + 10, threadFactory);
        for (int i = 0; i < RmiCallInvocationHandler.getMaxConcurrentCallsPerProject() + 10; ++i)
            executor.execute(new Runnable() {
                public void run() {
                    test();
                }
            });

        // Now test a couple more times now that the other threads should have finished.
        // This test is to make sure that calls can go through again.
        Time.sleep(3000);
        test();
        test();
    }
}
