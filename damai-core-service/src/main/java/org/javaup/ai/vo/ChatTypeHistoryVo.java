package org.javaup.ai.vo;

import lombok.Data;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 聊天记录实体类
 * @author: 阿星不是程序员
 **/
@Data
public class ChatTypeHistoryVo  {
    
    private Long id;
    
    private Integer type;
    
    private String chatId;
    
    private String title;
}
