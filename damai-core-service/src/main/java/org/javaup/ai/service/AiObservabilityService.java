package org.javaup.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.javaup.ai.config.AiObservabilityProperties;
import org.javaup.ai.entity.AiTrace;
import org.javaup.ai.mapper.AiTraceMapper;
import org.javaup.ai.vo.TokenStatisticsVo;
import org.javaup.ai.vo.TypeStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 提供AI调用追踪数据的存储与统计分析
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
public class AiObservabilityService {
    
    @Autowired
    private AiTraceMapper aiTraceMapper;
    
    @Autowired
    private AiObservabilityProperties properties;
    
    /** 默认输入Token价格（元/千Token），未配置模型时使用 */
    private static final BigDecimal DEFAULT_INPUT_PRICE = new BigDecimal("0.001");
    
    /** 默认输出Token价格（元/千Token），未配置模型时使用 */
    private static final BigDecimal DEFAULT_OUTPUT_PRICE = new BigDecimal("0.002");
    
    /**
     * 生成追踪ID
     * 
     * <p>生成16位随机字符串作为traceId，用于标识单次AI调用。</p>
     * 
     * @return 16位唯一追踪ID
     */
    public String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 异步保存追踪记录
     * 
     * <p>使用CompletableFuture异步执行保存操作，避免阻塞主流程。
     * 即使保存失败也不会影响AI调用的正常响应。</p>
     * 
     * @param trace 追踪记录实体
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
     * 
     * <p>根据模型名称从配置文件获取价格，计算本次调用的预估费用。
     * 如果模型未配置价格，则使用默认价格。</p>
     * 
     * <p>计算公式：费用 = (输入Token × 输入价格 + 输出Token × 输出价格) / 1000</p>
     * 
     * @param modelName 模型名称
     * @param promptTokens 输入Token数
     * @param completionTokens 输出Token数
     * @return 预估费用（单位：元）
     */
    public BigDecimal calculateCost(String modelName, int promptTokens, int completionTokens) {
        BigDecimal inputPrice = DEFAULT_INPUT_PRICE;
        BigDecimal outputPrice = DEFAULT_OUTPUT_PRICE;
        
        // 从配置读取价格
        AiObservabilityProperties.ModelPrice modelPrice = properties.getPricing().get(modelName);
        if (modelPrice != null) {
            inputPrice = modelPrice.getInput();
            outputPrice = modelPrice.getOutput();
        }
        
        BigDecimal inputCost = inputPrice.multiply(new BigDecimal(promptTokens))
                .divide(new BigDecimal(1000), 6, RoundingMode.HALF_UP);
        BigDecimal outputCost = outputPrice.multiply(new BigDecimal(completionTokens))
                .divide(new BigDecimal(1000), 6, RoundingMode.HALF_UP);
        
        return inputCost.add(outputCost);
    }
    
    /**
     * 获取指定会话的Token统计信息
     * 
     * <p>汇总指定会话下所有AI调用的统计数据，包括：</p>
     * <ul>
     *   <li>调用次数</li>
     *   <li>Token消耗（输入/输出/总计）</li>
     *   <li>延迟统计（总延迟/平均延迟）</li>
     *   <li>成功率</li>
     *   <li>累计费用</li>
     * </ul>
     * 
     * @param conversationId 会话ID
     * @return 会话统计信息
     */
    public TokenStatisticsVo getConversationStats(String conversationId) {
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
        
        return TokenStatisticsVo.builder()
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
     * 获取今日全局统计
     * 
     * <p>统计今日（0点至今）所有AI调用的汇总数据，用于监控大盘展示。</p>
     * 
     * @return 今日统计信息
     */
    public TokenStatisticsVo getTodayStats() {
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
        
        return TokenStatisticsVo.builder()
                .totalCalls(totalCalls)
                .totalPromptTokens(totalPromptTokens)
                .totalCompletionTokens(totalCompletionTokens)
                .totalTokens(totalPromptTokens + totalCompletionTokens)
                .totalCost(totalCost)
                .build();
    }
    
    /**
     * 获取指定会话的追踪详情列表
     * 
     * <p>查询指定会话下的所有AI调用记录，按时间倒序排列。</p>
     * 
     * @param conversationId 会话ID
     * @return 追踪记录列表
     */
    public List<AiTrace> getTracesByConversation(String conversationId) {
        LambdaQueryWrapper<AiTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiTrace::getConversationId, conversationId)
               .eq(AiTrace::getStatus, 1)
               .orderByDesc(AiTrace::getCreateTime);
        return aiTraceMapper.selectList(wrapper);
    }
    
    /**
     * 获取最近的追踪记录列表
     * 
     * <p>查询最近N条AI调用记录，用于监控页面展示实时调用情况。</p>
     * 
     * @param limit 返回记录数量上限
     * @return 追踪记录列表（按时间倒序）
     */
    public List<AiTrace> getRecentTraces(int limit) {
        LambdaQueryWrapper<AiTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiTrace::getStatus, 1)
               .orderByDesc(AiTrace::getCreateTime)
               .last("LIMIT " + limit);
        return aiTraceMapper.selectList(wrapper);
    }
    
    /**
     * 按请求类型统计今日数据
     * 
     * <p>将今日的AI调用按requestType（如：贴心助手、运维助手、规则助手）分组统计，
     * 便于了解各业务场景的资源消耗情况。</p>
     * 
     * @return 按类型分组的统计列表
     */
    public List<TypeStatisticsVo> getStatsByRequestType() {
        LambdaQueryWrapper<AiTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiTrace::getStatus, 1)
               .ge(AiTrace::getCreateTime, getTodayStart());
        
        List<AiTrace> traces = aiTraceMapper.selectList(wrapper);
        
        // 按requestType分组统计
        return traces.stream()
                .collect(Collectors.groupingBy(
                        trace -> trace.getRequestType() != null ? trace.getRequestType() : "UNKNOWN"))
                .entrySet().stream()
                .map(entry -> {
                    String type = entry.getKey();
                    List<AiTrace> typeTraces = entry.getValue();
                    int calls = typeTraces.size();
                    int tokens = typeTraces.stream()
                            .mapToInt(t -> t.getTotalTokens() != null ? t.getTotalTokens() : 0)
                            .sum();
                    BigDecimal cost = typeTraces.stream()
                            .map(t -> t.getEstimatedCost() != null ? t.getEstimatedCost() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new TypeStatisticsVo(type, calls, tokens, cost);
                })
                .toList();
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
