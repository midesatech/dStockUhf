package domain.usecase.tag;

import domain.gateway.SerialPortRepository;
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
    
    private final SerialPortRepository serialPortRepository;
    
    public SerialCommunicationUseCase(SerialPortRepository serialPortRepository) {
        this.serialPortRepository = serialPortRepository;
    }
    
    /**
     * Get available serial ports
     * @return Set of available port names
     */
    public Set<String> getAvailablePorts() {
        try {
            return serialPortRepository.getAvailablePorts();
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
            if (serialPortRepository.isConnected()) {
                logger.info("Already connected to port: {}", portName);
                return true;
            }
            
            boolean connected = serialPortRepository.connect(portName, baudRate);
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
            serialPortRepository.disconnect();
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
        return serialPortRepository.isConnected();
    }
    
    /**
     * Send data to the serial port
     * @param data the data to send
     */
    public void sendData(byte[] data) {
        try {
            if (!serialPortRepository.isConnected()) {
                logger.warn("Cannot send data: not connected to serial port");
                return;
            }
            serialPortRepository.sendData(data);
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
            if (!serialPortRepository.isConnected()) {
                logger.warn("Cannot read data: not connected to serial port");
                return new byte[0];
            }
            byte[] data = serialPortRepository.readData();
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
            if (!serialPortRepository.isConnected()) {
                logger.warn("Cannot read data: not connected to serial port");
                return new byte[0];
            }
            byte[] data = serialPortRepository.readData(timeoutMs);
            logger.debug("Read {} bytes from serial port with {}ms timeout", data.length, timeoutMs);
            return data;
        } catch (Exception e) {
            logger.error("Error reading data from serial port with timeout: {}", e.getMessage(), e);
            return new byte[0];
        }
    }
} 