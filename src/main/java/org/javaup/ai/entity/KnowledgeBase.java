package org.javaup.ai.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String remark;
    private String fileName;
    private Long userId;
    private Date uploadTime;
    private Integer status;
    @TableField("top_k")
    private Integer topK;
}
