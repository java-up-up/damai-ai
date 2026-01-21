package org.javaup.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 按类型统计结果
 * @author: 阿星不是程序员
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeStatisticsVo {
    
    /**
     * 请求类型
     */
    private String requestType;
    
    /**
     * 调用次数
     */
    private int calls;
    
    /**
     * Token总数
     */
    private int totalTokens;
    
    /**
     * 预估费用
     */
    private BigDecimal totalCost;
}
