package edu.ruc.controller;

import edu.ruc.exception.InvalidParameterException;
import edu.ruc.service.UserService;
import edu.ruc.util.Ret;
import edu.ruc.util.SendMail;
import edu.ruc.vo.Const;
import edu.ruc.vo.RetModel;
import edu.ruc.vo.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SendMail sendMail;

    @GetMapping("/user/describeUser")
    public RetModel DescribeUser(@RequestHeader(Const.userId) String userId) throws Exception {
        return Ret.ok(userService.getUser(userId));
    }

    @PostMapping("/user/register")
    public RetModel Register(@RequestBody User user, HttpServletRequest request) throws Exception {

        Map<String, Object> regMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        int addRes = userService.addUser(uuid, user.getUsername(), user.getPassword(), user.getPhone(), user.getGender(), user.getMail(), user.getCode(), request);
        if(addRes > 0) {
            regMap.put("success", true);
            regMap.put("userId", uuid);
        } else {
            regMap.put("success", false);
        }
        return Ret.ok(regMap);
    }

    @PostMapping("/user/login")
    public RetModel Login(@RequestBody User userReq) throws Exception {

        String mail = userReq.getMail();
        String password = userReq.getPassword();
        User user = userService.checkUser(mail, password);
        Map<String, Object> loginMap = new HashMap<>();
        if (user == null) {
            loginMap.put("success", false);
        } else {
            loginMap.put("success", true);
            loginMap.put("userId", user.getUuid());
        }
        return Ret.ok(loginMap);
    }

    @GetMapping("/user/sendCode")
    public RetModel sendCode(String toMail, HttpServletRequest request) throws Exception {
        if(toMail == null || toMail.length() == 0 || !toMail.contains("@")){
            throw new InvalidParameterException("the mail address is invalid");
        }
        String code = sendMail.sendCode(toMail);

        HttpSession session = request.getSession();
        session.setAttribute(toMail, code);
        session.setMaxInactiveInterval(300);

        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        return Ret.ok(map);
    }
}
