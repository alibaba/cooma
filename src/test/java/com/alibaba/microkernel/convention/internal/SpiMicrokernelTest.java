package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.mock.Bbb;
import com.alibaba.microkernel.mock.Bbb1Bbb;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpiMicrokernelTest {

    @Test
    public void testSpiMicrokernel() {
        try (Microkernel microkernel = new SpiMicrokernel()) {

            Bbb bbb = microkernel.create(Bbb.class, "bbb1");
            assertNotNull(bbb);
            assertEquals(Bbb1Bbb.class, bbb.getClass());
        }
    }

}
