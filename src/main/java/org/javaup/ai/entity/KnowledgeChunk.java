package org.javaup.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long kbId;
    private Integer chunkIndex;
    private String content;
    private Date createTime;
}