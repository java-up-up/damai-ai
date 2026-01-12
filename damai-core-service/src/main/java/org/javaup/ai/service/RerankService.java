package org.javaup.ai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: 大麦-ai智能服务项目
 * @description: Rerank重排序服务 - 对召回结果进行二次精排
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
public class RerankService {
    
    /**
     * 基于关键词重叠度的简单重排序
     * @param query 用户查询
     * @param documents 待排序文档列表
     * @param topK 返回数量
     * @return 重排序后的文档列表
     */
    public List<Document> rerank(String query, List<Document> documents, int topK) {
        if (documents == null || documents.isEmpty()) {
            return documents;
        }
        
        // 提取query关键词
        Set<String> queryKeywords = extractKeywords(query);
        
        // 计算每个文档的相关性分数
        List<ScoredDocument> scoredDocs = documents.stream()
            .map(doc -> {
                String docText = doc.getText();
                double score = (docText != null) ? computeRelevanceScore(queryKeywords, docText) : 0.0;
                return new ScoredDocument(doc, score);
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(topK)
            .collect(Collectors.toList());
        
        log.info("Rerank完成，原{}个文档，返回{}个", documents.size(), scoredDocs.size());
        
        return scoredDocs.stream()
            .map(ScoredDocument::getDocument)
            .collect(Collectors.toList());
    }
    
    /**
     * 使用LLM进行重排序（更精确但更慢）
     * @param query 用户查询
     * @param documents 待排序文档列表
     * @param chatClient 用于调用LLM的ChatClient
     * @param topK 返回数量
     * @return 重排序后的文档列表
     */
    public List<Document> rerankWithLLM(String query, List<Document> documents, 
                                         ChatClient chatClient, int topK) {
        if (documents == null || documents.size() <= 1) {
            return documents;
        }
        
        try {
            // 构建文档列表
            StringBuilder docList = new StringBuilder();
            for (int i = 0; i < documents.size(); i++) {
                String docText = documents.get(i).getText();
                if (docText != null) {
                    docList.append(String.format("[%d] %s\n", 
                        i + 1, 
                        docText.substring(0, Math.min(200, docText.length()))));
                }
            }
            
            String prompt = """
                请对以下文档按照与问题的相关性从高到低排序。
                
                问题：%s
                
                文档列表：
                %s
                
                请只返回排序后的文档编号（从1开始），用逗号分隔，如：3,1,2,5,4
                """.formatted(query, docList.toString());
            
            String result = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
            
            // 解析返回的排序结果
            List<Document> reranked = new ArrayList<>();
            String[] indices = result.replaceAll("[^0-9,]", "").split(",");
            for (String idx : indices) {
                try {
                    int index = Integer.parseInt(idx.trim()) - 1;
                    if (index >= 0 && index < documents.size()) {
                        reranked.add(documents.get(index));
                    }
                } catch (NumberFormatException ignored) {}
            }
            
            return reranked.isEmpty() ? documents.subList(0, Math.min(topK, documents.size())) 
                                       : reranked.subList(0, Math.min(topK, reranked.size()));
        } catch (Exception e) {
            log.warn("LLM Rerank失败，返回原始结果", e);
            return documents.subList(0, Math.min(topK, documents.size()));
        }
    }
    
    /**
     * 从文本中提取关键词
     */
    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.split("[\\s,，。？?！!]+"))
            .filter(s -> s.length() > 1)
            .collect(Collectors.toSet());
    }
    
    /**
     * 计算关键词与文档内容的相关性分数
     */
    private double computeRelevanceScore(Set<String> queryKeywords, String content) {
        if (queryKeywords.isEmpty()) return 0.0;
        
        long matchCount = queryKeywords.stream()
            .filter(content::contains)
            .count();
        
        // 归一化分数
        return (double) matchCount / queryKeywords.size();
    }
    
    /**
     * 带分数的文档包装类
     */
    @Data
    @AllArgsConstructor
    private static class ScoredDocument {
        private Document document;
        private double score;
    }
}
