package org.javaup.mcp;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 这是一个独立运行的MCP Server
 * @author: 阿星不是程序员
 **/
@SpringBootApplication
@EsMapperScan("org.javaup.mcp.mapper")
public class DaMaiMcpLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaMaiMcpLogApplication.class, args);
    }
}
