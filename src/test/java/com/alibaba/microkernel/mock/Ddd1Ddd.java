package com.alibaba.microkernel.mock;

import java.util.concurrent.atomic.AtomicInteger;

public class Ddd1Ddd implements Ddd {

    private Ccc ccc;

    private final AtomicInteger initCount = new AtomicInteger();

    private final AtomicInteger setterCount = new AtomicInteger();

    @Override
    public Ccc getCcc() {
        return ccc;
    }

    public void setCcc(Ccc ccc) {
        setterCount.incrementAndGet();
        this.ccc = ccc;
    }

    private Ddd[] allDdd;

    public Ddd[] getAllDdd() {
        return allDdd;
    }

    public void setAllDdd(Ddd[] allDdd) {
        this.allDdd = allDdd;
    }

    public boolean init() {
        initCount.incrementAndGet();
        return true;
    }

    public int initCount() {
        return initCount.get();
    }

    public int setterCount() {
        return setterCount.get();
    }

}
