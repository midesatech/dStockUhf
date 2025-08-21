package domain.usecase.tag;

import domain.model.tag.ESerialMode;
import domain.model.tag.ETagMem;
import domain.model.tag.RxDto;
import help.CommandFilterService;
import help.Constants;
import help.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class OperationsUseCase {

    private static final Logger logger = LogManager.getLogger(OperationsUseCase.class);
    private final SerialUseCase serialUseCase;
    private TagOperationsUseCase tagOperationsUseCase;
    private SerialCommunicationUseCase serialCommunicationUseCase;

    public OperationsUseCase(SerialUseCase serialUseCase) {
        this.serialUseCase = serialUseCase;
    }
    
    public OperationsUseCase(TagOperationsUseCase tagOperationsUseCase, SerialCommunicationUseCase serialCommunicationUseCase) {
        this.serialUseCase = null;
        this.tagOperationsUseCase = tagOperationsUseCase;
        this.serialCommunicationUseCase = serialCommunicationUseCase;
    }

    public boolean connect(String port, int bauds) {
        if (tagOperationsUseCase != null) {
            return serialCommunicationUseCase.connect(port, bauds);
        }
        return serialUseCase.connect(port, bauds);
    }

    public void disconnect() {
        if (tagOperationsUseCase != null) {
            serialCommunicationUseCase.disconnect();
        } else {
            serialUseCase.disconnect();
        }
    }

    public List<String> getPorts() {
        if (tagOperationsUseCase != null) {
            return serialCommunicationUseCase.getAvailablePorts().stream().toList();
        }
        return serialUseCase.getPorts();
    }

    public RxDto getFirmware() {
        if (tagOperationsUseCase != null) {
            return tagOperationsUseCase.getFirmware();
        }
        
        for (byte[] firmware : Constants.getFirmwares()) {
            // Log the firmware to be sent
            logger.info(Utils.byteArrayToHexString(firmware));

            serialUseCase.sendCommand(firmware);
            byte[] response = serialUseCase.readCommand();

            // Log the response
            logger.info(Utils.byteArrayToHexString(response));

            RxDto result = CommandFilterService.getConnectResponse(response);

            if (result != null && result.getIsValid()) {
                setParameters(result);
                return result; // Return as soon as we find a valid result
            }
        }
        return new RxDto(false);
    }

    public RxDto readBlock(ETagMem memory, String pass, String word, String len, ESerialMode serialMode) {
        if (tagOperationsUseCase != null) {
            return tagOperationsUseCase.readBlock(memory, pass, word, len, serialMode);
        }
        
        byte[] cmd = CommandFilterService.getReadCommand(serialMode,
                memory.getValue(),
                Utils.hexStringToByteArray(pass),
                (byte) Integer.parseInt(word, 16),
                (byte) Integer.parseInt(len, 16));
        logger.debug(Utils.byteArrayToHexString(cmd));
        this.serialUseCase.sendCommand(cmd);
        byte[] response = this.serialUseCase.readCommand();
        return CommandFilterService.getReadResponse(response);
    }

    public RxDto writeBlock(ETagMem memory, String pass, String word, String len, String data, ESerialMode serialMode) {
        if (tagOperationsUseCase != null) {
            return tagOperationsUseCase.writeBlock(memory, pass, word, len, data, serialMode);
        }
        
        byte[] cmd = CommandFilterService.getWriteCommand(serialMode,
                memory.getValue(),
                Utils.hexStringToByteArray(pass),
                (byte) Integer.parseInt(word, 16),
                (byte) Integer.parseInt(len, 16),
                Utils.hexStringToByteArray(data));
        logger.info(Utils.byteArrayToHexString(cmd));
        this.serialUseCase.sendCommand(cmd);
        byte[] response = this.serialUseCase.readCommand();
        logger.info(Utils.byteArrayToHexString(response));
        return CommandFilterService.getReadResponse(response);
    }

    private void setParameters(RxDto result) {
        if (Objects.nonNull(result) && result.getIsValid()) {

            byte[] params = CommandFilterService.getSetParameter(result.getMode());

            this.serialUseCase.sendCommand(params);

            byte[] response = this.serialUseCase.readCommand();

            logger.info(Utils.byteArrayToHexString(response));


        }
    }
}
