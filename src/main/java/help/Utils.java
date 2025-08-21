package help;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Usuario
 */
public class Utils {

    /**
     * Convierte la fecha en HEXSTRING de 4 bytes
     *
     * @param d Fecha
     * @return Cadena HEXADECIMAL de 8 caracteres
     */
    public static String _ztx(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        int ss = calendar.get(Calendar.SECOND);
        int mm = calendar.get(Calendar.MINUTE);
        int hh = calendar.get(Calendar.HOUR_OF_DAY);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        int me = calendar.get(Calendar.MONTH) + 1;
        int yy = calendar.get(Calendar.YEAR) - 0x7d0;

        if (yy < 0) {
            yy = 1;
        }

        String _a = "";

        _a = String.format("%" + 6 + "s", Integer.toBinaryString(yy)).replace(' ', '0');
        _a += String.format("%" + 4 + "s", Integer.toBinaryString(me)).replace(' ', '0');
        _a += String.format("%" + 5 + "s", Integer.toBinaryString(dd)).replace(' ', '0');
        _a += String.format("%" + 5 + "s", Integer.toBinaryString(hh)).replace(' ', '0');
        _a += String.format("%" + 6 + "s", Integer.toBinaryString(mm)).replace(' ', '0');
        _a += String.format("%" + 6 + "s", Integer.toBinaryString(ss)).replace(' ', '0');

        int decimal = Integer.parseInt(_a, 2);
        _a = String.format("%08X", decimal);
        return _a;
    }

    /**
     * Convierte una cadena HEXSTRING de 4 bytes a fecha
     *
     * @param ax Cadena HEXADECIMAL de 8 caracteres
     * @return Fecha
     */
    public static Date _xtz(String ax) {
        Calendar calendar = Calendar.getInstance();
        int ult = Integer.parseInt(ax, 16);
        String tax = String.format("%" + 32 + "s", Integer.toBinaryString(ult)).replace(' ', '0');

        int ss = Integer.parseInt(tax.substring(26, 26 + 6), 2);
        int mm = Integer.parseInt(tax.substring(20, 20 + 6), 2);
        int hh = Integer.parseInt(tax.substring(15, 15 + 5), 2);
        int dd = Integer.parseInt(tax.substring(10, 10 + 5), 2);
        int me = Integer.parseInt(tax.substring(6, 6 + 4), 2);
        int yy = 0x7d0 + Integer.parseInt(tax.substring(0, 6), 2);

        try {
            calendar.set(yy, me - 1, dd, hh, mm, ss);
        } catch (Exception ex) {
            calendar.set(99, 01, 01, 01, 01, 01);
        }
        return calendar.getTime();
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(" ", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[(i / 2)] = ((byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bArray) {
        StringBuffer buffer = new StringBuffer();

        byte[] arrayOfByte = bArray;
        int j = bArray.length;
        for (int i = 0; i < j; i++) {
            byte b = arrayOfByte[i];
            buffer.append(String.format("%02X",
                    new Object[]{Byte.valueOf(b)}));
        }
        return buffer.toString().toUpperCase();
    }

    public static byte XOR(byte[] valor) {
        byte ret = 0x00;

        for (int z = 0; z < valor.length - 2; z++) {
            //ret = (byte) (ret ^ valor[z]);
            if (ret == 0x00) {
                ret = (byte) (valor[z] ^ valor[z + 1]);
            } else {
                ret = (byte) (ret ^ valor[z + 1]);
            }
        }
        return ret;
    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static String hexToAscii(byte[] cadena) {
        String hexStr = byteArrayToHexString(cadena);
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static Architecture getArchitecture() {
        return Architecture.valueOf(System.getProperty("os.arch").toUpperCase());
    }
}
