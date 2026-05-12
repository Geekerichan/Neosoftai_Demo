package com.eric.neusoftai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eric.neusoftai.entity.DailyTip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DailyTipMapper extends BaseMapper<DailyTip> {

    @Select("SELECT * FROM daily_tip ORDER BY RAND() LIMIT 1")
    DailyTip randomTip();
}
