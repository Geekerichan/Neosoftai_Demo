package com.eric.neusoftai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 运维常识
 */
@Data
@TableName("daily_tip")
public class DailyTip {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 常识内容 */
    private String content;
    /** 分类标签 */
    private String category;
}
