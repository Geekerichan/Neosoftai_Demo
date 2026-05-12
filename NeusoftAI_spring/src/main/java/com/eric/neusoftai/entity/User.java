package com.eric.neusoftai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;

    /** 角色: ADMIN-管理员(可管理公告/设备/知识库), NORMAL-普通用户(仅查看和点赞) */
    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 判断是否为管理员 */
    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }
}
