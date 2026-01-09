package org.javaup.ai.ai.function;

import cn.hutool.core.collection.CollectionUtil;
import org.javaup.ai.ai.function.dto.CreateOrderFunctionDto;
import org.javaup.ai.ai.function.dto.ProgramRecommendFunctionDto;
import org.javaup.ai.ai.function.dto.ProgramSearchFunctionDto;
import org.javaup.ai.dto.ProgramDetailDto;
import org.javaup.ai.dto.ProgramOrderCreateDto;
import org.javaup.ai.dto.TicketCategoryListByProgramDto;
import org.javaup.ai.ai.function.call.OrderCall;
import org.javaup.ai.ai.function.call.ProgramCall;
import org.javaup.ai.ai.function.call.TicketCategoryCall;
import org.javaup.ai.ai.function.call.UserCall;
import org.javaup.ai.utils.StringUtil;
import org.javaup.ai.vo.CreateOrderVo;
import org.javaup.ai.vo.ProgramDetailVo;
import org.javaup.ai.vo.ProgramSearchVo;
import org.javaup.ai.vo.TicketCategoryDetailVo;
import org.javaup.ai.vo.TicketCategoryVo;
import org.javaup.ai.vo.TicketUserVo;
import org.javaup.ai.vo.UserDetailVo;
import org.javaup.ai.vo.result.ProgramDetailResultVo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.javaup.ai.constants.DaMaiConstant.ORDER_LIST_ADDRESS;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: ai执行 dto
 * @author: 阿星不是程序员
 **/
@Component
public class AiProgram {

    @Autowired
    private ProgramCall programCall;

    @Autowired
    private TicketCategoryCall ticketCategoryCall;
    
    @Autowired
    private UserCall userCall;
    
    @Autowired
    private OrderCall orderCall;
    
    @Tool(description = "根据地区或者类型查询推荐的节目")
    public List<ProgramSearchVo> selectProgramRecommendList(@ToolParam(description = "查询的条件", required = true) ProgramRecommendFunctionDto programRecommendFunctionDto){
        return programCall.recommendList(programRecommendFunctionDto);
    }

    @Tool(description = "根据条件查询节目")
    public List<ProgramSearchVo> selectProgramList(@ToolParam(description = "查询的条件", required = true) ProgramSearchFunctionDto programSearchFunctionDto){
        return programCall.search(programSearchFunctionDto);
    }
    
    @Tool(description = "根据条件查询节目和演唱会的详情")
    public ProgramDetailVo detail(@ToolParam(description = "查询的条件", required = true) ProgramSearchFunctionDto programSearchFunctionDto){
        return selectTicketCategory(programSearchFunctionDto);
    }

    @Tool(description = "根据条件查询节目和演唱会的票档信息")
    public ProgramDetailVo selectTicketCategory(@ToolParam(description = "查询的条件", required = true) ProgramSearchFunctionDto programSearchFunctionDto){
        List<ProgramSearchVo> programSearchVoList = programCall.search(programSearchFunctionDto);
        if (CollectionUtil.isEmpty(programSearchVoList)) {
            return null;
        }
        ProgramSearchVo programSearchVo = programSearchVoList.get(0);
        ProgramDetailDto programDetailDto = new ProgramDetailDto();
        programDetailDto.setId(programSearchVo.getId());
        ProgramDetailResultVo programDetailResultVo = programCall.detail(programDetailDto);
        if (Objects.isNull(programDetailResultVo.getData())) {
            return null;
        }
        ProgramDetailVo programDetailVo = programDetailResultVo.getData();
        TicketCategoryListByProgramDto ticketCategoryListByProgramDto = new TicketCategoryListByProgramDto();
        ticketCategoryListByProgramDto.setProgramId(programDetailVo.getId());
        List<TicketCategoryDetailVo> ticketCategoryDetailVoList = ticketCategoryCall.selectListByProgram(ticketCategoryListByProgramDto);
        Map<Long, TicketCategoryDetailVo> ticketCategoryDetailMap = ticketCategoryDetailVoList.stream()
                .collect(Collectors.toMap(TicketCategoryDetailVo::getId,
                        ticketCategoryDetailVo -> ticketCategoryDetailVo,
                        (v1, v2) -> v2));
        for (TicketCategoryVo ticketCategoryVo : programDetailVo.getTicketCategoryVoList()) {
            TicketCategoryDetailVo ticketCategoryDetailVo = ticketCategoryDetailMap.get(ticketCategoryVo.getId());
            if (Objects.nonNull(ticketCategoryDetailVo)) {
                ticketCategoryVo.setRemainNumber(ticketCategoryDetailVo.getRemainNumber());
                ticketCategoryVo.setTotalNumber(ticketCategoryDetailVo.getTotalNumber());
            }
        }
        return programDetailVo;
    }
    
    @Tool(description = "生成用户购买节目的订单，返回订单号")
    public CreateOrderVo createOrder(@ToolParam(description = "查询的条件", required = true) CreateOrderFunctionDto createOrderFunctionDto){
        ProgramSearchFunctionDto programSearchFunctionDto = new ProgramSearchFunctionDto();
        BeanUtils.copyProperties(createOrderFunctionDto, programSearchFunctionDto);
        ProgramDetailVo programDetailVo = selectTicketCategory(programSearchFunctionDto);
        if (Objects.isNull(programDetailVo)) {
            throw new RuntimeException("没有查询到节目，请检查查询条件是否正确");
        }
        UserDetailVo userDetailVo = userCall.userDetail(createOrderFunctionDto.getMobile());
        if (Objects.isNull(userDetailVo)) {
            throw new RuntimeException("用户信息不存在");
        }
        List<TicketUserVo> ticketUserVoList = userCall.ticketUserList(userDetailVo.getId());
        if (CollectionUtil.isEmpty(ticketUserVoList)) {
            throw new RuntimeException("购票人信息不存在");
        }
        List<TicketUserVo> ticketUserVoFilterList = new ArrayList<>();
        for (final TicketUserVo ticketUserVo : ticketUserVoList) {
            for (final String number : createOrderFunctionDto.getTicketUserNumberList()) {
                String ticketUserNumberFirst = StringUtil.getFirstN(ticketUserVo.getIdNumber(),4);
                String ticketUserNumberLast = StringUtil.getLastN(ticketUserVo.getIdNumber(),4);
                
                String paramNumberFirst = StringUtil.getFirstN(number,4);
                String paramNumberLast = StringUtil.getLastN(number,4);
                
                if (ticketUserNumberFirst.equals(paramNumberFirst) && ticketUserNumberLast.equals(paramNumberLast)) {
                    ticketUserVoFilterList.add(ticketUserVo);
                }
            }
        }
        if (ticketUserVoFilterList.size() != createOrderFunctionDto.getTicketUserNumberList().size()) {
            throw new RuntimeException("购票人信息不完整，请检查购票人信息是否正确");
        }
        Long ticketCategoryId = null;
        for (final TicketCategoryVo ticketCategoryVo : programDetailVo.getTicketCategoryVoList()) {
            if (createOrderFunctionDto.getTicketCategoryPrice().compareTo(ticketCategoryVo.getPrice()) == 0) {
                ticketCategoryId = ticketCategoryVo.getId();
                break;
            }
        }
        if (Objects.isNull(ticketCategoryId)) {
            throw new RuntimeException("没有查询到对应的票档信息");
        }
        ProgramOrderCreateDto programOrderCreateDto = new ProgramOrderCreateDto();
        programOrderCreateDto.setProgramId(programDetailVo.getId());
        programOrderCreateDto.setUserId(userDetailVo.getId());
        programOrderCreateDto.setTicketUserIdList(ticketUserVoFilterList.stream().map(TicketUserVo::getId).collect(Collectors.toList()));
        programOrderCreateDto.setTicketCategoryId(ticketCategoryId);
        programOrderCreateDto.setTicketCount(createOrderFunctionDto.getTicketCount());
        String orderNumber = orderCall.createOrder(programOrderCreateDto);
        CreateOrderVo createOrderVo = new CreateOrderVo();
        createOrderVo.setOrderNumber(orderNumber);
        createOrderVo.setOrderListAddress(ORDER_LIST_ADDRESS);
        return createOrderVo;
    }
}
