-- 东软智能运维咨询系统 - 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS neusoft_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE neusoft_ai;

-- ==================== 表1: 系统用户表 ====================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(12) NOT NULL UNIQUE COMMENT '账号 6-12位',
    password VARCHAR(32) NOT NULL COMMENT 'MD5加密密码',
    role VARCHAR(10) NOT NULL DEFAULT 'NORMAL' COMMENT '角色: ADMIN-管理员, NORMAL-普通用户',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='系统用户表';

-- ==================== 表2: 故障咨询记录表 ====================
DROP TABLE IF EXISTS fault_record;
CREATE TABLE fault_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) DEFAULT '' COMMENT '用户名冗余',
    fault_description TEXT NOT NULL COMMENT '故障描述',
    solution TEXT COMMENT 'AI解决方案',
    fault_type VARCHAR(20) DEFAULT 'OTHER' COMMENT '故障类型:SERVER/OFFICE/OTHER',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    optimized TINYINT DEFAULT 0 COMMENT '是否已优化 0否1是',
    optimized_solution TEXT COMMENT '优化后的方案',
    session_id VARCHAR(32) DEFAULT '' COMMENT '会话ID(多轮对话)',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_fault_type (fault_type),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB COMMENT='故障咨询记录表';

-- ==================== 表3: 运维常识库 ====================
DROP TABLE IF EXISTS daily_tip;
CREATE TABLE daily_tip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(500) NOT NULL COMMENT '常识内容',
    category VARCHAR(30) NOT NULL DEFAULT '日常维护' COMMENT '分类标签',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='运维常识库';

-- 运维常识初始数据
INSERT INTO daily_tip (content, category) VALUES
('【服务器维护】定期检查服务器硬件状态指示灯，异常闪烁时及时排查电源、风扇或硬盘故障。', '服务器运维'),
('【服务器安全】定期更新操作系统补丁和杀毒软件病毒库，防范勒索病毒攻击。', '服务器安全'),
('【办公设备】打印机卡纸时，先关闭电源打开盖板，沿走纸方向缓慢取出纸张，避免撕碎残留。', '办公设备'),
('【网络排查】网络不通时先ping网关确认本地连接，再ping外网DNS判断是否为ISP问题。', '网络安全'),
('【数据备份】重要数据遵循3-2-1原则：3份副本、2种介质、1个异地备份。', '数据管理'),
('【机房环境】机房温度应保持在22±2℃，湿度40%-55%，定期清洁防尘。', '日常维护'),
('【UPS电源】UPS电池每季度做一次放电测试，确保断电时能正常供电。', '日常维护'),
('【显示器】屏幕出现花屏或闪烁，先检查线缆连接，再尝试更换分辨率或刷新率。', '办公设备'),
('【键盘鼠标】键鼠无反应时更换USB端口测试，仍无效则检查驱动程序是否正常。', '办公设备'),
('【软件安装】安装应用软件前务必从官方渠道下载，避免捆绑流氓软件。', '安全防护'),
('【账号安全】系统登录密码每90天更换一次，勿使用生日、电话等易猜测密码。', '安全防护'),
('【日志管理】系统日志至少保留6个月，关键操作日志建议保留1年以上便于审计。', '数据管理'),
('【磁盘空间】系统盘剩余空间低于20%时应及时清理临时文件和回收站。', '日常维护'),
('【远程桌面】使用远程连接后记得注销而非直接关闭窗口，释放服务器资源。', '服务器运维'),
('【防火墙】仅开放必要的服务端口，关闭不用的端口减少攻击面。', '网络安全'),
('【蓝屏处理】Windows蓝屏时记录错误代码（如0x0000007A），用于快速定位问题原因。', '办公设备'),
('【邮件附件】来历不明的邮件附件切勿打开，可能是钓鱼或病毒载体。', '安全防护'),
('【Wi-Fi安全】企业无线网络采用WPA3加密，禁用WPS功能防止PIN码破解。', '网络安全'),
('【虚拟内存】服务器物理内存不足时可适当增大页面文件，建议设为物理内存的1.5倍。', '服务器运维'),
('【散热保养】每半年清理一次台式机内部灰尘，重点清理CPU风扇和电源风扇。', '日常维护');

-- ==================== 表4: 设备资产库 ====================
DROP TABLE IF EXISTS device_library;
CREATE TABLE device_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    model_number VARCHAR(100) DEFAULT '' COMMENT '设备型号',
    category VARCHAR(30) NOT NULL COMMENT '分类:SERVER/PRINTER/NETWORK/COMPUTER/OTHER',
    status VARCHAR(20) DEFAULT 'ONLINE' COMMENT '状态:ONLINE/OFFLINE/MAINTAINING/SCRAPPED',
    location VARCHAR(200) DEFAULT '' COMMENT '位置/机房编号',
    purchase_date VARCHAR(20) DEFAULT '' COMMENT '购买日期',
    responsible_person VARCHAR(50) DEFAULT '' COMMENT '负责人',
    remark VARCHAR(1000) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='设备资产库';

-- ==================== 表5: 运维知识库 ====================
DROP TABLE IF EXISTS knowledge_base;
CREATE TABLE knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '知识标题',
    content TEXT NOT NULL COMMENT '知识内容(Markdown)',
    category VARCHAR(30) NOT NULL COMMENT '分类:SERVER/OFFICE/NETWORK/DATA/MAINTAIN/OTHER',
    tags VARCHAR(200) DEFAULT '' COMMENT '标签(逗号分隔)',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    creator_name VARCHAR(50) DEFAULT '' COMMENT '创建者用户名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category (category)
) ENGINE=InnoDB COMMENT='运维知识库';

-- ==================== 表6: 系统公告 ====================
DROP TABLE IF EXISTS announcement;
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    announce_type VARCHAR(20) DEFAULT 'NOTICE' COMMENT '类型:NOTICE/WARNING/MAINTAIN/UPGRADE',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶 0否1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='系统公告';

-- ============================================================
-- 测试数据
-- ============================================================

INSERT INTO sys_user (username, password, role) VALUES ('admin', MD5('123456'), 'ADMIN');
INSERT INTO sys_user (username, password, role) VALUES ('geekeric', MD5('123456'), 'NORMAL');

-- 设备测试数据
INSERT INTO device_library (device_name, model_number, category, status, location, purchase_date, responsible_person, remark) VALUES
('Dell PowerEdge R740服务器', 'R740-XC42', 'SERVER', 'ONLINE', 'A栋机房-机柜03', '2023-06-15', '陈工', '核心业务服务器，配置双路Xeon Gold 6248R，512GB内存'),
('HP ProLiant DL380 Gen10', 'DL380-Gen10', 'SERVER', 'ONLINE', 'A栋机房-机柜05', '2023-09-20', '陈工', '数据库专用服务器'),
('Canon imageRUNNER ADVANCE C3520i', 'C3520i', 'PRINTER', 'ONLINE', 'B栋3楼办公区', '2024-01-10', '陈工', '多功能复合打印机，支持彩色打印'),
('HP LaserJet Pro M404n', 'M404n', 'PRINTER', 'OFFLINE', 'B栋2楼办公区', '2022-11-05', '陈工', '激光打印机，硒鼓需更换'),
('Cisco Catalyst 9300交换机', 'C9300-48T', 'NETWORK', 'ONLINE', 'A栋机房-网络机柜01', '2023-03-18', '陈工', '核心接入层交换机，48口千兆'),
('H3C S5500V2-34S-EI', 'S5500V2', 'NETWORK', 'ONLINE', 'A栋机房-网络机柜02', '2023-07-22', '陈工', '汇聚层交换机'),
('Dell OptiPlex 7090台式机', '7090-UCC', 'COMPUTER', 'MAINTAINING', 'C栋5楼研发区', '2024-02-28', '陈工', '开发人员用机，正在升级内存'),
('Lenovo ThinkPad T14 Gen3', 'T14-Gen3', 'COMPUTER', 'ONLINE', 'C栋4楼市场部', '2024-04-12', '陈工', '项目经理笔记本'),
('APC Smart-UPS 3000', 'SMT3000I', 'OTHER', 'ONLINE', 'A栋机房-UPS间', '2023-01-08', '陈工', '不间断电源，电池已做年度巡检'),
('NetApp FAS2750存储阵列', 'FAS2750', 'OTHER', 'ONLINE', 'A栋机房-存储机柜', '2023-05-30', '陈工', 'SAN存储，容量120TB');

-- 知识库测试数据
INSERT INTO knowledge_base (title, content, category, tags, view_count, like_count, creator_name) VALUES
('Linux服务器CPU使用率过高排查指南', '# Linux服务器CPU使用率过高排查\n\n## 常见原因\n1. **进程占用过多** - 使用 `top` 或 `htop` 查看\n2. **僵尸进程** - `ps aux | grep Z`\n3. **内存不足导致swap频繁** - `free -h` 查看\n\n## 排查步骤\n```bash\ntop -c        # 查看占用CPU最高的进程\nps aux --sort=-%cpu | head -10   # Top 10 CPU进程\nvmstat 1 5   # 查看系统资源趋势\n```\n\n## 处理方案\n- 终止异常进程：`kill -9 PID`\n- 调整服务配置：降低线程池大小\n- 升级硬件：增加CPU核心数或更换更高性能处理器', 'SERVER', 'Linux,CPU,性能调优,排查', 156, 23, 'admin'),

('Windows蓝屏代码0x0000007A解决方案', '# 蓝屏代码0x0000007A(KERNEL_DATA_INPAGE_ERROR)\n\n## 问题原因\n该错误通常由以下原因引起：\n- 硬盘坏道或文件系统损坏\n- 内存条故障\n- IDE/SATA数据线接触不良\n- 页面文件损坏\n\n## 解决步骤\n1. 进入安全模式运行 `chkdsk /f /r` 修复磁盘\n2. 运行 `sfc /scannow` 检查系统文件\n3. 更新硬盘和芯片组驱动程序\n4. 运行Windows内存诊断工具检查内存\n5. 如仍无法解决，考虑备份数据后重装系统', 'COMPUTER', 'Windows,蓝屏,0x0000007A,硬盘', 203, 45, 'admin'),

('企业内网DNS解析缓慢问题处理', '# DNS解析缓慢排查与优化\n\n## 排查思路\n1. **确认DNS服务器是否正常**：`nslookup www.baidu.com`\n2. **检查本地hosts文件**是否有异常劫持\n3. **查看网卡DNS设置**是否正确指向内部DNS\n\n## 优化方案\n```\n# Linux下/etc/resolv.conf配置\nnameserver 10.0.0.10    # 主DNS\nnameserver 10.0.0.11    # 备DNS\noptions timeout:2 attempts:3 rotate\n```\n\n## 预防措施\n- 配置DNS缓存服务器(Bind/unbound)\n- 定期清理DNS缓存\n- 监控DNS查询延迟指标', 'NETWORK', 'DNS,内网,网络优化,延迟', 89, 12, 'admin'),

('打印机卡纸常见原因及快速处理', '# 打印机卡纸处理指南\n\n## 常见卡纸原因\n| 原因 | 占比 |\n|------|------|\n| 纸张潮湿/卷曲 | 35% |\n| 进纸辊老化 | 25% |\n| 纸张规格不符 | 20% |\n| 异物掉入 | 15% |\n| 定影组件问题 | 5% |\n\n## 快速处理步骤\n1. 打开前盖/后盖，小心取出卡纸（**勿撕扯**）\n2. 检查进纸路径是否有残留纸屑\n3. 更换干燥平整的打印纸\n4. 清洁进纸辊（用无纺布蘸酒精）\n5. 若反复卡纸，联系厂商售后', 'PRINTER', '打印机,卡纸,维护,办公设备', 178, 31, 'admin'),

('MySQL数据库备份恢复最佳实践', '# MySQL备份恢复规范\n\n## 备份策略\n- **全量备份**: 每日凌晨2点执行 `mysqldump --all-databases --single-transaction --routines --triggers`\n- **增量备份**: 开启binlog，每小时归档一次\n- **异地备份**: 每日同步到灾备机房\n\n## 恢复命令示例\n```bash\n# 全量恢复\nmysql -u root -p < all_databases.sql\n\n# 基于binlog的时间点恢复\nmysqlbinlog --start-datetime=\"2024-01-15 00:00:00\" binlog.000123 | mysql -u root -p\n```\n\n## 注意事项\n- 备份前务必验证备份文件的完整性\n- 定期进行恢复演练（每季度至少一次）', 'DATA', 'MySQL,备份,数据库,容灾', 267, 58, 'admin');

-- 公告测试数据
INSERT INTO announcement (title, content, announce_type, is_top) VALUES
('关于系统升级维护的通知', '各位同事：\n\n为提升系统稳定性，运维团队计划于本周六（4月25日）02:00-06:00 对AI诊断服务进行版本升级。\n\n届时故障咨询功能将暂停使用，请提前安排好相关工作。升级完成后将第一时间通知。\n\n如有疑问请联系运维部 杨工。', 'MAINTAIN', 1),

('夏季机房温度监控提醒', '进入夏季高温季节，请各部门注意以下事项：\n\n1. 机房空调保持24小时正常运行\n2. 温度超过28度立即上报\n3. 严禁在机房堆放杂物影响散热\n4. UPS电池季度巡检已开始，请配合检查', 'WARNING', 1),

('新功能上线通知', '智能运维咨询平台已完成以下功能升级：\n\n- 新增【设备资产管理】模块，支持在线管理公司所有IT设备\n- 新增【运维知识库】，积累常见故障解决方案\n- AI诊断模型已更新至最新版本，准确率提升约15%\n\n欢迎大家体验并提出宝贵意见！', 'NOTICE', 0);
