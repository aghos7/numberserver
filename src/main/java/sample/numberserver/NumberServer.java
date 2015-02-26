package sample.numberserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A very simple server that accepts clients who write numbers Defaults to
 * handling 5 clients concurrently The numbers received are logged to a file
 * Statistics are logged to the console every 10 seconds
 * @author Lucas Anderson
 */
public class NumberServer implements Runnable {

    /**
     * The default port to accept clients on
     */
    public static final int DEFAULT_PORT = 4000;

    /**
     * The default number of threads to handle client connections
     */
    public static final int DEFAULT_CLIENT_THREADS = 5;

    /**
     * The default frequency at which to log statistics in seconds
     */
    public static final int DEFAULT_STATS_FREQ_SEC = 10;

    /**
     * The port to accept clients on
     */
    private int serverPort;

    /**
     * The server socket to accept clients on
     */
    private ServerSocket serverSocket;

    /**
     * The current state of the server
     */
    private ServerState state = ServerState.STOPPED;

    /**
     * Method to use when shutting down the server
     * @note defaults to GRACEFULLY
     */
    private ServerShutdownMethod shutdownMethod = ServerShutdownMethod.GRACEFULLY;

    /**
     * A fixed thread pool to handle clients An additional thread is allocated
     * for logging numbers to a file
     */
    private ExecutorService threadPool = Executors.newFixedThreadPool(DEFAULT_CLIENT_THREADS + 1);

    /**
     * A scheduled thread pool used schedule recurring tasks
     */
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    /**
     * An element broker to broker elements between threads Only unique elements
     * are retained
     */
    private UniqueElementBroker<Integer> elementBroker = new UniqueElementBroker<>();

    /**
     * Construct a number server on the default port
     */
    public NumberServer() {
        this(DEFAULT_PORT);
    }

    /**
     * Construct a number server on a specified port
     * @param port The port on which to accept clients
     */
    public NumberServer(int port) {
        this.serverPort = port;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        start();
        System.out.println("Running");
        // While the server is running accept clients and spawn workers to
        // handle clients
        while (getState().equals(ServerState.RUNNING)) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                threadPool.execute(new NumberClientWorker(this, clientSocket, elementBroker));
            } catch (IOException e) {
                // Only handle the exception if the server is running
                // stop() closes the server socket which raises an IOException
                if (getState().equals(ServerState.RUNNING)) {
                    e.printStackTrace();
                }
            }
        }
        shutdown();
    }

    /**
     * Start the server
     */
    private void start() {
        System.out.println("Starting server");
        setState(ServerState.RUNNING);
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Can't start server on port: " + serverPort, e);
        }
        // Add the thread to log the numbers to a file
        threadPool.execute(new NumberFileLogger(elementBroker));
        // Wait for DEFAULT_STATS_FREQ_SEC seconds and then repeat every
        // DEFAULT_STATS_FREQ_SEC seconds
        scheduledThreadPool.scheduleWithFixedDelay(new ConsoleStatsLogger(elementBroker), DEFAULT_STATS_FREQ_SEC,
                DEFAULT_STATS_FREQ_SEC, TimeUnit.SECONDS);
    }

    /**
     * Stop the server
     */
    public synchronized void stop() {
        setState(ServerState.STOPPING);
        try {
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Shutdown the server
     */
    private void shutdown() {
        switch (shutdownMethod) {
        case GRACEFULLY:
            gracefulShutdown();
            break;
        case ABRUPT:
            abruptShutdown();
            break;
        }
    }

    /**
     * Abrupty shutdown the server by killing the JVM
     */
    private void abruptShutdown() {
        System.exit(1);
    }

    /**
     * Gracefully shutdown the server by letting currently running tasks
     * shutdown If current tasks do not finish within a short period, an abrupt
     * shutdown occurs
     */
    private void gracefulShutdown() {
        try {
            if (!getState().equals(ServerState.STOPPED)) {
                System.out.println("Shutting down");
                threadPool.shutdown();
                if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                        System.out.println("Can't shutdown: " + threadPool);
                        abruptShutdown();
                    }
                }
                scheduledThreadPool.shutdown();
                if (!scheduledThreadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    scheduledThreadPool.shutdownNow();
                    if (!scheduledThreadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                        System.out.println("Can't shutdown: " + scheduledThreadPool);
                        abruptShutdown();
                    }
                }

                serverSocket.close();
                setState(ServerState.STOPPED);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return The current state of the server
     */
    public synchronized ServerState getState() {
        return state;
    }

    /**
     * @param state The server state to set
     */
    public synchronized void setState(ServerState state) {
        this.state = state;
    }

    /**
     * Main method for running a NumberServer
     * @param args
     * @note args[0] - The number minutes to run the server for, if it is
     *       omitted the server will run until killed
     */
    public static void main(String[] args) {
        final NumberServer server = new NumberServer(4000);
        Thread serverThread = new Thread(server);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
            }
        });

        try {
            serverThread.start();
            if (args.length > 0) {
                serverThread.join(Integer.parseInt(args[0]) * 60 * 1000);
                server.stop();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
