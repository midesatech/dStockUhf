package infrastructure.logging;

/**
 * Centralized logging messages for the dPassFx application.
 * This class provides consistent log messages across all components
 * to improve maintainability and reduce duplication.
 */
public final class LogMessages {

    // Task lifecycle messages
    public static final String TASK_STARTED = "Task started";
    public static final String TASK_CANCELLED = "Task cancelled";
    public static final String TASK_FAILED = "Task failed";
    public static final String TASK_COMPLETED = "Task completed successfully";

    // Background task messages
    public static final String BACKGROUND_TASK_STARTED = "Background task started";
    public static final String BACKGROUND_TASK_CANCELLED = "Background task cancelled";
    public static final String BACKGROUND_TASK_FAILED = "Background task failed";

    // Reader detection messages
    public static final String READER_DETECTION_STARTED = "Reader detection started";
    public static final String READER_DETECTED = "Reader detected successfully";
    public static final String READER_NOT_DETECTED = "No valid reader detected";
    public static final String READER_DETECTION_FAILED = "Reader detection failed";

    // EPC operation messages
    public static final String EPC_READ_STARTED = "EPC read operation started";
    public static final String EPC_READ_COMPLETED = "EPC read operation completed";
    public static final String EPC_READ_FAILED = "EPC read operation failed";
    public static final String EPC_WRITE_STARTED = "EPC write operation started";
    public static final String EPC_WRITE_COMPLETED = "EPC write operation completed";
    public static final String EPC_WRITE_FAILED = "EPC write operation failed";
    public static final String EPC_READ_OK = "EPC TAG read OK";
    public static final String EPC_READ_TIMEOUT = "Detección desactivada por tiempo de espera vencido";

    // Notification messages
    public static final String NOTIFICATION_STARTED = "Notification process started";
    public static final String NOTIFICATION_COMPLETED = "Notification process completed";
    public static final String NOTIFICATION_FAILED = "Notification process failed";
    public static final String NOTIFICATION_CANCELLED = "Notification process cancelled";

    // Serial communication messages
    public static final String SERIAL_CONNECTED = "Serial port connected successfully";
    public static final String SERIAL_DISCONNECTED = "Serial port disconnected";
    public static final String SERIAL_CONNECTION_FAILED = "Serial port connection failed";
    public static final String SERIAL_ALREADY_CONNECTED = "Serial port already connected";

    // Enrollment messages
    public static final String ENROLLMENT_STARTED = "Enrollment process started";
    public static final String ENROLLMENT_COMPLETED = "Enrollment process completed";
    public static final String ENROLLMENT_FAILED = "Enrollment process failed";
    public static final String ENROLLMENT_CANCELLED = "Enrollment process cancelled";

    // Application lifecycle messages
    public static final String APPLICATION_STARTED = "Application started successfully";
    public static final String APPLICATION_INITIALIZED = "Application initialized successfully";
    public static final String APPLICATION_SHUTDOWN = "Application shutdown initiated";

    // Error messages
    public static final String UNEXPECTED_ERROR = "Unexpected error occurred";
    public static final String CONFIGURATION_ERROR = "Configuration error";
    public static final String VALIDATION_ERROR = "Validation error";

    // Success messages
    public static final String OPERATION_SUCCESSFUL = "Operation completed successfully";
    public static final String CONNECTION_SUCCESSFUL = "Connection established successfully";
    public static final String DATA_SAVED_SUCCESSFULLY = "Data saved successfully";

    // Timeout messages
    public static final String OPERATION_TIMEOUT = "Operation timed out";
    public static final String CONNECTION_TIMEOUT = "Connection timed out";
    public static final String READ_TIMEOUT = "Read operation timed out";

    // State messages
    public static final String TASK_ALREADY_RUNNING = "Task is already running";
    public static final String TASK_RESTARTING = "Task restarting";
    public static final String TASK_STATE_CHANGED = "Task state changed";

    private LogMessages() {
        // Prevent instantiation
    }

    /**
     * Formats a log message with additional context.
     *
     * @param baseMessage the base message
     * @param context additional context information
     * @return formatted log message
     */
    public static String withContext(String baseMessage, String context) {
        return String.format("%s: %s", baseMessage, context);
    }

    /**
     * Formats a log message with component name.
     *
     * @param componentName the name of the component
     * @param message the message
     * @return formatted log message
     */
    public static String fromComponent(String componentName, String message) {
        return String.format("%s: %s", componentName, message);
    }
}
