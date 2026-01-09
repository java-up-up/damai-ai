package org.javaup.ai.test;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: RAG测试类
 * @author: 阿星不是程序员
 **/
@Slf4j
@Component
public class RagAiTest {
    
    @Autowired
    private VectorStore vectorStore;
    
    //@PostConstruct
    public void testVectorStore(){
        //搜索条件
        SearchRequest request = SearchRequest.builder()
                .query("退票政策")
                .topK(1)
                .similarityThreshold(0.6)
                .similarityThresholdAll()
                .build();
        //查询
        List<Document> docs = vectorStore.similaritySearch(request);
        if (CollectionUtil.isEmpty(docs)) {
            log.info("====没有搜索到任何内容===");
            return;
        }
        log.info("====搜索到内容了===");
        for (Document doc : docs) {
            log.info(doc.getId());
            log.info(String.valueOf(doc.getScore()));
            log.info(doc.getText());
        }
    }
}
