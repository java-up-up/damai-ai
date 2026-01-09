package org.javaup.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.javaup.ai.entity.base.BaseTableData;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 聊天记录实体类
 * @author: 阿星不是程序员
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("d_chat_type_history")
public class ChatTypeHistory extends BaseTableData {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Integer type;
    
    private String chatId;
    
    private String title;
}
