package com.alibaba.microkernel.mock;

import com.alibaba.microkernel.configuration.ConfigurerChain;
import com.alibaba.microkernel.convention.Order;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

@Order(310)
public class TestConfigurer extends ConfigurerChain {

    private String configurerValue;

    public TestConfigurer setConfigurerValue(String configurerValue) {
        this.configurerValue = configurerValue;
        return this;
    }

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        if (!"test.config".equals(key)) {
            return super.get(key);
        }
        return Stream.concat(Stream.of(configurerValue), super.get(key));
    }

}
