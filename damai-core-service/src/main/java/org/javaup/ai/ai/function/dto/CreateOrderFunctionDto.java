package org.javaup.ai.ai.function.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 创建订单 dto
 * @author: 阿星不是程序员
 **/
@Data
public class CreateOrderFunctionDto {
    
    @ToolParam(required = true, description = "节目演出城市")
    private String cityName;
    
    @ToolParam(required = true, description = "节目艺人或者节目明星")
    private String actor;
    
    @ToolParam(required = false, description = "节目演出时间")
    private Date showTime;
    
    @ToolParam(required = true, description = "用户手机号")
    private String mobile;
    
    @ToolParam(required = true, description = "购票人证件号码列表")
    private List<String> ticketUserNumberList;;
    
    @ToolParam(required = true, description = "节目的票档价位")
    private BigDecimal ticketCategoryPrice;
    
    @ToolParam(required = true, description = "节目的票档购买数量")
    private Integer ticketCount;
}
