package edu.ruc.service;

import edu.ruc.vo.user.User;

import javax.servlet.http.HttpServletRequest;

public interface UserService {
    public User checkUser(String mail, String password) throws Exception;
    public int addUser(String uuid, String username, String password, String phone, String gender, String mail, String code, HttpServletRequest request) throws Exception;
    public User getUser(String id) throws Exception;
}
