package domain.usecase.tag;

import app.config.AppConfig;
import app.config.SerialConfig;
import domain.model.tag.ESerialMode;
import domain.model.tag.RxDto;
import help.LogMessages;
import help.SerialConstants;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

public class ReaderUseCase extends Service<String> {
    private static final Logger logger = LogManager.getLogger(ReaderUseCase.class);
    private final OperationsUseCase operationsUseCase;
    private AppConfig appConfig;

    public ReaderUseCase(OperationsUseCase operationsUseCase) {
        this.operationsUseCase = operationsUseCase;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            @Override
            protected String call() {
                logger.info("ReaderUseCase: Starting task to find readers.");
                updateMessage("Detectando lector...");
                Optional<Map.Entry<String, RxDto>> comPortConnectedEntry = operationsUseCase.getPorts()
                        .stream()
                        .map(portName -> {
                            logger.info("ComPort {}", portName);
                            RxDto result = connect(portName);
                            return (Map.Entry<String, RxDto>) new AbstractMap.SimpleEntry<>(portName, result);
                        })
                        .filter(entry -> entry.getValue().getIsValid()) // Filter by valid RxDto
                        .findFirst(); // Take the first valid one
                if (comPortConnectedEntry.isPresent()) {
                    String foundPortName = comPortConnectedEntry.get().getKey(); // Get the portName
                    RxDto rxDto = comPortConnectedEntry.get().getValue(); // Get the RxDto

                    // 1. Call setSerialConfig with the found portName
                    setSerialConfig(rxDto.getMode(), foundPortName);
                    logger.info("ReaderUseCase: SerialConfig updated with port: {}", foundPortName);

                    // 2. Update message to include BOTH strVal and portName
                    String message = "¡Lector detectado en " + foundPortName + "! Detalles: " + rxDto.getStrVal();
                    updateMessage(message);
                    logger.info("ReaderUseCase: {}", message);

                    return foundPortName; // Still return the portName as the task's value
                } else {
                    updateMessage("¡Lector no fue detectado!");
                    logger.warn(LogMessages.fromComponent("ReaderUseCase", LogMessages.READER_NOT_DETECTED));
                    return "No reader detected";
                }

            }
        };
    }

    private RxDto connect(String portName) {
        long startTime = System.currentTimeMillis();
        if (!operationsUseCase.connect(portName, SerialConstants.DEFAULT_BAUD_RATE)) {
            return new RxDto(false);
        }
        return tryGetValidFirmware(startTime);
    }

    private RxDto tryGetValidFirmware(long startTime) {
        RxDto response = new RxDto(false);
        for (int x = 0; x < 5; x++) {
            response = operationsUseCase.getFirmware();
            if (response.getIsValid()) {
                return response;
            }

            if (hasTimedOut(startTime)) {
                logger.info("Pass 5 seconds");
                break;
            }
        }
        operationsUseCase.disconnect();
        return response;
    }

    private boolean hasTimedOut(long startTime) {
        return System.currentTimeMillis() - startTime > 5 * 800;
    }

    private void setSerialConfig(ESerialMode mode, String portName) {
        if (appConfig == null) {
            return;
        }

        SerialConfig current = appConfig.getSerialConfig();
        if (current == null) {
            return;
        }

        SerialConfig updatedConfig = new SerialConfig(
                portName,
                current.getTimeoutEnrolar(),
                current.getDb(),
                mode
        );

        appConfig.setSerialConfig(updatedConfig);
    }

    public void setAppConfig(AppConfig appconfig) {
        this.appConfig = appconfig;
    }
}
