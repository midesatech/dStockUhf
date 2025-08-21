package app.config;

import domain.model.tag.ESerialMode;

public class SerialConfig {
    private String comPort;
    private int timeoutEnrolar;
    private int db;
    private ESerialMode mode;

    public SerialConfig(String comPort, int timeoutEnrolar, int db, ESerialMode mode) {
        this.comPort = comPort;
        this.timeoutEnrolar = timeoutEnrolar;
        this.db = db;
        this.mode = mode;
    }

    public String getComPort() {
        return comPort;
    }

    public int getTimeoutEnrolar() {
        return timeoutEnrolar;
    }

    public int getDb() {
        return db;
    }

    public ESerialMode getMode() {
        return mode;
    }
}
