package edu.ruc.dao;

import edu.ruc.vo.msg.Msg;
import edu.ruc.vo.msg.MsgComment;
import edu.ruc.vo.msg.MsgLike;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MsgDao {

    @Insert("insert into msg(uuid,user_uuid,img_url,`describe`,create_time,update_time) VALUES(#{uuid}, #{userUuid}, #{imgUrl}, #{describe}, #{createTime}, #{updateTime})")
    public int insertMsg(Msg msg);

    @Insert("insert into `like`(uuid,user_uuid,msg_uuid,create_time,update_time) values(#{uuid}, #{userUuid}, #{msgUuid}, #{createTime}, #{updateTime})")
    public int insertLike(MsgLike msgLike);

    @Select("select * from `like` where user_uuid = #{userUuid} and msg_uuid = #{msgUuid} and deleted = 0")
    public MsgLike getMsgLike(MsgLike msgLike);

    @Select("select * from msg where uuid = #{msgUuid} and deleted = 0")
    public Msg getMsg(String msgUuid);

    @Insert("insert into `comment`(uuid,user_uuid,msg_uuid,content,create_time,update_time) values(#{uuid}, #{userUuid}, #{msgUuid}, #{content}, #{createTime}, #{updateTime})")
    public int insertComment(MsgComment msgComment);

    // 查msg
    // 没有实现分页或滚动加载，可优化
    @Select("select * from msg where deleted = 0 order by update_time desc")
    @Results({
            @Result(property="userUuid",column="user_uuid"),
            @Result(property="imgUrl",column="img_url"),
            @Result(property="createTime",column="create_time")
    })
    public List<Msg> getAllMsg();

    @Select("select * from msg where user_uuid = #{userUuid} and deleted = 0 order by update_time desc")
    @Results({
            @Result(property="userUuid",column="user_uuid"),
            @Result(property="imgUrl",column="img_url"),
            @Result(property="createTime",column="create_time")
    })
    public List<Msg> getMsgByUserUuid(String userUuid);

    @Select("select * from msg where uuid = #{msgUuid} and deleted = 0 order by update_time desc")
    @Results({
            @Result(property="userUuid",column="user_uuid"),
            @Result(property="imgUrl",column="img_url"),
            @Result(property="createTime",column="create_time")
    })
    public Msg getMsgByUuid(String msgUuid);

    @Select("select * from msg where `describe` like #{describe} and deleted = 0 order by update_time desc")
    @Results({
            @Result(property="userUuid",column="user_uuid"),
            @Result(property="imgUrl",column="img_url"),
            @Result(property="createTime",column="create_time")
    })
    public List<Msg> getMsgByDescribe(String describe);

    @Select("select * from msg where `describe` like #{describe} and user_uuid = #{userUuid} and deleted = 0 order by update_time desc")
    @Results({
            @Result(property="userUuid",column="user_uuid"),
            @Result(property="imgUrl",column="img_url"),
            @Result(property="createTime",column="create_time")
    })
    public List<Msg> getMsgByDescribeAndUserUuid(@Param("describe") String describe, @Param("userUuid") String userUuid);

    @Select("select * from comment where msg_uuid = #{msgUuid} and deleted = 0 order by update_time desc")
    @Results({
            @Result(property="userUuid",column="user_uuid"),
            @Result(property="msgUuid",column="msg_uuid"),
            @Result(property="createTime",column="create_time")
    })
    public List<MsgComment> getMsgComments(String msgUuid);

    @Select("select * from `like` where msg_uuid = #{msgUuid} and deleted = 0 order by update_time desc")
    @Results({
            @Result(property="userUuid",column="user_uuid"),
            @Result(property="msgUuid",column="msg_uuid"),
            @Result(property="createTime",column="create_time")
    })
    public List<MsgLike> getMsgLikes(String msgUuid);

    @Update("update msg set deleted = 1 where uuid = #{msgUuid} and deleted = 0")
    public int deleteMsg(String msgUuid);

    @Select("select * from msg where uuid = #{msgUuid} and user_uuid = #{userUuid} and deleted = 0")
    public Msg getMsgByUserUuidAndUuid(@Param("msgUuid") String msgUuid, @Param("userUuid") String userUuid);
}
