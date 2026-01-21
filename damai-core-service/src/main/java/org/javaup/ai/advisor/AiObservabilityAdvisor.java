package org.javaup.ai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.javaup.ai.entity.AiTrace;
import org.javaup.ai.enums.ChatType;
import org.javaup.ai.service.AiObservabilityService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.core.Ordered;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 基于Spring AI的Advisor模式实现AI调用链追踪
 * @author: 阿星不是程序员
 **/
@Slf4j
public class AiObservabilityAdvisor implements BaseChatMemoryAdvisor {
    
    /** Advisor执行顺序 */
    private final int order;
    
    /** 可观测性服务，用于保存追踪记录和计算费用 */
    private final AiObservabilityService observabilityService;
    
    /** 模型名称，用于费用计算和记录 */
    private final String modelName;
    
    /** 请求类型，用于区分不同业务场景（如：贴心助手、运维助手、规则助手） */
    private final String requestType;
    
    /**
     * Context键名常量 - 用于在before和after之间通过context传递数据
     * 
     * <p>重要：由于响应式编程的特性，before和after可能在不同线程执行（如boundedElastic-1和boundedElastic-2），
     * 所以不能使用ThreadLocal，必须通过request/response的context传递数据。</p>
     */
    private static final String CTX_START_TIME = "observability_start_time";
    private static final String CTX_TRACE_ID = "observability_trace_id";
    private static final String CTX_USER_INPUT = "observability_user_input";
    
    private AiObservabilityAdvisor(int order, AiObservabilityService observabilityService, 
                                    String modelName, String requestType) {
        this.order = order;
        this.observabilityService = observabilityService;
        this.modelName = modelName;
        this.requestType = requestType;
    }
    
    /**
     * 请求前置处理 - 记录调用开始信息
     * 
     * <p>在AI调用之前执行，主要完成：</p>
     * <ol>
     *   <li>生成唯一的traceId用于追踪</li>
     *   <li>记录请求开始时间（用于计算延迟）</li>
     *   <li>保存用户输入内容（截断到500字符）</li>
     *   <li>将以上信息存入context，传递给after阶段</li>
     * </ol>
     * 
     * @param request AI调用请求
     * @param chain Advisor链
     * @return 增强后的请求（包含追踪上下文）
     */
    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        // 记录用户输入
        String userMessage = request.prompt().getUserMessage().getText();
        String traceId = observabilityService.generateTraceId();
        
        log.debug("AI调用开始 - traceId: {}, input: {}", traceId, userMessage);
        
        // 通过context传递数据（跨线程安全）
        Map<String, Object> newContext = new HashMap<>(request.context());
        newContext.put(CTX_START_TIME, System.currentTimeMillis());
        newContext.put(CTX_TRACE_ID, traceId);
        newContext.put(CTX_USER_INPUT, truncate(userMessage, 500));
        
        return ChatClientRequest.builder()
                .prompt(request.prompt())
                .context(newContext)
                .build();
    }
    
    /**
     * 响应后置处理 - 采集调用指标并保存
     * 
     * <p>在AI调用完成后执行，主要完成：</p>
     * <ol>
     *   <li>从context获取before阶段存入的数据</li>
     *   <li>计算响应延迟（当前时间 - 开始时间）</li>
     *   <li>从ChatResponse中提取Token使用信息</li>
     *   <li>根据模型价格计算预估费用</li>
     *   <li>构建AiTrace实体并异步保存到数据库</li>
     * </ol>
     * 
     * @param response AI调用响应
     * @param chain Advisor链
     * @return 原始响应（不做修改）
     */
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        // 从context获取数据
        Map<String, Object> context = response.context();
        Long startTime = (Long) context.get(CTX_START_TIME);
        String traceId = (String) context.get(CTX_TRACE_ID);
        String userInput = (String) context.get(CTX_USER_INPUT);
        
        // 防御性检查
        if (startTime == null || traceId == null) {
            log.warn("Observability context数据丢失，跳过记录");
            return response;
        }
        
        long latencyMs = System.currentTimeMillis() - startTime;
        String conversationId = getConversationId(context, ChatMemory.DEFAULT_CONVERSATION_ID);
        
        try {
            ChatResponse chatResponse = response.chatResponse();
            
            // 构建追踪记录
            AiTrace trace = new AiTrace();
            trace.setTraceId(traceId);
            trace.setConversationId(conversationId);
            trace.setModelName(modelName);
            trace.setRequestType(requestType);
            trace.setLatencyMs(latencyMs);
            trace.setUserInput(userInput);
            trace.setSuccess(true);
            
            // 提取Token使用信息
            if (chatResponse != null && chatResponse.getMetadata() != null && 
                    chatResponse.getMetadata().getUsage() != null) {
                Usage usage = chatResponse.getMetadata().getUsage();
                int promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
                int completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
                
                trace.setPromptTokens(promptTokens);
                trace.setCompletionTokens(completionTokens);
                trace.setTotalTokens(promptTokens + completionTokens);
                
                // 计算费用
                BigDecimal cost = observabilityService.calculateCost(modelName, promptTokens, completionTokens);
                trace.setEstimatedCost(cost);
                
                log.info("AI调用完成 - traceId: {}, latency: {}ms, tokens: {}/{}/{}, cost: ¥{}", 
                        traceId, latencyMs, promptTokens, completionTokens, 
                        promptTokens + completionTokens, cost);
            }
            
            // 记录AI输出（流式场景下getText()为空，这是正常的）
            if (chatResponse != null && chatResponse.getResult() != null && 
                    chatResponse.getResult().getOutput() != null) {
                String aiOutput = chatResponse.getResult().getOutput().getText();
                // 流式输出时getText()为空，标记为流式响应
                if (aiOutput == null || aiOutput.isEmpty()) {
                    trace.setAiOutput("[流式输出]");
                } else {
                    trace.setAiOutput(truncate(aiOutput, 1000));
                }
            }
            
            // 异步保存
            observabilityService.saveTraceAsync(trace);
            
        } catch (Exception e) {
            log.error("记录AI调用追踪信息失败", e);
        }
        
        return response;
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    public static Builder builder(AiObservabilityService observabilityService) {
        return new Builder(observabilityService);
    }
    
    public static final class Builder {
        private int order = Ordered.LOWEST_PRECEDENCE;
        private final AiObservabilityService observabilityService;
        private String modelName = "unknown";
        private String requestType = ChatType.CHAT.getMsg();
        
        private Builder(AiObservabilityService observabilityService) {
            this.observabilityService = observabilityService;
        }
        
        public Builder order(int order) {
            this.order = order;
            return this;
        }
        
        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }
        
        public Builder requestType(String requestType) {
            this.requestType = requestType;
            return this;
        }
        
        public AiObservabilityAdvisor build() {
            return new AiObservabilityAdvisor(order, observabilityService, modelName, requestType);
        }
    }
}
