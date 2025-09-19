package org.javaup.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.javaup.ai.entity.KnowledgeBase;
import org.javaup.ai.mapper.KnowledgeBaseMapper;
import org.javaup.ai.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.RequestBody;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Override
    public Page<KnowledgeBase> pageList(int page, int size, String name) {
        QueryWrapper<KnowledgeBase> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        return this.page(new Page<>(page, size), wrapper);
    }
}