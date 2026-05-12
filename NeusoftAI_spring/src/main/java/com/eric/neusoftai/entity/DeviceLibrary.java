package com.eric.neusoftai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备资产库
 */
@Data
@TableName("device_library")
public class DeviceLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 设备名称 */
    private String deviceName;
    /** 设备型号 */
    private String modelNumber;
    /** 设备分类: SERVER-服务器, PRINTER-打印机, NETWORK-网络设备, COMPUTER-办公电脑, OTHER-其他 */
    private String category;
    /** 设备状态: ONLINE-正常, OFFLINE-离线, MAINTAINING-维修中, SCRAPPED-已报废 */
    private String status;
    /** 设备位置/机房编号 */
    private String location;
    /** 购买日期 */
    private String purchaseDate;
    /** 负责人 */
    private String responsiblePerson;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
