package edu.ruc.controller;

import edu.ruc.service.MsgService;
import edu.ruc.util.Ret;
import edu.ruc.vo.Const;
import edu.ruc.vo.RetModel;
import edu.ruc.vo.msg.Msg;
import edu.ruc.vo.msg.MsgComment;
import edu.ruc.vo.msg.MsgRet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MsgController {

    @Autowired
    private MsgService msgService;

    // 创建
    @PostMapping("/msg/msg")
    public RetModel createMsg(@RequestHeader(Const.userId) String userId, Msg msg) throws Exception {

        String msgUid = msgService.addMsg(userId, msg);
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("msgUuid", msgUid);
        return Ret.ok(map);
    }

    // 删除
    @DeleteMapping("/msg/msg/{msgUuid}")
    public RetModel deleteMsg(@PathVariable String msgUuid, @RequestHeader(Const.userId) String userId) throws Exception {

        String msgUid = msgService.deleteMsg(userId, msgUuid);
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("msgUuid", msgUid);
        return Ret.ok(map);
    }

    // 查询单条详情
    @GetMapping("/msg/msg/{msgUuid}")
    public RetModel describeMsg(@PathVariable String msgUuid, HttpServletRequest request) throws Exception {

        String userUuid = request.getHeader(Const.userId);
        MsgRet msgRet = msgService.describeMsg(msgUuid, userUuid);
        Map<String, Object> map = new HashMap<>();
        map.put("msg", msgRet);
        return Ret.ok(map);
    }

    // 查询列表
    @GetMapping("/msg/msgs")
    public RetModel describeMsgs(String type, String describe, HttpServletRequest request) throws Exception {

        // type为user，查user名下，否则全部
        // msg支持模糊匹配
        String userUuid = request.getHeader(Const.userId);
        List<MsgRet> msgRets = msgService.describeMsgs(type, describe, userUuid);
        Map<String, Object> map = new HashMap<>();
        map.put("msgs", msgRets);
        return Ret.ok(map);
    }

    // 评论
    @PostMapping("/msg/comment")
    public RetModel commentMsg(@RequestHeader(Const.userId) String userId, @RequestBody MsgComment msgComment) throws Exception {

        String commentUid = msgService.addComment(userId, msgComment.getMsgUuid(), msgComment.getContent());
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("commentUid", commentUid);
        return Ret.ok(map);
    }

    // 点赞
    @GetMapping("/msg/like/{msgUuid}")
    public RetModel likeMsg(@RequestHeader(Const.userId) String userId, @PathVariable String msgUuid) throws Exception {

        String likeUid = msgService.likeMsg(userId, msgUuid);
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("likeUid", likeUid);
        return Ret.ok(map);
    }
}
