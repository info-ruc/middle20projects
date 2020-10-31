package edu.ruc.dao;

import edu.ruc.vo.user.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {

    @Select("select * from user where mail = #{mail} and password = #{password}")
    public User selectUser(@Param("mail") String mail, @Param("password") String password);

    @Select("select * from user where mail = #{mail}")
    public User selectUserByMail(@Param("mail") String mail);

    @Select("select * from user where uuid = #{id}")
    public User selectUserById(@Param("id") String id);

    @Insert("insert into user(uuid,username,password,phone,gender,age,mail,create_time,update_time) VALUES(#{uuid}, #{username}, #{password}, #{phone}, #{gender}, #{age}, #{mail}, #{createTime}, #{updateTime})")
    public int insertUser(User user);
}
