package org.javaup.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import static org.springframework.ai.chat.messages.MessageType.ASSISTANT;
import static org.springframework.ai.chat.messages.MessageType.USER;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 聊天记录消息体
 * @author: 阿星不是程序员
 **/
@NoArgsConstructor
@Data
public class ChatHistoryMessageVO {
    private String role;
    private String content;

    public ChatHistoryMessageVO(Message message) {
        MessageType messageType = message.getMessageType();
        if (messageType == USER) {
            this.role = "user";
        }else if (messageType == ASSISTANT) {
            this.role = "assistant";
        }else {
            this.role = "";
        }
        this.content = message.getText();
    }
}
