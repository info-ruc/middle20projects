package org.example.entity;

import java.io.Serializable;

public class MessageInfo implements Serializable {
    /**
     * 消息的唯一标识
     */
    private Integer status;
    /**
     * 消息的内容
     */
    private String body;
    /**
     * 消息的状态 1代表消费成功 0代表消费失败
     */

    public MessageInfo() {
    }

    public MessageInfo(Integer status, String body) {
        this.status = status;
        this.body = body;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
