package com.alibaba.microkernel.mock;

public class Bbb1Bbb implements Bbb {

    private Ccc ccc;

    private String applicationName;

    private String inputEncoding;

    private String outputEncoding;

    private int defaultPort;

    private String[] keywords;

    private boolean init;

    private Ddd[] allDdd;

    @Override
    public Ddd[] getAllDdd() {
        return allDdd;
    }

    public void setAllDdd(Ddd[] allDdd) {
        this.allDdd = allDdd;
    }

    @Override
    public boolean isInit() {
        return init;
    }

    public void init() {
        init = true;
    }

    @Override
    public Ccc getCcc() {
        return ccc;
    }

    public void setCcc(Ccc ccc) {
        this.ccc = ccc;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        if ("test2".equals(applicationName)) {
            throw new RuntimeException(applicationName);
        }
    }

    @Override
    public String getInputEncoding() {
        return inputEncoding;
    }

    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    @Override
    public String getOutputEncoding() {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    @Override
    public int getDefaultPort() {
        return defaultPort;
    }

    public Bbb1Bbb setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
        return this;
    }

    @Override
    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

}
