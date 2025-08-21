package domain.usecase.tag;

import domain.gateway.SerialPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;

/**
 * Use case for serial communication operations.
 * This use case orchestrates serial port operations through domain ports.
 */
public class SerialCommunicationUseCase {
    
    private static final Logger logger = LogManager.getLogger(SerialCommunicationUseCase.class);
    
    private final SerialPort serialPort;
    
    public SerialCommunicationUseCase(SerialPort serialPort) {
        this.serialPort = serialPort;
    }
    
    /**
     * Get available serial ports
     * @return Set of available port names
     */
    public Set<String> getAvailablePorts() {
        try {
            return serialPort.getAvailablePorts();
        } catch (Exception e) {
            logger.error("Error getting available ports: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }
    
    /**
     * Connect to a serial port
     * @param portName the port name to connect to
     * @param baudRate the baud rate for communication
     * @return true if connection successful, false otherwise
     */
    public boolean connect(String portName, int baudRate) {
        try {
            if (serialPort.isConnected()) {
                logger.info("Already connected to port: {}", portName);
                return true;
            }
            
            boolean connected = serialPort.connect(portName, baudRate);
            if (connected) {
                logger.info("Successfully connected to port: {} at {} baud", portName, baudRate);
            } else {
                logger.error("Failed to connect to port: {} at {} baud", portName, baudRate);
            }
            return connected;
        } catch (Exception e) {
            logger.error("Error connecting to port {}: {}", portName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Disconnect from the serial port
     */
    public void disconnect() {
        try {
            serialPort.disconnect();
            logger.info("Disconnected from serial port");
        } catch (Exception e) {
            logger.error("Error disconnecting from serial port: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check if currently connected
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return serialPort.isConnected();
    }
    
    /**
     * Send data to the serial port
     * @param data the data to send
     */
    public void sendData(byte[] data) {
        try {
            if (!serialPort.isConnected()) {
                logger.warn("Cannot send data: not connected to serial port");
                return;
            }
            serialPort.sendData(data);
            logger.debug("Sent {} bytes to serial port", data.length);
        } catch (Exception e) {
            logger.error("Error sending data to serial port: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Read data from the serial port
     * @return the received data
     */
    public byte[] readData() {
        try {
            if (!serialPort.isConnected()) {
                logger.warn("Cannot read data: not connected to serial port");
                return new byte[0];
            }
            byte[] data = serialPort.readData();
            logger.debug("Read {} bytes from serial port", data.length);
            return data;
        } catch (Exception e) {
            logger.error("Error reading data from serial port: {}", e.getMessage(), e);
            return new byte[0];
        }
    }
    
    /**
     * Read data from the serial port with timeout
     * @param timeoutMs timeout in milliseconds
     * @return the received data
     */
    public byte[] readData(int timeoutMs) {
        try {
            if (!serialPort.isConnected()) {
                logger.warn("Cannot read data: not connected to serial port");
                return new byte[0];
            }
            byte[] data = serialPort.readData(timeoutMs);
            logger.debug("Read {} bytes from serial port with {}ms timeout", data.length, timeoutMs);
            return data;
        } catch (Exception e) {
            logger.error("Error reading data from serial port with timeout: {}", e.getMessage(), e);
            return new byte[0];
        }
    }
} 