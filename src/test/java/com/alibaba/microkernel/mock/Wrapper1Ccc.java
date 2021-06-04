package com.alibaba.microkernel.mock;

public class Wrapper1Ccc implements Ccc {

    private Ccc ccc;

    public Ccc getCcc() {
        return ccc;
    }

    public void setNext(Ccc ccc) {
        this.ccc = ccc;
    }

    @Override
    public Aaa getAaa() {
        return ccc.getAaa();
    }

    @Override
    public long getDefaultTimeout() {
        return ccc.getDefaultTimeout();
    }

    @Override
    public String getCccApplicationName() {
        return ccc.getCccApplicationName();
    }

    @Override
    public Ddd[] getDdd() {
        return ccc.getDdd();
    }

}
