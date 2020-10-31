package edu.ruc.aop;

import edu.ruc.util.Ret;
import edu.ruc.vo.Const;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Before("execution(public * edu.ruc.controller.*.*(..))")
    public void before() throws Exception {
        HttpServletRequest request = Ret.getRequest();
        logger.info("[request]requestId:{};token:{};userId:{};url:{}", request.getHeader(Const.requestId),
                request.getHeader(Const.token), request.getHeader(Const.userId), request.getRequestURL().toString());
    }
}
