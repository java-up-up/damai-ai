package org.javaup.ai.cotroller;

import lombok.RequiredArgsConstructor;
import org.javaup.ai.common.ApiResponse;
import org.javaup.ai.service.ChatTypeHistoryService;
import org.javaup.ai.vo.ChatHistoryMessageVO;
import org.javaup.ai.vo.ChatTypeHistoryVo;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 聊天记录控制器
 * @author: 阿星不是程序员
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatTypeHistoryController {

    private final ChatTypeHistoryService chatHistoryService;

    private final ChatMemory chatMemory;
    
    @RequestMapping("/type/history/list")
    public List<ChatTypeHistoryVo> getChatTypeHistoryList(@RequestParam("type") Integer type) {
        return chatHistoryService.getChatTypeHistoryList(type);
    }

    @RequestMapping("/history/message/list")
    public List<ChatHistoryMessageVO> getChatHistory(@RequestParam("chatId") String chatId,@RequestParam("type") Integer type) {
        List<Message> messages = chatMemory.get(chatId);
        return messages.stream().map(ChatHistoryMessageVO::new).toList();
    }
    
    @RequestMapping(value = "/delete")
    public ApiResponse<Void> delete(@RequestParam("type") Integer type, @RequestParam("chatId") String chatId){
        chatHistoryService.delete(type, chatId);
        return ApiResponse.ok();
    }
    
    
}
