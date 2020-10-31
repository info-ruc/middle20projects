package edu.ruc.util;

import edu.ruc.vo.Const;
import edu.ruc.vo.Error;
import edu.ruc.vo.RetModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class Ret {

    public static RetModel ok(Object object) {
        RetModel ret = new RetModel();
        ret.setResult(object);
        String requestId = getRequest().getHeader(Const.requestId);
        if(requestId == null || requestId.length() == 0) {
            requestId = UUID.randomUUID().toString();
        }
        ret.setRequestId(requestId);
        return ret;
    }

    public static RetModel err(String message, HttpStatus httpMsg) {
        RetModel ret = new RetModel();
        Error err = new Error(message, httpMsg);
        err.toString();
        ret.setError(err);
        String requestId = getRequest().getHeader(Const.requestId);
        if(requestId == null || requestId.length() == 0) {
            requestId = UUID.randomUUID().toString();
        }
        ret.setRequestId(requestId);
        getResponse().setStatus(httpMsg.value());
        return ret;
    }

    public static HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }

    public static HttpServletResponse getResponse() {
        HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
        return response;
    }
}
