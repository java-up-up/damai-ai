package org.javaup.ai.cotroller;

import org.javaup.ai.common.ApiResponse;
import org.javaup.ai.entity.KnowledgeBase;
import org.javaup.ai.service.FileRagService;
import org.javaup.ai.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.document.Document;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料
 * @description: 聊天记录控制器
 * @author: docFat
 **/
@RestController
@RequestMapping("/file-rag")
public class FileRagController {

    @Autowired
    private FileRagService fileRagService;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/upload")
    public ApiResponse<Long> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "remark", required = false) String remark
    ) {
        Long kbId = fileRagService.processFile(file, name, remark);
        return ApiResponse.ok(kbId);
    }

    @GetMapping("/list")
    public ApiResponse<List<KnowledgeBase>> listKnowledgeBases() {
        List<KnowledgeBase> list = knowledgeBaseService.list();
        return ApiResponse.ok(list);
    }

    @GetMapping("/page")
    public ApiResponse<Map<String, Object>> pageKnowledgeBases(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "name", required = false) String name
    ) {
        Page<KnowledgeBase> pageObj = knowledgeBaseService.pageList(page, size, name);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageObj.getTotal());
        result.put("list", pageObj.getRecords());
        return ApiResponse.ok(result);
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteKnowledgeBase(@RequestParam Long id) {
        boolean removed = knowledgeBaseService.removeById(id);
        return ApiResponse.ok(removed);
    }

    @PostMapping("/update")
    public ApiResponse<Boolean> updateKnowledgeBase(@RequestBody KnowledgeBase kb) {
        boolean updated = knowledgeBaseService.updateById(kb);
        return ApiResponse.ok(updated);
    }
}