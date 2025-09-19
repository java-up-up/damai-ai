package org.javaup.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.ai.entity.KnowledgeBase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    Page<KnowledgeBase> pageList(int page, int size, String name);
}