package com.es.config;

/**
 * @author fanqie
 * @ClassName ElasticsearchConfig2
 * @date 2021/7/30 下午4:41
 **/

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

/**
 * init es client config
 */
@Configuration
@ConfigurationProperties(prefix = "spring.elasticsearch.jest")
@ConditionalOnProperty(prefix = "commons.oauth2.server",name = "esEnable",havingValue = "true")
@Slf4j
public class ElasticsearchConfig2 {

    @Setter
    private static String uris = ""; // 集群地址，多个用,隔开

    private static ArrayList<HttpHost> hostList = null;
    private static int connectTimeOut = 2000; // 连接超时时间
    private static int socketTimeOut = 40000; // 连接超时时间
    private static int connectionRequestTimeOut = 1000; // 获取连接的超时时间
    private static int maxConnectNum = 100; // 最大连接数
    private static int maxConnectPerRoute = 100; // 最大路由连接数
    @Setter
    private static String username;
    @Setter
    private static String password;


    private static ArrayList<HttpHost> getHostList() {
        if (hostList == null) {
            synchronized (ElasticsearchConfig2.class) {
                if (hostList == null) {
                    hostList = new ArrayList<>();
                    if (StringUtils.isEmpty(uris)) {
                        String[] hostStrs = uris.split(",");
                        for (String host : hostStrs) {
                            hostList.add(HttpHost.create(host));
                        }
                    }
                }
            }
        }
        return hostList;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(getHostList().toArray(new HttpHost[0]));
        // 异步httpclient连接延时配置
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(connectTimeOut);
                requestConfigBuilder.setSocketTimeout(socketTimeOut);
                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
                return requestConfigBuilder;
            }
        });
        // 异步httpclient连接数配置
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                if (StringUtils.isEmpty(username)) {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password));  //es账号密码（默认用户名为elastic）
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                httpClientBuilder.setMaxConnTotal(maxConnectNum);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                return httpClientBuilder;
            }
        });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

    @Bean
    public BulkProcessor bulkProcessor() {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                log.info("序号：{} ，开始执行 {} 条数据批量操作。", executionId, request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                // 在每次执行BulkRequest后调用，通过此方法可以获取BulkResponse是否包含错误
                if (response.hasFailures()) {
                    log.info("Bulk {} executed with failures,{}", executionId, response.buildFailureMessage());
                } else {
                    log.info("序号：{} ，执行 {} 条数据批量操作成功，共耗费{}毫秒。", executionId, request.numberOfActions(), response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

                log.warn("序号：{} 批量操作失败，总记录数：{} ，报错信息为：{}", executionId, request.numberOfActions(), failure.getMessage());
            }
        };

        return BulkProcessor.builder((request, bulkListener) ->
                restHighLevelClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener)
                .setBulkActions(2000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .setConcurrentRequests(2)
                .build();
    }

}