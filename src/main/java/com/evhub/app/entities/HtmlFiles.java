package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.swing.text.html.HTML;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "files")
public class HtmlFiles {
    @Id
    private String id;
    private String fileName;
    private Set<String> forms;
    private Binary html;
}
