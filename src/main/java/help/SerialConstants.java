package help;

/**
 * Constants for serial communication configuration.
 * This class centralizes all serial-related constants to avoid magic numbers.
 */
public final class SerialConstants {
    
    /**
     * Default baud rate for serial communication
     */
    public static final int DEFAULT_BAUD_RATE = 115200;
    
    /**
     * Default timeout for serial operations in milliseconds
     */
    public static final int DEFAULT_TIMEOUT_MS = 1000;
    
    /**
     * Default read timeout for serial operations in milliseconds
     */
    public static final int DEFAULT_READ_TIMEOUT_MS = 500;
    
    /**
     * Default write timeout for serial operations in milliseconds
     */
    public static final int DEFAULT_WRITE_TIMEOUT_MS = 500;
    
    /**
     * Maximum number of retry attempts for serial operations
     */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * Delay between retry attempts in milliseconds
     */
    public static final int RETRY_DELAY_MS = 100;
    
    private SerialConstants() {
        // Prevent instantiation
    }
} 