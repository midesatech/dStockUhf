package domain.usecase.tag;

import app.config.AppConfig;
import app.config.SerialConfig;
import domain.model.tag.*;
import infrastructure.logging.LogMessages;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

public class ReadTagUseCase extends Service<ReadWriteResult> {

    private static final Logger logger = LogManager.getLogger(ReadTagUseCase.class);
    private static final Logger errLogger = LogManager.getLogger("errLogger");
    private final OperationsUseCase operationsUseCase;
    private AppConfig appConfig;

    public ReadTagUseCase(OperationsUseCase operationsUseCase) {
        this.operationsUseCase = operationsUseCase;
    }

    @Override
    protected Task<ReadWriteResult> createTask() {
        return new Task<>() {
            @Override
            protected ReadWriteResult call() {
                logger.info(LogMessages.fromComponent("ReadTagUseCase", LogMessages.EPC_READ_STARTED));
                long startTime = System.currentTimeMillis();

                while (true) {
                    RxDto result = getEpc();

                    if (isValidRx(result)) {
                        return new ReadWriteResult(result, ErrorCode.OK, LogMessages.EPC_READ_OK, true);
                    }

                    if (System.currentTimeMillis() - startTime > appConfig.getSerialConfig().getTimeoutEnrolar()) {
                        logger.info("Pass {} seconds", appConfig.getSerialConfig().getTimeoutEnrolar());
                        return new ReadWriteResult(
                                result,
                                ErrorCode.TM,
                                LogMessages.EPC_READ_TIMEOUT,
                                true);
                    }

                    if (isCancelled()) {
                        logger.info(LogMessages.fromComponent("ReadTagUseCase", LogMessages.TASK_CANCELLED));
                        break;
                    }
                }
                return new ReadWriteResult(new RxDto(false), ErrorCode.TC, "", false);
            }
        };
    }

    public void setAppConfig(AppConfig appconfig) {
        this.appConfig = appconfig;
    }

    private RxDto findTag(SerialConfig serialConfig) {
        if (operationsUseCase.connect(serialConfig.getComPort(), serialConfig.getDb())) {
            return operationsUseCase.readBlock(ETagMem.EPC, "00000000", "0002", "06", serialConfig.getMode());
        }
        return null;
    }

    private boolean isValidRx(RxDto rxDto) {
        return Objects.nonNull(rxDto) && rxDto.getIsValid();
    }

    private RxDto writeEpc(RxDto result) {
        String newEpc = getDataToWrite(result);
        return writeEpc(newEpc);
    }

    private String getDataToWrite(RxDto data) {
        String epc = data.getHexEpc().replace(" ", "");
        if (epc.length() <= 8) {
            epc = "F8".concat(String.format("%06X", 0L));
        } else {
            epc = "F8".concat(epc.substring(2, 14));
            epc = epc.concat(String.format("%010d", 0L));
        }
        logger.info("New EPC {}", epc);
        return epc;
    }

    public RxDto getEpc() {
        return Optional.ofNullable(appConfig)
                .flatMap(config -> Optional.ofNullable(config.getSerialConfig()))
                .map(this::findTag).orElse(null);
    }

    private RxDto writeEpc(String data) {
        return Optional.ofNullable(appConfig)
                .flatMap(config -> Optional.ofNullable(config.getSerialConfig()))
                .map(config -> writeTag(config, data)).orElse(null);
    }

    private RxDto writeTag(SerialConfig serialConfig, String data) {
        if (operationsUseCase.connect(serialConfig.getComPort(), serialConfig.getDb())) {
            return operationsUseCase.writeBlock(ETagMem.EPC,
                    "00000000",
                    "0002",
                    String.format("%02d", data.length() / 4), data, serialConfig.getMode());
        }
        return null;
    }

    public ESerialMode getSerialMode() {
        if (Objects.nonNull(appConfig)) {
            return appConfig.getSerialConfig().getMode();
        }
        return ESerialMode.NO;
    }
}
