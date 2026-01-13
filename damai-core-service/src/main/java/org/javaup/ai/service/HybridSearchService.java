package org.javaup.ai.service;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料
 * @description: 混合检索服务 - 结合向量检索和关键词检索提高召回率
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
public class HybridSearchService {
    
    @Autowired
    private VectorStore vectorStore;
    
    @Autowired
    private RerankService rerankService;
    
    private final Map<String, Document> documentCache = new HashMap<>();
    
    public void cacheDocuments(List<Document> documents) {
        for (Document doc : documents) {
            documentCache.put(doc.getId(), doc);
        }
        log.info("已缓存 {} 个文档用于关键词检索", documents.size());
    }
    
    /**
     * 混合检索入口
     * @param query 用户查询
     * @param topK 返回结果数量
     * @return 融合后的文档列表
     */
    public List<Document> hybridSearch(String query, int topK) {
        return hybridSearch(query, topK, true);
    }
    
    /**
     * 混合检索入口（可控制是否启用Rerank）
     * @param query 用户查询
     * @param topK 返回结果数量
     * @param enableRerank 是否启用Rerank精排
     * @return 融合后的文档列表
     */
    public List<Document> hybridSearch(String query, int topK, boolean enableRerank) {
      
        List<Document> vectorResults = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.2)
                .build()
        );
        if (vectorResults != null) {
            log.info("向量检索返回 {} 个结果", vectorResults.size());
        }
        List<Document> keywordResults = keywordSearch(query, topK);
        log.info("关键词检索返回 {} 个结果", keywordResults.size());
        
        List<Document> merged = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(vectorResults)) {
            merged = mergeWithRRF(vectorResults, keywordResults, topK * 2);
        }
        if (merged != null) {
            log.info("RRF融合后返回 {} 个结果", merged.size());
        }
        
        if (enableRerank && CollectionUtil.isNotEmpty(merged)) {
            List<Document> reranked = rerankService.rerank(query, merged, topK);
            log.info("Rerank精排后返回 {} 个结果", reranked.size());
            return reranked;
        }
        
        return merged.size() > topK ? merged.subList(0, topK) : merged;
    }
    
    private List<Document> keywordSearch(String query, int topK) {
      
        String[] keywords = query.split("[\\s,，。？?！!]+");
        
        return documentCache.values().stream()
            .map(doc -> {
                String docText = doc.getText();
                if (docText == null) {
                    return new AbstractMap.SimpleEntry<>(doc, 0L);
                }
                long matchCount = Arrays.stream(keywords)
                    .filter(kw -> kw.length() > 1 && docText.contains(kw))
                    .count();
                return new AbstractMap.SimpleEntry<>(doc, matchCount);
            })
            .filter(e -> e.getValue() > 0)
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(topK)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    private List<Document> mergeWithRRF(
            List<Document> vectorResults, 
            List<Document> keywordResults, 
            int topK) {
        
        Map<String, Double> scoreMap = new HashMap<>(vectorResults.size());
        Map<String, Document> docMap = new HashMap<>(vectorResults.size());
        int k = 60; 
        
        for (int i = 0; i < vectorResults.size(); i++) {
            Document doc = vectorResults.get(i);
            String id = doc.getId();
            scoreMap.merge(id, 1.0 / (k + i + 1), Double::sum);
            docMap.put(id, doc);
        }
        
        for (int i = 0; i < keywordResults.size(); i++) {
            Document doc = keywordResults.get(i);
            String id = doc.getId();
            scoreMap.merge(id, 1.0 / (k + i + 1), Double::sum);
            docMap.put(id, doc);
        }
        
        return scoreMap.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(topK)
            .map(e -> docMap.get(e.getKey()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
