package edu.ruc.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = {"classpath:config.properties"})
public class Config {

    @Value("${qcloud.secretId}")
    private String qcloudSecretId;
    @Value("${qcloud.secretKey}")
    private String qcloudSecretKey;
    @Value("${qcloud.bucketName}")
    private String qcloudBucketName;

    @Value("${qcloud.region}")
    private String qcloudRegion;

    @Value("${qcloud.url}")
    private String qcloudurl;

    @Value("${mail.sk}")
    private String mailSk;

    @Value("${mail.server}")
    private String mailServer;

    @Value("${mail.username}")
    private String mailUsername;

    @Value("${mail.nickname}")
    private String mailNickname;

    public String getQcloudSecretId() {
        return qcloudSecretId;
    }

    public void setQcloudSecretId(String qcloudSecretId) {
        this.qcloudSecretId = qcloudSecretId;
    }

    public String getQcloudSecretKey() {
        return qcloudSecretKey;
    }

    public void setQcloudSecretKey(String qcloudSecretKey) {
        this.qcloudSecretKey = qcloudSecretKey;
    }

    public String getQcloudBucketName() {
        return qcloudBucketName;
    }

    public void setQcloudBucketName(String qcloudBucketName) {
        this.qcloudBucketName = qcloudBucketName;
    }

    public String getQcloudRegion() {
        return qcloudRegion;
    }

    public void setQcloudRegion(String qcloudRegion) {
        this.qcloudRegion = qcloudRegion;
    }

    public String getQcloudurl() {
        return qcloudurl;
    }

    public void setQcloudurl(String qcloudurl) {
        this.qcloudurl = qcloudurl;
    }

    public String getMailSk() {
        return mailSk;
    }

    public void setMailSk(String mailSk) {
        this.mailSk = mailSk;
    }

    public String getMailServer() {
        return mailServer;
    }

    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }

    public String getMailUsername() {
        return mailUsername;
    }

    public void setMailUsername(String mailUsername) {
        this.mailUsername = mailUsername;
    }

    public String getMailNickname() {
        return mailNickname;
    }

    public void setMailNickname(String mailNickname) {
        this.mailNickname = mailNickname;
    }
}
