package com.eric.neusoftai.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Ollama大模型服务 - 支持文本和多模态(图片)诊断
 */
@Slf4j
@Service
public class OllamaService {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    @Value("${ollama.timeout:10}")
    private int timeoutSeconds;

    /**
     * 故障诊断 - 首次咨询
     * @param description 故障描述
     * @return 50-100字解决方案
     */
    public String diagnose(String description) {
        String prompt = buildDiagnosePrompt(description);
        return callOllama(prompt);
    }

    /**
     * 多轮追问诊断
     * @param history 对话历史
     * @param followUp 追问内容
     * @return 精准定位后的解决方案
     */
    public String followUpDiagnose(java.util.List<Map<String, String>> history, String followUp) {
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("以下是之前关于设备故障的咨询记录：\n");
        for (Map<String, String> msg : history) {
            contextBuilder.append(msg.getOrDefault("role", "unknown")).append(": ").append(msg.getOrDefault("content", "")).append("\n");
        }
        contextBuilder.append("\n用户新的补充信息：").append(followUp).append("\n");
        contextBuilder.append("\n请结合之前的对话和新信息，给出更精准的解决方案，50-100字。");

        return callOllama(contextBuilder.toString());
    }

    /**
     * 方案优化 - 用户对不满意方案重新优化
     * @param originalSolution 原始方案
     * @param faultDescription 故障描述
     * @param feedback 用户反馈
     * @return 优化后方案
     */
    public String optimizeSolution(String originalSolution, String faultDescription, String feedback) {
        String prompt = String.format("""
                你是东软设备运维专家。用户对以下解决方案不满意：
                
                【原始故障】%s
                【原解决方案】%s
                【用户反馈】%s
                
                请根据东软运维规范，重新生成更贴合实际场景的解决步骤，80-120字。
                """, faultDescription, originalSolution, feedback);

        return callOllama(prompt);
    }

    /**
     * 构建首次诊断Prompt
     */
    private String buildDiagnosePrompt(String description) {
        return String.format("""
                你是东软设备智能运维顾问。针对东软常用设备（服务器、办公电脑、打印机、网络设备等）的故障，请提供专业简洁的解决方案。
                
                用户描述的故障：%s
                
                要求：1）分析可能原因；2）给出具体解决步骤；3）控制在50-100字；4）语言简洁专业。
                """, description);
    }

    // ==================== 图片视觉诊断 ====================

    /**
     * 图片故障诊断 - 基于图片识别设备并分析故障
     * @param imageBase64 图片Base64编码(不含data:前缀)
     * @param description 用户补充的文字描述(可选)
     * @return 识别结果+解决方案
     */
    public String diagnoseByImage(String imageBase64, String description) {
        String prompt = buildVisionPrompt(description);
        return callOllamaWithImage(prompt, imageBase64);
    }

    /**
     * 构建视觉诊断Prompt
     */
    private String buildVisionPrompt(String userDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是东软设备智能运维专家，擅长通过图片识别设备并诊断故障。\n\n");
        sb.append("【任务】\n");
        sb.append("1. 仔细观察用户上传的图片，识别其中的设备类型、型号、状态。\n");
        sb.append("2. 分析图片中可见的异常现象：错误提示、指示灯状态、物理损坏、线缆连接等。\n");
        sb.append("3. 结合东软运维经验，判断可能的故障原因。\n");
        sb.append("4. 提供具体的维修建议和操作步骤。\n\n");

        if (userDescription != null && !userDescription.isBlank()) {
            sb.append("【用户补充说明】").append(userDescription).append("\n\n");
        }

        sb.append("【输出格式】\n");
        sb.append("## 设备识别\n（识别出的设备及关键特征）\n\n");
        sb.append("## 故障分析\n（观察到的问题及可能原因）\n\n");
        sb.append("## 解决方案\n（具体步骤，80-120字）\n\n");
        sb.append("请用中文回答。");

        return sb.toString();
    }

    /**
     * 调用Ollama多模态API (带图片) - 使用 /api/generate 接口 + images 参数
     */
    private String callOllamaWithImage(String prompt, String imageBase64) {
        try {
            // 使用 /api/generate 端点 + images 参数（Ollama原生视觉支持）
            String url = baseUrl + "/api/generate";

            // 日志：记录发送的图片信息（不记录完整base64）
            log.info("=== 视觉诊断开始 === 模型: {}, 图片Base64长度: {}字节(约{}KB)", 
                model, imageBase64.length(), imageBase64.length() * 3 / 4096);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("prompt", prompt);
            body.put("stream", false);
            body.put("images", Collections.singletonList(imageBase64));
            body.put("options", Map.of(
                "temperature", 0.7,
                "num_predict", 2048
            ));

            long start = System.currentTimeMillis();

            String rawResponse = HttpUtil.post(url, JSONUtil.toJsonStr(body), timeoutSeconds * 1000);
            long cost = System.currentTimeMillis() - start;

            log.info("Ollama视觉响应耗时: {}ms, 原始响应前300字符: {}", 
                cost, rawResponse.substring(0, Math.min(300, rawResponse.length())));

            if (rawResponse == null || rawResponse.isBlank()) {
                log.error("Ollama返回空响应，请检查模型是否支持视觉功能: {}", model);
                return "视觉模型无响应。请确认当前模型支持图像识别，或切换到 llava/minicpm-v 等多模态模型。";
            }

            JSONObject json;
            try {
                json = JSON.parseObject(rawResponse);
            } catch (Exception parseEx) {
                log.error("视觉JSON解析失败，原始前500字符: {}", 
                    rawResponse.substring(0, Math.min(500, rawResponse.length())), parseEx);
                return "视觉服务数据异常，请检查Ollama版本是否支持多模态。";
            }

            if (json.containsKey("error")) {
                String errorMsg = json.getString("error");
                log.error("Ollama视觉返回错误: {}", errorMsg);
                
                if (errorMsg.contains("multimodal") || errorMsg.contains("vision") || 
                    errorMsg.contains("image") || errorMsg.contains("unsupported")) {
                    log.warn("模型 {} 不支持视觉功能，建议使用 llava/minicpm-v 等多模态模型", model);
                    return "【视觉模型不支持】当前模型「" + model + "」不支持图像识别。\n\n" +
                           "解决方案：\n" +
                           "1. 终端执行：ollama pull minicpm-v\n" +
                           "2. 修改 application.yml 中 ollama.model 为 minicpm-v\n" +
                           "3. 重启应用\n\n" +
                           "或直接使用文字描述故障进行咨询。";
                }
                return "视觉调用失败: " + errorMsg;
            }

            // 解析 /api/generate 响应格式
            String result = json.getString("response");

            if (result != null && !result.isBlank()) {
                result = cleanResult(result);
                log.info("AI视觉方案生成成功, 耗时: {}ms, 长度: {}字", cost, result.length());
                return result;
            }

            log.warn("Ollama视觉返回response为空, 耗时: {}ms", cost);
            return "抱歉，视觉分析暂时无法生成结果，请稍后重试或改用文字咨询。";

        } catch (cn.hutool.core.io.IORuntimeException e) {
            String errMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errMsg.contains("timeout")) {
                log.error("Ollama视觉请求超时({}s), 模型={}", timeoutSeconds, model);
                return "视觉分析超时（图片可能较大），请稍后重试或使用文字描述故障。";
            }
            log.error("调用Ollama视觉IO异常, 模型={}", model, e);
            return "视觉服务连接异常：" + extractErrorMsg(errMsg) + "。请稍后重试或改用文字咨询。";
        } catch (Exception e) {
            log.error("调用Ollama视觉异常, 模型={}, URL={}", model, baseUrl, e);
            return "视觉分析出现异常：" + extractErrorMsg(e.getMessage()) + "。请稍后重试或改用文字咨询。";
        }
    }

    /**
     * 清理模型返回结果
     */
    private String cleanResult(String result) {
        result = result.replaceAll("<.*?>", "").trim();
        result = result.replaceAll("\\n{3,}", "\n\n").trim();
        return result;
    }

    /**
     * 提取简短错误信息
     */
    private String extractErrorMsg(String msg) {
        if (msg == null) return "未知错误";
        return msg.length() > 100 ? msg.substring(0, 100) + "..." : msg;
    }

    /**
     * 调用Ollama文本生成API
     */
    private String callOllama(String prompt) {
        try {
            String url = baseUrl + "/api/generate";

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("prompt", prompt);
            body.put("stream", false);
            body.put("options", Map.of("temperature", 0.7, "num_predict", 1024));

            long start = System.currentTimeMillis();
            String rawResponse = HttpUtil.post(url, JSONUtil.toJsonStr(body), timeoutSeconds * 1000);
            long cost = System.currentTimeMillis() - start;

            log.info("Ollama响应耗时: {}ms, 原始长度: {}字节", cost, rawResponse.length());

            if (rawResponse == null || rawResponse.isBlank()) {
                log.error("Ollama返回空响应，请检查模型是否正确: {}", model);
                return "模型无响应，请在终端执行 ollama list 确认模型名称正确";
            }

            JSONObject json;
            try {
                json = JSON.parseObject(rawResponse);
            } catch (Exception parseEx) {
                log.error("JSON解析失败，原始响应前500字符: {}", rawResponse.substring(0, Math.min(500, rawResponse.length())), parseEx);
                return "服务返回数据异常，请检查Ollama版本和模型兼容性";
            }

            // 检查是否有错误信息
            if (json.containsKey("error")) {
                String errorMsg = json.getString("error");
                log.error("Ollama返回错误: {}", errorMsg);
                return "模型调用失败: " + errorMsg + "（请确认已运行 ollama pull " + model + "）";
            }

            String result = json.getString("response");
            
            // 清理结果
            if (result != null && !result.isBlank()) {
                result = result.replaceAll("<.*?>", "").trim();
                // 移除多余空白行
                result = result.replaceAll("\\n{3,}", "\n\n").trim();
                log.info("AI生成方案成功, 长度: {}字", result.length());
                return result;
            }
            
            log.warn("Ollama返回response为空，done_reason可能为length限制");
            return "抱歉，暂时无法生成解决方案（response为空），请稍后重试。";
        } catch (Exception e) {
            log.error("调用Ollama失败, 模型={}, URL={}", model, baseUrl, e);
            return "服务暂时不可用，请检查Ollama服务是否正常运行（确保已启动ollama serve且已拉取模型）。";
        }
    }
}
