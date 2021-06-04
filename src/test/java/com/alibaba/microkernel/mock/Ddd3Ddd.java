package com.alibaba.microkernel.mock;

public class Ddd3Ddd implements Ddd {

    private Ccc ccc;

    @Override
    public Ccc getCcc() {
        return ccc;
    }

    public void setCcc(Ccc ccc) {
        this.ccc = ccc;
    }

    public boolean init() {
        return false;
    }

}
