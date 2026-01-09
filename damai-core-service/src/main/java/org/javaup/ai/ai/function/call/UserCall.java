package org.javaup.ai.ai.function.call;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import org.javaup.ai.enums.BaseCode;
import org.javaup.ai.vo.TicketUserVo;
import org.javaup.ai.vo.UserDetailVo;
import org.javaup.ai.vo.result.TicketUserResultVo;
import org.javaup.ai.vo.result.UserDetailResultVo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.javaup.ai.constants.DaMaiConstant.TICKET_USER_LIST_URL;
import static org.javaup.ai.constants.DaMaiConstant.USER_DETAIL_URL;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 用户服务类
 * @author: 阿星不是程序员
 **/
@Component
public class UserCall {
    
    public UserDetailVo userDetail(String mobile){
        Map<String,String> params = new HashMap<>(2);
        params.put("mobile", mobile);
        UserDetailResultVo userDetailResultVo = new UserDetailResultVo();
        String result = HttpRequest.post(USER_DETAIL_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(params))
                .timeout(20000)
                .execute().body();
        userDetailResultVo = JSON.parseObject(result, UserDetailResultVo.class);
        if (!Objects.equals(userDetailResultVo.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统查询用户信息失败");
        }
        return userDetailResultVo.getData();
    }
    
    public List<TicketUserVo> ticketUserList(Long userId){
        Map<String,Object> params = new HashMap<>(2);
        params.put("userId", userId);
        TicketUserResultVo ticketUserResultVo = new TicketUserResultVo();
        String result = HttpRequest.post(TICKET_USER_LIST_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(params))
                .timeout(20000)
                .execute().body();
        ticketUserResultVo = JSON.parseObject(result, TicketUserResultVo.class);
        if (!Objects.equals(ticketUserResultVo.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统查询购票人信息失败");
        }
        if (Objects.isNull(ticketUserResultVo.getData())) {
            throw new RuntimeException("购票人信息不存在");
        }
        return ticketUserResultVo.getData();
    }
}
