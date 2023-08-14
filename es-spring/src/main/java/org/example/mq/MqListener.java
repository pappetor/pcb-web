package org.example.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.xcontent.XContentType;
import org.example.dao.OverviewDao;
import org.example.entity.*;
import org.example.service.PcbImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class MqListener {
    @Autowired
    private OverviewDao overviewDao;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private PcbImageService pcbImageService;

    @KafkaListener(topics = {"production_line_1"}, groupId = "group5")
    public void onMessage(ConsumerRecord<?, ?> record, Acknowledgment ack,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        System.out.println("消费消息：" + record.value());
        saveMessage(record.value().toString());
        //确认消息
        ack.acknowledge();
    }
    public void saveMessage(String record) {
        JSONObject object = JSONObject.parseObject(record);
        String results = (String) object.get("results");
        String srcImg = (String) object.get("src_img");
        String detedImg = (String) object.get("deted_img");
        PcbImage pcbImage = new PcbImage();
        pcbImage.setPath(detedImg);
        pcbImageService.save(pcbImage);
        String linenumber = (String) object.get("linenumber");
        System.out.println(linenumber);
        String detedTime = (String) object.get("deted_time");
        String substring = detedTime.substring(0, detedTime.indexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(substring);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Overview overview = overviewDao.selectById("646e01acb880751f77f09e61");
        Doc doc = new Doc();
        PcbDefectLogs pcbDefectLogs = new PcbDefectLogs();
        doc.setCreated(date);
        doc.setDeted_file(detedImg);
        doc.setSrc_image(srcImg);
        doc.setProduction_line(linenumber);
        if (results.equals("[]")) {
            //更新数据库
            overview.setPcbs(overview.getPcbs() + 1);
            overviewDao.updateById(overview);
        } else {
            //更新数据库
            overview.setDefects(overview.getDefects() + 1);
            overview.setPcbs(overview.getPcbs() + 1);
            overviewDao.updateById(overview);
            results = results.replace("[","");
            results = results.replace("]","");
            String[] split = results.split(",");
            List<Results> resultsList = new ArrayList<>();
            for (int i = 0; i < split.length / 4; i++) {
                Results r = new Results();
                String conf = split[i * 4];
                r.setConf(conf);
                String label = split[i * 4 + 1];
                r.setLabel(label);
                String pcb = split[i * 4 + 2];
                r.setPcb(pcb);
                String part = split[i * 4 + 3];
                r.setPart("料号:" + part);
                resultsList.add(r);
            }
            doc.setResults(resultsList);
        }
        pcbDefectLogs.setDoc(doc);
        IndexRequest request = new IndexRequest("pcb_defect_logs");
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        String s = JSON.toJSONString(pcbDefectLogs, SerializerFeature.WriteDateUseDateFormat);
        System.out.println("封装的消息" + s);
        request.source(s, XContentType.JSON);
        try {
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        }
        catch (Exception e){
        }
    }

}
