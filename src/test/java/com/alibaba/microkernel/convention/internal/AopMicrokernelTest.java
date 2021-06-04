package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.mock.Ccc;
import com.alibaba.microkernel.mock.Ccc1Ccc;
import com.alibaba.microkernel.mock.Wrapper1Ccc;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AopMicrokernelTest {

    @Test
    public void testChainSpiMicrokernel() {
        try (Microkernel pluginFactory = new AopMicrokernel()
                                            .setNext(new ArrayMicrokernel()
                                            .setNext(new SpiMicrokernel()))) {

            Ccc ccc = pluginFactory.create(Ccc.class, "wrapper1", "ccc1");

            assertThat(ccc).isNotNull()
                    .isOfAnyClassIn(Wrapper1Ccc.class)
                    .extracting(c -> ((Wrapper1Ccc) c).getCcc())
                    .isOfAnyClassIn(Ccc1Ccc.class);
        }
    }

}
