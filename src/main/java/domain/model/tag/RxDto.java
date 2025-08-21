package domain.model.tag;

import java.util.HexFormat;

public class RxDto {

    private boolean isValid;
    private byte[] bytesVal;
    private String strVal;
    private byte[] epc;
    private byte[] data;
    private byte[] crc;
    private byte[] pc;
    private static final String HEX_FMT = "(.{2})";
    private ESerialMode mode = ESerialMode.NO;

    public RxDto(boolean isValid, byte[] bytesVal, String strVal) {
        this.isValid = isValid;
        this.bytesVal = bytesVal;
        this.strVal = strVal;
        this.epc = null;
        this.data = null;
    }

    public RxDto(boolean isValid) {
        new RxDto(isValid, null, null);
    }

    public RxDto(boolean isValid, byte[] bytesVal, String strVal, byte[] epc, byte[] data, byte[] crc, byte[] pc) {
        this.isValid = isValid;
        this.bytesVal = bytesVal;
        this.strVal = strVal;
        this.epc = epc;
        this.data = data;
        this.crc = crc;
        this.pc = pc;
    }

    public boolean getIsValid() {
        return this.isValid;
    }

    public byte[] getBytesVal() {
        return this.bytesVal;
    }

    public String getStrVal() {
        return this.strVal;
    }

    public byte[] getEpc() { return this.epc; }
    public byte[] getData() { return this.data; }

    public String getHexData() {
        return HexFormat.of().formatHex(data).replaceAll(HEX_FMT, "$1 ").trim().toUpperCase();
    }

    public String getHexEpc() {
        return HexFormat.of().formatHex(epc).replaceAll(HEX_FMT, "$1 ").trim().toUpperCase();
    }

    public byte[] getCrc() {
        return this.crc;
    }

    public String getHexCrc() {
        return HexFormat.of().formatHex(crc).replaceAll(HEX_FMT, "$1 ").trim().toUpperCase();
    }

    public byte[] getPc() {
        return this.pc;
    }

    public String getHexPc() {
        return HexFormat.of().formatHex(pc).replaceAll(HEX_FMT, "$1 ").trim().toUpperCase();
    }

    public ESerialMode getMode() {
       return this.mode;
    }
    public void setMode(ESerialMode mode) {
        this.mode = mode;
    }
}
