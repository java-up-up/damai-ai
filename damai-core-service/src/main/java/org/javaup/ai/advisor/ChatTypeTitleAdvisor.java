package org.javaup.ai.advisor;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.javaup.ai.entity.ChatTypeHistory;
import org.javaup.ai.service.ChatTypeHistoryService;
import org.javaup.ai.utils.StringUtil;
import org.javaup.ai.vo.ChatHistoryMessageVO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.Objects;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 保存会话历史Advisor
 * @author: 阿星不是程序员
 **/
@Slf4j
public class ChatTypeTitleAdvisor implements BaseChatMemoryAdvisor {
    
    private final Integer type;
    
    private final String defaultConversationId;
    
    private final Integer order;
    
    private final ChatTypeHistoryService chatTypeHistoryService;
    
    private final ChatMemory chatMemory;
    
    private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;
    
    private ChatClient chatClient;
    
    private ChatTypeTitleAdvisor(Integer type, String defaultConversationId,
                                 ChatTypeHistoryService chatTypeHistoryService, Integer order,
                                 ChatMemory chatMemory, ChatClient chatClient) {
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (StringUtil.isEmpty(defaultConversationId)) {
            throw new IllegalArgumentException("defaultConversationId cannot be empty");
        }
        if (Objects.isNull(chatTypeHistoryService)) {
            throw new IllegalArgumentException("chatHistoryService cannot be null");
        }
        if (Objects.isNull(order)) {
            throw new IllegalArgumentException("order cannot be null");
        }
        this.type = type;
        this.defaultConversationId = defaultConversationId;
        this.chatTypeHistoryService = chatTypeHistoryService;
        this.order = order;
        this.chatMemory = chatMemory;
        this.chatClient = chatClient;
    }
    
    @Override
    public ChatClientRequest before(final ChatClientRequest chatClientRequest, final AdvisorChain advisorChain) {
        return chatClientRequest;
    }
    
    @Override
    public ChatClientResponse after(final ChatClientResponse chatClientResponse, final AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientResponse.context(), this.defaultConversationId);
        List<Message> messages = chatMemory.get(conversationId);
        List<ChatHistoryMessageVO> list = messages.stream().map(ChatHistoryMessageVO::new).toList();
        log.info("会话记录: {}", JSON.toJSONString(list));
        
        ChatTypeHistory chatTypeHistory = chatTypeHistoryService.getChatTypeHistory(type, conversationId);
        if (Objects.isNull(chatTypeHistory) || StringUtil.isNotEmpty(chatTypeHistory.getTitle())) {
            return chatClientResponse;
        }
        
        String content = chatClient.prompt().user("请为以下对话总结一句简洁标题\n" + JSON.toJSONString(list) + "\n 只返回标题文本内容，不要其他样式")
                .call().content();
        
        log.info("生成的标题: {}", content);
        
        ChatTypeHistory updatedChatTypeHistory = new ChatTypeHistory();
        updatedChatTypeHistory.setId(chatTypeHistory.getId());
        updatedChatTypeHistory.setTitle(content);
        chatTypeHistoryService.updateById(updatedChatTypeHistory);
        return chatClientResponse;
    }
    
    @Override
    public Flux<ChatClientResponse> adviseStream(final ChatClientRequest chatClientRequest, final StreamAdvisorChain streamAdvisorChain) {
        return Mono.just(chatClientRequest)
                .publishOn(scheduler)
                .map(request -> this.before(request, streamAdvisorChain))
                .flatMapMany(streamAdvisorChain::nextStream)
                .transform(flux -> new ChatClientMessageAggregator()
                        .aggregateChatClientResponse(flux,
                                response -> this.after(response, streamAdvisorChain)));
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    public static Builder builder(ChatTypeHistoryService chatTypeHistoryService) {
        return new Builder(chatTypeHistoryService);
    }
    
    public static final class Builder {
        
        private Integer type;
        
        private Integer order = Ordered.HIGHEST_PRECEDENCE + 99;
        
        private ChatTypeHistoryService chatTypeHistoryService;
        
        private ChatMemory chatMemory;
        
        private ChatClient chatClient;
        
        private Builder(ChatTypeHistoryService chatTypeHistoryService) {
            this.chatTypeHistoryService = chatTypeHistoryService;
        }
        
        public ChatTypeTitleAdvisor.Builder type(Integer type) {
            this.type = type;
            return this;
        }
        
        public ChatTypeTitleAdvisor.Builder chatHistoryService(ChatTypeHistoryService chatTypeHistoryService) {
            this.chatTypeHistoryService = chatTypeHistoryService;
            return this;
        }
        
        public ChatTypeTitleAdvisor.Builder order(Integer order) {
            this.order = order;
            return this;
        }
        
        public ChatTypeTitleAdvisor.Builder chatMemory(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }
        
        public ChatTypeTitleAdvisor.Builder chatClient(ChatClient chatClient) {
            this.chatClient = chatClient;
            return this;
        }
        
        public ChatTypeTitleAdvisor build() {
            final String conversationId = ChatMemory.DEFAULT_CONVERSATION_ID;
            return new ChatTypeTitleAdvisor(this.type,conversationId, this.chatTypeHistoryService, this.order, 
                    this.chatMemory, this.chatClient);
        }
        
    }
}
