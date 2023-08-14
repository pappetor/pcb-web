package org.example.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Doc {
    private Date created;
    private String deted_file;
    private String production_line;
    private String src_image;
    private List<Results> results;
}
