package org.javaup.ai.service;

import org.javaup.ai.entity.KnowledgeBase;
import org.javaup.ai.entity.KnowledgeChunk;
import org.javaup.ai.mapper.KnowledgeChunkMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class FileRagService {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private KnowledgeChunkMapper knowledgeChunkMapper;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    public Long processFile(MultipartFile file, String name, String remark) {
        // 1. 解析、切分、向量化
        String content = parseFile(file);
        int chunkSize = estimateChunkSize(content.length());
        int topK = estimateTopK(content.length()); // 计算出topK

        // 2. 保存知识库元数据（只保存一次，topK 已经有值）
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(name);
        kb.setRemark(remark);
        kb.setFileName(file.getOriginalFilename());
        kb.setUploadTime(new Date());
        kb.setStatus(1);
        kb.setTopK(topK);
        knowledgeBaseService.save(kb);

        Long kbId = kb.getId();

        // 调试输出
        System.out.println("[FileRagService] 文件上传: chunkSize=" + chunkSize + ", topK=" + topK + ", 总字数=" + content.length());
        List<String> chunks = splitContent(content, chunkSize);
        int idx = 0;
        for (String chunk : chunks) {
            Document doc = new Document(chunk);
            doc.getMetadata().put("kbId", String.valueOf(kbId));
            doc.getMetadata().put("topK", String.valueOf(topK)); // 可选，便于后续检索
            vectorStore.add(Collections.singletonList(doc));
            KnowledgeChunk kc = new KnowledgeChunk();
            kc.setKbId(kbId);
            kc.setChunkIndex(idx++);
            kc.setContent(chunk);
            kc.setCreateTime(new Date());
            knowledgeChunkMapper.insert(kc);
        }
        return kbId;
    }

    private String parseFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new RuntimeException("文件名为空");
        String lower = filename.toLowerCase();
        try {
            if (lower.endsWith(".pdf")) {
                // PDF解析
                try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(file.getInputStream())) {
                    org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                    return stripper.getText(document);
                }
            } else if (lower.endsWith(".md")) {
                // Markdown解析
                String md = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                // 可选：去除markdown语法，仅保留文本
                org.commonmark.node.Node document = new org.commonmark.parser.Parser.Builder().build().parse(md);
                org.commonmark.renderer.text.TextContentRenderer renderer = org.commonmark.renderer.text.TextContentRenderer.builder().build();
                return renderer.render(document);
            } else if (lower.endsWith(".docx")) {
                // Word(docx)解析
                try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(file.getInputStream())) {
                    org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor = new org.apache.poi.xwpf.extractor.XWPFWordExtractor(doc);
                    return extractor.getText();
                }
            } else if (lower.endsWith(".txt")) {
                // 纯文本
                return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } else {
                throw new RuntimeException("暂不支持的文件类型: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件解析失败: " + filename, e);
        }
    }

    private List<String> splitContent(String content, int chunkSize) {
        // 优先按段落切分
        String[] paragraphs = content.split("\\n+|\\r+|\\r\\n+");
        List<String> chunks = new ArrayList<>();
        for (String para : paragraphs) {
            if (para.trim().isEmpty()) continue;
            // 按句子切分
            String[] sentences = para.split("(?<=[。！？.!?])");
            StringBuilder chunk = new StringBuilder();
            int sentenceCount = 0;
            for (String sentence : sentences) {
                if (sentence.trim().isEmpty()) continue;
                chunk.append(sentence);
                sentenceCount++;
                // 合并2-3句为一个chunk，或超出chunkSize就切分
                if (sentenceCount >= 3 || chunk.length() > chunkSize) {
                    chunks.add(chunk.toString());
                    chunk = new StringBuilder();
                    sentenceCount = 0;
                }
            }
            if (chunk.length() > 0) {
                chunks.add(chunk.toString());
            }
        }
        return chunks;
    }

    private int estimateChunkSize(int totalLength) {
        if (totalLength < 1000) return 100;
        if (totalLength < 5000) return 200;
        if (totalLength < 20000) return 300;
        return 400;
    }

    private int estimateTopK(int totalLength) {
        if (totalLength < 1000) return 3;
        if (totalLength < 5000) return 5;
        if (totalLength < 20000) return 8;
        return 10;
    }
}