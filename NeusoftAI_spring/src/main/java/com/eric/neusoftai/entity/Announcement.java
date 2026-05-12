package com.eric.neusoftai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统公告
 */
@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 公告标题 */
    private String title;
    /** 公告内容 */
    private String content;
    /** 公告类型: NOTICE-通知, WARNING-警告, MAINTAIN-维护, UPGRADE-升级 */
    private String announceType;
    /** 是否置顶 */
    private Integer isTop;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
