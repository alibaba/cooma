package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.configuration.internal.DotConfigurer;
import com.alibaba.microkernel.configuration.internal.PropertiesConfigurer;
import com.alibaba.microkernel.mock.Fff;
import com.alibaba.microkernel.mock.Fff1Fff;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigMicrokernelTest {

    @Test
    public void testConfigSpiMicrokernel() {
        try (IocMicrokernel ioCMicrokernel = new IocMicrokernel();
             Microkernel microkernel = new ConfigMicrokernel().setConfigurer(new DotConfigurer().setNext(new PropertiesConfigurer()))
                                            .setNext(new PrimitiveMicrokernel()
                                            .setNext(ioCMicrokernel
                                            .setNext(new SpiMicrokernel())))) {

            ioCMicrokernel.setMicrokernel(microkernel);

            Fff fff = microkernel.create(Fff.class);
            assertNotNull(fff);
            assertEquals(Fff1Fff.class, fff.getClass());
            assertEquals("lll", fff.getStr());
        }
    }

}
