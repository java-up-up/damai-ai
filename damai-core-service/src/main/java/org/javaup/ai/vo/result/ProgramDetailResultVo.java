package org.javaup.ai.vo.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.javaup.ai.vo.ProgramDetailVo;
import org.javaup.ai.vo.result.base.ApiResponse;

import java.io.Serial;
import java.io.Serializable;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 程序详情结果返回体
 * @author: 阿星不是程序员
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class ProgramDetailResultVo extends ApiResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ProgramDetailVo data;
}
