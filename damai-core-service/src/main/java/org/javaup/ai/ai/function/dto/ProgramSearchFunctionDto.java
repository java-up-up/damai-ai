package org.javaup.ai.ai.function.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Date;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 节目搜索 dto
 * @author: 阿星不是程序员
 **/
@Data
public class ProgramSearchFunctionDto {

    @ToolParam(required = false, description = "节目演出城市")
    private String cityName;

    @ToolParam(required = false, description = "节目艺人或者节目明星")
    private String actor;

    @ToolParam(required = false, description = "节目演出时间")
    private Date showTime;
}
