package domain.gateway;

import domain.model.tag.ESerialMode;
import domain.model.tag.ETagMem;
import domain.model.tag.RxDto;

/**
 * Domain port for UHF tag operations.
 * This interface defines the contract for tag-specific operations that the domain requires.
 */
public interface TagOperationsPort {
    
    /**
     * Get firmware information from the connected device
     * @return RxDto containing firmware information
     */
    RxDto getFirmware();
    
    /**
     * Read a block from a UHF tag
     * @param memory the memory bank to read from
     * @param password the access password
     * @param word the starting word address
     * @param length the number of words to read
     * @param serialMode the serial communication mode
     * @return RxDto containing the read result
     */
    RxDto readBlock(ETagMem memory, String password, String word, String length, ESerialMode serialMode);
    
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
    RxDto writeBlock(ETagMem memory, String password, String word, String length, String data, ESerialMode serialMode);
    
    /**
     * Clear a tag's memory
     * @param memory the memory bank to clear
     * @param password the access password
     * @param serialMode the serial communication mode
     * @return RxDto containing the clear result
     */
    RxDto clearTag(ETagMem memory, String password, ESerialMode serialMode);
} 