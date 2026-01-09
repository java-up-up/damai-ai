package org.javaup.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 这是一个独立运行的MCP Server，通过stdio与主应用通信提供票务查询能力给AI，让AI能够查询节目、票档等信息
 * @author: 阿星不是程序员
 **/
@SpringBootApplication
public class DaMaiMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaMaiMcpServerApplication.class, args);
    }
}
