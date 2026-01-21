package org.javaup.ai.structured;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 结构化输出示例 - 节目推荐结果
 * @author: 阿星不是程序员
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramRecommendation {
    
    /**
     * 推荐理由
     */
    private String reason;
    
    /**
     * 推荐节目列表
     */
    private List<RecommendedProgram> programs;
    
    /**
     * 用户偏好分析
     */
    private UserPreferenceAnalysis preferenceAnalysis;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedProgram {
        /**
         * 节目名称
         */
        private String name;
        
        /**
         * 艺人/演出者
         */
        private String artist;
        
        /**
         * 演出地点
         */
        private String location;
        
        /**
         * 演出时间
         */
        private String showTime;
        
        /**
         * 推荐指数（1-5）
         */
        private Integer recommendScore;
        
        /**
         * 推荐原因
         */
        private String recommendReason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferenceAnalysis {
        /**
         * 偏好类型
         */
        private List<String> preferredTypes;
        
        /**
         * 偏好地区
         */
        private List<String> preferredLocations;
        
        /**
         * 价格敏感度: LOW, MEDIUM, HIGH
         */
        private String priceSensitivity;
    }
}
