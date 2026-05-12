package com.eric.neusoftai.controller;

import com.eric.neusoftai.common.Result;
import com.eric.neusoftai.entity.DailyTip;
import com.eric.neusoftai.entity.FaultRecord;
import com.eric.neusoftai.entity.User;
import com.eric.neusoftai.service.DailyTipService;
import com.eric.neusoftai.service.FaultRecordService;
import com.eric.neusoftai.service.OllamaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 故障咨询控制器 - 纯 REST API（前后端分离）
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class FaultController {

    private final FaultRecordService faultRecordService;
    private final OllamaService ollamaService;
    private final DailyTipService dailyTipService;

    // ==================== 以下方法从 HttpServletRequest 获取用户信息（JWT拦截器注入） ====================

    private User getUser(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        return user;
    }

    /**
     * 故障咨询 - 首次诊断
     */
    @PostMapping("/fault/diagnose")
    public Result<Map<String, Object>> diagnose(
            @RequestParam String description,
            @RequestParam(defaultValue = "OTHER") String faultType,
            HttpServletRequest request) {
        User user = getUser(request);

        long start = System.currentTimeMillis();
        String solution = ollamaService.diagnose(description);
        long cost = System.currentTimeMillis() - start;

        // 保存记录
        FaultRecord record = new FaultRecord();
        record.setUserId(user.getId());
        record.setUsername(user.getUsername());
        record.setFaultDescription(description);
        record.setSolution(solution);
        record.setFaultType(faultType);
        record.setSessionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        faultRecordService.saveRecord(record);

        Map<String, Object> data = new HashMap<>();
        data.put("solution", solution);
        data.put("recordId", record.getId());
        data.put("sessionId", record.getSessionId());
        data.put("costMs", cost);

        return Result.ok(data);
    }

    /**
     * 图片故障诊断
     */
    @PostMapping("/fault/diagnose-image")
    public Result<Map<String, Object>> diagnoseByImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(defaultValue = "OTHER") String faultType,
            HttpServletRequest request) {
        User user = getUser(request);

        if (image.isEmpty()) {
            return Result.fail("请选择要上传的图片");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("只支持图片文件(jpg/png/gif/webp)");
        }
        long maxSize = 10 * 1024 * 1024;
        if (image.getSize() > maxSize) {
            return Result.fail("图片大小不能超过10MB");
        }

        try {
            byte[] bytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);

            String descText;
            if (description != null && !description.isBlank()) {
                descText = description + "\n\n[用户上传了设备故障图片，请结合图片进行分析]";
            } else {
                descText = "[用户上传了设备故障图片，请仔细观察图片中的设备状态、错误提示等信息]";
            }

            long start = System.currentTimeMillis();
            String solution = ollamaService.diagnoseByImage(base64Image, descText);
            long cost = System.currentTimeMillis() - start;

            FaultRecord record = new FaultRecord();
            record.setUserId(user.getId());
            record.setUsername(user.getUsername());
            record.setFaultDescription("[图片诊断] " +
                    (description != null && !description.isBlank() ? description : "上传图片进行设备识别与故障分析"));
            record.setSolution(solution);
            record.setFaultType(faultType);
            record.setSessionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            faultRecordService.saveRecord(record);

            Map<String, Object> data = new HashMap<>();
            data.put("solution", solution);
            data.put("recordId", record.getId());
            data.put("sessionId", record.getSessionId());
            data.put("costMs", cost);
            data.put("mode", "vision");

            return Result.ok(data);
        } catch (Exception e) {
            log.error("图片处理异常: {}", e.getMessage(), e);
            return Result.fail("图片处理失败：" + e.getMessage());
        }
    }

    /**
     * 多轮追问诊断
     */
    @PostMapping("/fault/followUp")
    public Result<Map<String, Object>> followUp(
            @RequestParam Long recordId,
            @RequestParam String followUpContent,
            HttpServletRequest request) {
        User user = getUser(request);

        List<FaultRecord> records = faultRecordService.listByUser(user.getId(), null);
        FaultRecord original = records.stream()
                .filter(r -> r.getId().equals(recordId))
                .findFirst()
                .orElse(null);

        if (original == null) {
            return Result.fail("未找到原始咨询记录");
        }

        // 权限校验：确保用户只能操作自己的记录
        if (!original.getUserId().equals(user.getId())) {
            return Result.fail("无权操作该记录");
        }

        List<Map<String, String>> history = List.of(
                Map.of("role", "用户", "content", original.getFaultDescription()),
                Map.of("role", "助手", "content", original.getSolution()),
                Map.of("role", "用户", "content", followUpContent)
        );

        String solution = ollamaService.followUpDiagnose(history, followUpContent);

        FaultRecord followUpRecord = new FaultRecord();
        followUpRecord.setUserId(user.getId());
        followUpRecord.setUsername(user.getUsername());
        followUpRecord.setFaultDescription("[追问] " + followUpContent);
        followUpRecord.setSolution(solution);
        followUpRecord.setFaultType(original.getFaultType());
        followUpRecord.setSessionId(original.getSessionId());
        faultRecordService.saveRecord(followUpRecord);

        Map<String, Object> data = new HashMap<>();
        data.put("solution", solution);
        data.put("recordId", followUpRecord.getId());
        data.put("sessionId", original.getSessionId());

        return Result.ok(data);
    }

    /**
     * 方案优化
     */
    @PostMapping("/fault/optimize")
    public Result<String> optimize(
            @RequestParam Long recordId,
            @RequestParam(required = false, defaultValue = "方案不够具体，请提供更详细的步骤") String feedback,
            HttpServletRequest request) {
        User user = getUser(request);

        List<FaultRecord> records = faultRecordService.listByUser(user.getId(), null);
        FaultRecord original = records.stream()
                .filter(r -> r.getId().equals(recordId))
                .findFirst()
                .orElse(null);

        if (original == null) {
            return Result.fail("未找到该记录");
        }

        // 权限校验：确保用户只能操作自己的记录
        if (!original.getUserId().equals(user.getId())) {
            return Result.fail("无权操作该记录");
        }

        String optimizedSolution = ollamaService.optimizeSolution(
                original.getSolution(), original.getFaultDescription(), feedback
        );

        faultRecordService.updateOptimizedSolution(recordId, optimizedSolution);
        return Result.ok(optimizedSolution);
    }

    /**
     * 点赞
     */
    @PostMapping("/fault/like/{id}")
    public Result<?> like(@PathVariable Long id, HttpServletRequest request) {
        User user = getUser(request);
        // 权限校验：确保用户只能点赞自己的记录
        FaultRecord record = faultRecordService.getById(id);
        if (record == null) {
            return Result.fail("记录不存在");
        }
        if (!record.getUserId().equals(user.getId())) {
            return Result.fail("无权操作该记录");
        }
        boolean ok = faultRecordService.like(id);
        return ok ? Result.ok("点赞成功", null) : Result.fail("点赞失败");
    }

    /**
     * 分页查询记录
     */
    @GetMapping("/records/list")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<FaultRecord>> listRecords(
            @RequestParam(defaultValue = "ALL") String faultType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        User user = getUser(request);
        var page = faultRecordService.pageByUser(user.getId(), faultType, current, size);
        return Result.ok(page);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/records/{id}")
    public Result<?> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        User user = getUser(request);
        // 权限校验：确保用户只能删除自己的记录
        FaultRecord record = faultRecordService.getById(id);
        if (record == null) {
            return Result.fail("记录不存在");
        }
        if (!record.getUserId().equals(user.getId())) {
            return Result.fail("无权操作该记录");
        }
        return faultRecordService.deleteById(id) ? Result.ok("删除成功", null) : Result.fail("删除失败");
    }

    /**
     * 获取今日常识（随机从知识库读取）
     */
    @GetMapping("/tip/today")
    public Result<DailyTip> todayTip() {
        DailyTip tip = dailyTipService.getTodayTip();
        return Result.ok(tip);
    }

    private static String getTypeName(String type) {
        if (type == null) return "其他";
        return switch (type) {
            case "SERVER" -> "服务器";
            case "OFFICE" -> "办公设备";
            default -> "其他";
        };
    }
}
