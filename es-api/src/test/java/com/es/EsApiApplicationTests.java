package com.es;

import com.alibaba.fastjson.JSON;
import com.es.pojo.PsSealLog;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {

    private static final String TABLE_NAME = "jd_goods";
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    PsSealLog getPsSealLog() {
        PsSealLog psSealLog = new PsSealLog();
        psSealLog.setEsId("44010100001100");
        psSealLog.setSealName("发票测试章");
        psSealLog.setUserAccount("syinitadmin");
        psSealLog.setUserName("测试员");
        psSealLog.setUserId(2L);
        psSealLog.setDeptName("国脉信安");
        psSealLog.setDeptId(23L);
        psSealLog.setBusinessName("pc控件");
        psSealLog.setBusinessType("1");

        psSealLog.setSignTime(new Date());


        return psSealLog;
    }


    //测试索引的创建
    @Test
    void testCrateIndex() throws IOException {
        //1.创建索引表
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(TABLE_NAME);
        //2.客户端执行请求
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);

    }

    //测试索引是否存在
    @Test
    void testIndexIsexist() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TABLE_NAME);
        boolean flag = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println("索引是否存在：" + flag);

    }

    //删除索引
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(TABLE_NAME);
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println("删除" + delete.isAcknowledged());
    }

    //测试插入对象
    @Test
    void insertObject() throws IOException {
        PsSealLog psSealLog = getPsSealLog();

        //创建请求
        IndexRequest request = new IndexRequest(TABLE_NAME);
        request.id("3");
        request.timeout(TimeValue.timeValueSeconds(1));

        //将数据放入json
        request.source(JSON.toJSONString(psSealLog), XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println("插入数据 = " + index.toString());
        System.out.println(index.status());
    }

    //测试数据是否存在
    @Test
    void testInsertDataIsExsit() throws IOException {
        GetRequest getRequest = new GetRequest(TABLE_NAME, "44010100001100");
        //不获取返回 的上下文效率更快
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        //设置不需要的排序字段
        getRequest.storedFields("_none_");

        //客户端发送请求，判断是否有数据
        boolean flag = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println("插入的数据存在" + flag);
    }

    //测试获取数据
    @Test
    void testGetData() throws IOException {
        GetRequest getRequest = new GetRequest(TABLE_NAME, "44010100001100");

        //客户端发送请求，获取请求结果
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        String sourceAsString = getResponse.getSourceAsString();
        System.out.println("存储的数据存:" + sourceAsString);
        System.out.println("存储的数据存:" + getResponse);
    }

    //测试更新数据
    @Test
    void testUpdateData() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(TABLE_NAME, "3");
        PsSealLog psSealLog = new PsSealLog();
        psSealLog.setEsId("44010100001103");

        updateRequest.doc(JSON.toJSONString(psSealLog),XContentType.JSON);
        //客户端发送请求，获取请求结果
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println("更新数据:" + update.status());
    }


    //测试更新数据
    @Test
    void testDeleteData() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(TABLE_NAME, "3");
        //客户端发送请求，获取请求结果
        DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println("删除数据:" + delete.status());
    }


    //测试更新数据
    @Test
    void testBulkData() throws IOException {

        ArrayList<PsSealLog> objects = new ArrayList<>();
        PsSealLog psSealLog = getPsSealLog();
        objects.add(psSealLog);
        objects.add(psSealLog);
        objects.add(psSealLog);
        objects.add(psSealLog);
        objects.add(psSealLog);

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        for (PsSealLog psSealLog1 :objects
             ) {
            bulkRequest.add(new IndexRequest(TABLE_NAME).source(JSON.toJSONString(psSealLog1),XContentType.JSON));

        }
        //客户端发送请求，获取请求结果
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //插入是否失败 =  false  代表成功
        System.out.println("批量插入数据:" + bulkResponse.hasFailures());

    }

    //查询
    //SearchRequest     搜索请求
    //SearchSourceBuilder  条件搜索
    //TermQueryBuilder  精确查找
    //HighlightBuilder  构建高亮
    //matchAllQueryBuilder 匹配所有查找
    @Test
    void testSearch() throws IOException{
        SearchRequest searchRequest = new SearchRequest(TABLE_NAME);
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        new HighlightBuilder()
//        searchSourceBuilder
        //termQuery 精确查询
        //matchAllQuery 匹配所有esId": "44010100001100",
        //
        TermQueryBuilder esId = QueryBuilders.termQuery("esId", "44010100001100");
//        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(esId);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("查询结果 = " + JSON.toJSONString(search.getHits()));
        System.out.println("查询总量 + " + search.getHits().getTotalHits().value);

        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap() );
        }

    }
}
