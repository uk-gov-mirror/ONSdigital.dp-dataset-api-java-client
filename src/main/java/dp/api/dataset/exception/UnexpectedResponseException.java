package dp.api.dataset.exception;

public class UnexpectedResponseException extends DatasetAPIException {

    private final int responseCode;

    public UnexpectedResponseException(String message, int responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
