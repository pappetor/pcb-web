package org.example;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.example.entity.PcbImage;
import org.example.service.PcbImageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RemoteConnection {
    @Autowired
    private PcbImageService pcbImageService;

    @Test
    public void testPcbImage() {
        List<PcbImage> pcbImageList = pcbImageService.selectNewImages(3);
        for (PcbImage pcbImage : pcbImageList) {
            System.out.println(pcbImage.getId());
        }
    }
    public static byte[] smbGet(String remoteUrl, NtlmPasswordAuthentication auth) {
        byte[] bytes = {};
        InputStream is = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            SmbFile remoteFile = new SmbFile(remoteUrl, auth);
            if (remoteFile == null) {
                System.out.println("共享文件不存在");
                return null;
            }
            is = remoteFile.getInputStream();
            IOUtils.copy(is, baos);
            bytes = baos.toByteArray();
        } catch (Exception e) {
            log.info("文件从共享目录下载失败", e);
        } finally {
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.info("文件从共享目录下载失败", e);
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.info("文件从共享目录下载失败", e);
                }
            }
        }
        return bytes;
    }

    @Test
    public void testGet() throws Exception {
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("10.100.156.27", "root", "123456");
        BASE64Encoder encoder = new BASE64Encoder();
        String remoteUrl = "file://10.100.156.27/defect_procuder_1/test_grap_img1/" + 1 + ".png";
        byte[] bytes = smbGet(remoteUrl, auth);
        FileOutputStream os = new FileOutputStream(1 + ".jpg");
        os.write(bytes);
        System.out.println(bytes.length);

    }


    /**
     * Description: 从本地上传文件到共享目录
     *
     * @param remoteUrl     共享文件目录
     * @param localFilePath 本地文件绝对路径
     * @Version1.0 Sep 25, 2009 3:49:00 PM
     */
    public static String smbPut(String remoteUrl, String localFilePath, NtlmPasswordAuthentication auth) {
        String result = null;
        FileInputStream fis = null;
        try {
            File localFile = new File(localFilePath);
            localFile.setReadOnly();
            String fileName = localFile.getName();
            SmbFile remoteFile = new SmbFile(remoteUrl + "/" + fileName, auth);
            fis = new FileInputStream(localFile);
            IOUtils.copyLarge(fis, remoteFile.getOutputStream());
            result = "success";
        } catch (Exception e) {
            result = "failed";
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                log.info("文件从上传失败", e);
            }
        }
        return result;
    }

    /**
     * Description: 从共享目录下载文件
     *
     * @param remoteUrl 共享目录上的文件路径
     * @Version1.0 Sep 25, 2009 3:48:38 PM
     */
    public static void smbDel(String remoteUrl, NtlmPasswordAuthentication auth) {
        try {
            SmbFile remoteFile = new SmbFile(remoteUrl, auth);
            if (remoteFile.exists()) {
                remoteFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
