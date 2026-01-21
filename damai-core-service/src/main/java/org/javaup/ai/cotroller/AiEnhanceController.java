package org.javaup.ai.cotroller;

import jakarta.annotation.Resource;
import org.javaup.ai.common.ApiResponse;
import org.javaup.ai.entity.AiTrace;
import org.javaup.ai.service.AiObservabilityService;
import org.javaup.ai.vo.TokenStatisticsVo;
import org.javaup.ai.vo.TypeStatisticsVo;
import org.javaup.ai.structured.IntentRecognition;
import org.javaup.ai.structured.ProgramRecommendation;
import org.javaup.ai.structured.StructuredOutputService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: AI增强功能演示Controller - 展示Observability、Structured Output
 * @author: 阿星不是程序员
 **/
@RestController
@RequestMapping("/ai/enhance")
public class AiEnhanceController {
    
    @Resource
    private AiObservabilityService observabilityService;
    
    @Resource
    private StructuredOutputService structuredOutputService;
    
    @Resource
    private ChatClient chatClient;
    
    // ==================== Observability 可观测性功能演示 ====================
    
    /**
     * 获取今日AI调用统计
     */
    @GetMapping("/observability/today")
    public ApiResponse<TokenStatisticsVo> getTodayStats() {
        TokenStatisticsVo stats = observabilityService.getTodayStats();
        return ApiResponse.ok(stats);
    }
    
    /**
     * 获取指定会话的统计信息
     */
    @GetMapping("/observability/conversation")
    public ApiResponse<TokenStatisticsVo> getConversationStats(@RequestParam("conversationId") String conversationId) {
        TokenStatisticsVo stats = observabilityService.getConversationStats(conversationId);
        return ApiResponse.ok(stats);
    }
    
    /**
     * 获取最近的追踪记录列表
     */
    @GetMapping("/observability/traces")
    public ApiResponse<List<AiTrace>> getRecentTraces(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        List<AiTrace> traces = observabilityService.getRecentTraces(limit);
        return ApiResponse.ok(traces);
    }
    
    /**
     * 按请求类型统计（今日）
     */
    @GetMapping("/observability/stats/type")
    public ApiResponse<List<TypeStatisticsVo>> getStatsByType() {
        List<TypeStatisticsVo> stats = observabilityService.getStatsByRequestType();
        return ApiResponse.ok(stats);
    }
    
    // ==================== Structured Output 结构化输出功能演示 ====================
    
    /**
     * 意图识别 - 返回结构化的意图分析结果
     * 示例输入: "我想买下周六北京周杰伦演唱会的VIP票"
     */
    @PostMapping("/structured/intent")
    public ApiResponse<IntentRecognition> recognizeIntent(@RequestParam("input") String input) {
        IntentRecognition result = structuredOutputService.recognizeIntent(chatClient, input);
        return ApiResponse.ok(result);
    }
    
    /**
     * 节目推荐 - 返回结构化的推荐结果
     * 示例输入: "我喜欢流行音乐，最近想在上海看演出"
     */
    @PostMapping("/structured/recommend")
    public ApiResponse<ProgramRecommendation> recommendPrograms(@RequestParam("preference") String preference) {
        ProgramRecommendation result = structuredOutputService.recommendPrograms(chatClient, preference);
        return ApiResponse.ok(result);
    }
}
