package com.alibaba.microkernel.mock;

import com.alibaba.microkernel.Microkernel;

public class Aaa1Aaa implements Aaa {

    private Bbb bbb;

    private String testConfig;

    private boolean closed = false;

    private Microkernel microkernel;

    private String[] fileValues;

    public String[] getFileValues() {
        return fileValues;
    }

    public Aaa1Aaa setFileValues(String[] fileValues) {
        this.fileValues = fileValues;
        return this;
    }

    @Override
    public Bbb getBbb() {
        return bbb;
    }

    public void setBbb(Bbb bbb) {
        this.bbb = bbb;
    }

    @Override
    public String getTestConfig() {
        return testConfig;
    }

    public Aaa1Aaa setTestConfig(String testConfig) {
        this.testConfig = testConfig;
        return this;
    }

    @Override
    public Microkernel getMicrokernel() {
        return microkernel;
    }

    public Aaa1Aaa setMicrokernel(Microkernel microkernel) {
        this.microkernel = microkernel;
        return this;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void close() throws Exception {
        closed = true;
    }
}
