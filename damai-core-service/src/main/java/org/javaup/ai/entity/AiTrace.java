package org.javaup.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.javaup.ai.entity.base.BaseTableData;

import java.math.BigDecimal;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: AI调用追踪记录实体
 * @author: 阿星不是程序员
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("d_ai_trace")
public class AiTrace extends BaseTableData {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 追踪ID（用于关联同一会话的多次调用）
     */
    private String traceId;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 使用的模型名称
     */
    private String modelName;
    
    /**
     * 请求类型: CHAT, RAG, FUNCTION_CALL
     */
    private String requestType;
    
    /**
     * 输入Token数
     */
    private Integer promptTokens;
    
    /**
     * 输出Token数
     */
    private Integer completionTokens;
    
    /**
     * 总Token数
     */
    private Integer totalTokens;
    
    /**
     * 响应延迟（毫秒）
     */
    private Long latencyMs;
    
    /**
     * 预估费用（单位：元）
     */
    private BigDecimal estimatedCost;
    
    /**
     * 用户输入（截断保存）
     */
    private String userInput;
    
    /**
     * AI输出（截断保存）
     */
    private String aiOutput;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 附加元数据（JSON格式）
     */
    private String metadata;
}
