package com.evhub.app.util;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.evhub.app.request.ImageRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

public final class CommonUtils {

    private static final Logger LOG = LogManager.getLogger();

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    public static Date applicableTo(String date) throws ParseException {
//        String date_string = "9999-12-31";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse( date);
    }

    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 330);
        return calendar.getTime();
    }

    public static long getCurrentTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static long getCurrentTimeInMinutes() {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTimeInMillis();
    }

    public static Date getFirstDayOfPreviousMonths(int month) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, month);
        date.set(Calendar.DATE, 1);
        return date.getTime();
    }

    public static Date getLastDayOfPreviousMonth() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, -1);
        date.set(Calendar.DATE, date.getActualMaximum(Calendar.DAY_OF_MONTH));
        return date.getTime();
    }


    public static String DateFormatter(Date date) {
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//        Date date = new Date();
        return formatter.format(date);
    }

    public static Long timestamp(String date) throws ParseException {
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
        if (date.contains("T")) {
            OffsetDateTime odt = OffsetDateTime.parse( date );
            return Date.from(odt.toInstant()).getTime();
        }
        return formatter1.parse(date).getTime();
    }
    public static Date getDate(Long timeStamp){
        if(ObjectUtils.isEmpty(timeStamp) || timeStamp==0)
            return null;
        return new Date(timeStamp);
    }
    public static Integer getDay(Date date){
        if(ObjectUtils.isEmpty(date))
            return null;
         Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);;
        return calendar.get(Calendar.DATE);
    }
    @NotNull
    public static SimpleDateFormat getSimpleDateFormat(Integer day) {
        switch (day) {
            case 1:
                return new SimpleDateFormat("dd'st' MMM yyyy");
            case 2:
                return new SimpleDateFormat("dd'nd' MMM yyyy");
            case 3:
                return new SimpleDateFormat("dd'rd' MMM yyyy");
            default:
                return new SimpleDateFormat("dd'th' MMM yyyy");

        }
    }
}

