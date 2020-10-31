package edu.ruc.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {

    private int code;
    private String massage;
    private String status;

    public Error(String message, HttpStatus httpMsg) {
        this.code = httpMsg.value();
        this.status = httpMsg.getReasonPhrase();
        this.massage = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code=" + code +
                ", massage='" + massage + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
