package org.javaup.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 节目订单创建 dto
 * @author: 阿星不是程序员
 **/
@Data
@Schema(title="ProgramOrderCreateDto", description ="节目订单创建")
public class ProgramOrderCreateDto {
    
    @Schema(name ="programId", type ="Long", description ="节目id",requiredMode= RequiredMode.REQUIRED)
    private Long programId;
    
    @Schema(name ="userId", type ="Long", description ="用户id",requiredMode= RequiredMode.REQUIRED)
    private Long userId;
    
    @Schema(name ="ticketUserIdList", type ="List<Long>", description ="购票人id集合",requiredMode= RequiredMode.REQUIRED)
    private List<Long> ticketUserIdList;
    
    @Schema(name ="ticketCategoryId", type ="Long", description = "节目票档id",requiredMode= RequiredMode.REQUIRED)
    private Long ticketCategoryId;
    
    @Schema(name ="ticketCount", type ="Integer", description = "购买票数量",requiredMode= RequiredMode.REQUIRED)
    private Integer ticketCount;
}
