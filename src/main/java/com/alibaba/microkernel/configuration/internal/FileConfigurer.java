package com.alibaba.microkernel.configuration.internal;

import com.alibaba.microkernel.configuration.ConfigurerChain;
import com.alibaba.microkernel.convention.Order;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Order(150)
public class FileConfigurer extends ConfigurerChain {

    private static final String FILE = "@";

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        return super.get(key).flatMap(FileConfigurer::getFile);
    }

    private static Stream<String> getFile(String file) {
        if (file.startsWith(FILE)) {
            file = file.substring(FILE.length());
            return findClassPath(file).flatMap(uri -> {
                try {
                    return Files.lines(Paths.get(uri));
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to load file in classpath "
                            + uri + ", cause: " + e.getClass().getName() + ": " + e.getMessage(), e);
                }
            });
        }
        // 将引用值转换成流
        return Stream.of(file);
    }

    private static Stream<URI> findClassPath(String file) {
        try {
            final Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(file);
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<URL>() {
                @Override public boolean hasNext() {
                    return urls.hasMoreElements();
                }
                @Override public URL next() {
                    return urls.nextElement();
                }
            }, Spliterator.NONNULL), false).map(url -> {
                try {
                    return url.toURI();
                } catch (URISyntaxException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find file in classpath "
                    + file + ", cause: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

}
