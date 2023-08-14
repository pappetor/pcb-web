package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pcb_image")
public class PcbImage {
    @TableId
    private Integer id;
    private String path;
}
