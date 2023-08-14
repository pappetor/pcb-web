package org.example;

import org.junit.Test;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MyTest {

    @Test
    public void test() throws UnsupportedEncodingException {
        String keyWord = URLDecoder.decode("\u6599", "UTF-8");
        System.out.println(keyWord);
    }


}
