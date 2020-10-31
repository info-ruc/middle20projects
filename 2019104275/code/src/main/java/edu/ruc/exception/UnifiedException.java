package edu.ruc.exception;

import edu.ruc.util.Ret;
import edu.ruc.vo.RetModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

// 统一错误返回格式
@ControllerAdvice
public class UnifiedException {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public RetModel err(Exception e) {
        if (e instanceof HttpRequestMethodNotSupportedException) {
            return Ret.err(e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
        } else if (e instanceof NoHandlerFoundException || e instanceof NotFoundException) {
            return Ret.err(e.getMessage(), HttpStatus.NOT_FOUND);
        } else if (e instanceof InvalidParameterException || e instanceof MissingServletRequestParameterException || e instanceof MissingRequestHeaderException) {
            return Ret.err(e.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (e instanceof PermissionException) {
            return Ret.err(e.getMessage(), HttpStatus.FORBIDDEN);
        }  else if (e instanceof InternalErrorException) {
            return Ret.err(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return Ret.err(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
