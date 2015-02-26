package sample.numberserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * A worker for processing socket clients from server Protocol description: All
 * valid messages are terminated with a newline Valid messages are "terminate"
 * or a 9 digit positive or negative number that may be padded with zeros When
 * "terminate" is the first message received, the server is instructed to stop
 * Valid 9 digit numbers are added to an element broker for further processing
 * All invalid messages are discarded and the client connection is closed
 * @author Lucas Anderson
 */
public class NumberClientWorker implements Runnable {

    /**
     * Default client socket timeout in milliseconds
     */
    public static final int DEFAULT_CLIENT_SOCKET_TIMEOUT_MS = 30 * 1000; // 30
                                                                          // seconds

    /**
     * The server this worker was ran from
     */
    private NumberServer server;

    /**
     * The socket associated with the client
     */
    private Socket socket;

    /**
     * The client socket timeout in milliseconds
     */
    private int clientSocketTimeoutMs = DEFAULT_CLIENT_SOCKET_TIMEOUT_MS;

    /**
     * Element broker to handle valid 9 digit numbers
     */
    private UniqueElementBroker<Integer> elementBroker;

    /**
     * Construct a client work
     * @param server The server that created the worker
     * @param socket The socket associated with the client
     * @param elementBroker The element broker to handle valid numbers
     */
    public NumberClientWorker(NumberServer server, Socket socket, UniqueElementBroker<Integer> elementBroker) {
        this.server = server;
        this.socket = socket;
        this.elementBroker = elementBroker;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            // Set a timeout so if the client doesn't send a newline, the worker
            // won't hang
            socket.setSoTimeout(clientSocketTimeoutMs);
            String message;
            boolean firstMessage = true;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while ((message = in.readLine()) != null) {
                if (message.matches("^-?[0-9]{9}$")) {
                    // A valid 9 digit number, handle it
                    elementBroker.put(Integer.parseInt(message));
                } else if (message.equals("terminate") && firstMessage) {
                    // Time to terminate the server
                    server.stop();
                    break;
                } else {
                    // Invalid message, finish quietly
                    break;
                }
                firstMessage = false;
            }
            // Close the input stream and socket
            in.close();
            socket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
