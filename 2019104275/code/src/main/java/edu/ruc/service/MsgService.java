package edu.ruc.service;

import edu.ruc.vo.msg.Msg;
import edu.ruc.vo.msg.MsgRet;
import java.util.List;

public interface MsgService {

    public List<MsgRet> describeMsgs(String type, String describe, String userUuid) throws Exception;
    public MsgRet describeMsg(String msgUuid, String userUuid) throws Exception;
    public String addMsg(String userUuid, Msg msg) throws Exception;
    public String deleteMsg(String userUuid, String msgUuid) throws Exception;
    public String likeMsg(String userUuid, String msgUuid) throws Exception;
    public String addComment(String userUuid, String msgUuid, String content) throws Exception;
}
