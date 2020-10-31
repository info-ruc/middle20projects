package edu.ruc.util;

import edu.ruc.conf.Config;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class SendMail {

    @Autowired
    private Config conf;

    public String sendCode(String toMail) throws Exception {
        String code = generateVerifyCode();
        HtmlEmail email = new HtmlEmail();
        email.setHostName(conf.getMailServer());
        email.setSSLOnConnect(true);
        email.setCharset("utf-8");
        email.addTo(toMail);
        email.setFrom(conf.getMailUsername(), conf.getMailNickname());
        email.setAuthentication(conf.getMailUsername(), conf.getMailSk());
        email.setSubject("【验证码】乐享网验证码");
        email.setMsg("您的验证码是：" + code +",5分钟内有效。");
        email.send();
        return code;
    }

    private String generateVerifyCode(){
        String num = "0123456789";
        int codesLen = num.length();
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder verifyCode = new StringBuilder(4);
        for(int i = 0; i < 4; i++){
            verifyCode.append(num.charAt(rand.nextInt(codesLen - 1)));
        }
        return verifyCode.toString();
    }
}
