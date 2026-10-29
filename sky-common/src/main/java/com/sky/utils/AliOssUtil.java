package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {
        OSS ossClient = null;
        try {
            // 创建OSSClient实例并上传文件
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            log.error("OSS文件上传失败，objectName={}", objectName, e);
            throw new BaseException(MessageConstant.UPLOAD_FAILED, e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 文件访问路径规则 https://BucketName.Endpoint/ObjectName
        String filePath = "https://" + bucketName + "." + endpoint + "/" + objectName;
        log.info("文件上传到:{}", filePath);
        return filePath;
    }
}