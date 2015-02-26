package sample.numberserver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Log numbers read from an element broker to a file The default file is
 * <current working directory>/numbers.log
 * @note This class should probably be made more generic to handle types other
 *       than Integer
 * @author Lucas Anderson
 */
public class NumberFileLogger implements Runnable {

    /**
     * The default path to log to
     */
    public static final Path DEFAULT_PATH = Paths.get("numbers.log");

    /**
     * The path to log to
     */
    private Path path;

    /**
     * The element broker to retrieve elements from
     */
    private UniqueElementBroker<Integer> elementBroker;

    /**
     * Construct a number file logger
     * @param elementBroker The element broker to retrieve elements from
     */
    public NumberFileLogger(UniqueElementBroker<Integer> elementBroker) {
        this(DEFAULT_PATH, elementBroker);
    }

    /**
     * Construct a number file logger
     * @param path The path to log to
     * @param elementBroker The element broker to retrieve elements from
     */
    public NumberFileLogger(Path path, UniqueElementBroker<Integer> elementBroker) {
        this.path = path;
        this.elementBroker = elementBroker;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try (BufferedWriter logWriter = Files.newBufferedWriter(path, Charset.defaultCharset())) {
            while (!Thread.interrupted()) { // Log elements until the thread is interrupted
                try {
                    logWriter.write(elementBroker.get() + System.lineSeparator());
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't write to log: " + path.toAbsolutePath(), e);
        }
    }
}
