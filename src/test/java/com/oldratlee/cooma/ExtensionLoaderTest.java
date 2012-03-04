package com.oldratlee.cooma;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ExtensionLoader;
import com.oldratlee.cooma.ext1.Ext1;
import com.oldratlee.cooma.ext1.impl.Ext1Impl1;
import com.oldratlee.cooma.ext1.impl.Ext1Impl2;
import com.oldratlee.cooma.ext2.Ext2;
import com.oldratlee.cooma.ext2.UrlHolder;
import com.oldratlee.cooma.ext3.Ext3;
import com.oldratlee.cooma.ext4.Ext4;
import com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod;
import com.oldratlee.cooma.ext5.impl.Ext5Wrapper1;
import com.oldratlee.cooma.ext5.impl.Ext5Wrapper2;
import com.oldratlee.cooma.ext6_inject.Ext6;
import com.oldratlee.cooma.ext6_inject.impl.Ext6Impl2;
import com.oldratlee.cooma.ext7.Ext7;

/**
 * @author oldratlee
 */
public class ExtensionLoaderTest {
    @Test
    public void test_getDefault() throws Exception {
        Ext1 ext = ExtensionLoader.getExtensionLoader(Ext1.class).getDefaultExtension();
        assertThat(ext, instanceOf(Ext1Impl1.class));
        
        String name = ExtensionLoader.getExtensionLoader(Ext1.class).getDefaultExtensionName();
        assertEquals("impl1", name);
    }
    
    @Test
    public void test_getDefault_NULL() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getDefaultExtension();
        assertNull(ext);
        
        String name = ExtensionLoader.getExtensionLoader(Ext2.class).getDefaultExtensionName();
        assertNull(name);
    }
    
    @Test
    public void test_getExtension() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(Ext1.class).getExtension("impl1") instanceof Ext1Impl1);
        assertTrue(ExtensionLoader.getExtensionLoader(Ext1.class).getExtension("impl2") instanceof Ext1Impl2);
    }
    
    @Test
    public void test_getExtension_WithWrapper() throws Exception {
        Ext5NoAdaptiveMethod impl1 = ExtensionLoader.getExtensionLoader(Ext5NoAdaptiveMethod.class).getExtension("impl1");
        assertThat(impl1, anyOf(instanceOf(Ext5Wrapper1.class), instanceOf(Ext5Wrapper2.class)));
        
        Ext5NoAdaptiveMethod impl2 = ExtensionLoader.getExtensionLoader(Ext5NoAdaptiveMethod.class).getExtension("impl2") ;
        assertThat(impl2, anyOf(instanceOf(Ext5Wrapper1.class), instanceOf(Ext5Wrapper2.class)));
        
        
        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");
        int echoCount1 = Ext5Wrapper1.echoCount.get();
        int echoCount2 = Ext5Wrapper2.echoCount.get();
        int yellCount1 = Ext5Wrapper1.yellCount.get();
        int yellCount2 = Ext5Wrapper2.yellCount.get();
        
        assertEquals("Ext5Impl1-echo", impl1.echo(config, "ha"));
        assertEquals(echoCount1 + 1, Ext5Wrapper1.echoCount.get());
        assertEquals(echoCount2 + 1, Ext5Wrapper2.echoCount.get());
        assertEquals(yellCount1, Ext5Wrapper1.yellCount.get());
        assertEquals(yellCount2, Ext5Wrapper2.yellCount.get());
        
        assertEquals("Ext5Impl2-yell", impl2.yell(config, "ha"));
        assertEquals(echoCount1 + 1, Ext5Wrapper1.echoCount.get());
        assertEquals(echoCount2 + 1, Ext5Wrapper2.echoCount.get());
        assertEquals(yellCount1 + 1, Ext5Wrapper1.yellCount.get());
        assertEquals(yellCount2 + 1, Ext5Wrapper2.yellCount.get());
    }
    
    @Test
    public void test_getExtension_ExceptionNoExtension() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(Ext1.class).getExtension("XXX");
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("No such extension com.oldratlee.cooma.ext1.Ext1 by name XXX"));
        }
    }
    
    @Test
    public void test_getExtension_ExceptionNoExtension_NameOnWrapperNoAffact() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(Ext5NoAdaptiveMethod.class).getExtension("XXX");
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("No such extension com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod by name XXX"));
        }
    }
    
    @Test
    public void test_getExtension_ExceptionNullArg() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(Ext1.class).getExtension(null);
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Extension name == null"));
        }
    }
    
    @Test
    public void test_hasExtension() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(Ext1.class).hasExtension("impl1"));
        assertFalse(ExtensionLoader.getExtensionLoader(Ext1.class).hasExtension("impl1,impl2"));
        assertFalse(ExtensionLoader.getExtensionLoader(Ext1.class).hasExtension("xxx"));
        
        try {
            ExtensionLoader.getExtensionLoader(Ext1.class).hasExtension(null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Extension name == null"));
        }
    }
    
    @Test
    public void test_getSupportedExtensions() throws Exception {
        Set<String> exts = ExtensionLoader.getExtensionLoader(Ext1.class).getSupportedExtensions();
        
        Set<String> expected = new HashSet<String>();
        expected.add("impl1");
        expected.add("impl2");
        expected.add("impl3");
        
        assertEquals(expected, exts);
    }
    
    @Test
    public void test_getSupportedExtensions_NoExtension() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(ExtensionLoaderTest.class).getSupportedExtensions();
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), 
                    allOf(containsString("com.oldratlee.cooma.ExtensionLoaderTest"),
                            containsString("is not extension"),
                            containsString("WITHOUT @Extension Annotation")));
       
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_defaultExtension() throws Exception {
        Ext1 ext = ExtensionLoader.getExtensionLoader(Ext1.class).getAdaptiveExtension();

        Map<String, String> map = new HashMap<String, String>();
        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");

        String echo = ext.echo(config, "haha");
        assertEquals("Ext1Impl1-echo", echo);
    }

    @Test
    public void test_getAdaptiveExtension() throws Exception {
        Ext1 ext = ExtensionLoader.getExtensionLoader(Ext1.class).getAdaptiveExtension();

        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "ext1", "impl2");

        String echo = ext.echo(config, "haha");
        assertEquals("Ext1Impl2-echo", echo);
    }

    @Test
    public void test_getAdaptiveExtension_customizeKey() throws Exception {
        Ext1 ext = ExtensionLoader.getExtensionLoader(Ext1.class).getAdaptiveExtension();

        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "key2", "impl2");

        String echo = ext.yell(config, "haha");
        assertEquals("Ext1Impl2-yell", echo);

        config = config.addConfig("key1", "impl3"); // 注意： URL是值类型
        echo = ext.yell(config, "haha");
        assertEquals("Ext1Impl3-yell", echo);
    }

    @Test
    public void test_getAdaptiveExtension_UrlNpe() throws Exception {
        Ext1 ext = ExtensionLoader.getExtensionLoader(Ext1.class).getAdaptiveExtension();

        try {
            ext.echo(null, "haha");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("config == null", e.getMessage());
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_ExceptionWhenNoAdativeMethodOnInterface() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(Ext5NoAdaptiveMethod.class).getAdaptiveExtension();
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), 
                    allOf(containsString("Can not create adaptive extenstion interface com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod"),
                            containsString("No adaptive method on extension com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod, refuse to create the adaptive class")));
        }
        // 多次get，都会报错且相同
        try {
            ExtensionLoader.getExtensionLoader(Ext5NoAdaptiveMethod.class).getAdaptiveExtension();
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), 
                    allOf(containsString("Can not create adaptive extenstion interface com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod"),
                            containsString("No adaptive method on extension com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod, refuse to create the adaptive class")));
        }
    }

    @Test
    public void test_getAdaptiveExtension_ExceptionWhenNotAdativeMethod() throws Exception {
        Ext1 ext = ExtensionLoader.getExtensionLoader(Ext1.class).getAdaptiveExtension();

        
        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");
        try {
            ext.bang(config, 33);
            fail();
        } catch (UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), containsString("method "));
            assertThat(
                    expected.getMessage(),
                    containsString("of interface com.oldratlee.cooma.ext1.Ext1 is not adaptive method!"));
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_ExceptionWhenNoUrlAttrib() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(Ext4.class).getAdaptiveExtension();
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), containsString("fail to create adative class for interface "));
            assertThat(expected.getMessage(), containsString(": not found config parameter or config attribute in parameters of method "));
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_protocolKey() throws Exception {
        Ext3 ext = ExtensionLoader.getExtensionLoader(Ext3.class).getAdaptiveExtension();
    
        Configs config = Configs.fromKv("protocol", "impl3", "host", "1.2.3.4", "port", "1010", "path", "path1");

        String echo = ext.echo(config, "s");
        assertEquals("Ext3Impl3-echo", echo);
    
        config = config.addConfig("key1", "impl2");
        echo = ext.echo(config, "s");
        assertEquals("Ext3Impl2-echo", echo);
        
        String yell = ext.yell(config, "d");
        assertEquals("Ext3Impl3-yell", yell);
    }
    
    
    @Test
    public void test_getAdaptiveExtension_lastProtocolKey() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getAdaptiveExtension();
        
        
        Configs config = Configs.fromKv("protocol", "impl1", "host", "1.2.3.4", "port", "1010", "path", "path1");

        String yell = ext.yell(config, "s");
        assertEquals("Ext2Impl1-yell", yell);
        
        config = config.addConfig("key1", "impl2");
        yell = ext.yell(config, "s");
        assertEquals("Ext2Impl2-yell", yell);
    }

    @Test
    public void test_urlHolder_getAdaptiveExtension() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getAdaptiveExtension();
        
        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "ext2", "impl1");
        
        UrlHolder holder = new UrlHolder();
        holder.setUrl(config);
    
        String echo = ext.echo(holder, "haha");
        assertEquals("Ext2Impl1-echo", echo);
    }

    @Test
    public void test_urlHolder_getAdaptiveExtension_noExtension() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getAdaptiveExtension();

        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");

        UrlHolder holder = new UrlHolder();
        holder.setUrl(config);
        
        try {
            ext.echo(holder, "haha");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Fail to get extension("));
        }
        
        config = config.addConfig("ext2", "XXX");
        holder.setUrl(config);
        try {
            ext.echo(holder, "haha");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("No such extension"));
        }
    }

    @Test
    public void test_urlHolder_getAdaptiveExtension_UrlNpe() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getAdaptiveExtension();

        try {
            ext.echo(null, "haha");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("com.oldratlee.cooma.ext2.UrlHolder argument == null", e.getMessage());
        }
        
        try {
            ext.echo(new UrlHolder(), "haha");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("com.oldratlee.cooma.ext2.UrlHolder argument getUrl() == null", e.getMessage());
        }
    }

    @Test
    public void test_urlHolder_getAdaptiveExtension_ExceptionWhenNotAdativeMethod() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getAdaptiveExtension();

        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");
        
        try {
            ext.bang(config, 33);
            fail();
        } catch (UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), containsString("method "));
            assertThat(
                    expected.getMessage(),
                    containsString("of interface com.oldratlee.cooma.ext2.Ext2 is not adaptive method!"));
        }
    }

    @Test
    public void test_urlHolder_getAdaptiveExtension_ExceptionWhenNameNotProvided() throws Exception {
        Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getAdaptiveExtension();

        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");

        UrlHolder holder = new UrlHolder();
        holder.setUrl(config);
        
        try {
            ext.echo(holder, "impl1");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Fail to get extension("));
        }
        
        config = config.addConfig("key1", "impl1");
        holder.setUrl(config);
        try {
            ext.echo(holder, "haha");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Fail to get extension(com.oldratlee.cooma.ext2.Ext2) name from config"));
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_inject() throws Exception {
        Ext6 ext = ExtensionLoader.getExtensionLoader(Ext6.class).getAdaptiveExtension();

        Configs config = Configs.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "ext6", "impl1");
        
        assertEquals("Ext6Impl1-echo-Ext1Impl1-echo", ext.echo(config, "ha"));
        
        config = config.addConfig("ext1", "impl2");
        assertEquals("Ext6Impl1-echo-Ext1Impl2-echo", ext.echo(config, "ha"));
        
    }
    
    @Test
    public void test_getAdaptiveExtension_InjectNotExtFail() throws Exception {
        Ext6 ext = ExtensionLoader.getExtensionLoader(Ext6.class).getExtension("impl2");
        
        Ext6Impl2 impl = (Ext6Impl2) ext;
        assertNull(impl.getList());
    }
    
    @Test
    public void test_InitError() throws Exception {
        ExtensionLoader<Ext7> loader = ExtensionLoader.getExtensionLoader(Ext7.class);
        
        loader.getExtension("ok");
        
        try {
            loader.getExtension("error");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Failed to load extension class(interface: interface com.oldratlee.cooma.ext7.Ext7"));
            assertThat(expected.getCause(), instanceOf(ExceptionInInitializerError.class));
        }
    }
}