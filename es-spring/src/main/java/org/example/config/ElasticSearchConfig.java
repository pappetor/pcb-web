package org.example.config;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.lang.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix="elasticsearch")
@Data
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {
    private String host;
    private Integer port;
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("10.100.156.39", 9200,"http"));
        RestHighLevelClient restHighLevelClient = new
                RestHighLevelClient(builder);
        return restHighLevelClient;

    }
    /**
     * 指定日期转换器，解决日期转换错误问题
     * @return
     */
    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions(){
        List<Converter<?,?>> converterList = Lists.newArrayList(StringToDateConverter.INSTANT);
        return new ElasticsearchCustomConversions(converterList);
    }

    /**
     * 字符串转换日期
     */
    private enum StringToDateConverter implements Converter<String, Date> {
        /**
         * 转换器实例
         */
        INSTANT;

        @Override
        public Date convert(@NonNull String source) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(source);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
