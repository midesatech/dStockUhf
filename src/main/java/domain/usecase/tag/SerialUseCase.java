package domain.usecase.tag;

import help.Utils;
import infrastructure.adapter.serial.Serial;
import infrastructure.adapter.serial.SerialFactory;

import java.util.Collections;
import java.util.List;

public class SerialUseCase {

    private Serial serial;
    private final SerialFactory serialFactory;
    private SerialCommunicationUseCase serialCommunicationUseCase;

    public SerialUseCase(SerialFactory serialFactory) {
        this.serialFactory = serialFactory;
        this.serial = serialFactory.getSerial(Utils.getArchitecture()).orElse(null);
    }
    
    public SerialUseCase(SerialCommunicationUseCase serialCommunicationUseCase) {
        this.serialFactory = null;
        this.serial = null;
        this.serialCommunicationUseCase = serialCommunicationUseCase;
    }

    public List<String> getPorts() {
        if (serialCommunicationUseCase != null) {
            return serialCommunicationUseCase.getAvailablePorts().stream().toList();
        } else if (serial != null) {
            return List.copyOf(serial.getPorts());
        }
        return Collections.emptyList();
    }

    public boolean connect(String port, int baudRate) {
        if (serialCommunicationUseCase != null) {
            return serialCommunicationUseCase.connect(port, baudRate);
        } else if (serial != null && serial.isOpen()) {
            return true;
        } else if (serial != null && serial.configure(port, baudRate)) {
            return serial.init();
        }
        return false;
    }

    public void disconnect() {
        if (serialCommunicationUseCase != null) {
            serialCommunicationUseCase.disconnect();
        } else if (serial != null) {
            serial.close();
        }
    }

    public void sendCommand(byte[] command) {
        if (serialCommunicationUseCase != null) {
            serialCommunicationUseCase.sendData(command);
        } else if (serial != null) {
            serial.write(command);
        }
    }

    public byte[] readCommand() {
        if (serialCommunicationUseCase != null) {
            return serialCommunicationUseCase.readData();
        } else if (serial != null) {
            return serial.read();
        }
        return new byte[0];
    }
}