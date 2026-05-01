package net.arctel.opkit.controller;

import jakarta.annotation.Resource;
import net.arctel.platform.framework.utils.Result;
import net.arctel.opkit.output.OpkitListOutput;
import net.arctel.opkit.service.OpkitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/opkit/v")
public class OpkitController {

    @Resource
    OpkitService opkitService;

    /**
     * 列出所有工具
     */
    @GetMapping("/tools")
    public Result<List<OpkitListOutput>> listTools() {
        return Result.success(opkitService.listTools());
    }
}
