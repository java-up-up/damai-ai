package org.javaup.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 节目票档集合 dto
 * @author: 阿星不是程序员
 **/
@Data
@Schema(title="TicketCategoryListByProgramDto", description ="节目票档集合")
public class TicketCategoryListByProgramDto {
    
    @Schema(name ="programId", type ="Long", description ="节目id",requiredMode= RequiredMode.REQUIRED)
    private Long programId;
}
