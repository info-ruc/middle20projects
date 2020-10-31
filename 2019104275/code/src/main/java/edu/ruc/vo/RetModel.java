package edu.ruc.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetModel<T> {

    private String requestId;
    private T      result;
    private Error  error;

    public RetModel() {

    }

    public RetModel(String requestId, T result, Error error) {
        this.requestId = requestId;
        this.result = result;
        this.error = error;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
