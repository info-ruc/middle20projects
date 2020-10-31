package edu.ruc.service;

import edu.ruc.dao.UserDao;
import edu.ruc.exception.InvalidParameterException;
import edu.ruc.exception.NotFoundException;
import edu.ruc.vo.user.User;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Service
@MapperScan("edu.ruc.dao")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public User checkUser(String mail, String password) throws Exception {
        if(mail == null || mail.length() == 0){
            throw new InvalidParameterException("mail can not empty");
        }

        if(password == null || password.length() == 0){
            throw new InvalidParameterException("password can not empty");
        }

        User user = userDao.selectUser(mail, password);
        return user;
    }

    @Override
    public int addUser(String uuid, String username, String password, String phone, String gender, String mail, String code, HttpServletRequest request) throws Exception {
        User user = new User();
        if(mail == null || mail.length() == 0){
            throw new InvalidParameterException("mail can not empty");
        }

        if(code == null || code.length() == 0){
            throw new InvalidParameterException("code can not empty");
        }

        HttpSession session = request.getSession();
        String codeReal =  (String) session.getAttribute(mail);
        if(!code.equals(codeReal)){
            throw new InvalidParameterException("the code is invalid");
        }

        if(password == null || password.length() == 0){
            throw new InvalidParameterException("password can not empty");
        }
        User usrTmp = userDao.selectUserByMail(mail);
        if(usrTmp != null){
            throw new InvalidParameterException("the mail has register");
        }
        if(username == null || username.length() == 0) {
            user.setUsername(mail);
        } else {
            user.setUsername(username);
        }
        if(gender == null || gender.length() == 0) {
            user.setGender("男");
        } else {
            user.setGender(gender);
        }
        if(phone == null || phone.length() == 0) {
            user.setPhone("00");
        } else {
            user.setPhone(phone);
        }

        user.setUuid(uuid);
        user.setMail(mail);
        user.setAge(0);
        user.setPassword(password);
        Date date = new Date();
        user.setCreateTime(date);
        user.setUpdateTime(date);
        return userDao.insertUser(user);
    }

    @Override
    public User getUser(String id) throws Exception {
        User user = userDao.selectUserById(id);
        if(user == null) {
            throw new NotFoundException("this user" + id + "is not exist");
        }
        user.setPassword(null);
        return user;
    }
}
