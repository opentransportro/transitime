/* (C)2023 */
package org.transitclock.core.avl;

import java.util.Map;
import java.util.concurrent.*;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.core.avl.AvlReportProcessor.AvlReportProcessingTask;
import org.transitclock.domain.structs.AvlReport;

/**
 * A queue of {@link AvlReportProcessingTask} runnables that can be used with a ThreadPoolExecutor. Implements by
 * subclassing a ArrayBlockingQueue<Runnable> where the Runnable is an AvlClient. Also keeps track
 * of the last AVL report per vehicle. When getting data from queue, if the data is obsolete (a new
 * AVL report has been received for the vehicle) then that element from the queue is thrown out and
 * the next item is retrieved until a non-obsolete one is found.
 *
 * <p>Extended ArrayBlockingQueue class so that don't have to create a blocking queue from scratch.
 *
 * <p>Note: wanted to extend from ArrayBlockingQueue<AvlReportProcessingTask> but that didn't work for the
 * ThreadPoolExecutor which expects a BlockingQueue<Runnable>. So had to resort to doing ugly casts.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class AvlReportProcessorQueue extends ArrayBlockingQueue<AvlReportProcessingTask> {
    // For keeping track of the last AVL report for each vehicle. Used to
    // determine if AVL report from queue is obsolete.
    private final Map<String, AvlReport> avlDataPerVehicleMap = new ConcurrentHashMap<>();

    /**
     * Constructs the queue to have specified size.
     *
     * @param queueSize How many AVL elements can be put into the queue before it blocks.
     */
    public AvlReportProcessorQueue(int queueSize) {
        super(queueSize);
    }

    /**
     * Adds the AVL report to the map of last AVL report for each vehicle
     *
     * @param task the AvlClient
     */
    private void addToAvlDataPerVehicleMap(AvlReportProcessingTask task) {
        if (task == null)
            throw new IllegalArgumentException("Runnable must be AvlClient.");

        AvlReport avlReport = task.getAvlReport();
        avlDataPerVehicleMap.put(avlReport.getVehicleId(), avlReport);
    }

    /**
     * Returns true if the AVL report is older than the latest one for the vehicle and is therefore
     * obsolete and doesn't need to be processed.
     *
     * @param task {@link AvlReportProcessingTask} from the queue containing an AvlReport
     * @return true of obsolete
     */
    private boolean isObsolete(AvlReportProcessingTask task) {
        if (task == null)
            throw new IllegalArgumentException("Runnable must be AvlClient.");

        AvlReport avlReportFromQueue = task.getAvlReport();

        AvlReport lastAvlReportForVehicle = avlDataPerVehicleMap.get(avlReportFromQueue.getVehicleId());
        boolean obsolete = lastAvlReportForVehicle != null && avlReportFromQueue.getTime() < lastAvlReportForVehicle.getTime();
        if (obsolete) {
            logger.debug(
                "AVL report from queue is obsolete (there is a newer "
                    + "one for the vehicle). Therefore ignoring this report so "
                    + "can move on to next valid report for another vehicle. "
                    + "From queue {}. Last AVL report in map {}. Size of queue "
                    + "is {}",
                avlReportFromQueue,
                lastAvlReportForVehicle,
                size());
        }
        return obsolete;
    }

    /**
     * Calls superclass add() method but also updates the AVL data per vehicle map. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public boolean add(@NonNull AvlReportProcessingTask task) {
        addToAvlDataPerVehicleMap(task);
        return super.add(task);
    }

    /**
     * Calls superclass put() method but also updates the AVL data per vehicle map. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public void put(@NonNull AvlReportProcessingTask task) throws InterruptedException {
        super.put(task);
        addToAvlDataPerVehicleMap(task);
    }

    /**
     * Calls superclass offer() method but also updates the AVL data per vehicle map. Used by
     * ThreadPoolExecutor.
     */
    @Override
    public boolean offer(@NonNull AvlReportProcessingTask task) {
        AvlReport avlReport = task.getAvlReport();
        logger.debug("offer() remainingCapacity={} {}", remainingCapacity(), avlReport);

        boolean successful = super.offer(task);
        if (successful) {
            addToAvlDataPerVehicleMap(task);
        }

        logger.debug("offer() returned {} for {}", successful, avlReport);
        return successful;
    }

    /**
     * Calls superclass offer(timeout, unit) method but also updates the AVL data per vehicle map.
     * Doesn't seem to be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public boolean offer(AvlReportProcessingTask task, long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        boolean successful = super.offer(task, timeout, unit);
        if (successful) addToAvlDataPerVehicleMap(task);

        return successful;
    }

    /**
     * Calls superclass poll() method until it gets AVL data that is not obsolete. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    public AvlReportProcessingTask poll() {
        AvlReportProcessingTask task;
        do {
            task = super.poll();
        } while (isObsolete(task));

        return task;
    }

    /**
     * Calls superclass poll(timeout, unit) method until it gets AVL data that is not obsolete. Used
     * by ThreadPoolExecutor.
     */
    @Override
    public AvlReportProcessingTask poll(long timeout, TimeUnit unit) throws InterruptedException {
        logger.debug("In poll(t,u) timeout={} units={}", timeout, unit);

        AvlReportProcessingTask task;
        do {
            task = super.poll(timeout, unit);
        } while (task != null && isObsolete(task));

        if (task != null) {
            logger.debug("poll(t,u) in AvlQueue returned {}", task.getAvlReport());
        }

        return task;
    }

    /**
     * Calls superclass take() method until it gets AVL data that is not obsolete. Doesn't seem to
     * be used by ThreadPoolExecutor but still included for completeness.
     */
    @Override
    @NonNull
    public AvlReportProcessingTask take() throws InterruptedException {
        AvlReportProcessingTask task;
        do {
            task = super.take();
        } while (isObsolete(task));
        return task;
    }
}
