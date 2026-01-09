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
     * MCP Filesystem服务器让AI能够操作文件系统（AI本身做不到的事情）：
     * 示例问题：
     * "帮我读取项目根目录下的pom.xml文件内容"
     */
    @RequestMapping(value = "/chat/mcp", produces = "text/html;charset=utf-8")
    public Flux<String> chatWithMcp(@RequestParam("prompt") String prompt) {
        return chatClient.prompt()
                .user(prompt)
                // 注入MCP工具
                .toolCallbacks(mcpToolCallbackProvider)
                .stream()
                .content();
    }
}
