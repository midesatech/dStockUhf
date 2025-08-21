package domain.usecase.tag;

import domain.gateway.TagOperationsPort;
import domain.model.tag.ESerialMode;
import domain.model.tag.ETagMem;
import domain.model.tag.RxDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Use case for UHF tag operations.
 * This use case orchestrates tag-specific operations through domain ports.
 */
public class TagOperationsUseCase {
    
    private static final Logger logger = LogManager.getLogger(TagOperationsUseCase.class);
    
    private final TagOperationsPort tagOperationsPort;
    
    public TagOperationsUseCase(TagOperationsPort tagOperationsPort) {
        this.tagOperationsPort = tagOperationsPort;
    }
    
    /**
     * Get firmware information from the connected device
     * @return RxDto containing firmware information
     */
    public RxDto getFirmware() {
        try {
            logger.info("Getting firmware information");
            RxDto result = tagOperationsPort.getFirmware();
            
            if (result != null && result.getIsValid()) {
                logger.info("Firmware information retrieved successfully");
            } else {
                logger.warn("Failed to get valid firmware information");
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error getting firmware information: {}", e.getMessage(), e);
            return new RxDto(false);
        }
    }
    
    /**
     * Read a block from a UHF tag
     * @param memory the memory bank to read from
     * @param password the access password
     * @param word the starting word address
     * @param length the number of words to read
     * @param serialMode the serial communication mode
     * @return RxDto containing the read result
     */
    public RxDto readBlock(ETagMem memory, String password, String word, String length, ESerialMode serialMode) {
        try {
            logger.info("Reading block from tag - Memory: {}, Word: {}, Length: {}", memory, word, length);
            
            RxDto result = tagOperationsPort.readBlock(memory, password, word, length, serialMode);
            
            if (result != null && result.getIsValid()) {
                logger.info("Block read successfully");
            } else {
                logger.warn("Failed to read block from tag");
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error reading block from tag: {}", e.getMessage(), e);
            return new RxDto(false);
        }
    }
    
    /**
     * Write a block to a UHF tag
     * @param memory the memory bank to write to
     * @param password the access password
     * @param word the starting word address
     * @param length the number of words to write
     * @param data the data to write
     * @param serialMode the serial communication mode
     * @return RxDto containing the write result
     */
    public RxDto writeBlock(ETagMem memory, String password, String word, String length, String data, ESerialMode serialMode) {
        try {
            logger.info("Writing block to tag - Memory: {}, Word: {}, Length: {}, Data: {}", memory, word, length, data);
            
            RxDto result = tagOperationsPort.writeBlock(memory, password, word, length, data, serialMode);
            
            if (result != null && result.getIsValid()) {
                logger.info("Block written successfully");
            } else {
                logger.warn("Failed to write block to tag");
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error writing block to tag: {}", e.getMessage(), e);
            return new RxDto(false);
        }
    }
    
    /**
     * Clear a tag's memory
     * @param memory the memory bank to clear
     * @param password the access password
     * @param serialMode the serial communication mode
     * @return RxDto containing the clear result
     */
    public RxDto clearTag(ETagMem memory, String password, ESerialMode serialMode) {
        try {
            logger.info("Clearing tag memory - Memory: {}", memory);
            
            RxDto result = tagOperationsPort.clearTag(memory, password, serialMode);
            
            if (result != null && result.getIsValid()) {
                logger.info("Tag memory cleared successfully");
            } else {
                logger.warn("Failed to clear tag memory");
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error clearing tag memory: {}", e.getMessage(), e);
            return new RxDto(false);
        }
    }
} 