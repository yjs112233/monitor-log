package pine.log.monitor.engine.impl;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.alibaba.druid.util.StringUtils;
import pine.log.monitor.LogGlobalException;
import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.domain.TableMonitorLog;
import pine.log.monitor.engine.DataEngine;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchDataEngine implements DataEngine {

    @Autowired(required = false)
    private ElasticsearchClient client;

    @Autowired(required = false)
    private ElasticsearchAsyncClient asyncClient;

    /**
     *  索引名称
     */
    private static final String INDEX_NAME = "monitors";


    @Override
    public String getName() {
        return "Elasticsearch";
    }

    @Override
    public void initCreate() throws IOException {
        ExistsRequest request = new ExistsRequest.Builder()
                .index(INDEX_NAME)
                .build();
        BooleanResponse booleanResponse = client.indices().exists(request);
        // if not exsit
        if (!booleanResponse.value()){
            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .build();
            CreateIndexResponse response = client.indices().create(createIndexRequest);
            if (response.acknowledged() == null || !response.acknowledged()){
                throw new LogGlobalException("ES 索引创建失败");
            }
        }
    }

    @Override
    public void insert(MonitorLog monitorLog) {
        IndexRequest<MonitorLog> indexRequest = new IndexRequest.Builder<MonitorLog>()
                .index(INDEX_NAME)
                .document(monitorLog)
                .build();

        asyncClient.index(indexRequest);
    }

    @Override
    public TableMonitorLog list(MonitorLog monitorLogBO, int page, int size) {

        SearchRequest.Builder searchBuild = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .from((page - 1) * size)
                .size(size);

        CountRequest.Builder countBuild = new CountRequest.Builder()
                .index(INDEX_NAME);


        Query query = null;
        // 条件构造
        if (!StringUtils.isEmpty(monitorLogBO.getOperType())){
            query = Query.of(m -> m.match(k -> k.field("operType").query(monitorLogBO.getOperType())));
        }

        else if (!StringUtils.isEmpty(monitorLogBO.getOperParam())){
            query = Query.of(m -> m.wildcard(WildcardQuery.of(w -> w.field("operParam")
                    .value("*" + monitorLogBO.getOperParam()+"*"))));
        }

        else if (!StringUtils.isEmpty(monitorLogBO.getJsonBody())){
            query = Query.of(m -> m.wildcard(WildcardQuery.of(w -> w.field("jsonBody")
                    .value("*" + monitorLogBO.getJsonBody()+"*"))));
        }

        SearchRequest request = null;
        CountRequest countRequest = null;
        if (query != null){
            request = searchBuild.query(query).build();
            countRequest = countBuild.query(query).build();
        }else {
            request = searchBuild.build();
            countRequest = countBuild.build();
        }

        // 查询
        List<MonitorLog> result = new ArrayList<>();
        int count = 0;
        try {
            CountResponse countResponse = client.count(countRequest);
            count = (int) countResponse.count();
            SearchResponse<MonitorLog> response = client.search(request, MonitorLog.class);
            for (Hit<MonitorLog> hit : response.hits().hits()) {
                MonitorLog monitorLog = hit.source();
                if (monitorLog != null){
                    monitorLog.setId(hit.id());
                    result.add(monitorLog);
                }
            }
        } catch (IOException e) {
            throw new LogGlobalException("查询失败:" + e.getMessage());
        }
        return new TableMonitorLog(count, result);
    }
}
