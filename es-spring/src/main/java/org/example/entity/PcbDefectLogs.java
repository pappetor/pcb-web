package org.example.entity;

import co.elastic.clients.util.DateTime;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


import java.util.Date;
import java.util.List;

@Data
public class PcbDefectLogs {
    private Doc doc;
}
