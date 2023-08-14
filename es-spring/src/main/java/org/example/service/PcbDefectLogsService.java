package org.example.service;

import jcifs.smb.NtlmPasswordAuthentication;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.aggregations.pipeline.SumBucketPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PcbDefectLogsService {

    /**
     * 获取缺陷的所有日志结果
     *
     * @param productionLine
     * @return
     * @throws Exception
     */
    public List<List> getDefectsLogs(String productionLine) throws Exception ;


    /**
     * 获取每条生产线上的缺陷数量
     *
     * @return
     * @throws Exception
     */
    public List<String> getProductionLineNum() throws Exception;


    /**
     * 获取缺陷最高的三类缺陷以及他们的数量
     * @return
     * @throws Exception
     */
    public List<Map> getTop3DefectsLogsAll() throws Exception;

    /**
     *获取今日的缺陷总数
     *
     * @throws Exception
     */
    public List getTodayIncrease() throws Exception;

    /**
     * 获取所有的缺陷总数
     *
     * @throws Exception
     */
    public List getDefectsLogsAll() throws Exception;

    /**
     *
     *获取指定生产线上前五数量的缺陷类型及其数据
     * @throws Exception
     */
    public List<Object> getTop5DefectsLogsBuyProductionLine(String productionLine) throws Exception;

    /**
     *
     *统计输入天数之内的缺陷
     * @param day
     * @throws Exception
     */
    public Double getAllByDate(int day) throws Exception;
    public List<byte[]> smbGet(String[] remoteUrl, NtlmPasswordAuthentication auth);

}
