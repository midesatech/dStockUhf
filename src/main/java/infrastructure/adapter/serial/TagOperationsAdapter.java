package infrastructure.adapter.serial;

import domain.gateway.SerialPort;
import domain.gateway.TagOperationsPort;
import domain.model.tag.ESerialMode;
import domain.model.tag.ETagMem;
import domain.model.tag.RxDto;
import help.CommandFilterService;
import help.Constants;
import help.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Infrastructure adapter for UHF tag operations.
 * This adapter implements the TagOperationsPort domain port using the existing command filter service.
 */
public class TagOperationsAdapter implements TagOperationsPort {
    
    private static final Logger logger = LogManager.getLogger(TagOperationsAdapter.class);
    
    private final SerialPort serialPort;
    
    public TagOperationsAdapter(SerialPort serialPort) {
        this.serialPort = serialPort;
    }
    
    @Override
    public RxDto getFirmware() {
        for (byte[] firmware : Constants.getFirmwares()) {
            logger.info("Sending firmware command: {}", Utils.byteArrayToHexString(firmware));
            
            serialPort.sendData(firmware);
            byte[] response = serialPort.readData();
            
            logger.info("Received firmware response: {}", Utils.byteArrayToHexString(response));
            
            RxDto result = CommandFilterService.getConnectResponse(response);
            
            if (result != null && result.getIsValid()) {
                setParameters(result);
                return result;
            }
        }
        return new RxDto(false);
    }
    
    @Override
    public RxDto readBlock(ETagMem memory, String password, String word, String length, ESerialMode serialMode) {
        byte[] cmd = CommandFilterService.getReadCommand(serialMode,
                memory.getValue(),
                Utils.hexStringToByteArray(password),
                (byte) Integer.parseInt(word, 16),
                (byte) Integer.parseInt(length, 16));
        
        logger.debug("Sending read command: {}", Utils.byteArrayToHexString(cmd));
        serialPort.sendData(cmd);
        
        byte[] response = serialPort.readData();
        logger.debug("Received read response: {}", Utils.byteArrayToHexString(response));
        
        return CommandFilterService.getReadResponse(response);
    }
    
    @Override
    public RxDto writeBlock(ETagMem memory, String password, String word, String length, String data, ESerialMode serialMode) {
        byte[] cmd = CommandFilterService.getWriteCommand(serialMode,
                memory.getValue(),
                Utils.hexStringToByteArray(password),
                (byte) Integer.parseInt(word, 16),
                (byte) Integer.parseInt(length, 16),
                Utils.hexStringToByteArray(data));
        
        logger.info("Sending write command: {}", Utils.byteArrayToHexString(cmd));
        serialPort.sendData(cmd);
        
        byte[] response = serialPort.readData();
        logger.info("Received write response: {}", Utils.byteArrayToHexString(response));
        
        return CommandFilterService.getReadResponse(response);
    }
    
    @Override
    public RxDto clearTag(ETagMem memory, String password, ESerialMode serialMode) {
        // For clearing tags, we typically write zeros or a specific pattern
        // This is a simplified implementation - you might need to adjust based on your specific requirements
        String clearData = "00000000000000000000000000000000"; // 32 bytes of zeros
        return writeBlock(memory, password, "00", "10", clearData, serialMode);
    }
    
    private void setParameters(RxDto result) {
        if (Objects.nonNull(result) && result.getIsValid()) {
            byte[] params = CommandFilterService.getSetParameter(result.getMode());
            serialPort.sendData(params);
            
            byte[] response = serialPort.readData();
            logger.info("Parameter set response: {}", Utils.byteArrayToHexString(response));
        }
    }
} 