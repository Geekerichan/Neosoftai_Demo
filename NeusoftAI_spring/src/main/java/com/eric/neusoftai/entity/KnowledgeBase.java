package com.eric.neusoftai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 运维知识库
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 知识标题 */
    private String title;
    /** 知识内容(Markdown) */
    private String content;
    /** 分类: SERVER-服务器运维, OFFICE-办公设备, NETWORK-网络安全, DATA-数据管理, MAINTAIN-日常维护, OTHER-其他 */
    private String category;
    /** 标签(逗号分隔) */
    private String tags;
    /** 浏览次数 */
    private Integer viewCount;
    /** 点赞数 */
    private Integer likeCount;
    /** 创建者用户名 */
    private String creatorName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
