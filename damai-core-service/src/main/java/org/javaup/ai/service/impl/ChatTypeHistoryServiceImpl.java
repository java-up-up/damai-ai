package org.javaup.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.javaup.ai.entity.ChatTypeHistory;
import org.javaup.ai.mapper.ChatHistoryMapper;
import org.javaup.ai.service.ChatTypeHistoryService;
import org.javaup.ai.vo.ChatTypeHistoryVo;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 聊天记录服务类实现
 * @author: 阿星不是程序员
 **/
@Service
public class ChatTypeHistoryServiceImpl implements ChatTypeHistoryService {
    
    @Autowired
    private ChatHistoryMapper chatHistoryMapper;
    
    @Autowired 
    private ChatMemory chatMemory;
     
    
    /**
     * 保存会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Integer type, String chatId){
        LambdaQueryWrapper<ChatTypeHistory> chatHistroyLambdaQueryWrapper =
                Wrappers.lambdaQuery(ChatTypeHistory.class).eq(ChatTypeHistory::getType, type).eq(ChatTypeHistory::getChatId, chatId);
        ChatTypeHistory chatTypeHistory = chatHistoryMapper.selectOne(chatHistroyLambdaQueryWrapper);
        if (Objects.isNull(chatTypeHistory)){
            chatTypeHistory = new ChatTypeHistory();
            chatTypeHistory.setType(type);
            chatTypeHistory.setChatId(chatId);
            chatHistoryMapper.insert(chatTypeHistory);
        }
    }
    
    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    @Override
    public List<String> getChatIdList(Integer type){
        LambdaQueryWrapper<ChatTypeHistory> chatHistroyLambdaQueryWrapper =
                Wrappers.lambdaQuery(ChatTypeHistory.class).eq(ChatTypeHistory::getType, type);
        List<ChatTypeHistory> chatTypeHistoryList = chatHistoryMapper.selectList(chatHistroyLambdaQueryWrapper);
        return chatTypeHistoryList.stream()
                .map(ChatTypeHistory::getChatId)
                .toList();
    }
    
    /**
     * 删除会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer type, String chatId) {
        LambdaUpdateWrapper<ChatTypeHistory> chatHistroyLambdaUpdateWrapper =
                Wrappers.lambdaUpdate(ChatTypeHistory.class).eq(ChatTypeHistory::getType, type).eq(ChatTypeHistory::getChatId, chatId);
        chatHistoryMapper.delete(chatHistroyLambdaUpdateWrapper);
        chatMemory.clear(chatId);
    }
    
    /**
     * 获取会话
     * @param type 业务类型
     * @param chatId 会话ID
     * @return 会话
     */
    @Override
    public ChatTypeHistory getChatTypeHistory(Integer type, String chatId) {
        LambdaQueryWrapper<ChatTypeHistory> chatHistroyLambdaQueryWrapper =
                Wrappers.lambdaQuery(ChatTypeHistory.class).eq(ChatTypeHistory::getType, type).eq(ChatTypeHistory::getChatId, chatId);
        return chatHistoryMapper.selectOne(chatHistroyLambdaQueryWrapper);
    }
    
    @Override
    public void updateById(ChatTypeHistory chatTypeHistory){
        chatHistoryMapper.updateById(chatTypeHistory);
    }
    
    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    @Override
    public List<ChatTypeHistoryVo> getChatTypeHistoryList(Integer type){
        LambdaQueryWrapper<ChatTypeHistory> chatHistroyLambdaQueryWrapper =
                Wrappers.lambdaQuery(ChatTypeHistory.class).eq(ChatTypeHistory::getType, type);
        List<ChatTypeHistory> chatTypeHistoryList = chatHistoryMapper.selectList(chatHistroyLambdaQueryWrapper);
        return chatTypeHistoryList.stream()
                .map(chatTypeHistory -> {
                    ChatTypeHistoryVo chatTypeHistoryVo = new ChatTypeHistoryVo();
                    BeanUtils.copyProperties(chatTypeHistory, chatTypeHistoryVo);
                    return chatTypeHistoryVo;
                })
                .toList();
    }
}
