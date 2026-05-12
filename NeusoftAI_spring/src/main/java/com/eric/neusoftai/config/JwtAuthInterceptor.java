package com.eric.neusoftai.config;

import com.eric.neusoftai.entity.User;
import com.eric.neusoftai.service.UserService;
import com.eric.neusoftai.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * 从请求头 Authorization 中提取并验证 Token
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从 Header 获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthResponse(response, "未登录或Token已过期");
            return false;
        }

        String token = authHeader.substring(7);

        // 验证 Token
        if (!JwtUtil.validateToken(token)) {
            writeUnauthResponse(response, "Token无效或已过期");
            return false;
        }

        // 解析用户信息并注入 Request 属性
        try {
            Long userId = JwtUtil.getUserId(token);
            User user = userService.getById(userId);
            if (user == null) {
                writeUnauthResponse(response, "用户不存在");
                return false;
            }
            user.setPassword(null);
            request.setAttribute("user", user);
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            writeUnauthResponse(response, "Token解析失败");
            return false;
        }
    }

    /**
     * 写入 401 未认证响应
     */
    private void writeUnauthResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(
                new com.eric.neusoftai.common.Result<>(401, message, null)
        );
        response.getWriter().write(json);
    }
}
