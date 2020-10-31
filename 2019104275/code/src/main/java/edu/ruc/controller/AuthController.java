package edu.ruc.controller;

import edu.ruc.exception.InvalidParameterException;
import edu.ruc.exception.PermissionException;
import edu.ruc.vo.RetModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @RequestMapping("/lackHeader")
    public RetModel lackHeader() throws Exception {
        throw new InvalidParameterException("lack header of Authorization");
    }

    @RequestMapping("/errToken")
    public RetModel errToken() throws Exception {
        throw new PermissionException("invalid token");
    }
}
