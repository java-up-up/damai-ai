package org.javaup.ai.cotroller;

import jakarta.annotation.Resource;
import org.javaup.ai.dto.AskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

import org.javaup.ai.service.KnowledgeBaseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;


/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料
 * @description: 聊天记录控制器
 * @author: docFat
 **/
@RestController
@RequestMapping("/rag-query")
public class RagQueryController {

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    // 如果你确实需要用 embeddingModel，指定 @Qualifier
    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private org.springframework.ai.embedding.EmbeddingModel embeddingModel;
    @Resource
    private ChatClient vipRagChatClient;

    @PostMapping(value = "/ask", produces = "text/html;charset=utf-8")
    public Flux<String> ask(@RequestBody AskRequest req) {
        String question = req.question;
        String chatId = req.chatId;
        Integer topK = req.topK;
        String kbId = req.kbId;

        // 1. 召回知识块
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(topK != null ? topK * 2 : 40)
                .build();
        List<Document> docs = vectorStore.similaritySearch(request);
        List<Document> filteredDocs = docs.stream()
                .filter(doc -> kbId.equals(doc.getMetadata().get("kbId")))
                .limit(topK != null ? topK : 5)
                .collect(Collectors.toList());
        String context = filteredDocs.stream().map(Document::getText).collect(Collectors.joining("\n"));
        System.out.println("使用的召回率为: " + topK);
        // 2. 用 context 作为 system prompt
        return vipRagChatClient.prompt()
                .system("请结合以下知识库内容回答用户问题：\n" + context)
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}