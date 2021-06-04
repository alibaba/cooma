package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.mock.Aaa;
import com.alibaba.microkernel.mock.Aaa1Aaa;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IocMicrokernelTest {

    @Test
    public void testSetterSpiMicrokernel() {
        try (Microkernel microkernel = new IocMicrokernel()
                                            .setNext(new SpiMicrokernel())) {

            Aaa aaa = microkernel.create(Aaa.class, "aaa1");
            assertNotNull(aaa);
            assertEquals(Aaa1Aaa.class, aaa.getClass());
        }
    }

}
