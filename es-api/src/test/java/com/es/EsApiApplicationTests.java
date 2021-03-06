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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {

    private static final String TABLE_NAME = "jd_goods";
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    PsSealLog getPsSealLog() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = "2021-08-03 10:59:27";
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateStr);
        }catch (Exception e){

        }

        PsSealLog psSealLog = new PsSealLog();
        psSealLog.setActionTime(date);
        psSealLog.setBusinessType(1);
        psSealLog.setSealId(9L);
        psSealLog.setEsId("11010100007955");
        psSealLog.setSealDeptId(2L);
        psSealLog.setSealName("1027????????????14");


        return psSealLog;
    }


    //?????????????????????
    @Test
    void testCrateIndex() throws IOException {
        //1.???????????????
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(TABLE_NAME);
        //2.?????????????????????
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);

    }

    //????????????????????????
    @Test
    void testIndexIsexist() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TABLE_NAME);
        boolean flag = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println("?????????????????????" + flag);

    }

    //????????????
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(TABLE_NAME);
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println("??????" + delete.isAcknowledged());
    }

    //??????????????????
    @Test
    void insertObject() throws IOException {
        PsSealLog psSealLog = getPsSealLog();

        //????????????
        IndexRequest request = new IndexRequest(TABLE_NAME);
//        request.id("3");
        request.timeout(TimeValue.timeValueSeconds(1));

        //???????????????json
        request.source(JSON.toJSONString(psSealLog), XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println("???????????? = " + index.toString());
        System.out.println(index.status());
    }

    //????????????????????????
    @Test
    void testInsertDataIsExsit() throws IOException {
        GetRequest getRequest = new GetRequest(TABLE_NAME, "44010100001100");
        //??????????????? ????????????????????????
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        //??????????????????????????????
        getRequest.storedFields("_none_");

        //?????????????????????????????????????????????
        boolean flag = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println("?????????????????????" + flag);
    }

    //??????????????????
    @Test
    void testGetData() throws IOException {
        GetRequest getRequest = new GetRequest(TABLE_NAME, "44010100001100");

        //??????????????????????????????????????????
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        String sourceAsString = getResponse.getSourceAsString();
        System.out.println("??????????????????:" + sourceAsString);
        System.out.println("??????????????????:" + getResponse);
    }

    //??????????????????
    @Test
    void testUpdateData() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(TABLE_NAME, "3");
        PsSealLog psSealLog = new PsSealLog();
        psSealLog.setEsId("44010100001103");

        updateRequest.doc(JSON.toJSONString(psSealLog),XContentType.JSON);
        //??????????????????????????????????????????
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println("????????????:" + update.status());
    }


    //??????????????????
    @Test
    void testDeleteData() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(TABLE_NAME, "3");
        //??????????????????????????????????????????
        DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println("????????????:" + delete.status());
    }


    //??????????????????
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
        //??????????????????????????????????????????
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //?????????????????? =  false  ????????????
        System.out.println("??????????????????:" + bulkResponse.hasFailures());

    }

    //??????
    //SearchRequest     ????????????
    //SearchSourceBuilder  ????????????
    //TermQueryBuilder  ????????????
    //HighlightBuilder  ????????????
    //matchAllQueryBuilder ??????????????????
    @Test
    void testSearch() throws IOException{
        SearchRequest searchRequest = new SearchRequest(TABLE_NAME);
        //??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        new HighlightBuilder()
//        searchSourceBuilder
        //termQuery ????????????
        //matchAllQuery ????????????esId": "44010100001100",
        //
        TermQueryBuilder esId = QueryBuilders.termQuery("esId", "44010100001100");
//        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(esId);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("???????????? = " + JSON.toJSONString(search.getHits()));
        System.out.println("???????????? + " + search.getHits().getTotalHits().value);

        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap() );
        }

    }
}
