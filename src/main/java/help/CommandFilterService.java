package help;

import domain.model.tag.ESerialMode;
import domain.model.tag.RxDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public class CommandFilterService implements Serializable {
    private static final long serialVersionUID = 7230564086526997551L;
    private static final Logger errorLog = LogManager.getLogger("errorLogger");
    private static final Logger infLog = LogManager.getLogger("infLogger");
    private static final Logger debLog = LogManager.getLogger("stdout");

    private CommandFilterService() {

    }

    public static RxDto getConnectResponse(byte[] response) {
        if (response.length < 5 ) {
            return new RxDto(false, null, null);
        }
        byte header = response[0];
        return switch (header) {
            case (byte) 0xCF -> getConnectResponseCF(response);
            case (byte) 0xBB -> getConnectResponseBB(response);
            default -> new RxDto(false, null, null);
        };
    }


    public static RxDto getReadResponse(byte[] response) {
        if (response.length < 5 ) {
            return new RxDto(false, null, null);
        }
        byte header = response[0];

        return switch (header) {
            case (byte) 0xCF -> getReadResponseCF(response);
            case (byte) 0xBB -> getReadResponseBB(response);
            default -> new RxDto(false, null, null);
        };
    }

    public static RxDto getConnectResponseBB(byte[] response) {
        RxDto result = new RxDto(false, null, null);
        int pos = 0;
        int header = Byte.toUnsignedInt(response[pos]);
        int len = Byte.toUnsignedInt(response[pos + 4]);
        int status = Byte.toUnsignedInt(response[pos + 5]); // Convert 0 to 0x00
        if (header == 0xBB && status == 0x00 && len >= 0x10) {
            byte[] data = java.util.Arrays.copyOfRange(response, pos + 6, response.length -2);
            String ascii = Utils.hexToAscii(java.util.Arrays.copyOf(data, 30));
            result = new RxDto(true, data, ascii);
            result.setMode(ESerialMode.BB);
        }
        return result;
    }

    public static RxDto getReadResponseBB(byte[] response) {
        if (response.length < 5 ) {
            return new RxDto(false, null, null);
        }
        int header = Byte.toUnsignedInt(response[0]); // Convert -49 to 0xCF
        int len = Byte.toUnsignedInt(response[4]);    // Convert 77 to 0x4D
        int status = Byte.toUnsignedInt(response[5]); // Convert 0 to 0x00

        if (header == 0xBB && (status == 0x0E || status == 0x06) && len >= 0x08) {
            byte[] allData = java.util.Arrays.copyOfRange(response, 4, len + 6);
            int from = 5;
            int to = from + allData[1] + 1;
            byte[] pcEpc = java.util.Arrays.copyOfRange(response, from, to);
            from = 1;
            to = from + 2;
            byte[] pc = java.util.Arrays.copyOfRange(pcEpc, from, to);
            int epcLen = Byte.toUnsignedInt(pcEpc[0]) -2;
            from = to;
            to = from + epcLen;
            byte[] epc = java.util.Arrays.copyOfRange(pcEpc, from, to);
            from = to + 1;
            to = len + 1;
            byte[] data = null;
            if (from <= to) {
                data = java.util.Arrays.copyOfRange(allData, from, to);
            }
            byte[] crc = new byte[]{0x00, (byte)allData[to]};
            RxDto result = new RxDto(true, allData, Utils.byteArrayToHexString(allData), epc, data, crc, pc);
            result.setMode(ESerialMode.BB);
            return result;
        }
        return new RxDto(false, null, null);
    }

    public static RxDto getConnectResponseCF(byte[] response) {
        RxDto result = new RxDto(false, null, null);
        int pos = 0;
        if (Byte.toUnsignedInt(response[0]) == 0x53 && Byte.toUnsignedInt(response[1]) == 0x6F ) {
            pos = 58;
        }
        int header = Byte.toUnsignedInt(response[pos]); // Convert -49 to 0xCF
        int len = Byte.toUnsignedInt(response[pos + 4]);    // Convert 77 to 0x4D
        int status = Byte.toUnsignedInt(response[pos + 5]); // Convert 0 to 0x00
        if (header == 0xCF && status == 0x00 && len >= 0x20) {
            byte[] data = java.util.Arrays.copyOfRange(response, pos + 6, len);
            String ascii = Utils.hexToAscii(java.util.Arrays.copyOf(data, 32));
            result = new RxDto(true, data, ascii);
            result.setMode(ESerialMode.CF);
        }
        return result;
    }

    public static RxDto getReadResponseCF(byte[] response) {
        if (response.length < 5 ) {
            return new RxDto(false, null, null);
        }
        int header = Byte.toUnsignedInt(response[0]); // Convert -49 to 0xCF
        int len = Byte.toUnsignedInt(response[4]);    // Convert 77 to 0x4D
        int status = Byte.toUnsignedInt(response[5]); // Convert 0 to 0x00

        if (header == 0xCF && status == 0x00 && len >= 0x0C) {
            byte[] allData = java.util.Arrays.copyOfRange(response, 6, len + 5);
            int from = 8;
            int to = from + 2;
            byte[] crc = java.util.Arrays.copyOfRange(response, from, to);
            from = to;
            to = from + 2;
            byte[] pc = java.util.Arrays.copyOfRange(response, from, to);
            int epcLen = Byte.toUnsignedInt(response[12]);
            from = to + 1;
            to = from + epcLen;
            byte[] epc = java.util.Arrays.copyOfRange(response, from, to);
            from = to + 1;
            to = len + 5;
            //to = response.length - 2;
            byte[] data = null;
            if (from <= to) {
                data = java.util.Arrays.copyOfRange(response, from, to);
            }

            return new RxDto(true, allData, Utils.byteArrayToHexString(allData), epc, data, crc, pc);
        }
        return new RxDto(false, null, null);
    }

    public static byte[] getReadCommand(ESerialMode serialMode, byte mem, byte[] password, byte word, byte count) {
        return switch (serialMode) {
            case BB -> Constants.getReadCommandBB(mem, password, word, count);
            case CF -> Constants.getReadCommand(mem, password, word, count);
            default -> new byte[1];
        };
    }

    public static byte[] getSetParameter(ESerialMode serialMode) {
        return switch (serialMode) {
            case BB -> Constants.getSelectParametersBB();
            case CF -> Constants.getSelectParametersCF();
            default -> new byte[1];
        };
    }

    public static byte[] getWriteCommand(ESerialMode serialMode, byte mem, byte[] password, byte word, byte count, byte[] data) {
        return switch (serialMode) {
            case BB -> Constants.getWriteCommandBB(mem, password, word, count, data);
            case CF -> Constants.getWriteCommand(mem, password, word, count, data);
            default -> new byte[1];
        };
    }

}
