package com.evhub.app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.evhub.app.config.S3Config;
import com.evhub.app.entities.Vehicle;
import com.evhub.app.request.ImageRequest;
import com.evhub.app.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class S3Service {
    @Value("${bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3client;

    @Autowired
    private S3Config s3Config;
    @Autowired
    private VehicleService vehicleService;

    @Value("${bucket.name}")
    private String bucketNames;

    public String uploadBase64Image(String imageRequest, Vehicle vehicleRequest) {
        try {
            String bucketName = bucketNames;

            byte[] imageBytes = Base64.getDecoder().decode(imageRequest);
            InputStream imageStream = new ByteArrayInputStream(imageBytes);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/" + vehicleService.extensionforVehicleImage(vehicleRequest));
            String fileName = UUID.randomUUID().toString() + "."
                    + vehicleService.extensionforVehicleImage(vehicleRequest); // Generate a unique file name

            String key = "images/" + fileName;

            PutObjectRequest request = new PutObjectRequest(bucketName, key, imageStream, metadata);

            s3client.putObject(request);
            vehicleRequest.setImage(s3client.getUrl(bucketName, key).toString());
            System.out.println(s3client.getUrl(bucketName, key).toString());
            return s3client.getUrl(bucketName, key).toString();
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_GATEWAY.value(), e.getMessage());
        }
    }
}
