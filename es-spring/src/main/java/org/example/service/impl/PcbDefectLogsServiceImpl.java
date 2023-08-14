package org.example.service.impl;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.aggregations.pipeline.SumBucketPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.example.service.PcbDefectLogsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PcbDefectLogsServiceImpl implements PcbDefectLogsService {
    @Autowired
    private RestHighLevelClient esClient;
    public List<byte[]> smbGet(String[] remoteUrl, NtlmPasswordAuthentication auth) {
        List<byte[]> list = new ArrayList<>();
        for (String s : remoteUrl) {
            byte[] bytes = {};
            InputStream is = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                SmbFile remoteFile = new SmbFile(s, auth);
                if (remoteFile == null) {
                    System.out.println("共享文件不存在");
                    return null;
                }
                is = remoteFile.getInputStream();
                IOUtils.copy(is, baos);
                bytes = baos.toByteArray();
                list.add(bytes);
            } catch (Exception e) {
                log.info("文件从共享目录下载失败", e);
            } finally {
                if (null != baos){
                    try {
                        baos.close();
                    } catch (IOException e) {
                        log.info("文件从共享目录下载失败", e);
                    }
                }

                if (null != is){
                    try {
                        is.close();
                    } catch (IOException e) {
                        log.info("文件从共享目录下载失败", e);
                    }
                }
            }

        }
        return list;
    }

    /**
     * 获取缺陷的所有日志结果
     *
     * @param productionLine
     * @return
     * @throws Exception
     */
    public List<List> getDefectsLogs(String productionLine) throws Exception {
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("10.100.156.13", 9200, "http"))
        );
        //构建请求
        SearchRequest request = new SearchRequest();
        request.indices("pcb_defect_logs");
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("doc.production_line.keyword", productionLine);
        SearchSourceBuilder builder = new SearchSourceBuilder().query(queryBuilder);
        builder.from(0);
        builder.size(10);
        builder.sort("doc.created", SortOrder.DESC);
        request.source(builder);
//        System.out.println(builder);
        //得到响应
        SearchResponse resonse = esClient.search(request, RequestOptions.DEFAULT);

//        System.out.println("响应结果" + resonse);
        //打印输出

        SearchHits hits = resonse.getHits();
        List<List> ans = new ArrayList<>();
        int index = 0;
        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Map doc = (Map) sourceAsMap.get("doc");
            List<Map<String, Object>> results = (List<Map<String, Object>>) doc.get("results");
            if(results != null && results.size() > 0){
                //index part pcb
                for (Map result : results) {
                    List list = new ArrayList();
                    list.add(index++);
                    list.add(result.get("pcb"));
                    list.add(result.get("part"));
                    list.add("NG");
                    ans.add(list);
                }
            }
        }
        //关闭资源
        esClient.close();
        return ans;
    }


    /**
     * 获取每条生产线上的缺陷数量
     *
     * @return
     * @throws Exception
     */
    public List<String> getProductionLineNum() throws Exception {
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("10.100.156.13", 9200, "http"))
        );
        //构建请求
        SearchRequest request = new SearchRequest();
        request.indices("pcb_defect_logs");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("count").field("doc.production_line.keyword");
        builder.aggregation(aggregationBuilder);
        builder.size(0);
        request.source(builder);
        //得到响应
        SearchResponse resonse = esClient.search(request, RequestOptions.DEFAULT);
//        System.out.println("响应结果" + resonse);
        Aggregations aggregations = resonse.getAggregations();
        Terms aggTerms = aggregations.get("count");
        List<? extends Terms.Bucket> buckets = aggTerms.getBuckets();
        List<String> ans = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            String key = bucket.getKeyAsString();
            ans.add(key);
        }
        //关闭资源
        esClient.close();
        return ans;
    }


    /**
     * 获取缺陷最高的三类缺陷以及他们的数量
     *
     * @return
     * @throws Exception
     */
    public List<Map> getTop3DefectsLogsAll() throws Exception {
        Map<String, String> pcbMap = new HashMap<String, String>() {
            {
                this.put("offset", "偏移");
                this.put("miss", "缺件");
                this.put("Polytin", "多锡");
                this.put("Little_Tin", "少锡");
                this.put("Faulty_Soldering", "错件");
                this.put("bridging", "桥接");
                this.put("libei", "立碑");
                this.put("other", "其他");
            }
        };
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("10.100.156.13", 9200, "http"))
        );
        //构建请求
        SearchRequest request = new SearchRequest();
        request.indices("pcb_defect_logs");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("count").field("doc.results.label.keyword").size(3);
        builder.aggregation(aggregationBuilder);
//        System.out.println(builder.toString());
        request.source(builder);
        //得到响应
        SearchResponse resonse = esClient.search(request, RequestOptions.DEFAULT);
//        System.out.println("响应结果" + resonse);
        //打印输出
        List<Map> list = new ArrayList();
        Aggregations aggregations = resonse.getAggregations();
        Terms aggTerms = aggregations.get("count");
        List<? extends Terms.Bucket> buckets = aggTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Map tempMap = new HashMap();
            String key = bucket.getKeyAsString();
            key = key.replace("\"","");
            key = key.replace(" ","");
            tempMap.put("name", pcbMap.get(key));
            list.add(tempMap);
        }
        //关闭资源
        esClient.close();
        return list;
    }


    /**
     * 获取今日的缺陷总数
     *
     * @throws Exception
     */
    public List getTodayIncrease() throws Exception {
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("10.100.156.13", 9200, "http"))
        );
        //构建请求
        SearchRequest request = new SearchRequest();
        request.indices("pcb_defect_logs");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("doc.created", "now/d");
        builder.query(queryBuilder);
        builder.size(0);
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("count").field("doc.results.label.keyword").size(10);
        SumBucketPipelineAggregationBuilder sumBucket = new SumBucketPipelineAggregationBuilder("sum_all_buckets", "count>_count");
        builder.aggregation(aggregationBuilder).aggregation(sumBucket);
        request.source(builder);
        //得到响应
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
//        System.out.println("响应结果" + response);
        Aggregations aggregations = response.getAggregations();

        ParsedSimpleValue sumAllBuckets = aggregations.get("sum_all_buckets");
        Double value = sumAllBuckets.value();
//        System.out.println(value);
        List ans = new ArrayList();
        ans.add(value);
        //关闭资源
        esClient.close();
        return ans;
    }



    /**
     * 获取所有的缺陷总数
     *
     * @throws Exception
     */
    public List getDefectsLogsAll() throws Exception {
        Map<String, String> pcbMap = new HashMap<String, String>() {
            {
                this.put("offset", "偏移");
                this.put("miss", "缺件");
                this.put("Polytin", "多锡");
                this.put("Little_Tin", "少锡");
                this.put("Faulty_Soldering", "错件");
                this.put("bridging", "桥接");
                this.put("libei", "立碑");
                this.put("other", "其他");
            }
        };
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("10.100.156.13", 9200, "http"))
        );
        //构建请求
        SearchRequest request = new SearchRequest();
        request.indices("pcb_defect_logs");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(0);
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("count").field("doc.results.label.keyword").size(10);
        SumBucketPipelineAggregationBuilder sumBucket = new SumBucketPipelineAggregationBuilder("sum_all_buckets", "count>_count");
        builder.aggregation(aggregationBuilder).aggregation(sumBucket);
        request.source(builder);
        //得到响应
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
//        System.out.println("响应结果" + response);

        Aggregations aggregations = response.getAggregations();
        Terms aggTerms = aggregations.get("count");
        List<? extends Terms.Bucket> buckets = aggTerms.getBuckets();
        List<Map> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            Map<String, Object> map = new HashMap<>();
            String key = bucket.getKeyAsString();
            key = key.replace("\"","");
            key = key.replace(" ","");
            long value = bucket.getDocCount();
            map.put("name", pcbMap.get(key));
            map.put("value", value);
            list.add(map);
//            System.out.println(key + ":" + value);
        }

        ParsedSimpleValue sumAllBuckets = aggregations.get("sum_all_buckets");
        Double value = sumAllBuckets.value();
//        System.out.println(value);
        List ans = new ArrayList();
        ans.add(list);
        ans.add(value);
        //关闭资源
        esClient.close();
        return ans;
    }



    /**
     * 获取指定生产线上前五数量的缺陷类型及其数据
     *
     * @throws Exception
     */
    public List<Object> getTop5DefectsLogsBuyProductionLine(String productionLine) throws Exception {
        Map<String, String> pcbMap = new HashMap<String, String>() {
            {
                this.put("offset", "偏移");
                this.put("miss", "缺件");
                this.put("Polytin", "多锡");
                this.put("Little_Tin", "少锡");
                this.put("Faulty_Soldering", "错件");
                this.put("bridging", "桥接");
                this.put("libei", "立碑");
                this.put("other", "其他");
            }
        };
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("10.100.156.13", 9200, "http"))
        );
        //构建请求
        SearchRequest request = new SearchRequest();
        request.indices("pcb_defect_logs");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("doc.created").gte("now-30d/d"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("doc.production_line.keyword", productionLine));
        builder.query(boolQueryBuilder);
        builder.size(0);
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("count").field("doc.results.label.keyword").size(5);
        SumBucketPipelineAggregationBuilder sumBucketAggregation = new SumBucketPipelineAggregationBuilder("sum_all_buckets", "count>_count");
        builder.aggregation(aggregationBuilder).aggregation(sumBucketAggregation);
        request.source(builder);
        //得到响应
        SearchResponse resonse = esClient.search(request, RequestOptions.DEFAULT);
//        System.out.println("响应结果" + resonse);
        //打印输出
        Terms aggTerms = resonse.getAggregations().get("count");
        List<? extends Terms.Bucket> buckets = aggTerms.getBuckets();
        List<Object> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            Map<String, Object> map = new HashMap<>();
            String key = bucket.getKeyAsString();
            key = key.replace("\"","");
            key = key.replace(" ","");
            long val = bucket.getDocCount();
            map.put("name", pcbMap.get(key));
            map.put("num", val);
            list.add(map);
        }
        ParsedSimpleValue sumAllBuckets = resonse.getAggregations().get("sum_all_buckets");
        Double value = sumAllBuckets.value();
        list.add(value);
//        System.out.println(value);
        //关闭资源
        esClient.close();
        return list;
    }


    /**
     * 统计输入天数之内的缺陷
     *
     * @param day
     * @throws Exception
     */
    public Double getAllByDate(int day) throws Exception {
        String strDay = "now-" + day + "d/d";
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("10.100.156.13", 9200, "http"))
        );
        //构建请求
        SearchRequest request = new SearchRequest();
        request.indices("pcb_defect_logs");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("doc.created").gte(strDay));
        builder.query(boolQueryBuilder);
        builder.size(0);
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("terms_agg").field("doc.results.label.keyword").size(10);
        SumBucketPipelineAggregationBuilder sumBucketAggregation = new SumBucketPipelineAggregationBuilder("sum_all_buckets", "terms_agg>_count");
        builder.aggregation(aggregationBuilder).aggregation(sumBucketAggregation);
        request.source(builder);
        //得到响应
        SearchResponse resonse = esClient.search(request, RequestOptions.DEFAULT);
//        System.out.println("响应结果" + resonse);
        //打印输出
        Aggregations aggregations = resonse.getAggregations();
        ParsedSimpleValue sumAllBuckets = aggregations.get("sum_all_buckets");
        Double value = sumAllBuckets.value();
//        System.out.println(value);
        //关闭资源
        esClient.close();
        return value;
    }


}
