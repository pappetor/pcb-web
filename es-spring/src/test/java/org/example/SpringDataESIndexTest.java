package org.example;


import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.xcontent.XContentType;
import org.example.dao.OverviewDao;
import org.example.entity.*;
import org.example.service.PcbDefectLogsService;
import org.example.service.PcbImageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringDataESIndexTest {
    //注入 ElasticsearchRestTemplate
//    @Autowired
//    private PcbDefectLogsService pcbDefectLogsService;
    //创建索引并增加映射配置
    @Autowired
    private OverviewDao overviewDao;
    @Autowired
    private PcbDefectLogsService pcbDefectLogsService;

    @Test
    public void test() {
        overviewDao.selectList(null).forEach(System.out::println);
    }
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testConnection() throws Exception {
        List<List> defectsLogs = pcbDefectLogsService.getDefectsLogs("产线1");
        System.out.println(defectsLogs);
    }
    @Test
    public void test1() throws ParseException, IOException {
        String str = "{\n" +
                "\t\"src_img\": \"53c92271-2ce4-428b-9038-96fe31cda5b0_src_1686575042702.jpg\",\n" +
                "\t\"deted_img\": \"53c92271-2ce4-428b-9038-96fe31cda5b0_det_1686575042702.jpg\",\n" +
                "\t\"results\": \"[[0.53, \\\"miss\\\", \\\"PCBA_:29\\\", \\\"\\\\u6599\\\\u53f7:56300\\\"], [0.53, \\\"miss\\\", \\\"PCBA_:53\\\", \\\"\\\\u6599\\\\u53f7:78565\\\"], [0.68, \\\"miss\\\", \\\"PCBA_:48\\\", \\\"\\\\u6599\\\\u53f7:69663\\\"], [0.4, \\\"miss\\\", \\\"PCBA_:77\\\", \\\"\\\\u6599\\\\u53f7:18140\\\"], [0.5, \\\"miss\\\", \\\"PCBA_:56\\\", \\\"\\\\u6599\\\\u53f7:91896\\\"]]\",\n" +
                "\t\"deted_time\": \"2023-06-12 21:04:02.702175\",\n" +
                "\t\"linenumber\": \"产线_2\"\n" +
                "}";
//        先把String对象转换成json对象
        JSONObject object = JSONObject.parseObject(str);
        String results = (String) object.get("results");
        String srcImg = (String) object.get("src_img");
        String detedImg = (String) object.get("deted_img");
        String linenumber = (String) object.get("linenumber");
        if(linenumber.equalsIgnoreCase("产线_3")){
            linenumber = "产线3";
        }else if(linenumber.equalsIgnoreCase("产线_2")){
            linenumber = "产线2";
        }else {
            linenumber = "产线1";
        }
        String detedTime = (String) object.get("deted_time");
        String substring = detedTime.substring(0, detedTime.indexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(substring);
        Overview overview = overviewDao.selectById("646e01acb880751f77f09e61");
        Doc doc = new Doc();
        PcbDefectLogs pcbDefectLogs = new PcbDefectLogs();
        doc.setCreated(date);
        doc.setDeted_file(detedImg);
        doc.setSrc_image(srcImg);
        doc.setSrc_image("test");
        doc.setProduction_line(linenumber);
        pcbDefectLogs.setDoc(doc);
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
                r.setPart(part);
                resultsList.add(r);
            }
            doc.setResults(resultsList);
        }
        IndexRequest request = new IndexRequest("pcb_defect_logs");
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        String s = JSON.toJSONString(pcbDefectLogs, SerializerFeature.WriteDateUseDateFormat);

        request.source(s, XContentType.JSON);
        try {
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        }
        catch (Exception e){
        }

    }

    //    @Resource
//    private ProductDao productDao;
//    public String  createMessage(){
//        Product product = new Product();
//        product.setId(21l);
//        product.setImages("sdfsd");
//        product.setCategory("123");
//        product.setPrice(12.0);
//        product.setTitle("我的11");
//        try {
//            productDao.save(product);
//        } catch (Exception var12) {
//
//        }
//        return "创建成功";
//    }
//
//    @Test
//    public void test10() {
//        createMessage();
//    }
    @Test
    public void test2() throws ParseException {
        String s = "2023-06-12 21:04:02.432342";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        Date parse = sdf.parse(s);
        Date test = parse;
        System.out.println(test);
    }
    @Test
    public void login() {
        //创建远程连接，默认连接端口为22，如果不使用默认，可以使用方法
        //new Connection(ip, port)创建对象
        Connection conn = new Connection("192.168.100.105");
        try {
            //连接远程服务器
            conn.connect();
            //使用用户名和密码登录
            String user  = "root";
            String password = "atguigu";
            boolean b = conn.authenticateWithPassword(user, password);
            System.out.println("连接结果:" + b);
            SCPClient sc = new SCPClient(conn);
            try {
                String fileName = "/tmp/hello.txt";
                SCPInputStream file = sc.get(fileName);

                FileOutputStream os = new FileOutputStream(new File("D://test.txt"));
                byte[] buffer = new byte[1024];
                int len;
                while ((len = file.read(buffer)) != -1){
                    os.write(buffer);
                }
                System.out.println(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.printf("用户%s密码%s登录服务器%s失败！");
            e.printStackTrace();
        }
    }


}

