package com.alibaba.microkernel.mock;

import com.alibaba.microkernel.MicrokernelChain;
import com.alibaba.microkernel.convention.Order;

@Order(1)
public class TestMicrokernel extends MicrokernelChain {

    private String microkernelStatus;

    public String getMicrokernelStatus() {
        return microkernelStatus;
    }

    public TestMicrokernel setMicrokernelStatus(String microkernelStatus) {
        this.microkernelStatus = microkernelStatus;
        return this;
    }

}
