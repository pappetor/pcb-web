package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.PcbImage;

import java.util.List;

public interface PcbImageService extends IService<PcbImage> {
    List<PcbImage> selectNewImages(Integer code);
}
