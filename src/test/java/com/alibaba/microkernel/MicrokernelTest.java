package com.alibaba.microkernel;

import com.alibaba.microkernel.mock.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class MicrokernelTest {

    @Test
    public void testMicrokernel() {
        try (Microkernel microkernel = Microkernel.getMicrokernel()) {

            assertNotNull(microkernel);
            assertEquals(TestMicrokernel.class, microkernel.getClass());
            assertEquals("ok", ((TestMicrokernel) microkernel).getMicrokernelStatus());

            assertNull(microkernel.create(String.class));
            assertNull(microkernel.create(String.class, ""));

            Aaa aaa = microkernel.create(Aaa.class);
            assertNotNull(aaa);
            assertEquals(aaa.getClass(), Aaa1Aaa.class); // SPI注入
            assertThat(aaa.getFileValues()).containsExactly("abc", "xyz");
            assertEquals("loaded", aaa.getTestConfig()); // Configurer注入
            assertFalse(aaa.isClosed());
            //assertEquals(aaa.getMicrokernel(), microkernel); // 注入Microkernel本身

            Bbb bbb = aaa.getBbb();
            assertNotNull(bbb);
            assertEquals(Bbb1Bbb.class, bbb.getClass());
            assertEquals("test", bbb.getApplicationName()); // 配置注入
            assertEquals(20882, bbb.getDefaultPort()); // 配置覆盖注入：plugintest-debug > plugintest > plugintest-default
            assertEquals("GBK", bbb.getInputEncoding());
            assertEquals("GBK", bbb.getOutputEncoding()); // 配置引用注入output.encoding=$input.encoding

            assertThat(bbb.getKeywords()).containsExactly("www", "xxx", "yyy", "zzz"); // 数组配置注入
            assertTrue(bbb.isInit()); // init初始化方法被调用

            Ddd[] allDdd = bbb.getAllDdd();
            assertThat(allDdd).isNotNull()
                    .extracting(d -> (Class) d.getClass())
                    .containsExactly(Ddd1Ddd.class, Ddd2Ddd.class, Ddd4Ddd.class, Ddd5Ddd.class);

            Ccc ccc = bbb.getCcc(); // 多层注入
            assertNotNull(ccc);
            assertEquals(ccc.getClass(), Wrapper1Ccc.class);
            assertNotNull(((Wrapper1Ccc) ccc).getCcc());
            assertEquals(((Wrapper1Ccc) ccc).getCcc().getClass(), Ccc1Ccc.class);
            assertEquals(ccc.getDefaultTimeout(), 100L); // 数字注入
            assertEquals(ccc.getCccApplicationName(), "test2"); // 前缀区隔注入：ccc.application.name=test2
            assertEquals(ccc.getAaa(), aaa); // 循环引用

            Ddd[] ddd = ccc.getDdd(); // 数组注入
            assertNotNull(ddd);
            // Ddd3的init返回false，不被加载
            // Ddd2在配置plugintest.properties中ddd-=ddd2被去除
            // Ddd2在配置plugintest.properties中ddd+=ddd4被加入
            // Ddd5在配置plugintest-debug.properties中ddd+=ddd5被加入
            assertThat(ddd).extracting(d -> (Class) d.getClass())
                    .containsExactly(Ddd1Ddd.class, Ddd4Ddd.class, Ddd5Ddd.class);

            assertThat(ddd).extracting(Ddd::getCcc).containsExactly(ccc, ccc, ccc);

            assertEquals(1, ((Ddd1Ddd) ddd[0]).initCount()); // init方法只能被调一次
            assertEquals(1, ((Ddd1Ddd) ddd[0]).setterCount()); // setter方法只能被调一次


            Ddd[] allDddLoop = ((Ddd1Ddd) ddd[0]).getAllDdd();
            assertThat(allDddLoop).isNotNull()
                    .extracting(d -> (Class) d.getClass())
                    .containsExactly(Ddd1Ddd.class, Ddd2Ddd.class, Ddd4Ddd.class, Ddd5Ddd.class);

            assertFalse(aaa.isClosed());
            microkernel.close();
            assertTrue(aaa.isClosed());
        }
    }


    @Test
    public void testCreateArray() {
        try (Microkernel microkernel = Microkernel.getMicrokernel()) {

            Ddd[] ddd = microkernel.create(Ddd[].class);
            assertThat(ddd).isNotNull()
                    .extracting(d -> (Class) d.getClass())
                    // Ddd3的init返回false，不被加载
                    // Ddd2在配置plugintest.properties中ddd-=ddd2被去除
                    // Ddd2在配置plugintest.properties中ddd+=ddd4被加入
                    // Ddd5在配置plugintest-debug.properties中ddd+=ddd5被加入
                    .containsExactly(Ddd1Ddd.class, Ddd4Ddd.class, Ddd5Ddd.class);
            assertEquals(1, ((Ddd1Ddd) ddd[0]).initCount()); // init方法只能被调一次
            assertEquals(1, ((Ddd1Ddd) ddd[0]).setterCount()); // setter方法只能被调一次

            Ccc ccc = ddd[0].getCcc(); // 多层注入
            assertNotNull(ccc);
            assertEquals(ccc.getClass(), Wrapper1Ccc.class);
            assertNotNull(((Wrapper1Ccc) ccc).getCcc());
            assertEquals(Ccc1Ccc.class, ((Wrapper1Ccc) ccc).getCcc().getClass());
            assertEquals(100L, ccc.getDefaultTimeout()); // 数字注入

            assertEquals("test2", ccc.getCccApplicationName()); // 前缀区隔注入：ccc.application.name=test2

            Aaa aaa = ccc.getAaa();
            assertNotNull(aaa);
            assertEquals(Aaa1Aaa.class, aaa.getClass()); // SPI注入
            assertEquals("loaded", aaa.getTestConfig());
            assertFalse(aaa.isClosed());

            microkernel.close();
            assertTrue(aaa.isClosed());
        }
    }

}
