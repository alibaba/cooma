package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.Microkernel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrimitiveMicrokernelTest {

    @Test
    public void testPrimitiveMicrokernel() {
        try (Microkernel microkernel = new PrimitiveMicrokernel()) {

            String str = microkernel.create(String.class, "aaa");
            assertEquals("aaa", str);

            Integer num = microkernel.create(Integer.class, "3");
            assertEquals(Integer.valueOf(3), num);
        }
    }

}
