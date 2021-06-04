package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.mock.Ddd;
import com.alibaba.microkernel.mock.Ddd1Ddd;
import com.alibaba.microkernel.mock.Ddd2Ddd;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayMicrokernelTest {

    @Test
    public void testArraySpiMicrokernel() {
        try (Microkernel pluginFactory = new ArrayMicrokernel()
                                            .setNext(new PrimitiveMicrokernel()
                                            .setNext(new SpiMicrokernel()))) {

            String[] result = pluginFactory.create(String[].class, "aaa", "bbb");
            assertThat(result).containsExactly("aaa", "bbb");

            Integer[] num = pluginFactory.create(Integer[].class, "3", "5");
            assertThat(num).containsExactly(3, 5);

            Ddd[] ddd = pluginFactory.create(Ddd[].class, "ddd1", "ddd2");
            assertThat(ddd).isNotNull()
                    .extracting(d -> (Class) d.getClass())
                    .containsExactly(Ddd1Ddd.class, Ddd2Ddd.class);
        }
    }

}
