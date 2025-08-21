package infrastructure.adapter.serial;

import domain.gateway.SerialPort;
import help.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Infrastructure adapter for serial port operations.
 * This adapter implements the SerialPort domain port using the existing serial infrastructure.
 */
public class SerialPortAdapter implements SerialPort {
    
    private static final Logger logger = LogManager.getLogger(SerialPortAdapter.class);
    
    private final SerialFactory serialFactory;
    private Serial serial;
    
    public SerialPortAdapter(SerialFactory serialFactory) {
        this.serialFactory = serialFactory;
        this.serial = serialFactory.getSerial(Utils.getArchitecture()).orElse(null);
    }
    
    @Override
    public Set<String> getAvailablePorts() {
        if (serial != null) {
            return serial.getPorts();
        }
        logger.warn("Serial interface not available");
        return Set.of();
    }
    
    @Override
    public boolean connect(String portName, int baudRate) {
        if (serial == null) {
            logger.error("Serial interface not available");
            return false;
        }
        
        if (serial.isOpen()) {
            logger.info("Already connected to port: {}", portName);
            return true;
        }
        
        boolean configured = serial.configure(portName, baudRate);
        if (configured) {
            return serial.init();
        }
        
        logger.error("Failed to configure serial port: {} at {} baud", portName, baudRate);
        return false;
    }
    
    @Override
    public void disconnect() {
        if (serial != null) {
            serial.close();
            logger.info("Disconnected from serial port");
        }
    }
    
    @Override
    public boolean isConnected() {
        return serial != null && serial.isOpen();
    }
    
    @Override
    public void sendData(byte[] data) {
        if (serial != null) {
            serial.write(data);
            logger.debug("Sent {} bytes to serial port", data.length);
        } else {
            logger.warn("Cannot send data: serial interface not available");
        }
    }
    
    @Override
    public byte[] readData() {
        if (serial != null) {
            byte[] data = serial.read();
            logger.debug("Read {} bytes from serial port", data.length);
            return data;
        }
        logger.warn("Cannot read data: serial interface not available");
        return new byte[0];
    }
    
    @Override
    public byte[] readData(int timeoutMs) {
        // Note: The existing Serial interface doesn't support timeout,
        // so we'll use the default read method. In a real implementation,
        // you might want to add timeout support to the Serial interface.
        logger.debug("Reading data with {}ms timeout (using default read)", timeoutMs);
        return readData();
    }
} 