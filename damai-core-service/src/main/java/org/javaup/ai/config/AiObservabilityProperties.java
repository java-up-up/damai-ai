package org.javaup.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: AI可观测性配置属性 - 从配置文件读取模型价格
 * @author: 阿星不是程序员
 **/
@Data
@Component
@ConfigurationProperties(prefix = "ai.observability")
public class AiObservabilityProperties {
    
    /**
     * 模型价格配置
     * key: 模型名称
     * value: 价格配置（input/output）
     */
    private Map<String, ModelPrice> pricing = new HashMap<>();
    
    @Data
    public static class ModelPrice {
        /**
         * 输入价格（元/1K tokens）
         */
        private BigDecimal input = new BigDecimal("0.001");
        
        /**
         * 输出价格（元/1K tokens）
         */
        private BigDecimal output = new BigDecimal("0.002");
    }
}
