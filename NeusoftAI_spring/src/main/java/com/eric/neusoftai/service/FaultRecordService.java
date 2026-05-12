package com.eric.neusoftai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eric.neusoftai.entity.FaultRecord;
import com.eric.neusoftai.mapper.FaultRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaultRecordService {

    private final FaultRecordMapper faultRecordMapper;

    /**
     * 新增故障记录
     */
    public void saveRecord(FaultRecord record) {
        if (record.getLikeCount() == null) {
            record.setLikeCount(0);
        }
        if (record.getOptimized() == null) {
            record.setOptimized(0);
        }
        faultRecordMapper.insert(record);
    }

    /**
     * 根据ID查询记录
     */
    public FaultRecord getById(Long id) {
        return faultRecordMapper.selectById(id);
    }

    /**
     * 分页查询用户的故障记录（支持按类型筛选）
     */
    public Page<FaultRecord> pageByUser(Long userId, String faultType, Integer current, Integer size) {
        Page<FaultRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<FaultRecord> wrapper = new LambdaQueryWrapper<FaultRecord>()
                .eq(FaultRecord::getUserId, userId)
                .orderByDesc(FaultRecord::getLikeCount)
                .orderByDesc(FaultRecord::getCreateTime);
        
        if (faultType != null && !faultType.isEmpty() && !"ALL".equals(faultType)) {
            wrapper.eq(FaultRecord::getFaultType, faultType);
        }
        
        return faultRecordMapper.selectPage(page, wrapper);
    }

    /**
     * 删除单条记录
     */
    public boolean deleteById(Long id) {
        return faultRecordMapper.physicalDeleteById(id) > 0;
    }

    /**
     * 点赞
     */
    public boolean like(Long id) {
        return faultRecordMapper.incrementLike(id) > 0;
    }

    /**
     * 更新优化方案
     */
    public void updateOptimizedSolution(Long id, String optimizedSolution) {
        FaultRecord record = new FaultRecord();
        record.setId(id);
        record.setOptimizedSolution(optimizedSolution);
        record.setOptimized(1);
        faultRecordMapper.updateById(record);
    }

    /**
     * 查询用户所有记录（用于导出）
     */
    public java.util.List<FaultRecord> listByUser(Long userId, String faultType) {
        LambdaQueryWrapper<FaultRecord> wrapper = new LambdaQueryWrapper<FaultRecord>()
                .eq(FaultRecord::getUserId, userId)
                .orderByDesc(FaultRecord::getCreateTime);

        if (faultType != null && !faultType.isEmpty() && !"ALL".equals(faultType)) {
            wrapper.eq(FaultRecord::getFaultType, faultType);
        }

        return faultRecordMapper.selectList(wrapper);
    }
}
