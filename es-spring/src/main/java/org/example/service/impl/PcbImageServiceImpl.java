package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dao.PcbImageDao;
import org.example.entity.PcbImage;
import org.example.service.PcbImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PcbImageServiceImpl extends ServiceImpl<PcbImageDao, PcbImage> implements PcbImageService {
    @Autowired
    private PcbImageDao pcbImageDao;

    @Override
    public List<PcbImage> selectNewImages(Integer code) {
        return pcbImageDao.selectNewImages(code);
    }
}
