package org.javaup.ai.observability;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.javaup.ai.entity.AiTrace;
import org.javaup.ai.mapper.AiTraceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: AI可观测性服务 - 提供调用追踪、Token统计、费用计算
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
public class AiObservabilityService {
    
    @Autowired
    private AiTraceMapper aiTraceMapper;
    
    /**
     * 模型价格配置（单位：元/1K tokens）
     */
    private static final Map<String, BigDecimal[]> MODEL_PRICING = new HashMap<>() {{
        // [输入价格, 输出价格]
        put("deepseek-chat", new BigDecimal[]{new BigDecimal("0.001"), new BigDecimal("0.002")});
        put("qwen-max-latest", new BigDecimal[]{new BigDecimal("0.02"), new BigDecimal("0.06")});
        put("gpt-4", new BigDecimal[]{new BigDecimal("0.03"), new BigDecimal("0.06")});
        put("gpt-3.5-turbo", new BigDecimal[]{new BigDecimal("0.0015"), new BigDecimal("0.002")});
    }};
    
    /**
     * 生成追踪ID
     */
    public String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 异步保存追踪记录
     */
    public void saveTraceAsync(AiTrace trace) {
        CompletableFuture.runAsync(() -> {
            try {
                trace.setCreateTime(new Date());
                trace.setEditTime(new Date());
                trace.setStatus(1);
                aiTraceMapper.insert(trace);
                log.info("AI调用追踪记录已保存: traceId={}, tokens={}", 
                        trace.getTraceId(), trace.getTotalTokens());
            } catch (Exception e) {
                log.error("保存AI调用追踪记录失败", e);
            }
        });
    }
    
    /**
     * 计算预估费用
     */
    public BigDecimal calculateCost(String modelName, int promptTokens, int completionTokens) {
        BigDecimal[] pricing = MODEL_PRICING.getOrDefault(modelName, 
                new BigDecimal[]{new BigDecimal("0.001"), new BigDecimal("0.002")});
        
        BigDecimal inputCost = pricing[0].multiply(new BigDecimal(promptTokens)).divide(new BigDecimal(1000), 6, RoundingMode.HALF_UP);
        BigDecimal outputCost = pricing[1].multiply(new BigDecimal(completionTokens)).divide(new BigDecimal(1000), 6, RoundingMode.HALF_UP);
        
        return inputCost.add(outputCost);
    }
    
    /**
     * 获取会话的Token统计
     */
    public TokenStatistics getConversationStats(String conversationId) {
        LambdaQueryWrapper<AiTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiTrace::getConversationId, conversationId)
               .eq(AiTrace::getStatus, 1);
        
        List<AiTrace> traces = aiTraceMapper.selectList(wrapper);
        
        int totalPromptTokens = 0;
        int totalCompletionTokens = 0;
        int totalCalls = traces.size();
        long totalLatency = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        int successCount = 0;
        
        for (AiTrace trace : traces) {
            if (trace.getPromptTokens() != null) {
                totalPromptTokens += trace.getPromptTokens();
            }
            if (trace.getCompletionTokens() != null) {
                totalCompletionTokens += trace.getCompletionTokens();
            }
            if (trace.getLatencyMs() != null) {
                totalLatency += trace.getLatencyMs();
            }
            if (trace.getEstimatedCost() != null) {
                totalCost = totalCost.add(trace.getEstimatedCost());
            }
            if (Boolean.TRUE.equals(trace.getSuccess())) {
                successCount++;
            }
        }
        
        return TokenStatistics.builder()
                .conversationId(conversationId)
                .totalCalls(totalCalls)
                .totalPromptTokens(totalPromptTokens)
                .totalCompletionTokens(totalCompletionTokens)
                .totalTokens(totalPromptTokens + totalCompletionTokens)
                .totalLatencyMs(totalLatency)
                .avgLatencyMs(totalCalls > 0 ? totalLatency / totalCalls : 0)
                .totalCost(totalCost)
                .successRate(totalCalls > 0 ? (double) successCount / totalCalls * 100 : 0)
                .build();
    }
    
    /**
     * 获取全局统计（今日）
     */
    public TokenStatistics getTodayStats() {
        LambdaQueryWrapper<AiTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiTrace::getStatus, 1)
               .ge(AiTrace::getCreateTime, getTodayStart());
        
        List<AiTrace> traces = aiTraceMapper.selectList(wrapper);
        
        int totalPromptTokens = 0;
        int totalCompletionTokens = 0;
        int totalCalls = traces.size();
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (AiTrace trace : traces) {
            if (trace.getPromptTokens() != null) {
                totalPromptTokens += trace.getPromptTokens();
            }
            if (trace.getCompletionTokens() != null) {
                totalCompletionTokens += trace.getCompletionTokens();
            }
            if (trace.getEstimatedCost() != null) {
                totalCost = totalCost.add(trace.getEstimatedCost());
            }
        }
        
        return TokenStatistics.builder()
                .totalCalls(totalCalls)
                .totalPromptTokens(totalPromptTokens)
                .totalCompletionTokens(totalCompletionTokens)
                .totalTokens(totalPromptTokens + totalCompletionTokens)
                .totalCost(totalCost)
                .build();
    }
    
    /**
     * 获取追踪详情
     */
    public List<AiTrace> getTracesByConversation(String conversationId) {
        LambdaQueryWrapper<AiTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiTrace::getConversationId, conversationId)
               .eq(AiTrace::getStatus, 1)
               .orderByDesc(AiTrace::getCreateTime);
        return aiTraceMapper.selectList(wrapper);
    }
    
    private Date getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
