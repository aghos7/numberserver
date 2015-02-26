package sample.numberserver;

/**
 * Different ways to shutdown a server
 * @author Lucas Anderson
 */
public enum ServerShutdownMethod {
    GRACEFULLY, // Attempt to let current tasks finish and gracefully close all
                // connections / files
    ABRUPT // Stop everything instantly by killing the JVM
}
