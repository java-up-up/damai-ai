package org.javaup.ai.ai.function.call;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import org.javaup.ai.dto.ProgramOrderCreateDto;
import org.javaup.ai.enums.BaseCode;
import org.javaup.ai.vo.result.CreateOrderResult;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.javaup.ai.constants.DaMaiConstant.CREATE_ORDER_URL;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 订单服务类
 * @author: 阿星不是程序员
 **/
@Component
public class OrderCall {
    
    public String createOrder(ProgramOrderCreateDto programOrderCreateDto){
        CreateOrderResult createOrderResult = new CreateOrderResult();
        String result = HttpRequest.post(CREATE_ORDER_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(programOrderCreateDto))
                .timeout(20000)
                .execute().body();
        createOrderResult = JSON.parseObject(result, CreateOrderResult.class);
        if (!Objects.equals(createOrderResult.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统创建订单失败");
        }
        return createOrderResult.getData();
    }
}
