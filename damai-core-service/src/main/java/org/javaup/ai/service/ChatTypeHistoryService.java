package org.javaup.ai.service;

import org.javaup.ai.entity.ChatTypeHistory;
import org.javaup.ai.vo.ChatTypeHistoryVo;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 聊天记录服务
 * @author: 阿星不是程序员
 **/
public interface ChatTypeHistoryService {
    
    /**
     * 保存会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    void save(Integer type, String chatId);
    
    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    List<String> getChatIdList(Integer type);
    
    /**
     * 删除会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    void delete(Integer type, String chatId);
    
    /**
     * 获取会话
     * @param type 业务类型
     * @param chatId 会话ID
     * @return 会话
     */
    ChatTypeHistory getChatTypeHistory(Integer type, String chatId);
    
    /**
     * 更新会话类型历史
     * @param chatTypeHistory 会话类型历史记录
     */
    void updateById(ChatTypeHistory chatTypeHistory);
    
    /**
     * 获取会话列表
     * @param type 业务类型
     * @return 会话列表
     */
    List<ChatTypeHistoryVo> getChatTypeHistoryList(Integer type);
}
