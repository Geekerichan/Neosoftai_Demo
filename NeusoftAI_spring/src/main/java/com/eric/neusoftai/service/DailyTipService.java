package com.eric.neusoftai.service;

import com.eric.neusoftai.entity.DailyTip;
import com.eric.neusoftai.mapper.DailyTipMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTipService {

    private final DailyTipMapper dailyTipMapper;

    /**
     * 随机获取一条运维常识
     */
    public DailyTip getTodayTip() {
        return dailyTipMapper.randomTip();
    }
}
