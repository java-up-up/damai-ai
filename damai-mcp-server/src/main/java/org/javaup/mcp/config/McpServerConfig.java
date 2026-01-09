package org.javaup.mcp.config;

import org.javaup.mcp.tool.TicketMcpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: MCP Server配置类 - 注册MCP工具这个配置类告诉Spring AI MCP Server要暴露哪些工具
 * @author: 阿星不是程序员
 **/
@Configuration
public class McpServerConfig {

    /**
     * 注册MCP工具回调提供者
     * 把TicketMcpTool中的@Tool方法注册为MCP可调用的工具
     */
    @Bean
    public ToolCallbackProvider ticketToolCallbackProvider(TicketMcpTool ticketMcpTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(ticketMcpTool)
                .build();
    }
}
