package edu.ruc.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import edu.ruc.conf.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;

// 腾讯云对象存储工具类
@Component
public class QcloudCos {

    @Autowired
    private Config conf;

    public String sendObject(MultipartFile blFile) throws Exception {

        COSCredentials cred = new BasicCOSCredentials(conf.getQcloudSecretId(), conf.getQcloudSecretKey());
        Region region = new Region(conf.getQcloudRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        COSClient cosClient = new COSClient(cred, clientConfig);
        File localFile = File.createTempFile("temp",null);;
        blFile.transferTo(localFile);
        String bucketName = conf.getQcloudBucketName();
        String originFileName = blFile.getOriginalFilename();
        String key = UUID.randomUUID().toString() + originFileName.substring(originFileName.lastIndexOf("."));
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        cosClient.shutdown();
        return conf.getQcloudurl()+putObjectRequest.getKey();
    }
}
