package edu.ruc.service;

import edu.ruc.dao.MsgDao;
import edu.ruc.dao.UserDao;
import edu.ruc.exception.InternalErrorException;
import edu.ruc.exception.InvalidParameterException;
import edu.ruc.exception.NotFoundException;
import edu.ruc.util.QcloudCos;
import edu.ruc.vo.msg.Msg;
import edu.ruc.vo.msg.MsgComment;
import edu.ruc.vo.msg.MsgLike;
import edu.ruc.vo.msg.MsgRet;
import edu.ruc.vo.user.User;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@MapperScan("edu.ruc.dao")
public class MsgServiceImpl implements MsgService {

    @Autowired
    private MsgDao msgDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private QcloudCos qcloudCos;

    @Override
    public List<MsgRet> describeMsgs(String type, String describe, String userUuid) throws Exception {

        List<Msg> msgs = null;
        if(type != null && type.equals("user")) {
            if(describe == null || describe.length() == 0){
                msgs = msgDao.getMsgByUserUuid(userUuid);
            } else {
                msgs = msgDao.getMsgByDescribeAndUserUuid("%"+describe+"%", userUuid);
            }
        } else {
            if(describe == null || describe.length() == 0){
                msgs = msgDao.getAllMsg();
            } else {
                msgs = msgDao.getMsgByDescribe("%"+describe+"%");
            }
        }

        List<MsgRet> msgRets = new ArrayList<>();
        for (Msg msg : msgs) {
            msgRets.add(buildMsgRet(msg, userUuid));
        }

        return msgRets;
    }

    @Override
    public MsgRet describeMsg(String msgUuid, String userUuid) throws Exception {

        Msg msg = msgDao.getMsgByUuid(msgUuid);

        if(msg == null){
            throw new NotFoundException("the msg is not exist");
        }
        MsgRet msgRet = buildMsgRet(msg, userUuid);

        List<MsgComment> msgComments = msgDao.getMsgComments(msgUuid);
        if(msgComments != null){
            for(MsgComment msgComment : msgComments){
                User user = userDao.selectUserById(msgComment.getUserUuid());
                if(user != null) {
                    msgComment.setUsername(user.getUsername());
                    msgComment.setUserImg(user.getImg());
                }
            }
        }

        msgRet.setComments(msgComments);
        return msgRet;
    }

    @Override
    public String addMsg(String userUuid, Msg msg) throws Exception {
        if(msg.getDescribe() == null || msg.getDescribe().length() == 0){
            throw new InvalidParameterException("describe can not empty");
        }

        Msg msgRes = new Msg();
        // 图片转存，目前是串行，可优化为并行
        if(msg.getBlFile()!=null){
            if(msg.getBlFile().length>9){
                throw new InvalidParameterException("the max image length is 9");
            }
            StringBuffer imgUrls = new StringBuffer();
            for(MultipartFile file : msg.getBlFile()) {
                String fileUrl = qcloudCos.sendObject(file);
                imgUrls.append(fileUrl);
                imgUrls.append("@$#");
            }
            msgRes.setImgUrl(imgUrls.toString());
        } else {
            msgRes.setImgUrl("");
        }

        String msgUid = UUID.randomUUID().toString();
        Date date = new Date();
        msgRes.setUuid(msgUid);
        msgRes.setUserUuid(userUuid);
        msgRes.setCreateTime(date);
        msgRes.setUpdateTime(date);
        msgRes.setDescribe(msg.getDescribe());
        int insertRes = msgDao.insertMsg(msgRes);
        if(insertRes > 0){
            return msgUid;
        } else {
            throw new InternalErrorException("insert msg to db error");
        }
    }

    @Override
    public String deleteMsg(String userUuid, String msgUuid) throws Exception {

        Msg msg = msgDao.getMsgByUserUuidAndUuid(msgUuid, userUuid);
        if(msg == null){
            throw new NotFoundException("the msg is not exist");
        }

        if(msgDao.deleteMsg(msgUuid) > 0){
            return msgUuid;
        }
        return null;
    }

    @Override
    public String likeMsg(String userUuid, String msgUuid) throws Exception {

        Msg msg = msgDao.getMsg(msgUuid);
        if(msg == null){
            throw new NotFoundException("the msg is not exist");
        }

        MsgLike msgLikeQ = new MsgLike();
        msgLikeQ.setUserUuid(userUuid);
        msgLikeQ.setMsgUuid(msgUuid);
        MsgLike msgLike = msgDao.getMsgLike(msgLikeQ);
        if(msgLike != null) {
            throw new InvalidParameterException("the user has like this msg");
        }

        MsgLike msgLikeIn = new MsgLike();
        String uuid = UUID.randomUUID().toString();
        msgLikeIn.setUuid(uuid);
        msgLikeIn.setMsgUuid(msgUuid);
        msgLikeIn.setUserUuid(userUuid);
        Date date = new Date();
        msgLikeIn.setCreateTime(date);
        msgLikeIn.setUpdateTime(date);
        if(msgDao.insertLike(msgLikeIn) > 0){
            return uuid;
        } else {
            throw new InternalErrorException("insert msg like error");
        }
    }

    @Override
    public String addComment(String userUuid, String msgUuid, String content) throws Exception {

        if(msgUuid == null || msgUuid.length() == 0){
            throw new InvalidParameterException("msgUuid can not empty");
        }

        if(content == null || content.length() == 0){
            throw new InvalidParameterException("content can not empty");
        }

        Msg msg = msgDao.getMsg(msgUuid);
        if(msg == null){
            throw new NotFoundException("the msg is not exist");
        }

        String uuid = UUID.randomUUID().toString();
        Date date = new Date();
        MsgComment msgComment = new MsgComment();
        msgComment.setUuid(uuid);
        msgComment.setUserUuid(userUuid);
        msgComment.setMsgUuid(msgUuid);
        msgComment.setContent(content);
        msgComment.setCreateTime(date);
        msgComment.setUpdateTime(date);
        if(msgDao.insertComment(msgComment) > 0){
            return uuid;
        } else {
            throw new InternalErrorException("insert msg comment error");
        }
    }

    private MsgRet buildMsgRet(Msg msg, String userUuid) {
        MsgRet msgRet = new MsgRet();
        msgRet.setUuid(msg.getUuid());
        msgRet.setDescribe(msg.getDescribe());
        msgRet.setCreateTime(msg.getCreateTime());
        String imgs[] = msg.getImgUrl().split("@\\$#");
        if(imgs != null && imgs.length>0 && imgs[0].equals("")){
            imgs = new String[]{};
        }
        msgRet.setImg(imgs);

        User user = userDao.selectUserById(msg.getUserUuid());
        msgRet.setUsername(user.getUsername());
        msgRet.setUserUuid(user.getUuid());
        msgRet.setUserImg(user.getImg());

        if(userUuid != null && userUuid.length() != 0) {
            MsgLike msgLikeQ = new MsgLike();
            msgLikeQ.setUserUuid(userUuid);
            msgLikeQ.setMsgUuid(msg.getUuid());
            MsgLike msgLike = msgDao.getMsgLike(msgLikeQ);
            if(msgLike != null) {
                msgRet.setLike(true);
            }
        }

        List<MsgComment> msgComments = msgDao.getMsgComments(msg.getUuid());

        if(msgComments != null){
            msgRet.setCommentsNum(msgComments.size());
        }


        List<MsgLike> msgLikes = msgDao.getMsgLikes(msg.getUuid());
        if(msgLikes != null){
            msgRet.setLikeNum(msgLikes.size());
        }

        return msgRet;
    }
}
