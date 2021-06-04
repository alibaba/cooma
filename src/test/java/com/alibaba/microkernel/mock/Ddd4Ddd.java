package com.alibaba.microkernel.mock;

public class Ddd4Ddd implements Ddd {

    private Ccc ccc;

    @Override
    public Ccc getCcc() {
        return ccc;
    }

    public Ddd4Ddd setCcc(Ccc ccc) {
        this.ccc = ccc;
        return this;
    }

}
