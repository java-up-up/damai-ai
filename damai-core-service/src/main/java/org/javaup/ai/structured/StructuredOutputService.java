package org.javaup.ai.structured;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 结构化输出服务 - 让AI返回指定格式的JSON数据
 * @author: 阿星不是程序员
 **/
@Slf4j
@Service
public class StructuredOutputService {
    
    /**
     * 意图识别 - 返回结构化的意图识别结果
     * 
     * @param chatClient ChatClient实例
     * @param userInput 用户输入
     * @return 结构化的意图识别结果
     */
    public IntentRecognition recognizeIntent(ChatClient chatClient, String userInput) {
        BeanOutputConverter<IntentRecognition> converter = new BeanOutputConverter<>(IntentRecognition.class);
        
        String prompt = """
                请分析以下用户输入，识别用户意图并提取关键实体。
                
                用户输入: %s
                
                请按照以下JSON格式返回结果:
                %s
                
                意图类型说明:
                - QUERY_PROGRAM: 查询节目/演唱会信息
                - BUY_TICKET: 购买门票
                - CHECK_ORDER: 查询订单
                - REFUND: 退票/退款
                - CONSULT: 一般咨询
                - OTHER: 其他
                
                实体类型说明:
                - ARTIST: 艺人/明星名称
                - LOCATION: 地点/城市
                - DATE: 日期/时间
                - PRICE: 价格
                - TICKET_TYPE: 票档类型
                """.formatted(userInput, converter.getFormat());
        
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        
        log.debug("意图识别原始响应: {}", response);
        
        return converter.convert(response);
    }
    
    /**
     * 节目推荐 - 返回结构化的推荐结果
     * 
     * @param chatClient ChatClient实例
     * @param userPreference 用户偏好描述
     * @return 结构化的推荐结果
     */
    public ProgramRecommendation recommendPrograms(ChatClient chatClient, String userPreference) {
        BeanOutputConverter<ProgramRecommendation> converter = new BeanOutputConverter<>(ProgramRecommendation.class);
        
        String prompt = """
                根据用户的偏好，分析并生成节目推荐。
                
                用户偏好描述: %s
                
                请按照以下JSON格式返回推荐结果:
                %s
                
                注意:
                1. 分析用户可能喜欢的节目类型和地区
                2. 推荐3-5个节目
                3. 为每个推荐给出具体理由
                4. 推荐指数范围1-5，5为最高
                """.formatted(userPreference, converter.getFormat());
        
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        
        log.debug("节目推荐原始响应: {}", response);
        
        return converter.convert(response);
    }
    
    /**
     * 通用结构化输出 - 支持任意类型
     * 
     * @param chatClient ChatClient实例
     * @param prompt 提示词
     * @param targetClass 目标类型
     * @param <T> 泛型类型
     * @return 结构化结果
     */
    public <T> T getStructuredOutput(ChatClient chatClient, String prompt, Class<T> targetClass) {
        BeanOutputConverter<T> converter = new BeanOutputConverter<>(targetClass);
        
        String fullPrompt = prompt + "\n\n请按照以下JSON格式返回:\n" + converter.getFormat();
        
        String response = chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
        
        log.debug("结构化输出原始响应: {}", response);
        
        return converter.convert(response);
    }
}
