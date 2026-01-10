package org.javaup.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 大麦监控指标 MCP Server - 通过 Prometheus API 查询 JVM、内存、线程等指标
 * @author: 阿星不是程序员
 **/
@SpringBootApplication
public class DaMaiMcpMetricsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaMaiMcpMetricsApplication.class, args);
    }
}
