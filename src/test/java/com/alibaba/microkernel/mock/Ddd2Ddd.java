package com.alibaba.microkernel.mock;

public class Ddd2Ddd implements Ddd {

    private Ccc ccc;

    @Override
    public Ccc getCcc() {
        return ccc;
    }

    public Ddd2Ddd setCcc(Ccc ccc) {
        this.ccc = ccc;
        return this;
    }

}
