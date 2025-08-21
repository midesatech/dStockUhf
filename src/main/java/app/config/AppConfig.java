package app.config;

public class AppConfig {

    private SerialConfig serialConfig;
    private final String stage;
    private final String device;

    public AppConfig(SerialConfig serialConfig, String stage, String device) {
        this.serialConfig = serialConfig;
        this.stage = stage;
        this.device = device;
    }


    public SerialConfig getSerialConfig() {
        return serialConfig;
    }
    public void setSerialConfig(SerialConfig serialConfig) {
        this.serialConfig = serialConfig;
    }
    public String getStage() {
        return stage;
    }

    public String getDevice() {
        return device;
    }
}
