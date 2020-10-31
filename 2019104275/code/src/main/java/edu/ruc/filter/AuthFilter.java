package edu.ruc.filter;

import edu.ruc.vo.Const;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

// 以token进行简单的模拟网关认证
@WebFilter(filterName = "authFilter", urlPatterns = "/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String token = req.getHeader(Const.token);
        if(token == null || token.length() == 0) {
            servletRequest.getRequestDispatcher("/lackHeader").forward(servletRequest, servletResponse);
        } else {
            if(token.equals(Const.accessKey)){
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                servletRequest.getRequestDispatcher("/errToken").forward(servletRequest, servletResponse);
            }
        }
    }

    @Override
    public void destroy() {

    }
}
