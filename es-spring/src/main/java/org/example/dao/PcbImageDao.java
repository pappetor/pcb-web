package org.example.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.PcbImage;

import java.util.List;

@Mapper
public interface PcbImageDao extends BaseMapper<PcbImage> {

    List<PcbImage> selectNewImages(@Param("code") Integer code);
}
