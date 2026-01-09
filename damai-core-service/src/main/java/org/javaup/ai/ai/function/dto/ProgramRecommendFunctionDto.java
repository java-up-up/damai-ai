package org.javaup.ai.ai.function.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 节目推荐 dto
 * @author: 阿星不是程序员
 **/
@Data
public class ProgramRecommendFunctionDto {

    @ToolParam(required = false, description = "节目演出地点")
    private String areaName;

    @ToolParam(required = false, description = "节目类型")
    private String programCategory;
}
