package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dao.OverviewDao;
import org.example.entity.Overview;
import org.example.service.OverviewService;
import org.springframework.stereotype.Service;

@Service
public class OverviewServiceImpl extends ServiceImpl<OverviewDao, Overview> implements OverviewService {
}
