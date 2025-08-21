package infrastructure.adapter.serial;

import com.fazecast.jSerialComm.SerialPort;
import help.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class SerialAmdImpl implements Serial {

    private static SerialAmdImpl instance = null;
    private SerialPort serialPort;
    private boolean started;

    private SerialAmdImpl() {

    }

    public static SerialAmdImpl getInstance() {
        if (instance == null) {
            instance = new SerialAmdImpl();
        }
        return instance;
    }

    @Override
    public void write(byte[] data) {
        try {
            int bytesWritten = serialPort.writeBytes(data, data.length);
            if (bytesWritten < 0) {
                errorLog.error(" ==>> SERIAL WRITE FAILED: writeBytes returned negative value");
                debLog.debug(" ==>> SERIAL WRITE FAILED: writeBytes returned negative value");
            }
        } catch (Exception ex) {
            errorLog.error(" ==>> SERIAL WRITE FAILED: " + ex.getMessage());
            debLog.debug(" ==>> SERIAL WRITE FAILED: " + ex.getMessage());
        }
    }

    @Override
    public byte[] read() {
        byte[] ret = new byte[0];
        byte[] response;

        try {
            int len = 0;
            len = serialPort.bytesAvailable();
            debLog.debug("Bytes to read: {}", len);
            response = new byte[len];

            for (int i = 0; i < 30; i++) {
                if (len > 0 && response[0] != 0x00) {
                    debLog.debug("Count: {}", i);
                    break;
                }
                Thread.sleep(20);
                len = serialPort.bytesAvailable();
                response = new byte[len];
            }

            int hay = serialPort.readBytes(response, len);
            ret = java.util.Arrays.copyOfRange(response, 0, hay);

            debLog.debug("Bytes2 " + hay + " " + response.length);
            debLog.debug("Cadena " + Utils.byteArrayToHexString(ret));
            debLog.debug("Bytes3 " + hay + " " + response.length);

            return ret;
        } catch (Exception ex) {
            errorLog.error(" ==>> SERIAL READ FAILED : " + ex.getMessage());
            debLog.debug(" ==>> SERIAL READ FAILED : " + ex.getMessage());
        }

        return ret;
    }

    @Override
    public boolean configure() {
        return false;
    }

    @Override
    public boolean configure(String portName, int bauds) {
        SerialPort[] ports = SerialPort.getCommPorts();

        try {
            if (ports.length > 0) {
                serialPort = SerialPort.getCommPort(portName);
                if (serialPort.getDescriptivePortName().contains("Bluetooth") || serialPort.getDescriptivePortName().contains("debug")) {
                    serialPort.closePort();
                    return false;
                }
                serialPort.setBaudRate(bauds);
                serialPort.setNumDataBits(8);
                serialPort.setParity(SerialPort.NO_PARITY);
                serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
                serialPort.setNumStopBits(1);
                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
                return true;
            }
        } catch (Exception ex) {
            errorLog.error(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean init() {
        if (serialPort == null) {
            return false;
        }
        //boolean close = serialPort.closePort();
        return serialPort.openPort();
    }

    @Override
    public void close() {
        if (serialPort != null && serialPort.isOpen()) serialPort.closePort();
    }


    @Override
    public Set<String> getPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();

        Set<String> portNames = new HashSet<>();

        for (SerialPort port : ports) {
            portNames.add(port.getSystemPortPath()); // Get the port name as a String
        }
        return portNames;
    }

    @Override
    public boolean isOpen() {
        if (serialPort != null) {
            return serialPort.isOpen();
        }
        return false;
    }

    private static final long serialVersionUID = 7000564086526997551L;
    private static final Logger errorLog = LogManager.getLogger("errorLogger");
    private static final Logger infLog = LogManager.getLogger("infLogger");
    private static final Logger debLog = LogManager.getLogger("stdout");

}