package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("overview")
public class Overview {
    @TableId
    private String id;
    private String name;
    @TableField("production_line")
    private String productionLine;
    private Integer cameras;
    private Integer pcbs;
    private Integer defects;
}
