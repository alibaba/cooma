package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.mock.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllMicrokernelTest {

    @Test
    public void testAllMicrokernel() {
        try (Microkernel microkernel = new AllMicrokernel()
                                        .setNext(new ArrayMicrokernel()
                                        .setNext(new SpiMicrokernel()))) {

            Ddd[] ddd = microkernel.create(Ddd[].class, "*");

            assertThat(ddd).isNotNull()
                    .extracting(d -> (Class) d.getClass())
                    .containsExactly(Ddd1Ddd.class, Ddd2Ddd.class, Ddd3Ddd.class, Ddd4Ddd.class, Ddd5Ddd.class);
        }
    }

}
