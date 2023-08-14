package org.example;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageTest {
    @Test
    public void test1() throws IOException {
        byte[] bytes;
        FileInputStream is = new FileInputStream("./d8d3f0a7-0770-4814-9fce-44703f7dabaa_src_1687779103091.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        bytes = baos.toByteArray();
        System.out.println(bytes.length);
        BASE64Encoder encoder = new BASE64Encoder();
        String encode = encoder.encode(bytes);
        System.out.println(encode);
    }
}
