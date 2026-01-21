package org.javaup.mcp.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 监控指标MCP工具类 - 通过 Prometheus API 查询 JVM、内存、线程等指标
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
public class MetricsQueryMcpTool {

    @Value("${prometheus.url:http://localhost:9090}")
    private String prometheusUrl;

    private HttpClient httpClient;

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        log.info("初始化 Prometheus 客户端，地址: {}", prometheusUrl);
    }

    /**
     * 获取可用的服务列表（指标监控）
     */
    @Tool(description = "获取大麦系统中所有被 Prometheus 监控的微服务列表")
    public ToolResult getMetricsServiceList() {
        try {
            // 查询 application 标签的所有值
            String response = queryPrometheus("/api/v1/label/application/values");
            log.info("获取服务列表响应: {}", response);
            
            if (response == null || response.isEmpty()) {
                return ToolResult.error("查询服务列表失败: 响应为空");
            }
            
            // 检查是否为 JSON
            if (!response.trim().startsWith("{")) {
                log.error("响应不是有效的 JSON: {}", response);
                return ToolResult.error("查询服务列表失败: Prometheus 返回非 JSON 响应");
            }
            
            JSONObject json = JSON.parseObject(response);
            
            if (!"success".equals(json.getString("status"))) {
                return ToolResult.error("查询服务列表失败: " + json.getString("error"));
            }
            
            JSONArray data = json.getJSONArray("data");
            List<String> services = data.stream()
                    .map(Object::toString)
                    .sorted()
                    .collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("服务列表", services);
            result.put("服务数量", services.size());
            return ToolResult.success("获取服务列表成功", result);
        } catch (Exception e) {
            log.error("获取服务列表失败", e);
            return ToolResult.error("获取服务列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询 JVM 堆内存使用情况
     */
    @Tool(description = "查询指定微服务的 JVM 堆内存使用情况，包括已用内存、最大内存、使用率等")
    public ToolResult getJvmMemory(
            @ToolParam(description = "服务名称，如：user-service、order-service") String serviceName) {
        try {
            // 查询堆内存已使用
            String usedQuery = String.format("jvm_memory_used_bytes{application=\"%s\",area=\"heap\"}", serviceName);
            Map<String, Double> usedMemory = queryMetricByPool(usedQuery);
            
            // 查询堆内存最大值
            String maxQuery = String.format("jvm_memory_max_bytes{application=\"%s\",area=\"heap\"}", serviceName);
            Map<String, Double> maxMemory = queryMetricByPool(maxQuery);
            
            // 查询堆内存已提交
            String committedQuery = String.format("jvm_memory_committed_bytes{application=\"%s\",area=\"heap\"}", serviceName);
            Map<String, Double> committedMemory = queryMetricByPool(committedQuery);

            // 计算总量
            double totalUsed = usedMemory.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalMax = maxMemory.values().stream().filter(v -> v > 0).mapToDouble(Double::doubleValue).sum();
            double totalCommitted = committedMemory.values().stream().mapToDouble(Double::doubleValue).sum();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("服务名称", serviceName);
            result.put("堆内存概览", Map.of(
                    "已使用", formatBytes(totalUsed),
                    "已提交", formatBytes(totalCommitted),
                    "最大值", totalMax > 0 ? formatBytes(totalMax) : "无限制",
                    "使用率", totalMax > 0 ? String.format("%.2f%%", totalUsed / totalMax * 100) : "N/A"
            ));
            result.put("内存池详情", buildMemoryPoolDetail(usedMemory, maxMemory, committedMemory));
            
            return ToolResult.success("JVM 堆内存查询成功", result);
        } catch (Exception e) {
            log.error("查询 JVM 内存失败", e);
            return ToolResult.error("查询 JVM 内存失败: " + e.getMessage());
        }
    }

    /**
     * 查询 GC 指标
     */
    @Tool(description = "查询指定微服务的 GC（垃圾回收）指标，包括 GC 次数、GC 耗时等")
    public ToolResult getGcMetrics(
            @ToolParam(description = "服务名称，如：user-service") String serviceName) {
        try {
            // GC 暂停次数
            String pauseCountQuery = String.format("jvm_gc_pause_seconds_count{application=\"%s\"}", serviceName);
            Map<String, Double> pauseCounts = queryMetricByAction(pauseCountQuery);
            
            // GC 暂停总时间
            String pauseSumQuery = String.format("jvm_gc_pause_seconds_sum{application=\"%s\"}", serviceName);
            Map<String, Double> pauseSums = queryMetricByAction(pauseSumQuery);

            List<Map<String, Object>> gcDetails = new ArrayList<>();
            for (String action : pauseCounts.keySet()) {
                Double count = pauseCounts.get(action);
                Double totalTime = pauseSums.getOrDefault(action, 0.0);
                double avgTime = count > 0 ? totalTime / count : 0;
                
                gcDetails.add(Map.of(
                        "类型", action,
                        "GC次数", count.longValue(),
                        "总耗时", String.format("%.3f 秒", totalTime),
                        "平均耗时", String.format("%.3f 毫秒", avgTime * 1000)
                ));
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("服务名称", serviceName);
            result.put("GC指标", gcDetails);
            
            return ToolResult.success("GC 指标查询成功", result);
        } catch (Exception e) {
            log.error("查询 GC 指标失败", e);
            return ToolResult.error("查询 GC 指标失败: " + e.getMessage());
        }
    }

    /**
     * 查询线程指标
     */
    @Tool(description = "查询指定微服务的线程指标，包括活跃线程数、峰值线程数、守护线程数等")
    public ToolResult getThreadMetrics(
            @ToolParam(description = "服务名称，如：user-service") String serviceName) {
        try {
            // 活跃线程数
            Double liveThreads = querySingleMetric(
                    String.format("jvm_threads_live_threads{application=\"%s\"}", serviceName));
            
            // 峰值线程数
            Double peakThreads = querySingleMetric(
                    String.format("jvm_threads_peak_threads{application=\"%s\"}", serviceName));
            
            // 守护线程数
            Double daemonThreads = querySingleMetric(
                    String.format("jvm_threads_daemon_threads{application=\"%s\"}", serviceName));
            
            // 线程状态分布
            String stateQuery = String.format("jvm_threads_states_threads{application=\"%s\"}", serviceName);
            Map<String, Double> threadStates = queryMetricByState(stateQuery);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("服务名称", serviceName);
            result.put("线程概览", Map.of(
                    "活跃线程数", liveThreads != null ? liveThreads.longValue() : 0,
                    "峰值线程数", peakThreads != null ? peakThreads.longValue() : 0,
                    "守护线程数", daemonThreads != null ? daemonThreads.longValue() : 0
            ));
            result.put("线程状态分布", threadStates.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().longValue())));
            
            return ToolResult.success("线程指标查询成功", result);
        } catch (Exception e) {
            log.error("查询线程指标失败", e);
            return ToolResult.error("查询线程指标失败: " + e.getMessage());
        }
    }

    /**
     * 查询 CPU 使用率
     */
    @Tool(description = "查询指定微服务的 CPU 使用情况，包括进程 CPU 使用率和系统 CPU 使用率")
    public ToolResult getCpuMetrics(
            @ToolParam(description = "服务名称，如：user-service") String serviceName) {
        try {
            // 进程 CPU 使用率
            Double processCpu = querySingleMetric(
                    String.format("process_cpu_usage{application=\"%s\"}", serviceName));
            
            // 系统 CPU 使用率
            Double systemCpu = querySingleMetric(
                    String.format("system_cpu_usage{application=\"%s\"}", serviceName));
            
            // 可用处理器数
            Double cpuCount = querySingleMetric(
                    String.format("system_cpu_count{application=\"%s\"}", serviceName));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("服务名称", serviceName);
            result.put("CPU指标", Map.of(
                    "进程CPU使用率", processCpu != null ? String.format("%.2f%%", processCpu * 100) : "N/A",
                    "系统CPU使用率", systemCpu != null ? String.format("%.2f%%", systemCpu * 100) : "N/A",
                    "可用处理器数", cpuCount != null ? cpuCount.intValue() : "N/A"
            ));
            
            return ToolResult.success("CPU 指标查询成功", result);
        } catch (Exception e) {
            log.error("查询 CPU 指标失败", e);
            return ToolResult.error("查询 CPU 指标失败: " + e.getMessage());
        }
    }

    /**
     * 查询服务健康概览
     */
    @Tool(description = "查询指定微服务的健康概览，包括 JVM内存、CPU、线程、GC 等关键指标的综合展示")
    public ToolResult getServiceHealthOverview(
            @ToolParam(description = "服务名称，如：user-service") String serviceName) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("服务名称", serviceName);
            
            // JVM 内存
            Double heapUsed = querySingleMetric(
                    String.format("sum(jvm_memory_used_bytes{application=\"%s\",area=\"heap\"})", serviceName));
            Double heapMax = querySingleMetric(
                    String.format("sum(jvm_memory_max_bytes{application=\"%s\",area=\"heap\"})", serviceName));
            
            Map<String, String> memoryInfo = new LinkedHashMap<>();
            memoryInfo.put("堆内存已用", heapUsed != null ? formatBytes(heapUsed) : "N/A");
            memoryInfo.put("堆内存最大", heapMax != null && heapMax > 0 ? formatBytes(heapMax) : "无限制");
            memoryInfo.put("内存使用率", heapUsed != null && heapMax != null && heapMax > 0 
                    ? String.format("%.2f%%", heapUsed / heapMax * 100) : "N/A");
            result.put("JVM内存", memoryInfo);
            
            // CPU
            Double processCpu = querySingleMetric(
                    String.format("process_cpu_usage{application=\"%s\"}", serviceName));
            result.put("CPU使用率", processCpu != null ? String.format("%.2f%%", processCpu * 100) : "N/A");
            
            // 线程
            Double liveThreads = querySingleMetric(
                    String.format("jvm_threads_live_threads{application=\"%s\"}", serviceName));
            result.put("活跃线程数", liveThreads != null ? liveThreads.longValue() : "N/A");
            
            // GC
            Double gcCount = querySingleMetric(
                    String.format("sum(jvm_gc_pause_seconds_count{application=\"%s\"})", serviceName));
            Double gcTime = querySingleMetric(
                    String.format("sum(jvm_gc_pause_seconds_sum{application=\"%s\"})", serviceName));
            result.put("GC统计", Map.of(
                    "总次数", gcCount != null ? gcCount.longValue() : 0,
                    "总耗时", gcTime != null ? String.format("%.3f 秒", gcTime) : "N/A"
            ));
            
            // 判断健康状态
            String healthStatus = evaluateHealth(heapUsed, heapMax, processCpu, liveThreads);
            result.put("健康状态", healthStatus);
            
            return ToolResult.success("服务健康概览查询成功", result);
        } catch (Exception e) {
            log.error("查询服务健康概览失败", e);
            return ToolResult.error("查询服务健康概览失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有服务的健康状态
     */
    @Tool(description = "查询所有微服务的健康状态概览，快速了解系统整体运行情况")
    public ToolResult getAllServicesHealth() {
        try {
            // 先获取服务列表
            String response = queryPrometheus("/api/v1/label/application/values");
            JSONObject json = JSON.parseObject(response);
            JSONArray data = json.getJSONArray("data");
            
            List<String> services = data.stream()
                    .map(Object::toString)
                    .sorted()
                    .collect(Collectors.toList());

            List<Map<String, Object>> healthList = new ArrayList<>();
            
            for (String serviceName : services) {
                Map<String, Object> serviceHealth = new LinkedHashMap<>();
                serviceHealth.put("服务名", serviceName);
                
                // 内存使用率
                Double heapUsed = querySingleMetric(
                        String.format("sum(jvm_memory_used_bytes{application=\"%s\",area=\"heap\"})", serviceName));
                Double heapMax = querySingleMetric(
                        String.format("sum(jvm_memory_max_bytes{application=\"%s\",area=\"heap\"})", serviceName));
                String memoryUsage = (heapUsed != null && heapMax != null && heapMax > 0)
                        ? String.format("%.1f%%", heapUsed / heapMax * 100) : "N/A";
                serviceHealth.put("内存使用率", memoryUsage);
                
                // CPU
                Double cpu = querySingleMetric(
                        String.format("process_cpu_usage{application=\"%s\"}", serviceName));
                serviceHealth.put("CPU使用率", cpu != null ? String.format("%.1f%%", cpu * 100) : "N/A");
                
                // 线程数
                Double threads = querySingleMetric(
                        String.format("jvm_threads_live_threads{application=\"%s\"}", serviceName));
                serviceHealth.put("线程数", threads != null ? threads.longValue() : "N/A");
                
                // 健康状态
                serviceHealth.put("状态", evaluateHealth(heapUsed, heapMax, cpu, threads));
                
                healthList.add(serviceHealth);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("服务数量", services.size());
            result.put("服务健康列表", healthList);
            
            return ToolResult.success("所有服务健康状态查询成功", result);
        } catch (Exception e) {
            log.error("查询所有服务健康状态失败", e);
            return ToolResult.error("查询所有服务健康状态失败: " + e.getMessage());
        }
    }

    // ======================== 私有方法 ========================

    /**
     * 查询 Prometheus API（使用 curl 命令）
     */
    private String queryPrometheus(String path) {
        String url = prometheusUrl + path;
        try {
            ProcessBuilder pb = new ProcessBuilder("curl", "-s", url);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String result = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            log.debug("curl 退出码: {}, 结果: {}", exitCode, result);
            return result;
        } catch (Exception e) {
            log.error("curl 执行失败: {}", url, e);
            return null;
        }
    }

    /**
     * 执行 PromQL 查询
     */
    private String executePromQL(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = prometheusUrl + "/api/v1/query?query=" + encodedQuery;
        log.info("执行 PromQL: {}", query);
        try {
            ProcessBuilder pb = new ProcessBuilder("curl", "-s", url);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String result = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor();
            return result;
        } catch (Exception e) {
            log.error("curl 执行失败: {}", query, e);
            return null;
        }
    }

    /**
     * 查询单个指标值
     */
    private Double querySingleMetric(String query) {
        try {
            String response = executePromQL(query);
            JSONObject json = JSON.parseObject(response);
            
            if (!"success".equals(json.getString("status"))) {
                return null;
            }
            
            JSONArray results = json.getJSONObject("data").getJSONArray("result");
            if (results == null || results.isEmpty()) {
                return null;
            }
            
            JSONArray value = results.getJSONObject(0).getJSONArray("value");
            return Double.parseDouble(value.getString(1));
        } catch (Exception e) {
            log.warn("查询指标失败: {}, 错误: {}", query, e.getMessage());
            return null;
        }
    }

    /**
     * 按内存池查询指标
     */
    private Map<String, Double> queryMetricByPool(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            String response = executePromQL(query);
            JSONObject json = JSON.parseObject(response);
            JSONArray results = json.getJSONObject("data").getJSONArray("result");
            
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    String pool = item.getJSONObject("metric").getString("id");
                    Double value = Double.parseDouble(item.getJSONArray("value").getString(1));
                    result.put(pool, value);
                }
            }
        } catch (Exception e) {
            log.warn("查询指标失败: {}", query);
        }
        return result;
    }

    /**
     * 按 GC action 查询指标
     */
    private Map<String, Double> queryMetricByAction(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            String response = executePromQL(query);
            JSONObject json = JSON.parseObject(response);
            JSONArray results = json.getJSONObject("data").getJSONArray("result");
            
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    JSONObject metric = item.getJSONObject("metric");
                    String action = metric.getString("action") + " (" + metric.getString("cause") + ")";
                    Double value = Double.parseDouble(item.getJSONArray("value").getString(1));
                    result.merge(action, value, Double::sum);
                }
            }
        } catch (Exception e) {
            log.warn("查询指标失败: {}", query);
        }
        return result;
    }

    /**
     * 按线程状态查询指标
     */
    private Map<String, Double> queryMetricByState(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            String response = executePromQL(query);
            JSONObject json = JSON.parseObject(response);
            JSONArray results = json.getJSONObject("data").getJSONArray("result");
            
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    String state = item.getJSONObject("metric").getString("state");
                    Double value = Double.parseDouble(item.getJSONArray("value").getString(1));
                    result.put(state, value);
                }
            }
        } catch (Exception e) {
            log.warn("查询指标失败: {}", query);
        }
        return result;
    }

    /**
     * 构建内存池详情
     */
    private List<Map<String, Object>> buildMemoryPoolDetail(Map<String, Double> used, 
                                                            Map<String, Double> max, 
                                                            Map<String, Double> committed) {
        List<Map<String, Object>> details = new ArrayList<>();
        for (String pool : used.keySet()) {
            Double usedVal = used.get(pool);
            Double maxVal = max.getOrDefault(pool, -1.0);
            Double committedVal = committed.getOrDefault(pool, 0.0);
            
            details.add(Map.of(
                    "内存池", pool,
                    "已用", formatBytes(usedVal),
                    "已提交", formatBytes(committedVal),
                    "最大", maxVal > 0 ? formatBytes(maxVal) : "无限制"
            ));
        }
        return details;
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(double bytes) {
        if (bytes < 1024) {
            return String.format("%.0f B", bytes);
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024 * 1024));
        }
        return String.format("%.2f GB", bytes / (1024 * 1024 * 1024));
    }

    /**
     * 评估健康状态
     */
    private String evaluateHealth(Double heapUsed, Double heapMax, Double cpu, Double threads) {
        // 内存使用率超过 90% 或 CPU 超过 80% 认为异常
        if (heapUsed != null && heapMax != null && heapMax > 0) {
            double memUsage = heapUsed / heapMax;
            if (memUsage > 0.9) {
                return "⚠️ 内存告警";
            }
            if (memUsage > 0.8) {
                return "⚠️ 内存较高";
            }
        }
        if (cpu != null && cpu > 0.8) {
            return "⚠️ CPU较高";
        }
        if (cpu != null && cpu > 0.9) {
            return "⚠️ CPU告警";
        }
        return "✅ 正常";
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
