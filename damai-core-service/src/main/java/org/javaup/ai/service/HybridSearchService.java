package org.javaup.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: 大麦-ai智能服务项目
 * @description: 混合检索服务 - 结合向量检索和关键词检索提高召回率
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
public class HybridSearchService {
    
    @Autowired
    private VectorStore vectorStore;
    
    // 文档缓存（简化版，生产环境建议用ES或其他存储）
    private final Map<String, Document> documentCache = new HashMap<>();
    
    /**
     * 缓存文档（在加载文档时调用）
     */
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
        // 1. 向量检索
        List<Document> vectorResults = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.2)
                .build()
        );
        log.info("向量检索返回 {} 个结果", vectorResults.size());
        
        // 2. 关键词检索（BM25简化版）
        List<Document> keywordResults = keywordSearch(query, topK);
        log.info("关键词检索返回 {} 个结果", keywordResults.size());
        
        // 3. RRF融合
        List<Document> merged = mergeWithRRF(vectorResults, keywordResults, topK);
        log.info("RRF融合后返回 {} 个结果", merged.size());
        
        return merged;
    }
    
    /**
     * 简化版关键词检索（基于字符串匹配）
     */
    private List<Document> keywordSearch(String query, int topK) {
        // 提取查询关键词
        String[] keywords = query.split("[\\s,，。？?！!]+");
        
        return documentCache.values().stream()
            .map(doc -> {
                // 计算关键词匹配分数
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
    
    /**
     * RRF融合算法（Reciprocal Rank Fusion）
     * 公式：score = Σ 1/(k + rank_i)
     */
    private List<Document> mergeWithRRF(
            List<Document> vectorResults, 
            List<Document> keywordResults, 
            int topK) {
        
        Map<String, Double> scoreMap = new HashMap<>();
        Map<String, Document> docMap = new HashMap<>();
        int k = 60; // RRF常数
        
        // 计算向量检索结果的分数
        for (int i = 0; i < vectorResults.size(); i++) {
            Document doc = vectorResults.get(i);
            String id = doc.getId();
            scoreMap.merge(id, 1.0 / (k + i + 1), Double::sum);
            docMap.put(id, doc);
        }
        
        // 计算关键词检索结果的分数
        for (int i = 0; i < keywordResults.size(); i++) {
            Document doc = keywordResults.get(i);
            String id = doc.getId();
            scoreMap.merge(id, 1.0 / (k + i + 1), Double::sum);
            docMap.put(id, doc);
        }
        
        // 按融合分数排序返回topK
        return scoreMap.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(topK)
            .map(e -> docMap.get(e.getKey()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
