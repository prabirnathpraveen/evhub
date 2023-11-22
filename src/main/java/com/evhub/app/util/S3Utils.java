package com.evhub.app.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.evhub.app.exception.ValidationException;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class S3Utils {

    private final Logger logger = LoggerFactory.getLogger(S3Utils.class);

    public AmazonS3 getClient(String region) {
        try {
            log.info("region : " + region);
            if (region == null)
                region = "eu-west-1";

            log.info("region : " + region);
            // BasicSessionCredentials basicSessionCredentials = new
            // BasicSessionCredentials(accessKey, secretKey, null);
            return AmazonS3ClientBuilder.standard()
                    // .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(region) // Replace with your desired region
                    .build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Error while creating s3 client");
        }
    }

    public void uploadInputStream(InputStream inputStream, String objectKey, String bucket, AmazonS3 client) {
        try {
            logger.info("Inside uploadInputStream of S3Utils");
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(inputStream.available());
            logger.info("before putting the object to the new s3 method");
            client.putObject(bucket, objectKey, inputStream, metadata);
            logger.info("Exiting uploadInputStream of S3Utils");
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(),
                    "Error while uploadInputStream into S3Utils :: " + e);
        }
    }

    public void uploadMultipartFile(InputStream inputStream, String objectKey, String bucket, AmazonS3 client,
            long size) {
        try {
            logger.info("Inside uploadInputStream of S3Utils");
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(size);
            client.putObject(bucket, objectKey, inputStream, metadata);
            logger.info("Exiting uploadInputStream of S3Utils");
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(),
                    "Error while uploadInputStream into S3Utils :: " + e);
        }
    }

    public Set<String> createS3SignedURL(AmazonS3 client, String filepath, String bucket, Long urlExpiry) {
        urlExpiry = urlExpiry == null ? System.currentTimeMillis() + 600_000L : urlExpiry;
        Instant instant = Instant.ofEpochMilli(urlExpiry);
        Date date = Date.from(instant);
        URL url = client.generatePresignedUrl(bucket, filepath, date, HttpMethod.GET);
        return Collections.singleton(url.toString());
    }

    public InputStream downloadFile(String objectKey, String bucket, AmazonS3 client) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, objectKey);
        S3Object s3Object = client.getObject(getObjectRequest);
        return s3Object.getObjectContent();
    }

    // tested
    public void ensureBucketExist(String bucket, AmazonS3 client) {
        if (client.listBuckets().stream().noneMatch(b -> b.getName().equals(bucket))) {
            client.createBucket(bucket);
        }
    }

    public List<String> getItemsName(String bucket, AmazonS3 client, String path) {

        List<String> itemNames = new ArrayList<>();
        String prefix = path.endsWith("/") ? path : path + "/";

        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucket)
                .withPrefix(prefix)
                .withDelimiter("/");

        ListObjectsV2Result response = client.listObjectsV2(request);

        for (S3ObjectSummary object : response.getObjectSummaries()) {
            String itemPath = object.getKey();
            itemNames.add(itemPath);
        }

        itemNames.addAll(response.getCommonPrefixes());
        return itemNames;
    }

    public void deleteObject(String filePath, AmazonS3 client, String bucket) {

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(filePath);

        ListObjectsV2Result listObjectsV2Result = client.listObjectsV2(request);
        for (S3ObjectSummary item : listObjectsV2Result.getObjectSummaries()) {
            client.deleteObjects(new DeleteObjectsRequest(bucket).withKeys(item.getKey()));
        }
    }

    public void migrateDirectory(String sourceBucket, String prefix, String targetBucket, AmazonS3 client) {
        try {
            ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(sourceBucket).withPrefix(prefix);
            ListObjectsV2Result listObjectsV2Result = client.listObjectsV2(request);
            for (S3ObjectSummary item : listObjectsV2Result.getObjectSummaries()) {
                client.copyObject(sourceBucket, item.getKey(), targetBucket, item.getKey());
                deleteObject(item.getKey(), client, sourceBucket);
            }
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(),
                    "Error while migrating directory in S3:: " + e);
        }
    }
}