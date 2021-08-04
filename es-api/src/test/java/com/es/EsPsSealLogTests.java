package com.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.es.pojo.PsSealLog;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author fanqie
 * @ClassName EsPsSealLogTests
 * @date 2021/7/30 下午1:43
 **/
@SpringBootTest
public class EsPsSealLogTests {

    private static final String TABLE_NAME = "ps_seal_log";
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    PsSealLog getPsSealLog() {

        PsSealLog psSealLog = new PsSealLog();
        psSealLog.setSealId(9L);
        psSealLog.setEsId("11010100007955");
        psSealLog.setSealName("1027测试印章14");
        psSealLog.setSealDeptId(2L);
        psSealLog.setSealDeptName("国脉广州分公司");
        psSealLog.setUserId(2L);
        psSealLog.setSealName("发苶");
        psSealLog.setSealDeptId(2L);
        psSealLog.setUserDeptName("国脉信安");
        psSealLog.setBusinessType(1);
        psSealLog.setActionTimeStamp(getCurrentDate());
        psSealLog.setActionTime(getCurrentDate());
        psSealLog.setStatus(1);



        return psSealLog;
    }
    public static Date getCurrentDate() {
        LocalDateTime datetime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        ZonedDateTime zdt = datetime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }


    //删除索引
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(TABLE_NAME);
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println("删除" + delete.isAcknowledged());
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

    @Test
    void testIndexIsexist() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TABLE_NAME);
        boolean flag = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println("索引是否存在：" + flag);

    }




    //测试插入对象
    @Test
    void insertObject() throws IOException {
        PsSealLog psSealLog = getPsSealLog();

        //创建请求
        IndexRequest request = new IndexRequest(TABLE_NAME);
//        request.id("3");
        request.timeout(TimeValue.timeValueSeconds(1));
        String a = JSONObject.toJSONString(psSealLog);
        //将数据放入json
        request.source(a, XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println("插入数据 = " + index.toString());
        System.out.println(index.status());
    }
}
