package com.evhub.app.service;

import com.amazonaws.util.IOUtils;
import com.evhub.app.entities.HtmlFiles;
import com.evhub.app.repository.HtmlFileRepo;
import com.evhub.app.util.CommonUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class HtmlFilrService {
    @Autowired
    private HtmlFileRepo htmlFileRepo;
    public String saveHtmlFile(MultipartFile file) throws IOException {
        HtmlFiles htmlFiles= new HtmlFiles();
        htmlFiles.setFileName(file.getOriginalFilename().replace(".html","").trim());
        htmlFiles.setId(CommonUtils.generateUUID());
        htmlFiles.setHtml( new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        htmlFileRepo.save(htmlFiles);
        return "File Save Successfully";
    }
    public Object updateHtmlFile(MultipartFile file,String id) throws IOException {
        HtmlFiles htmlFiles = htmlFileRepo.findById(id).get();
        htmlFiles.setHtml(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        htmlFileRepo.save(htmlFiles);
        return "Saved";
    }
}
