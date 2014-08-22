package com.yeezhao.hound.runner;

import junit.framework.TestCase;

/**
 * Created by zhibin on 14-8-20.
 */
public class ParserTest extends TestCase {
    public void testGenerateUUIDWith16Bit() throws Exception {
      Parser parser = new Parser();
        System.out.println(parser.generateUUIDWith16Bit("1000plan艾春荣"));
    }
}
