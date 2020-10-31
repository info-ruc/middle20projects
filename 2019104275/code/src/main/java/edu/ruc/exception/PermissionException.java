package edu.ruc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.FORBIDDEN)
public class PermissionException extends Exception {

    public PermissionException(String msg) {
        super(msg);
    }
}
