package com.evhub.app.util;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageService {

    public byte[] compressImage(BufferedImage image, String extension, Double sizeInKB) throws IOException {
        double compressionQuality = 0.0;
        if (sizeInKB > 1024.0f) {
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImage);
//            BufferedImage image = ImageIO.read(inputStream);
            compressionQuality = getCompression(image.getWidth(), sizeInKB) / sizeInKB;
            int compressedWidth = (int) (image.getWidth() * compressionQuality);
            int compressedHeight = (int) (compressedWidth / ((double)image.getWidth() / image.getHeight()));
             image= Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH ,compressedWidth, compressedHeight);

        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageIO.write(image, extension, os);
        byte[] output = os.toByteArray();
        os.close();
        return output;
    }

    public  int getOrientation(byte[] originalImage,String extention) throws IOException, ImageProcessingException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImage);
        Metadata metadata;
       try {
           metadata = ImageMetadataReader.readMetadata(inputStream, originalImage.length, FileType.Jpeg);
       } catch (Exception ex) {
           try {
               metadata = ImageMetadataReader.readMetadata(inputStream, originalImage.length, FileType.Png);
           } catch (Exception exception){
               return 1;
           }
       }

        ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        int orientation = 1;
        try {
            orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        } catch (Exception ex) {
            return 1;
        }
        return orientation;
    }

    public  BufferedImage getRotatedImage(byte[] imageByteArray, int orientation) throws IOException {
        AffineTransform affineTransform = new AffineTransform();
        InputStream imageInputStream = new ByteArrayInputStream(imageByteArray);
        BufferedImage image = ImageIO.read(imageInputStream);
        Integer height =image.getHeight();
        Integer width =image.getWidth();
        switch (orientation) {
            case 1:
                break;
            case 2: // Flip X
                affineTransform.scale(-1.0, 1.0);
                affineTransform.translate(-width, 0);
                break;
            case 3: // PI rotation
                affineTransform.translate(width, height);
                affineTransform.rotate(Math.PI);
                break;
            case 4: // Flip Y
                affineTransform.scale(1.0, -1.0);
                affineTransform.translate(0, -height);
                break;
            case 5: // - PI/2 and Flip X
                affineTransform.rotate(-Math.PI / 2);
                affineTransform.scale(-1.0, 1.0);
                break;
            case 6: // -PI/2 and -width
                affineTransform.translate(height, 0);
                affineTransform.rotate(Math.PI / 2);
                height=image.getWidth();
                width= image.getHeight();
                break;
            case 7: // PI/2 and Flip
                affineTransform.scale(-1.0, 1.0);
                affineTransform.translate(-height, 0);
                affineTransform.translate(0, width);
                affineTransform.rotate(3 * Math.PI / 2);
                break;
            case 8: // PI / 2
                affineTransform.translate(0, width);
                affineTransform.rotate(3 * Math.PI / 2);
                break;
            default:
                break;
        }
        AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage destinationImage = new BufferedImage(width, height, image.getType());
        destinationImage = affineTransformOp.filter(image, destinationImage);
        return destinationImage;
    }

    private int getCompression(int width, Double size) {
        if (width > 1000 && width <= 2000)
            return 1400;
        else if (width > 2000 && width <= 3000)
            return 600;
        else if (width > 3000 && width <= 4000)
            return 900;
        else if (width > 4000 && width <= 5000)
            return 1200;
        else if (width > 5000)
            return 550;
        else
            return size.intValue();
    }
}