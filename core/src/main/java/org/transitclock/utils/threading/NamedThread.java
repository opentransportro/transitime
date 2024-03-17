/* (C)2023 */
package org.transitclock.utils.threading;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.AgencyConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates a Thread but sets the name of it and sets the UncaughtExceptionHandler so that uncaught
 * exceptions are logged. These features can make debugging of multiple threaded system much easier.
 *
 * <p>Based on code from the book "Java Concurrency in Practice" by Brian Goetz
 *
 * @author SkiBu Smith
 */
@Slf4j
public class NamedThread extends Thread {
    public static final String DEFAULT_NAME = "UnnamedThread";
    private static final Map<String, Integer> threadNameCountMap = new HashMap<>();
    // Number of live threads isn't currently used but was in Brian Goetz book
    private static final AtomicInteger numAlive = new AtomicInteger();

    /**
     * Creates the named thread using a default name.
     *
     * @param r
     */
    public NamedThread(Runnable r) {
        this(r, DEFAULT_NAME);
    }

    /**
     * Creates the named thread. The actual name of the thread will include the name and a counter,
     * such as "name-6".
     *
     * @param r
     * @param name
     */
    public NamedThread(Runnable r, String name) {
        super(r, threadNameWithCounter(name));
        this.setUncaughtExceptionHandler(new org.transitclock.utils.threading.UncaughtExceptionHandler());
    }

    /**
     * Returns the name of the thread. Each thread name has its own counter and the name returned
     * will be "name-3".
     *
     * @param name
     * @return
     */
    private static String threadNameWithCounter(String name) {
        synchronized (threadNameCountMap) {
            Integer count = threadNameCountMap.get(name);
            if (count == null) {
                count = 1;
            } else {
                // Increment the count;
                count++;
            }
            threadNameCountMap.put(name, count);
            return name + "-" + count;
        }
    }

    @Override
    public void run() {
        logger.debug("Created NamedThread {}", getName());
        try {
            numAlive.incrementAndGet();
            super.run();
        } catch (Throwable t) {
            // Log the problem but do so within a try/catch in case it is
            // an OutOfMemoryError and need to exit even if get another
            // OutOfMemoryError when logging.
            try {
                // Output info to stderr since this is an exception situation.
                // This will log it to the nohup file used for running core app.
                logger.error("Throwable \"{}\" occurred at {}", t.getMessage(), new Date());

                // Log since this is a serious problem
                if (t instanceof OutOfMemoryError) {
                    // Would like to send out an e-mail as part of logging but
                    // found that when running out of memory that sending out an
                    // e-mail can hang the system for a while. This is a bad
                    // thing since OutOfMemoryError is really serious and want
                    // to terminate the program right away so that it can be
                    // automatically restarted. This is unfortunate since
                    // really want to notify folks that there is an out of
                    // memory problem but notifying via email is not as
                    // important as quickly getting the system restarted.
                    logger.error(
                            "OutOfMemoryError occurred in thread {} so " + "terminating application",
                            getName(),
                            t);
                } else {
                    // Log and send out e-mail since this is an unexpected problem
                    logger.error(
                            "Unexpected Throwable occurred which will cause "
                                    + "thread {} to terminate",
                            getName(),
                            t);
                }
            } catch (Throwable ignored) {
            }

            // OutOfMemoryErrors are really serious. Don't want application to
            // continue in some kind of crippled mode that monitoring has a
            // difficult time detecting. Therefore, exit the application so that
            // can be automatically restarted.
            if (t instanceof OutOfMemoryError) {
                System.exit(-1);
            }
        } finally {
            numAlive.decrementAndGet();
            logger.debug("Exiting NamedThread {}", getName());
        }
    }
}
