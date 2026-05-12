package com.eric.neusoftai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 故障咨询记录
 */
@Data
@TableName("fault_record")
public class FaultRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 用户名(冗余存储方便查询) */
    private String username;
    /** 故障描述 */
    private String faultDescription;
    /** AI解决方案 */
    private String solution;
    /** 故障类型: SERVER-服务器, OFFICE-办公设备, OTHER-其他 */
    private String faultType;
    /** 点赞数 */
    private Integer likeCount;
    /** 是否已优化: 0-否 1-是 */
    private Integer optimized;
    /** 优化后的方案 */
    private String optimizedSolution;
    /** 会话ID（多轮对话） */
    private String sessionId;
    /** 删除标志 */
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
