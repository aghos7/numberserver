package sample.numberserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NumberClient implements Runnable {

    private String serverName;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;

    public static final int MAX_THREADS = 5;
    public static final int MAX_VALUE = 1000000000;
    public static final int MAX_NUMBERS = 100;

    private int maxNumbers = MAX_NUMBERS;
    private boolean isTerminate = false;

    public NumberClient(String serverName, int serverPort, boolean isTerminate) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.isTerminate = isTerminate;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverName, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Random randomGenerator = new Random();
            while (maxNumbers > 0) {
                try {
                    if (isTerminate) {
                        out.println("terminate");
                    } else {
                        out.println(String.format("-%09d", randomGenerator.nextInt(MAX_VALUE)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                maxNumbers--;
            }
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting");
        long timeBefore = System.currentTimeMillis();
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < MAX_THREADS; i++) {
            executor.execute(new NumberClient("localhost", 4000, false));
        }
        // executor.execute(new NumberClient("localhost", 4000, true));
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            System.out.println("WTF");
            e.printStackTrace();
        }
        long timeAfter = System.currentTimeMillis();
        long elapsed = timeAfter - timeBefore;
        System.out.println("elapsed:" + elapsed + " num/sec: " + (MAX_THREADS * MAX_NUMBERS) / elapsed * 1000);
    }

}
