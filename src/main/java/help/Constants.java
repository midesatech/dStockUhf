package help;

import java.util.List;

public class Constants {

    private static final int PRESET_VALUE = 0xFFFF;
    private static final int POLYNOMIAL = 0x8408;
    private static final byte[] FIRMWARE = {(byte) 0xCF, (byte) 0xFF, (byte)0x00, (byte)0x51, (byte)0x00};
    private static final byte[] READ_BLOCK = {(byte) 0xCF, (byte) 0xFF, 0x00, 0x03, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00};
    private static final byte[] WRITE_BLOCK = { (byte) 0xCF, (byte) 0xFF, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] GET_FIRMWARE = { (byte) 0xBB, 0x00, 0x03, 0x00, 0x01, 0x00, 0x04, 0x7E };
    private static final byte[] SELECT_PARAMETER = { (byte)0xCF, (byte) 0xFF, 0x00, 0x71, 0x19, 0x00, 0x00, 0x00, (byte) 0x80, 0x04, 0x00, 0x01, 0x01, 0x03, (byte)0x86, 0x00, 0x4B, 0x00, 0x19, 0x01, 0x14, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x05, (byte) 0x8C, 0x13 };
    private static final byte[] SELECT_PARAMETER_BB = { (byte) 0xBB, 0x00, 0x0C, 0x00, 0x07, 0x25, 0x00, 0x00, 0x00, 0x20, 0x60, 0x00, (byte) 0xB8, 0x7E };
    private static final byte[] READ_BLOCK_BB = { (byte) 0xBB, 0x00, 0x39, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] WRITE_BLOCK_03 = { (byte) 0xBB, 0x00, 0x49, 0x00, 0x15, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x06 };
    private static final byte[] READ_BLOCK_03 = { (byte) 0xBB, 0x00, 0x39, 0x00, 0x09, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x03, 0x48, 0x7E };
    private static final byte[] READ_BLOCK_02 = { (byte) 0xBB, 0x00, 0x39, 0x00, 0x09, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x06, 0x4A, 0x7E };
    private static final byte LAST_BYTE = 0x7E;
    private static final byte[] OK = { 0x00 };
    private static final byte[] READ_BLOCK_01 = { (byte) 0xBB, 0x00, 0x39, 0x00, 0x09, 0x00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x06, 0x4B, 0x7E };
    private static final byte[] WRITE_BLOCK_BB = { (byte) 0xBB, 0x00, 0x49, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] WRITE_BLOCK_BB_4B = { (byte) 0xBB, 0x00, 0x49, 0x00, 0x0D, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x02 };
    private static final byte VIN =  (byte) 0xF8 ;



    public static int uiCrc16Cal(byte[] pucY) {
        int uiCrcValue = PRESET_VALUE;

        for (byte b : pucY) {
            uiCrcValue = uiCrcValue ^ (b & 0xFF); // Important: Mask byte to treat as unsigned
            for (int j = 0; j < 8; j++) {
                if ((uiCrcValue & 0x0001) != 0) {
                    uiCrcValue = (uiCrcValue >> 1) ^ POLYNOMIAL;
                } else {
                    uiCrcValue = (uiCrcValue >> 1);
                }
            }
        }
        return uiCrcValue;
    }

    public static byte[] addCheck(byte[] command) {
        int crc = uiCrc16Cal(command);
        byte crcHigh = (byte) (crc >> 8);
        byte crcLow = (byte) (crc & 0xFF);

        byte[] newCommand = new byte[command.length + 2];
        System.arraycopy(command, 0, newCommand, 0, command.length);
        newCommand[command.length] = crcHigh;
        newCommand[command.length + 1] = crcLow;

        return newCommand;
    }

    public static byte[] addCrc(byte[] command) {
        int crc = 0;
        for (int i = 1; i < command.length; ++i) {
            crc += command[i];
        }
        byte[] newCommand = new byte[command.length + 2];
        System.arraycopy(command, 0, newCommand, 0, command.length);
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((crc >> 8) & 0xFF); // Most significant byte
        bytes[1] = (byte) (crc & 0xFF);       // Least significant byte
        newCommand[newCommand.length-2] = bytes[1];
        newCommand[newCommand.length-1] = LAST_BYTE;
        return newCommand;
    }

    public static byte[] getFirmware() {
        return addCheck(FIRMWARE);
    }

    public static List<byte[]> getFirmwares() {
        return List.of(GET_FIRMWARE, getFirmware());
    }

    public static byte[] getReadCommand(byte mem, byte[] password, byte word, byte count) {
        System.arraycopy(password, 0, READ_BLOCK, 6, 4);
        READ_BLOCK[10] = mem;
        READ_BLOCK[12] = word;
        READ_BLOCK[13] = count;
        return addCheck(READ_BLOCK);
    }

    public static byte[] getReadCommandBB(byte mem, byte[] password, byte word, byte count) {
        System.arraycopy(password, 0, READ_BLOCK_BB, 5, 4);
        READ_BLOCK_BB[9] = mem;
        READ_BLOCK_BB[11] = word;
        READ_BLOCK_BB[13] = count;
        return addCrc(READ_BLOCK_BB);
    }

    public static byte[] getWriteCommand(byte mem, byte[] password, byte word, byte count, byte[] data) {
        System.arraycopy(password, 0, WRITE_BLOCK, 6, 4);
        WRITE_BLOCK[4] = (byte) (9 + count * 2);
        WRITE_BLOCK[10] = mem;
        WRITE_BLOCK[12] = word;
        WRITE_BLOCK[13] = count;
        byte[] newCommand = new byte[WRITE_BLOCK.length + data.length];
        System.arraycopy(WRITE_BLOCK, 0, newCommand, 0, WRITE_BLOCK.length);
        System.arraycopy(data, 0, newCommand, WRITE_BLOCK.length, data.length);
        return addCheck(newCommand);
    }

    public static byte[] getWriteCommandBB(byte mem, byte[] password, byte word, byte count, byte[] data) {
        WRITE_BLOCK_BB[4] = (byte) (password.length + 1 + 2 + 2 + data.length);
        System.arraycopy(password, 0, WRITE_BLOCK_BB, 5, password.length);
        WRITE_BLOCK_BB[9] = mem;
        WRITE_BLOCK_BB[11] = word;
        WRITE_BLOCK_BB[13] = count;
        //WRITE_BLOCK_BB[14] = VIN;
        byte[] newCommand = new byte[WRITE_BLOCK_BB.length + data.length];
        System.arraycopy(WRITE_BLOCK_BB, 0, newCommand, 0, WRITE_BLOCK_BB.length);
        System.arraycopy(data, 0, newCommand, WRITE_BLOCK_BB.length, data.length);
        return addCrc(newCommand);
    }

    public static byte[] getSelectParametersBB() {
        return SELECT_PARAMETER_BB;
    }

    public static byte[] getSelectParametersCF() {
        return SELECT_PARAMETER;
    }

}
