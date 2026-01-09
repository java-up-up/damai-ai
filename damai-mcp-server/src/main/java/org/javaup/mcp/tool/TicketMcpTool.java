package org.javaup.mcp.tool;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 票务查询MCP工具类 - 这里定义的方法会自动暴露给MCP Client使用通过 @Tool 注解标记的方法，AI就能调用它们来查询票务信息
 * @author: 阿星不是程序员
 **/
@Service
public class TicketMcpTool {

    @Value("${damai.api.base-url:http://localhost:6085}")
    private String apiBaseUrl;

    /**
     * 搜索节目
     * AI可以调用这个工具来搜索演唱会、话剧等节目
     */
    @Tool(description = "搜索节目信息，可以根据城市、艺人名称来搜索演唱会、话剧等节目")
    public ToolResult searchPrograms(
            @ToolParam(description = "城市名称，比如：北京、上海、广州") String cityName,
            @ToolParam(description = "艺人或明星名称，比如：周杰伦、林俊杰", required = false) String actor) {
        
        try {
            // 构建ES查询参数（简化版，实际可以更复杂）
            JSONObject searchParams = new JSONObject();
            if (cityName != null && !cityName.isEmpty()) {
                searchParams.put("cityName", cityName);
            }
            if (actor != null && !actor.isEmpty()) {
                searchParams.put("actor", actor);
            }
            
            // 调用大麦后端API
            String result = HttpRequest.post(apiBaseUrl + "/damai/program/program/search")
                    .header("no_verify", "true")
                    .body(JSON.toJSONString(searchParams))
                    .timeout(20000)
                    .execute().body();
            
            JSONObject response = JSON.parseObject(result);
            if (response.getInteger("code") == 0) {
                return ToolResult.success("搜索成功", response.get("data"));
            } else {
                return ToolResult.error("搜索失败: " + response.getString("message"));
            }
        } catch (Exception e) {
            return ToolResult.error("调用票务API异常: " + e.getMessage());
        }
    }

    /**
     * 获取节目详情
     * AI可以调用这个工具来获取某个节目的详细信息，包括时间、地点、票价等
     */
    @Tool(description = "获取节目详情，包括演出时间、地点、票价等详细信息")
    public ToolResult getProgramDetail(
            @ToolParam(description = "节目ID") Long programId) {
        
        try {
            JSONObject params = new JSONObject();
            params.put("id", programId);
            
            String result = HttpRequest.post(apiBaseUrl + "/damai/program/program/detail")
                    .header("no_verify", "true")
                    .body(JSON.toJSONString(params))
                    .timeout(20000)
                    .execute().body();
            
            JSONObject response = JSON.parseObject(result);
            if (response.getInteger("code") == 0) {
                return ToolResult.success("查询成功", response.get("data"));
            } else {
                return ToolResult.error("查询失败: " + response.getString("message"));
            }
        } catch (Exception e) {
            return ToolResult.error("调用票务API异常: " + e.getMessage());
        }
    }

    /**
     * 获取票档信息
     * AI可以调用这个工具来查询某个节目的票档信息，包括价格和余票情况
     */
    @Tool(description = "获取节目的票档信息，包括各个票档的价格和余票数量")
    public ToolResult getTicketCategories(
            @ToolParam(description = "节目ID") Long programId) {
        
        try {
            JSONObject params = new JSONObject();
            params.put("programId", programId);
            
            String result = HttpRequest.post(apiBaseUrl + "/damai/program/ticket/category/select/list/by/program")
                    .header("no_verify", "true")
                    .body(JSON.toJSONString(params))
                    .timeout(20000)
                    .execute().body();
            
            JSONObject response = JSON.parseObject(result);
            if (response.getInteger("code") == 0) {
                return ToolResult.success("查询成功", response.get("data"));
            } else {
                return ToolResult.error("查询失败: " + response.getString("message"));
            }
        } catch (Exception e) {
            return ToolResult.error("调用票务API异常: " + e.getMessage());
        }
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
