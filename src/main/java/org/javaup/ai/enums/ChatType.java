package org.javaup.ai.enums;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 业务类型枚举类
 * @author: 阿星不是程序员
 **/
public enum ChatType {
    /**
     * 通用状态枚举
     * */
    CHAT(1,"普通会话"),
    ASSISTANT(2,"助理智能客户"),
    MARKDOWN(3,"Markdown助手"),
    PRIVATERAG(4," 私人知识库rag 助手"),
    ;

    private final Integer code;

    private final String msg;

    ChatType(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }
    
    public String getMsg() {
        return this.msg == null ? "" : this.msg;
    }
    
    public static String getMsg(Integer code) {
        for (ChatType re : ChatType.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re.msg;
            }
        }
        return "";
    }

    public static ChatType getRc(Integer code) {
        for (ChatType re : ChatType.values()) {
            if (re.code.intValue() == code.intValue()) {
                return re;
            }
        }
        return null;
    }
}
