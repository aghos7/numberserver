package sample.numberserver;

/**
 * Log statistics about an element broker to the console
 * @author Lucas Anderson
 */
public class ConsoleStatsLogger implements Runnable {

    /**
     * The element broker to log statistics from
     */
    private UniqueElementBroker<Integer> elementBroker;

    /**
     * Construct an element broker statistics logger Prints the element brokers
     * statistics to standard output
     * @param elementBroker The element broker
     */
    public ConsoleStatsLogger(UniqueElementBroker<Integer> elementBroker) {
        this.elementBroker = elementBroker;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // Log the total count of numbers received since the server started and
        // the number of duplicate numbers received since the last report
        System.out.println("received " + elementBroker.getTotalCount() + " numbers, "
                + elementBroker.getDuplicateCount() + " duplicates");
        elementBroker.setDuplicateCount(0);
    }

}
