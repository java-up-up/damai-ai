package org.javaup.ai.structured;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 结构化输出示例 - 意图识别结果
 * @author: 阿星不是程序员
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentRecognition {
    
    /**
     * 主要意图: QUERY_PROGRAM, BUY_TICKET, CHECK_ORDER, REFUND, CONSULT, OTHER
     */
    private String primaryIntent;
    
    /**
     * 意图置信度（0-1）
     */
    private Double confidence;
    
    /**
     * 识别出的实体
     */
    private List<RecognizedEntity> entities;
    
    /**
     * 是否需要进一步澄清
     */
    private Boolean needsClarification;
    
    /**
     * 澄清问题（如果需要）
     */
    private String clarificationQuestion;
    
    /**
     * 情感分析
     */
    private SentimentAnalysis sentiment;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecognizedEntity {
        /**
         * 实体类型: ARTIST, LOCATION, DATE, PRICE, TICKET_TYPE
         */
        private String type;
        
        /**
         * 实体值
         */
        private String value;
        
        /**
         * 原始文本
         */
        private String originalText;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentAnalysis {
        /**
         * 情感倾向: POSITIVE, NEUTRAL, NEGATIVE
         */
        private String sentiment;
        
        /**
         * 情感强度（0-1）
         */
        private Double intensity;
        
        /**
         * 是否紧急
         */
        private Boolean isUrgent;
    }
}
