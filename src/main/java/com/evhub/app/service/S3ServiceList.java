package com.evhub.app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.evhub.app.config.S3Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;


@Service
public class S3ServiceList {

    @Autowired
    private AmazonS3 s3client;

    @Autowired
    private S3Config s3Config;

    @Value("${bucket.name}")
    private String bucketNames;

    private static final Logger LOG = LogManager.getLogger(S3ServiceList.class);

    public String uploadBase64Image(byte[] image,String extension) throws Exception {
        try {
//        for (String imageRequest : image) {
            String bucketName = bucketNames;
//            byte[] imageBytes = Base64.getDecoder().decode(imageRequest);
            InputStream imageStream = new ByteArrayInputStream(image);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(image.length);
            metadata.setContentType("image/" + extension);
//            String fileName = UUID.randomUUID().toString() + ".png"; // Generate a unique file name
//            System.out.println(fileName);
            String fileName = UUID.randomUUID().toString() + "." + extension;

            String key = "images/" + fileName;

            PutObjectRequest request = new PutObjectRequest(bucketName, key, imageStream, metadata);

            s3client.putObject(request);
            //   vehicleRequest.setImage(s3client.getUrl(bucketName, key).toString());
            // System.out.println(s3client.getUrl(bucketName, key).toString());
            return s3client.getUrl(bucketName, key).toString();
        }
        catch (Exception e){
            LOG.error("Error while uploading image" + e.getMessage());
            e.printStackTrace();
            LOG.error(image + extension);
            throw new Exception("Exception Occur " + e.getMessage());





        }

    }

    public void deleteS3SingleObject(List<String> deleteObject) {
        if (!ObjectUtils.isEmpty(deleteObject)) {
            for (String image : deleteObject) {
                int startIndex = image.indexOf("com") + 3;
                String substring = image.substring(startIndex);
                DeleteObjectRequest dor = new DeleteObjectRequest(bucketNames, substring);
                s3client.deleteObject(dor);
            }
        }
    }
}


