package org.javaup.ai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料
 * @description: Query改写Advisor - 在检索前对用户问题进行优化，参考现有Advisor实现：{@link ChatTypeHistoryAdvisor} 
 * @author: 阿星不是程序员
 **/
@Slf4j
public class QueryRewriteAdvisor implements BaseAdvisor {
    
    private final int order;
    
    private final boolean enableLLMRewrite;
    
    private final ChatClient rewriteClient;  
    
    private static final Map<String, String> SYNONYM_MAP = new HashMap<>() {{
        put("退票", "退票 退款 取消订单");
        put("退款", "退款 退票 退钱");
        put("买票", "买票 购票 订票 下单");
        put("取消", "取消 作废 退订");
        put("演出", "演出 节目 表演 演唱会");
        put("门票", "门票 票 入场券");
    }};
    
    private QueryRewriteAdvisor(int order, boolean enableLLMRewrite, ChatClient rewriteClient) {
        this.order = order;
        this.enableLLMRewrite = enableLLMRewrite;
        this.rewriteClient = rewriteClient;
    }
    
    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String originalQuery = request.prompt().getUserMessage().getText();
        log.info("原始Query: {}", originalQuery);
        
        String enhancedQuery;
        if (enableLLMRewrite && rewriteClient != null) {
            enhancedQuery = llmRewrite(originalQuery);
        } else {
            enhancedQuery = ruleBasedExpand(originalQuery);
        }
        
        log.info("改写后Query: {}", enhancedQuery);
        
        return request;
    }
    
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    private String ruleBasedExpand(String query) {
        StringBuilder expanded = new StringBuilder(query);
        
        for (Map.Entry<String, String> entry : SYNONYM_MAP.entrySet()) {
            if (query.contains(entry.getKey())) {
                expanded.append(" ").append(entry.getValue());
            }
        }
        
        return expanded.toString();
    }
    
    private String llmRewrite(String originalQuery) {
        try {
            String prompt = """
                请将以下用户问题改写为更适合文档检索的形式，要求：
                1. 保持原意
                2. 扩展同义词（如：退票->退票、退款、取消订单）
                3. 补充可能的相关概念
                4. 只返回改写结果，不要其他内容
                
                原始问题：%s
                """.formatted(originalQuery);
            
            return rewriteClient.prompt()
                .user(prompt)
                .call()
                .content();
        } catch (Exception e) {
            log.warn("LLM改写失败，使用原始Query", e);
            return originalQuery;
        }
    }
    
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private int order = Ordered.HIGHEST_PRECEDENCE + 50;
        private boolean enableLLMRewrite = false;
        private ChatClient rewriteClient;
        
        public Builder order(int order) {
            this.order = order;
            return this;
        }
        
        public Builder enableLLMRewrite(boolean enable) {
            this.enableLLMRewrite = enable;
            return this;
        }
        
        public Builder rewriteClient(ChatClient client) {
            this.rewriteClient = client;
            return this;
        }
        
        public QueryRewriteAdvisor build() {
            return new QueryRewriteAdvisor(order, enableLLMRewrite, rewriteClient);
        }
    }
}