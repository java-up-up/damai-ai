package org.javaup.ai.ai.rag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javaup.ai.utils.StringUtil;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig.Builder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: markdown文档读取 dto
 * @author: 阿星不是程序员
 **/
@AllArgsConstructor
@Slf4j
public class MarkdownLoader {

    private final ResourcePatternResolver resourcePatternResolver;
    
    /**
     * 加载 Markdown 文档
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:datum/*.md");
            log.info("找到 {} 个Markdown文件", resources.length);
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                log.info("正在处理文件: {}", fileName);
                
                String label = fileName;
                if (StringUtil.isNotEmpty(fileName)) {
                    final String[] parts = fileName.split("-");
                    if (parts.length > 1) {
                        label = parts[0];
                    }
                }
                log.info("提取的文档标签: {}", label);
   
                Builder builder = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false);
                if (StringUtil.isNotEmpty(fileName)) {
                    builder.withAdditionalMetadata("name", fileName);
                }
                if (StringUtil.isNotEmpty(label)) {
                    builder.withAdditionalMetadata("label", label);
                }
                MarkdownDocumentReaderConfig config = builder.build();
                        MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                List<Document> documents = markdownDocumentReader.get();
                log.info("文件 {} 加载了 {} 个文档片段", fileName, documents.size());
                allDocuments.addAll(documents);
            }
            log.info("总共加载了 {} 个文档片段", allDocuments.size());
        } catch (IOException e) {
           log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
}
