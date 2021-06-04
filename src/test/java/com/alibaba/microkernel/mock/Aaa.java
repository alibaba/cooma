package com.alibaba.microkernel.mock;

import com.alibaba.microkernel.Microkernel;

public interface Aaa {

    Bbb getBbb();

    String getTestConfig();

    boolean isClosed();

    Microkernel getMicrokernel();

    String[] getFileValues();

}
