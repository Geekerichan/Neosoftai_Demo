package com.eric.neusoftai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eric.neusoftai.entity.Announcement;
import com.eric.neusoftai.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public Page<Announcement> pageList(String announceType, Integer current, Integer size) {
        Page<Announcement> page = new Page<>(current, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(announceType != null && !announceType.isEmpty() && !"ALL".equals(announceType), Announcement::getAnnounceType, announceType)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectPage(page, wrapper);
    }

    public boolean add(Announcement announcement) {
        return announcementMapper.insert(announcement) > 0;
    }

    public boolean update(Announcement announcement) {
        return announcementMapper.updateById(announcement) > 0;
    }

    public boolean deleteById(Long id) {
        return announcementMapper.deleteById(id) > 0;
    }
}
