package domain.model.tag;

public class ReadWriteResult {

    private RxDto data;
    private ErrorCode errorCode;
    private String message;
    private boolean showMessage;

    public ReadWriteResult(RxDto data, ErrorCode errorCode, String message, boolean showMessage) {
        this.data = data;
        this.errorCode = errorCode;
        this.message = message;
        this.showMessage = showMessage;
    }


    public RxDto getData() {
        return data;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isShowMessage() {
        return showMessage;
    }
}
