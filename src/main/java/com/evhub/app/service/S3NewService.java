package com.evhub.app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.evhub.app.util.S3Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;



@Component
@Slf4j
public class S3NewService {

    @Autowired
    S3Utils s3Utils;

    @Value("${bucket.name}")
    private String bucketNames;


    @Value("${region.name}")
    private String region;

//    @Value("${s3.accessKey}")
//    private  String accessKey;
//
//    @Value("${s3.secretKey}")
//    private  String secretKey;

    @Autowired
    private AmazonS3 s3client;
    public static final Logger LOG = LogManager.getLogger(S3NewService.class);

    public String uploadImage(byte[] image,String extension){
        String out;
        try{
            String bucketName=bucketNames;
        LOG.info("Inside upload image method aws new Service");
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(image.length);
        metadata.setContentType("image/" + extension);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String key = "images/" + fileName;
        LOG.info("Before uploading image to aws new Service");
       //  s3Utils.uploadInputStream(new ByteArrayInputStream(image),key,bucketName,s3Utils.getClient(region));
            s3Utils.uploadMultipartFile(new ByteArrayInputStream(image),key,bucketName,s3Utils.getClient(region),image.length );
        LOG.info("After uploading image to the service s3 new");
         out= s3Utils.getClient(region).getUrl(bucketName,key).toString();
    }
        catch (Exception e){
            log.error("Exception occurred while uploading", e);
            e.printStackTrace();
            out=e.getMessage();
            throw new RuntimeException(e.getMessage());
        }
         return out;
    }

    public void deleteNewS3SingleObject(List<String> deleteObject){
        if (!ObjectUtils.isEmpty(deleteObject)) {
            for (String image : deleteObject) {
                int startIndex = image.indexOf("com") + 3;
                String substring = image.substring(startIndex);
                DeleteObjectRequest dor = new DeleteObjectRequest(bucketNames, substring);
                s3Utils.getClient(region).deleteObject(dor);
            }
        }
    }
}
