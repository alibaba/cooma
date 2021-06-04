package com.alibaba.microkernel.configuration.internal;

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.configuration.Configurer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurerTest {

    @Test
    public void testMicrokernelConfigurer() {
        try (Microkernel microkernel = Microkernel.getMicrokernel()) {

            Configurer configurer = microkernel.create(Configurer.class);
            // configurer.get("test.reference.entry").forEach(System.out::println);

            assertThat(configurer.get("test.key")).containsExactly("vvv");
            assertThat(configurer.get("test.list")).containsExactly("xxx", "yyy", "zzz");

            assertThat(configurer.get("test.reference")).containsExactly("vvv");
            assertThat(configurer.get("test.reference.list")).containsExactly("aaa", "xxx", "yyy", "zzz");
            assertThat(configurer.get("test.reference.indirection")).containsExactly("aaa", "xxx", "yyy", "zzz", "bbb");
            assertThat(configurer.get("test.reference.key")).containsExactly("aaa", "xxx", "www", "bbb", "yyy", "www", "ccc", "zzz", "www");
            assertThat(configurer.get("test.reference.entry")).containsExactly("aaa", "vvv", "xxx", "bbb", "vvv", "yyy", "ccc", "vvv", "zzz");

            assertThat(configurer.get("test.remove")).containsExactly("aaa", "ddd");
            assertThat(configurer.get("test.replace")).containsExactly("aaa", "xxx", "yyy", "ddd");
            assertThat(configurer.get("test.append")).containsExactly("aaa", "fff", "bbb", "ggg", "ccc", "ddd", "eee");
            assertThat(configurer.get("test.prepend")).containsExactly("ddd", "eee", "aaa", "fff", "bbb", "xxx", "yyy", "zzz", "ccc");

            assertThat(configurer.get("test.combine")).containsExactly("ggg", "aaa", "fff", "hhh", "uuu", "vvv", "ddd", "eee");
        }
    }

}
