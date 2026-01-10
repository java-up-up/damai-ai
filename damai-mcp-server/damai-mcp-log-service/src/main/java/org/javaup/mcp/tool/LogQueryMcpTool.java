package org.javaup.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.javaup.mcp.entity.LogDocument;
import org.javaup.mcp.mapper.LogMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 日志查询MCP工具类 - 通过 ES 查询微服务日志，支持 traceId 链路追踪
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LogQueryMcpTool {

    private final LogMapper logMapper;
    
    private final ObjectMapper objectMapper;
    
    /**
     * 获取可用的服务列表
     */
    @Tool(description = "获取大麦系统中所有可用的微服务列表")
    public ToolResult getServiceList() {
        List<String> serviceList = getServiceListFromEs();
        
        Map<String, Object> data = new HashMap<>();
        data.put("服务列表", serviceList);
        data.put("服务数量", serviceList.size());
        return ToolResult.success("获取服务列表成功", data);
    }
    
    /**
     * 从 ES 中获取服务列表（使用 DSL 聚合查询）
     */
    private List<String> getServiceListFromEs() {
        try {
            String dsl = "{" +
                    "\"size\": 0," +
                    "\"aggs\": {" +
                    "  \"service_names\": {" +
                    "    \"terms\": {" +
                    "      \"field\": \"projectName.keyword\"," +
                    "      \"size\": 100" +
                    "    }" +
                    "  }" +
                    "}" +
                    "}";
            String jsonResult = logMapper.executeDSL(dsl);
            return parseServiceListFromJson(jsonResult);
        } catch (Exception e) {
            log.error("DSL查询服务列表失败，使用默认列表", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 从 JSON 结果中解析服务列表
     */
    private List<String> parseServiceListFromJson(String jsonResult) {
        List<String> serviceList = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResult);
            JsonNode buckets = root.path("aggregations").path("service_names").path("buckets");
            if (buckets.isArray()) {
                for (JsonNode bucket : buckets) {
                    String key = bucket.path("key").asText();
                    if (key != null && !key.isEmpty()) {
                        serviceList.add(key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析服务列表JSON失败", e);
        }
        Collections.sort(serviceList);
        return serviceList;
    }

    /**
     * 按关键词搜索日志
     */
    @Tool(description = "根据关键词搜索日志内容，支持模糊匹配日志消息")
    public ToolResult searchLogsByKeyword(
            @ToolParam(description = "搜索关键词，用于匹配日志消息内容") String keyword,
            @ToolParam(description = "服务名称，可选。如：gateway-service、order-service等", required = false) String serviceName,
            @ToolParam(description = "日志级别，可选。如：INFO、WARN、ERROR、DEBUG", required = false) String level,
            @ToolParam(description = "返回的日志条数，默认20条", required = false) Integer size) {

        try {
            int limit = (size != null && size > 0) ? Math.min(size, 100) : 20;

            LambdaEsQueryWrapper<LogDocument> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.match(LogDocument::getMessage, keyword);

            if (serviceName != null && !serviceName.isEmpty()) {
                wrapper.eq(LogDocument::getProjectName, serviceName);
            }
            if (level != null && !level.isEmpty()) {
                wrapper.eq(LogDocument::getLevel, level.toUpperCase());
            }

            wrapper.orderByDesc(LogDocument::getTimestamp);
            wrapper.limit(limit);

            List<LogDocument> logs = logMapper.selectList(wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("查询条件", buildQueryDesc(keyword, serviceName, level));
            data.put("日志数量", logs.size());
            data.put("日志列表", formatLogs(logs));

            return ToolResult.success("日志搜索成功", data);
        } catch (Exception e) {
            log.error("日志搜索失败", e);
            return ToolResult.error("日志搜索失败: " + e.getMessage());
        }
    }

    /**
     * 通过 traceId 查询调用链路日志
     */
    @Tool(description = "通过traceId查询完整的调用链路日志，串联所有微服务的日志记录，用于问题排查和链路追踪")
    public ToolResult getLogsByTraceId(
            @ToolParam(description = "链路追踪ID（traceId）") String traceId) {

        try {
            if (traceId == null || traceId.isEmpty() || "-".equals(traceId)) {
                return ToolResult.error("请提供有效的traceId");
            }

            LambdaEsQueryWrapper<LogDocument> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(LogDocument::getTraceId, traceId);
            wrapper.orderByAsc(LogDocument::getTimeMillis);
            wrapper.limit(200);

            List<LogDocument> logs = logMapper.selectList(wrapper);

            if (logs.isEmpty()) {
                return ToolResult.error("未找到traceId为 " + traceId + " 的日志记录");
            }

            // 按服务分组
            Map<String, List<Map<String, Object>>> logsByService = logs.stream()
                    .collect(Collectors.groupingBy(
                            LogDocument::getProjectName,
                            LinkedHashMap::new,
                            Collectors.mapping(this::formatLog, Collectors.toList())
                    ));

            // 获取涉及的服务调用顺序
            List<String> serviceOrder = logs.stream()
                    .map(LogDocument::getProjectName)
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("traceId", traceId);
            data.put("日志总数", logs.size());
            data.put("涉及服务", serviceOrder);
            data.put("调用链路", logsByService);

            return ToolResult.success("链路日志查询成功", data);
        } catch (Exception e) {
            log.error("链路日志查询失败", e);
            return ToolResult.error("链路日志查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定服务的最新日志
     */
    @Tool(description = "查询指定微服务的最新日志记录")
    public ToolResult getLatestLogs(
            @ToolParam(description = "服务名称，如：gateway-service、order-service、user-service等") String serviceName,
            @ToolParam(description = "日志级别，可选。如：INFO、WARN、ERROR、DEBUG", required = false) String level,
            @ToolParam(description = "返回的日志条数，默认20条", required = false) Integer size) {

        try {
            int limit = (size != null && size > 0) ? Math.min(size, 100) : 20;

            LambdaEsQueryWrapper<LogDocument> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(LogDocument::getProjectName, serviceName);

            if (level != null && !level.isEmpty()) {
                wrapper.eq(LogDocument::getLevel, level.toUpperCase());
            }

            wrapper.orderByDesc(LogDocument::getTimestamp);
            wrapper.limit(limit);

            List<LogDocument> logs = logMapper.selectList(wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("服务名称", serviceName);
            data.put("日志级别", level != null ? level : "全部");
            data.put("日志数量", logs.size());
            data.put("日志列表", formatLogs(logs));

            return ToolResult.success("日志查询成功", data);
        } catch (Exception e) {
            log.error("日志查询失败", e);
            return ToolResult.error("日志查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询错误日志
     */
    @Tool(description = "查询系统中的错误日志（ERROR级别），可指定服务和时间范围")
    public ToolResult getErrorLogs(
            @ToolParam(description = "服务名称，可选。不填则查询所有服务的错误日志", required = false) String serviceName,
            @ToolParam(description = "返回的日志条数，默认30条", required = false) Integer size) {

        try {
            int limit = (size != null && size > 0) ? Math.min(size, 100) : 30;

            LambdaEsQueryWrapper<LogDocument> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(LogDocument::getLevel, "ERROR");

            if (serviceName != null && !serviceName.isEmpty()) {
                wrapper.eq(LogDocument::getProjectName, serviceName);
            }

            wrapper.orderByDesc(LogDocument::getTimestamp);
            wrapper.limit(limit);

            List<LogDocument> logs = logMapper.selectList(wrapper);

            // 按服务分组统计错误数
            Map<String, Long> errorCountByService = logs.stream()
                    .collect(Collectors.groupingBy(LogDocument::getProjectName, Collectors.counting()));

            Map<String, Object> data = new HashMap<>();
            data.put("查询范围", serviceName != null ? serviceName : "全部服务");
            data.put("错误日志数量", logs.size());
            data.put("各服务错误数", errorCountByService);
            data.put("错误日志列表", formatLogs(logs));

            return ToolResult.success("错误日志查询成功", data);
        } catch (Exception e) {
            log.error("错误日志查询失败", e);
            return ToolResult.error("错误日志查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询警告日志
     */
    @Tool(description = "查询系统中的警告日志（WARN级别）")
    public ToolResult getWarnLogs(
            @ToolParam(description = "服务名称，可选。不填则查询所有服务的警告日志", required = false) String serviceName,
            @ToolParam(description = "返回的日志条数，默认30条", required = false) Integer size) {

        try {
            int limit = (size != null && size > 0) ? Math.min(size, 100) : 30;

            LambdaEsQueryWrapper<LogDocument> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(LogDocument::getLevel, "WARN");

            if (serviceName != null && !serviceName.isEmpty()) {
                wrapper.eq(LogDocument::getProjectName, serviceName);
            }

            wrapper.orderByDesc(LogDocument::getTimestamp);
            wrapper.limit(limit);

            List<LogDocument> logs = logMapper.selectList(wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("查询范围", serviceName != null ? serviceName : "全部服务");
            data.put("警告日志数量", logs.size());
            data.put("警告日志列表", formatLogs(logs));

            return ToolResult.success("警告日志查询成功", data);
        } catch (Exception e) {
            log.error("警告日志查询失败", e);
            return ToolResult.error("警告日志查询失败: " + e.getMessage());
        }
    }

    /**
     * 日志统计概览
     */
    @Tool(description = "获取各微服务的日志统计概览，包括各级别日志的数量分布")
    public ToolResult getLogStatistics(
            @ToolParam(description = "服务名称，可选。不填则统计所有服务", required = false) String serviceName) {

        try {
            List<String> levels = Arrays.asList("ERROR", "WARN", "INFO", "DEBUG");
            
            // 动态获取服务列表
            List<String> services;
            if (serviceName != null && !serviceName.isEmpty()) {
                services = Collections.singletonList(serviceName);
            } else {
                // 使用原生 ES 客户端聚合查询获取服务列表
                services = getServiceListFromEs();
            }

            Map<String, Map<String, Long>> statistics = new LinkedHashMap<>();

            for (String service : services) {
                Map<String, Long> levelCounts = new LinkedHashMap<>();
                for (String level : levels) {
                    LambdaEsQueryWrapper<LogDocument> wrapper = new LambdaEsQueryWrapper<>();
                    wrapper.eq(LogDocument::getProjectName, service);
                    wrapper.eq(LogDocument::getLevel, level);
                    Long count = logMapper.selectCount(wrapper);
                    levelCounts.put(level, count);
                }
                statistics.put(service, levelCounts);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("统计范围", serviceName != null ? serviceName : "全部服务");
            data.put("日志统计", statistics);

            return ToolResult.success("日志统计成功", data);
        } catch (Exception e) {
            log.error("日志统计失败", e);
            return ToolResult.error("日志统计失败: " + e.getMessage());
        }
    }

    /**
     * 按类名或方法名搜索日志
     */
    @Tool(description = "根据类名或方法名搜索日志，用于定位特定代码位置的日志")
    public ToolResult searchLogsByClass(
            @ToolParam(description = "类名，支持模糊匹配，如：UserController、OrderService") String className,
            @ToolParam(description = "方法名，可选", required = false) String methodName,
            @ToolParam(description = "返回的日志条数，默认20条", required = false) Integer size) {

        try {
            int limit = (size != null && size > 0) ? Math.min(size, 100) : 20;

            LambdaEsQueryWrapper<LogDocument> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.match(LogDocument::getSourceClass, className);

            if (methodName != null && !methodName.isEmpty()) {
                wrapper.eq(LogDocument::getSourceMethod, methodName);
            }

            wrapper.orderByDesc(LogDocument::getTimestamp);
            wrapper.limit(limit);

            List<LogDocument> logs = logMapper.selectList(wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("查询类名", className);
            data.put("查询方法名", methodName != null ? methodName : "不限");
            data.put("日志数量", logs.size());
            data.put("日志列表", formatLogs(logs));

            return ToolResult.success("日志搜索成功", data);
        } catch (Exception e) {
            log.error("日志搜索失败", e);
            return ToolResult.error("日志搜索失败: " + e.getMessage());
        }
    }

    /**
     * 格式化日志列表
     */
    private List<Map<String, Object>> formatLogs(List<LogDocument> logs) {
        return logs.stream().map(this::formatLog).collect(Collectors.toList());
    }

    /**
     * 格式化单条日志
     */
    private Map<String, Object> formatLog(LogDocument log) {
        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put("时间", log.getTimestamp());
        formatted.put("服务", log.getProjectName());
        formatted.put("级别", log.getLevel());
        formatted.put("消息", log.getMessage());
        formatted.put("traceId", log.getTraceId());
        formatted.put("类", log.getSourceClass());
        formatted.put("方法", log.getSourceMethod());
        formatted.put("行号", log.getSourceLine());
        formatted.put("线程", log.getThread());
        return formatted;
    }

    /**
     * 构建查询描述
     */
    private String buildQueryDesc(String keyword, String serviceName, String level) {
        StringBuilder sb = new StringBuilder();
        sb.append("关键词=").append(keyword);
        if (serviceName != null && !serviceName.isEmpty()) {
            sb.append(", 服务=").append(serviceName);
        }
        if (level != null && !level.isEmpty()) {
            sb.append(", 级别=").append(level);
        }
        return sb.toString();
    }

    /**
     * 工具返回结果的包装类
     */
    @Data
    public static class ToolResult {
        private boolean success;
        private String message;
        private Object data;

        public static ToolResult success(String message, Object data) {
            ToolResult result = new ToolResult();
            result.setSuccess(true);
            result.setMessage(message);
            result.setData(data);
            return result;
        }

        public static ToolResult error(String message) {
            ToolResult result = new ToolResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }
}
