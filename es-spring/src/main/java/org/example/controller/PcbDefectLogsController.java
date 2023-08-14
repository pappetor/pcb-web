package org.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jcifs.smb.NtlmPasswordAuthentication;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.example.entity.Overview;

import org.example.entity.PcbImage;
import org.example.service.OverviewService;
import org.example.service.PcbDefectLogsService;
import org.example.service.PcbImageService;
import org.example.utils.R;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class PcbDefectLogsController {
    public Integer code = 1;
    @Resource
    private PcbDefectLogsService pcbDefectLogsService;
    @Autowired
    private OverviewService overviewService;
    @Autowired
    private PcbImageService pcbImageService;

    @GetMapping("/hello")
    public String hello() {
        return "测试方法，hello,world";
    }


//    @GetMapping("/getImage")
//    public R getImageTest() throws Exception {
//        List list = new ArrayList<>();
//        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("10.100.156.27", "root", "123456");
//        BASE64Encoder encoder = new BASE64Encoder();
//        String[] remoteUrl = new String[]{"file://10.100.156.27/defect_procuder_1/2023-06-30-20/0211fef3-9893-4173-8fe1-743bed6806d0_src_1688127850194.jpg",
//                "file://10.100.156.27/defect_procuder_1/2023-06-30-20/1d7b5983-9e1c-4031-8ddb-53fef803db37_src_1688127844217.jpg",
//                "file://10.100.156.27/defect_procuder_1/2023-06-28-21/0acdd03b-5445-4abd-ad1c-532fa59be23f_src_1687958309690.jpg"};
//        List<byte[]> imgs = pcbDefectLogsService.smbGet(remoteUrl, auth);
//        for (byte[] img : imgs) {
//            String encode = encoder.encode(img);
//            list.add(encode);
//        }
//        System.out.println("方法结束");
//        return R.ok().put("images", list);
//    }
    @GetMapping("/getImage")
    public R getImage() throws Exception {
        System.out.println("方法开始");
        List list = new ArrayList<>();
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("10.100.156.27", "root", "123456");
        BASE64Encoder encoder = new BASE64Encoder();
        String baseUrl = "file://10.100.156.27/defect_procuder_1/";

        System.out.println("code:" + code);
        List<PcbImage> pcbImageList = pcbImageService.selectNewImages(code);
        String[] remoteUrl = new String[5];
        for (int i = 0; i < pcbImageList.size(); i++) {
            remoteUrl[i] = baseUrl + pcbImageList.get(i).getPath();
            System.out.println("路径" + remoteUrl[i]);
            System.out.println("id:" + pcbImageList.get(i).getId());
        }
        List<byte[]> imgs = pcbDefectLogsService.smbGet(remoteUrl, auth);
        for (byte[] img : imgs) {
            System.out.println("读取数据");
            String encode = encoder.encode(img);
            list.add(encode);
            System.out.println("方法结束");
        }
        code++;
        return R.ok().put("images", list);
    }

    @RequestMapping("/times_distribute_viewer")
    public R timesViewer() throws Exception {
        Map<String, Object> map = new HashMap<>();
        List defectsLogsAll = pcbDefectLogsService.getDefectsLogsAll();
        List<Map> distribute = (List<Map>) defectsLogsAll.get(0);
        Double all = (Double) defectsLogsAll.get(1);
        map.put("distribute", distribute);
        map.put("all", all);
        List todayIncrease = pcbDefectLogsService.getTodayIncrease();
        map.put("today", todayIncrease);
        return R.ok().put("message", map);
    }

    @RequestMapping("/monitor_line")
    public R monitorLine() throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<String> productionLineNum = pcbDefectLogsService.getProductionLineNum();
        map.put("lines", productionLineNum);
        List lineData = new ArrayList();
        for (String s : productionLineNum) {
            List<List> defectsLogs = pcbDefectLogsService.getDefectsLogs(s);
            lineData.add(defectsLogs);
        }
        map.put("line_data", lineData);
        return R.ok().put("message", map);
    }

    @RequestMapping("/statistic_viewer")
    public R statisticViewer() throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Object> list1 = new ArrayList<Object>() {{
            this.add(39);
            this.add("产线2");
        }};
        List<Object> list2 = new ArrayList<Object>() {{
            this.add(60);
            this.add("产线1");
        }};
        List<Object> list3 = new ArrayList<Object>() {{
            this.add(77);
            this.add("产线3");
        }};

        List<List> lists = new ArrayList<List>() {{
            this.add(list1);
            this.add(list2);
            this.add(list3);
        }};
        map.put("pcba", lists);
        map.put("pcb_line_all", 75);
        return R.ok().put("message", map);
    }

    @PostMapping("/times_viewer")
    public R times_viewer(@RequestBody Integer index) throws Exception {
        int[] DAYS = new int[]{365, 90, 30, 1};
        int day = DAYS[index];
        Long[] LOSES = new Long[]{306845l, 65548l, 48214l, 4512l};
        Double total = pcbDefectLogsService.getAllByDate(day);
        List list = new ArrayList();
        list.add(total);
        list.add(LOSES[index]);
        return R.ok().put("message", list);
    }

    @RequestMapping("/top_defect_lists")
    public R top_defect_lists(Integer index) throws Exception {
        Map map = new HashMap();
        List list = new ArrayList();
        List<Map> top3DefectsLogsAll = pcbDefectLogsService.getTop3DefectsLogsAll();
        map.put("top_defects", top3DefectsLogsAll);
        list.add(top3DefectsLogsAll);
        List<String> productionLineNum = pcbDefectLogsService.getProductionLineNum();
        List lineDatas = new ArrayList();
        List lines = new ArrayList();
        for (String s : productionLineNum) {
            List<Object> top5DefectsLogsBuyProductionLine = pcbDefectLogsService.getTop5DefectsLogsBuyProductionLine(s);
            Double total = (Double) top5DefectsLogsBuyProductionLine.get(top5DefectsLogsBuyProductionLine.size() - 1);
            top5DefectsLogsBuyProductionLine.remove(top5DefectsLogsBuyProductionLine.size() - 1);
            lineDatas.add(top5DefectsLogsBuyProductionLine);
            Map tempMap = new HashMap();
            tempMap.put(s, total);
            lines.add(tempMap);
        }
        map.put("datas", lineDatas);
        map.put("lines", lines);
        return R.ok().put("message", map);
    }

    @RequestMapping("/trend_viewer")
    public R trend_viewer() throws Exception {
        Double[] ng_data = new Double[]{0.3, 0.4, 0.3, 0.4, 0.3, 0.4, 0.3, 0.6, 0.2, 0.4, 0.2, 0.4, 0.3, 0.4, 0.3, 0.4, 0.3, 0.4, 0.3, 0.3, 0.2, 0.4, 0.2, 0.4};
        Double[] ok_data = new Double[]{0.5, 0.3, 0.5, 0.6, 0.1, 0.5, 0.3, 0.5, 0.6, 0.4, 0.6, 0.4, 0.8, 0.3, 0.5, 0.6, 1.0, 0.5, 0.8, 0.7, 0.6, 0.5, 1.0, 0.4};
        Queue<Double> ng_queue = new ArrayDeque<Double>(Arrays.asList(ng_data));
        Queue<Double> ok_queue = new ArrayDeque<Double>(Arrays.asList(ok_data));
        ng_queue.poll();
        Overview overview = overviewService.getById("646e01acb880751f77f09e61");
        ng_queue.offer((double) (overview.getDefects() / overview.getPcbs()));
        ok_queue.poll();
        ok_queue.offer((double) (overview.getPcbs() - overview.getDefects() / overview.getPcbs()));
        Map map = new HashMap();
        map.put("ng_data", ng_data);
        map.put("ok_data", ok_data);
        return R.ok().put("message", map);
    }

    @RequestMapping("/overviewer")
    public R overviewer() throws Exception {
        Map map = new HashMap();
        Overview overview = overviewService.getById("646e01acb880751f77f09e61");
        map.put("name", overview.getName());
        map.put("production_line", overview.getProductionLine());
        map.put("cameras", overview.getCameras());
        map.put("pcbs", overview.getPcbs());
        map.put("defects", overview.getDefects());
        return R.ok().put("message", map);
    }


}
