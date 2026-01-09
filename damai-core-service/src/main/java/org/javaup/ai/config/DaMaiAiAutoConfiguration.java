package org.javaup.ai.config;


import org.javaup.ai.advisor.ChatTypeHistoryAdvisor;
import org.javaup.ai.advisor.ChatTypeTitleAdvisor;
import org.javaup.ai.ai.function.AiProgram;
import org.javaup.ai.constants.DaMaiConstant;
import org.javaup.ai.enums.ChatType;
import org.javaup.ai.service.ChatTypeHistoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import static org.javaup.ai.constants.DaMaiConstant.CHAT_TITLE_ADVISOR_ORDER;
import static org.javaup.ai.constants.DaMaiConstant.CHAT_TYPE_HISTORY_ADVISOR_ORDER;
import static org.javaup.ai.constants.DaMaiConstant.MESSAGE_CHAT_MEMORY_ADVISOR_ORDER;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 自动装配类
 * @author: 阿星不是程序员
 **/
public class DaMaiAiAutoConfiguration {


    @Bean
    public ChatClient chatClient(DeepSeekChatModel model) {
        return ChatClient
                .builder(model)
                .defaultSystem("你是一位智能助手，你的特点是温柔、善良，你的名字叫智能小艾，要结合你的特点积极的回答用户的问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient assistantChatClient(DeepSeekChatModel model, ChatMemory chatMemory, AiProgram aiProgram,
                                          ChatTypeHistoryService chatTypeHistoryService,@Qualifier("titleChatClient")ChatClient titleChatClient) {
        return ChatClient
                .builder(model)
                .defaultSystem(DaMaiConstant.DA_MAI_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.ASSISTANT.getCode())
                                .order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.ASSISTANT.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build()
                )
                .defaultTools(aiProgram)
                .build();
    }
    
    @Bean
    public ChatClient titleChatClient(DeepSeekChatModel model) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
    
    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
