package com.eric.neusoftai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eric.neusoftai.common.Result;
import com.eric.neusoftai.entity.DeviceLibrary;
import com.eric.neusoftai.entity.User;
import com.eric.neusoftai.service.DeviceLibraryService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 设备资产库控制器
 */
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceLibraryService deviceLibraryService;

    /** 分页查询设备列表(支持搜索和筛选) */
    @GetMapping("/list")
    public Result<Page<DeviceLibrary>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(deviceLibraryService.pageList(keyword, category, status, current, size));
    }

    /** 新增设备(仅管理员) */
    @PostMapping("/add")
    public Result<?> add(@RequestBody DeviceLibrary device, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可添加设备");
        }
        if (device.getDeviceName() == null || device.getDeviceName().isBlank()) {
            return Result.fail("设备名称不能为空");
        }
        return deviceLibraryService.add(device) ? Result.ok("添加成功", null) : Result.fail("添加失败");
    }

    /** 更新设备(仅管理员) */
    @PutMapping("/update")
    public Result<?> update(@RequestBody DeviceLibrary device, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可修改设备");
        }
        return deviceLibraryService.update(device) ? Result.ok("更新成功", null) : Result.fail("更新失败");
    }

    /** 删除设备(仅管理员) */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return Result.fail("仅管理员可删除设备");
        }
        return deviceLibraryService.deleteById(id) ? Result.ok("删除成功", null) : Result.fail("删除失败");
    }

    /** 根据ID查询设备详情 */
    @GetMapping("/{id}")
    public Result<DeviceLibrary> detail(@PathVariable Long id) {
        DeviceLibrary device = deviceLibraryService.getById(id);
        return device != null ? Result.ok(device) : Result.fail("设备不存在");
    }
}
