package com.eric.neusoftai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eric.neusoftai.entity.DeviceLibrary;
import com.eric.neusoftai.mapper.DeviceLibraryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceLibraryService {

    private final DeviceLibraryMapper deviceLibraryMapper;

    public Page<DeviceLibrary> pageList(String keyword, String category, String status, Integer current, Integer size) {
        Page<DeviceLibrary> page = new Page<>(current, size);
        LambdaQueryWrapper<DeviceLibrary> wrapper = new LambdaQueryWrapper<DeviceLibrary>()
                .like(keyword != null && !keyword.isBlank(), DeviceLibrary::getDeviceName, keyword)
                .eq(category != null && !category.isEmpty() && !"ALL".equals(category), DeviceLibrary::getCategory, category)
                .eq(status != null && !status.isEmpty() && !"ALL".equals(status), DeviceLibrary::getStatus, status)
                .orderByDesc(DeviceLibrary::getCreateTime);
        return deviceLibraryMapper.selectPage(page, wrapper);
    }

    public boolean add(DeviceLibrary device) {
        return deviceLibraryMapper.insert(device) > 0;
    }

    public boolean update(DeviceLibrary device) {
        return deviceLibraryMapper.updateById(device) > 0;
    }

    public boolean deleteById(Long id) {
        return deviceLibraryMapper.deleteById(id) > 0;
    }

    public DeviceLibrary getById(Long id) {
        return deviceLibraryMapper.selectById(id);
    }
}
