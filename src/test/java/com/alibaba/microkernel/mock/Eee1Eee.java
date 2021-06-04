package com.alibaba.microkernel.mock;

public class Eee1Eee implements Eee {

    private String testConfig;

    @Override
    public String getTestConfig() {
        return testConfig;
    }

    public Eee1Eee setTestConfig(String testConfig) {
        this.testConfig = testConfig;
        return this;
    }

}
