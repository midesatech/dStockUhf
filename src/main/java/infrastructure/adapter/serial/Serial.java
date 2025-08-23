package infrastructure.adapter.serial;

import java.util.Set;

public interface Serial {
    void write(byte[] data);
    byte[] read();
    byte[] read(int timeout);
    boolean configure(String portName, int bauds);
    boolean configure();
    boolean init();
    void close();
    Set<String> getPorts();
    boolean isOpen();
}
