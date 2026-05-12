package com.eric.neusoftai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eric.neusoftai.entity.FaultRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FaultRecordMapper extends BaseMapper<FaultRecord> {

    @Update("UPDATE fault_record SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLike(@Param("id") Long id);

    /** 物理删除记录（绕过逻辑删除） */
    @Delete("DELETE FROM fault_record WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
