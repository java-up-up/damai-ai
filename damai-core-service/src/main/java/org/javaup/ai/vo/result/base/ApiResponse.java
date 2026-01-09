package org.javaup.ai.vo.result.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 接口返回体基础类 dto
 * @author: 阿星不是程序员
 **/
@Data
public class ApiResponse {

    @Schema(name ="code", type ="Integer", description ="响应码 0:成功 其余:失败")
    private Integer code;

    @Schema(name ="message", type ="String", description ="错误信息")
    private String message;
}
