package com.alibaba.microkernel.mock;

public class Ccc1Ccc implements Ccc {

    private Aaa aaa;

    private Ddd[] ddd;

    private long defaultTimeout;

    private String cccApplicationName;

    @Override
    public Aaa getAaa() {
        return aaa;
    }

    public void setAaa(Aaa aaa) {
        this.aaa = aaa;
    }

    @Override
    public Ddd[] getDdd() {
        return ddd;
    }

    public void setDdd(Ddd[] ddd) {
        this.ddd = ddd;
    }

    @Override
    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public String getCccApplicationName() {
        return cccApplicationName;
    }

    public void setCccApplicationName(String applicationName) {
        this.cccApplicationName = applicationName;
    }

}
