package org.javaup.ai.ai.function.call;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import org.javaup.ai.dto.TicketCategoryListByProgramDto;
import org.javaup.ai.enums.BaseCode;
import org.javaup.ai.vo.TicketCategoryDetailVo;
import org.javaup.ai.vo.result.TicketCategoryListResultVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static org.javaup.ai.constants.DaMaiConstant.TICKET_LIST_URL;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 票档服务类
 * @author: 阿星不是程序员
 **/
@Component
public class TicketCategoryCall {

    public List<TicketCategoryDetailVo> selectListByProgram(TicketCategoryListByProgramDto ticketCategoryListByProgramDto) {
        String result = HttpRequest.post(TICKET_LIST_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(ticketCategoryListByProgramDto))
                .timeout(20000)
                .execute().body();
        TicketCategoryListResultVo ticketCategoryListResultVo = JSON.parseObject(result, TicketCategoryListResultVo.class);
        if (!Objects.equals(ticketCategoryListResultVo.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统查询票档信息失败");
        }
        if (CollectionUtil.isEmpty(ticketCategoryListResultVo.getData())) {
            throw new RuntimeException("票档信息不存在");
        }
        return ticketCategoryListResultVo.getData();
    }
}
