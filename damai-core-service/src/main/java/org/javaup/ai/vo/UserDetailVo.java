package org.javaup.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 用户数据
 * @author: 阿星不是程序员
 **/
@Data
@Schema(title="UserDetailVo", description ="用户数据")
public class UserDetailVo {
    
    @Schema(name ="id", type ="String", description ="用户id")
    private Long id;
    
    @Schema(name ="name", type ="String", description ="用户名字")
    private String name;
    
    @Schema(name ="relName", type ="String", description ="用户真实名字")
    private String relName;
    
    @Schema(name ="gender", type ="Integer", description ="1:男 2:女")
    private Integer gender;
    
    @Schema(name ="name", type ="String", description ="用户手机号")
    private String mobile;
    
    @Schema(name ="emailStatus", type ="Integer", description ="是否邮箱认证 1:已验证 0:未验证")
    private Integer emailStatus;
    
    @Schema(name ="email", type ="String", description ="邮箱地址")
    private String email;
    
    @Schema(name ="relAuthenticationStatus", type ="Integer", description ="是否实名认证 1:已验证 0:未验证")
    private Integer relAuthenticationStatus;
    
    @Schema(name ="idNumber", type ="String", description ="身份证号码")
    private String idNumber;
    
    @Schema(name ="address", type ="String", description ="收货地址")
    private String address;
}
