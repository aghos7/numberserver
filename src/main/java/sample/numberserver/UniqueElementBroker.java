package sample.numberserver;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An element broker that discards duplicate elements
 * @note Queues elements in a blocking queue that is FIFO
 * @note Uses a Set to detect duplicates
 * @author Lucas Anderson
 */
public class UniqueElementBroker<T> {
    /**
     * The set of elements that have been seen thus far
     */
    private Set<T> uniqueElements = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());

    /**
     * Blocking queue used to transfer elements between threads
     */
    private BlockingQueue<T> elementQueue = new LinkedBlockingDeque<>();

    /**
     * The total number of elements seen
     */
    private AtomicLong totalCount = new AtomicLong();

    /**
     * The number of duplicate elements seen
     */
    private AtomicLong duplicateCount = new AtomicLong();

    /**
     * Puts an element in the broker at the tail of the queue Blocks until there
     * is space to add the element
     * @param element The element to add
     * @throws InterruptedException Putting the element was interrupted while
     *         waiting for space
     */
    public void put(T element) throws InterruptedException {
        totalCount.incrementAndGet();
        if (uniqueElements.add(element)) {
            elementQueue.put(element);
        } else {
            duplicateCount.incrementAndGet();
        }
    }

    /**
     * Retrieve an element from the broker Blocks until there is an element to
     * retrieve
     * @return The head element from the queue
     * @throws InterruptedException Getting the element was interrupted while
     *         waiting for one to be available
     */
    public T get() throws InterruptedException {
        return elementQueue.take();
    }

    /**
     * @return The total count of elements seen
     */
    public long getTotalCount() {
        return totalCount.get();
    }

    /**
     * @param totalCount The total count of elements seen
     */
    public void setTotalCount(long totalCount) {
        this.totalCount.set(totalCount);
    }

    /**
     * @return The number of duplicate elements seen
     */
    public long getDuplicateCount() {
        return duplicateCount.get();
    }

    /**
     * @param duplicateCount The duplicate count of elements to set
     */
    public void setDuplicateCount(long duplicateCount) {
        this.duplicateCount.set(duplicateCount);
    }
}
