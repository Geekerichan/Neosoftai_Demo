package com.eric.neusoftai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eric.neusoftai.common.Result;
import com.eric.neusoftai.entity.KnowledgeBase;
import com.eric.neusoftai.entity.User;
import com.eric.neusoftai.service.KnowledgeBaseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 运维知识库控制器
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeBaseService knowledgeBaseService;

    /** 分页查询知识列表(支持搜索和分类筛选) */
    @GetMapping("/list")
    public Result<Page<KnowledgeBase>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(knowledgeBaseService.pageList(keyword, category, current, size));
    }

    /** 查看知识详情(浏览量+1) */
    @GetMapping("/{id}")
    public Result<KnowledgeBase> detail(@PathVariable Long id) {
        KnowledgeBase kb = knowledgeBaseService.getById(id);
        if (kb != null) {
            knowledgeBaseService.incrementView(id);
        }
        return kb != null ? Result.ok(kb) : Result.fail("知识不存在");
    }

    /** 新增知识(仅管理员) */
    @PostMapping("/add")
    public Result<?> add(@RequestBody KnowledgeBase kb, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可发布知识");
        }
        if (kb.getTitle() == null || kb.getTitle().isBlank()) {
            return Result.fail("标题不能为空");
        }
        if (kb.getContent() == null || kb.getContent().isBlank()) {
            return Result.fail("内容不能为空");
        }
        kb.setCreatorName(user.getUsername());
        return knowledgeBaseService.add(kb) ? Result.ok("发布成功", null) : Result.fail("发布失败");
    }

    /** 更新知识(仅管理员) */
    @PutMapping("/update")
    public Result<?> update(@RequestBody KnowledgeBase kb, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可修改知识");
        }
        return knowledgeBaseService.update(kb) ? Result.ok("更新成功", null) : Result.fail("更新失败");
    }

    /** 删除知识(仅管理员) */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可删除知识");
        }
        return knowledgeBaseService.deleteById(id) ? Result.ok("删除成功", null) : Result.fail("删除失败");
    }

    /** 点赞 */
    @PostMapping("/like/{id}")
    public Result<?> like(@PathVariable Long id) {
        knowledgeBaseService.incrementLike(id);
        return Result.ok("点赞成功", null);
    }
}
