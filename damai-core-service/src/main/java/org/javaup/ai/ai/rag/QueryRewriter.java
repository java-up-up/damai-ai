package org.javaup.ai.ai.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * @program: 大麦-ai智能服务项目。 添加 阿星不是程序员 微信，添加时备注 ai 来获取项目的完整资料 
 * @description: 查询重写器
 * @author: 阿星不是程序员
 **/
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(DeepSeekChatModel model) {
        PromptTemplate template = PromptTemplate.builder().template("""
                你是演出/演唱会退票规则专家。
                请将用户查询重写为最适合用在 {target} 中检索演出门票退票规则的版本。

                用户原始查询："{query}"

                注意：不要提及机票、火车票等交通票务，只专注演唱会/演出门票退票规则。
                """).build();
        // 创建查询重写转换器
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(model))
                .promptTemplate(template)
                .targetSearchSystem("节目和演唱会规则向量库")
                .build();
    }


    public String doQueryRewrite(String prompt) {
        // 执行查询重写
        return queryTransformer.transform(new Query(prompt)).text();
    }
}
