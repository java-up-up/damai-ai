package org.javaup.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 节目详情返回体
 * @author: 阿星不是程序员
 **/
@Data
public class ProgramDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name ="id", type ="Long", description ="主键id")
    private Long id;

    @Schema(name ="title", type ="Long", description ="标题")
    private String title;

    @Schema(name ="actor", type ="Long", description ="艺人")
    private String actor;

    @Schema(name ="place", type ="String", description ="地点")
    private String place;


    @Schema(name ="areaId", type ="Long", description ="区域id")
    private Long areaId;

    @Schema(name ="areaName", type ="Long", description ="区域名字")
    private String areaName;

    @Schema(name ="programCategoryId", type ="Long", description ="节目类型表id")
    private Long programCategoryId;

    @Schema(name ="programCategoryName", type ="Long", description ="节目类型表名字")
    private String programCategoryName;

    @Schema(name ="parentProgramCategoryId", type ="Long", description ="父节目类型表id")
    private Long parentProgramCategoryId;

    @Schema(name ="parentProgramCategoryName", type ="String", description ="父节目类型名字")
    private String parentProgramCategoryName;

    /**
     * 业务字段
     * */
    @Schema(name ="showTime", type ="Date", description ="演出时间")
    private Date showTime;

    @Schema(name ="showDayTime", type ="Date", description ="演出时间(精确到天)")
    private Date showDayTime;

    @Schema(name ="showWeekTime", type ="String", description ="演出时间所在的星期")
    private String showWeekTime;

    @Schema(name ="ticketCategoryVoList", type ="List<TicketCategoryVo>", description ="节目票档")
    private List<TicketCategoryVo> ticketCategoryVoList;
}
