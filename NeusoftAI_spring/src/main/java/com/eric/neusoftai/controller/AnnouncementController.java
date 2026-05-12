package com.eric.neusoftai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eric.neusoftai.common.Result;
import com.eric.neusoftai.entity.Announcement;
import com.eric.neusoftai.entity.User;
import com.eric.neusoftai.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 系统公告控制器
 */
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /** 分页查询公告列表(置顶优先) */
    @GetMapping("/list")
    public Result<Page<Announcement>> list(
            @RequestParam(defaultValue = "ALL") String announceType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(announcementService.pageList(announceType, current, size));
    }

    /** 获取最新公告(用于首页展示) */
    @GetMapping("/latest")
    public Result<Page<Announcement>> latest() {
        Page<Announcement> page = announcementService.pageList("ALL", 1, 5);
        return Result.ok(page);
    }

    /** 新增公告(仅管理员) */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Announcement announcement, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可发布公告");
        }
        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            return Result.fail("标题不能为空");
        }
        if (announcement.getContent() == null || announcement.getContent().isBlank()) {
            return Result.fail("内容不能为空");
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        return announcementService.add(announcement) ? Result.ok("发布成功", null) : Result.fail("发布失败");
    }

    /** 更新公告(仅管理员) */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Announcement announcement, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可修改公告");
        }
        return announcementService.update(announcement) ? Result.ok("更新成功", null) : Result.fail("更新失败");
    }

    /** 删除公告(仅管理员) */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可删除公告");
        }
        return announcementService.deleteById(id) ? Result.ok("删除成功", null) : Result.fail("删除失败");
    }
}
