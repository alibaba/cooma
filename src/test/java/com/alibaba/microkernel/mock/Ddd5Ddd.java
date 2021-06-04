package com.alibaba.microkernel.mock;

public class Ddd5Ddd implements Ddd {

    private Ccc ccc;

    @Override
    public Ccc getCcc() {
        return ccc;
    }

    public Ddd5Ddd setCcc(Ccc ccc) {
        this.ccc = ccc;
        return this;
    }

}
