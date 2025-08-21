package domain.gateway;

import java.util.Set;

/**
 * Domain port for serial communication operations.
 * This interface defines the contract for serial port operations that the domain requires.
 */
public interface SerialPort {
    
    /**
     * Get available serial ports
     * @return Set of available port names
     */
    Set<String> getAvailablePorts();
    
    /**
     * Connect to a serial port
     * @param portName the port name to connect to
     * @param baudRate the baud rate for communication
     * @return true if connection successful, false otherwise
     */
    boolean connect(String portName, int baudRate);
    
    /**
     * Disconnect from the serial port
     */
    void disconnect();
    
    /**
     * Check if the port is currently open
     * @return true if port is open, false otherwise
     */
    boolean isConnected();
    
    /**
     * Send data to the serial port
     * @param data the data to send
     */
    void sendData(byte[] data);
    
    /**
     * Read data from the serial port
     * @return the received data
     */
    byte[] readData();
    
    /**
     * Read data from the serial port with timeout
     * @param timeoutMs timeout in milliseconds
     * @return the received data
     */
    byte[] readData(int timeoutMs);
} 