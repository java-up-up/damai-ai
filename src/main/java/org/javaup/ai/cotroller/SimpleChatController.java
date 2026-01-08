package org.javaup.ai.cotroller;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 简单聊天控制器
 * @author: 阿星不是程序员
 **/
@RestController
@RequestMapping("/simple")
public class SimpleChatController {

    @Resource
    private ChatClient chatClient;

    @Resource
    private ToolCallbackProvider mcpToolCallbackProvider;


    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    /**
     * 使用MCP工具的聊天接口
     * MCP时间服务器提供了获取当前时间和时区转换的能力
     * 示例问题："现在几点了？" "北京时间现在是几点？" "把北京时间转换成纽约时间"
     */
    @RequestMapping(value = "/chat/mcp", produces = "text/html;charset=utf-8")
    public Flux<String> chatWithMcp(@RequestParam("prompt") String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .toolCallbacks(mcpToolCallbackProvider)  // 注入MCP工具
                .stream()
                .content();
    }
}
