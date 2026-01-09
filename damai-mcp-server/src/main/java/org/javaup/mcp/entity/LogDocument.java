package org.javaup.mcp.entity;

import lombok.Data;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.annotation.rely.IdType;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: ES日志文档实体类 - 对应 damai-logs-* 索引
 * @author: 阿星不是程序员
 **/
@Data
@IndexName(value = "damai-logs-*", keepGlobalPrefix = true)
public class LogDocument {

    @IndexId(type = IdType.CUSTOMIZE)
    private String id;

    /**
     * 时间戳
     */
    @IndexField(value = "@timestamp", fieldType = FieldType.DATE)
    private String timestamp;

    /**
     * 链路追踪ID
     */
    @IndexField(value = "traceId", fieldType = FieldType.KEYWORD)
    private String traceId;

    /**
     * 服务名称
     */
    @IndexField(value = "projectName", fieldType = FieldType.KEYWORD)
    private String projectName;

    /**
     * 日志级别
     */
    @IndexField(value = "level", fieldType = FieldType.KEYWORD)
    private String level;

    /**
     * 日志消息
     */
    @IndexField(value = "message", fieldType = FieldType.TEXT)
    private String message;

    /**
     * 类名
     */
    @IndexField(value = "sourceClass", fieldType = FieldType.KEYWORD)
    private String sourceClass;

    /**
     * 方法名
     */
    @IndexField(value = "sourceMethod", fieldType = FieldType.KEYWORD)
    private String sourceMethod;

    /**
     * 文件名
     */
    @IndexField(value = "sourceFile", fieldType = FieldType.KEYWORD)
    private String sourceFile;

    /**
     * 行号
     */
    @IndexField(value = "sourceLine", fieldType = FieldType.KEYWORD)
    private String sourceLine;

    /**
     * 线程名
     */
    @IndexField(value = "thread", fieldType = FieldType.KEYWORD)
    private String thread;

    /**
     * Logger名称
     */
    @IndexField(value = "loggerName", fieldType = FieldType.KEYWORD)
    private String loggerName;

    /**
     * 本地IP
     */
    @IndexField(value = "localIp", fieldType = FieldType.KEYWORD)
    private String localIp;

    /**
     * 环境
     */
    @IndexField(value = "env", fieldType = FieldType.KEYWORD)
    private String env;

    /**
     * 时间戳（毫秒）
     */
    @IndexField(value = "timeMillis", fieldType = FieldType.LONG)
    private Long timeMillis;
}
