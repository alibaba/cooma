package com.metaframe.cooma;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.util.HashSet;
import java.util.Set;

import com.metaframe.cooma.ext1.SimpleExt;
import com.metaframe.cooma.ext1.impl.SimpleExtImpl1;
import com.metaframe.cooma.ext1.impl.SimpleExtImpl2;
import com.metaframe.cooma.ext2.ConfigHolder;
import com.metaframe.cooma.ext2.NoDefaultExt;
import com.metaframe.cooma.ext3.WrappedExt;
import com.metaframe.cooma.ext3.impl.Ext3Wrapper1;
import com.metaframe.cooma.ext3.impl.Ext3Wrapper2;
import com.metaframe.cooma.ext4.AdaptiveMethodNoConfig_Ext;
import com.metaframe.cooma.ext5.NoAdaptiveMethodExt;
import com.metaframe.cooma.ext6.InjectExt;
import com.metaframe.cooma.ext7.InitErrorExt;
import org.junit.Test;

import com.metaframe.cooma.ext6.impl.Ext6Impl2;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class ExtensionLoaderTest {
    @Test
    public void test_getExtensionLoader_Null() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),
                    containsString("Extension type == null"));
        }
    }

    @Test
    public void test_getExtensionLoader_NotInterface() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(ExtensionLoaderTest.class);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),
                    containsString("type(class com.metaframe.cooma.ExtensionLoaderTest) is not interface"));
        }
    }

    @Test
    public void test_getExtensionLoader_NotSpiAnnotation() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(NoExtensionExt.class);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),
                    allOf(containsString(NoExtensionExt.class.getName()),
                            containsString("is not a extension"),
                            containsString("WITHOUT @Extension Annotation")));
        }
    }

    @Test
    public void test_getDefault() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtension();
        assertThat(ext, instanceOf(SimpleExtImpl1.class));
        
        String name = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtensionName();
        assertEquals("impl1", name);
    }
    
    @Test
    public void test_getDefault_NULL() throws Exception {
        NoDefaultExt ext = ExtensionLoader.getExtensionLoader(NoDefaultExt.class).getDefaultExtension();
        assertNull(ext);
        
        String name = ExtensionLoader.getExtensionLoader(NoDefaultExt.class).getDefaultExtensionName();
        assertNull(name);
    }
    
    @Test
    public void test_getExtension() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("impl1") instanceof SimpleExtImpl1);
        assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("impl2") instanceof SimpleExtImpl2);
    }
    
    @Test
    public void test_getExtension_WithWrapper() throws Exception {
        WrappedExt impl1 = ExtensionLoader.getExtensionLoader(WrappedExt.class).getExtension("impl1");
        assertThat(impl1, anyOf(instanceOf(Ext3Wrapper1.class), instanceOf(Ext3Wrapper2.class)));

        WrappedExt impl2 = ExtensionLoader.getExtensionLoader(WrappedExt.class).getExtension("impl2") ;
        assertThat(impl2, anyOf(instanceOf(Ext3Wrapper1.class), instanceOf(Ext3Wrapper2.class)));
        
        
        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");
        int echoCount1 = Ext3Wrapper1.echoCount.get();
        int echoCount2 = Ext3Wrapper2.echoCount.get();
        int yellCount1 = Ext3Wrapper1.yellCount.get();
        int yellCount2 = Ext3Wrapper2.yellCount.get();
        
        assertEquals("Ext3Impl1-echo", impl1.echo(config, "ha"));
        assertEquals(echoCount1 + 1, Ext3Wrapper1.echoCount.get());
        assertEquals(echoCount2 + 1, Ext3Wrapper2.echoCount.get());
        assertEquals(yellCount1, Ext3Wrapper1.yellCount.get());
        assertEquals(yellCount2, Ext3Wrapper2.yellCount.get());
        
        assertEquals("Ext3Impl2-yell", impl2.yell(config, "ha"));
        assertEquals(echoCount1 + 1, Ext3Wrapper1.echoCount.get());
        assertEquals(echoCount2 + 1, Ext3Wrapper2.echoCount.get());
        assertEquals(yellCount1 + 1, Ext3Wrapper1.yellCount.get());
        assertEquals(yellCount2 + 1, Ext3Wrapper2.yellCount.get());
    }
    
    @Test
    public void test_getExtension_ExceptionNoExtension() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("XXX");
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("No such extension com.metaframe.cooma.ext1.SimpleExt by name XXX"));
        }
    }
    
    @Test
    public void test_getExtension_ExceptionNoExtension_NameOnWrapperNoAffact() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(NoAdaptiveMethodExt.class).getExtension("XXX");
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("No such extension com.metaframe.cooma.ext5.NoAdaptiveMethodExt by name XXX"));
        }
    }
    
    @Test
    public void test_getExtension_ExceptionNullArg() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension(null);
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Extension name == null"));
        }
    }
    
    @Test
    public void test_hasExtension() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("impl1"));
        assertFalse(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("impl1,impl2"));
        assertFalse(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("xxx"));
        
        try {
            ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension(null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Extension name == null"));
        }
    }

    @Test
    public void test_hasExtension_wrapperIsNotExt() throws Exception {
        assertTrue(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("impl1"));
        assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("impl1,impl2"));
        assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("xxx"));

        assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("wrapper1"));

        try {
            ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension(null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Extension name == null"));
        }
    }
    
    @Test
    public void test_getSupportedExtensions() throws Exception {
        Set<String> exts = ExtensionLoader.getExtensionLoader(SimpleExt.class).getSupportedExtensions();
        
        Set<String> expected = new HashSet<String>();
        expected.add("impl1");
        expected.add("impl2");
        expected.add("impl3");
        
        assertEquals(expected, exts);
    }

    @Test
    public void test_getSupportedExtensions_wrapperIsNotExt() throws Exception {
        Set<String> exts = ExtensionLoader.getExtensionLoader(WrappedExt.class).getSupportedExtensions();

        Set<String> expected = new HashSet<String>();
        expected.add("impl1");
        expected.add("impl2");

        assertEquals(expected, exts);
    }

    @Test
    public void test_getAdaptiveExtension_defaultExtension() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");

        String echo = ext.echo(config, "haha");
        assertEquals("Ext1Impl1-echo", echo);
    }

    @Test
    public void test_getAdaptiveExtension_useTypeNameAsKey() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "simple.ext", "impl2");

        String echo = ext.echo(config, "haha");
        assertEquals("Ext1Impl2-echo", echo);
    }

    @Test
    public void test_getAdaptiveExtension_customizeKey() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "key2", "impl2");

        String echo = ext.yell(config, "haha");
        assertEquals("Ext1Impl2-yell", echo);

        config = config.addConfig("key1", "impl3");
        echo = ext.yell(config, "haha");
        assertEquals("Ext1Impl3-yell", echo);
    }

    @Test
    public void test_getAdaptiveExtension_ConfigNpe() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

        try {
            ext.echo(null, "haha");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("config == null", e.getMessage());
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_ExceptionWhenNoAdaptiveMethodOnInterface() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(NoAdaptiveMethodExt.class).getAdaptiveExtension();
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), 
                    allOf(containsString("Fail to create adaptive extension interface com.metaframe.cooma.ext5.NoAdaptiveMethodExt"),
                            containsString("No adaptive method on extension com.metaframe.cooma.ext5.NoAdaptiveMethodExt, refuse to create the adaptive class")));
        }
        // 多次get，都会报错且相同
        try {
            ExtensionLoader.getExtensionLoader(NoAdaptiveMethodExt.class).getAdaptiveExtension();
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), 
                    allOf(containsString("Fail to create adaptive extension interface com.metaframe.cooma.ext5.NoAdaptiveMethodExt"),
                            containsString("No adaptive method on extension com.metaframe.cooma.ext5.NoAdaptiveMethodExt, refuse to create the adaptive class")));
        }
    }

    @Test
    public void test_getAdaptiveExtension_ExceptionWhenNotAdativeMethod() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

        
        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");
        try {
            ext.bang(config, 33);
            fail();
        } catch (UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), containsString("method "));
            assertThat(
                    expected.getMessage(),
                    containsString("of interface com.metaframe.cooma.ext1.SimpleExt is not adaptive method!"));
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_ExceptionWhenNoConfigAttrib() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(AdaptiveMethodNoConfig_Ext.class).getAdaptiveExtension();
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), containsString("fail to create adaptive class for interface "));
            assertThat(expected.getMessage(), containsString(": not found config parameter or config attribute in parameters of method "));
        }
    }

    @Test
    public void test_configHolder_getAdaptiveExtension() throws Exception {
        NoDefaultExt ext = ExtensionLoader.getExtensionLoader(NoDefaultExt.class).getAdaptiveExtension();
        
        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "no.default.ext", "impl1");
        
        ConfigHolder holder = new ConfigHolder();
        holder.setConfig(config);
    
        String echo = ext.echo(holder, "haha");
        assertEquals("Ext2Impl1-echo", echo);
    }

    @Test
    public void test_configHolder_getAdaptiveExtension_noExtension() throws Exception {
        NoDefaultExt ext = ExtensionLoader.getExtensionLoader(NoDefaultExt.class).getAdaptiveExtension();

        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");

        ConfigHolder holder = new ConfigHolder();
        holder.setConfig(config);
        
        try {
            ext.echo(holder, "haha");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Fail to get extension("));
        }
        
        config = config.addConfig("no.default.ext", "XXX");
        holder.setConfig(config);
        try {
            ext.echo(holder, "haha");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("No such extension"));
        }
    }

    @Test
    public void test_configHolder_getAdaptiveExtension_ConfigNpe() throws Exception {
        NoDefaultExt ext = ExtensionLoader.getExtensionLoader(NoDefaultExt.class).getAdaptiveExtension();

        try {
            ext.echo(null, "haha");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("com.metaframe.cooma.ext2.ConfigHolder argument == null", e.getMessage());
        }
        
        try {
            ext.echo(new ConfigHolder(), "haha");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("com.metaframe.cooma.ext2.ConfigHolder argument getConfig() == null", e.getMessage());
        }
    }

    @Test
    public void test_configHolder_getAdaptiveExtension_ExceptionWhenNotAdaptiveMethod() throws Exception {
        SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getAdaptiveExtension();

        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");
        
        try {
            ext.bang(config, 33);
            fail();
        } catch (UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), containsString("method "));
            assertThat(
                    expected.getMessage(),
                    containsString("of interface com.metaframe.cooma.ext1.SimpleExt is not adaptive method!"));
        }
    }

    @Test
    public void test_configHolder_getAdaptiveExtension_ExceptionWhenNameNotProvided() throws Exception {
        NoDefaultExt ext = ExtensionLoader.getExtensionLoader(NoDefaultExt.class).getAdaptiveExtension();

        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1");

        ConfigHolder holder = new ConfigHolder();
        holder.setConfig(config);
        
        try {
            ext.echo(holder, "impl1");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Fail to get extension("));
        }
        
        config = config.addConfig("key1", "impl1");
        holder.setConfig(config);
        try {
            ext.echo(holder, "haha");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Fail to get extension(com.metaframe.cooma.ext2.NoDefaultExt) name from config"));
        }
    }
    
    @Test
    public void test_getAdaptiveExtension_inject() throws Exception {
        InjectExt ext = ExtensionLoader.getExtensionLoader(InjectExt.class).getAdaptiveExtension();

        Config config = Config.fromKv("protocol", "p1", "host", "1.2.3.4", "port", "1010", "path", "path1", "key", "impl1");
        
        assertEquals("Ext6Impl1-echo-Ext1Impl1-echo", ext.echo(config, "ha"));
        
        config = config.addConfig("simple.ext", "impl2");
        assertEquals("Ext6Impl1-echo-Ext1Impl2-echo", ext.echo(config, "ha"));
    }
    
    @Test
    public void test_getAdaptiveExtension_InjectNotExtFail() throws Exception {
        InjectExt ext = ExtensionLoader.getExtensionLoader(InjectExt.class).getExtension("impl2");
        
        Ext6Impl2 impl = (Ext6Impl2) ext;
        assertNull(impl.getList());
    }
    
    @Test
    public void test_InitError() throws Exception {
        ExtensionLoader<InitErrorExt> loader = ExtensionLoader.getExtensionLoader(InitErrorExt.class);
        
        loader.getExtension("ok");
        
        try {
            loader.getExtension("error");
            fail();
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage(), containsString("Failed to load config line(error=com.metaframe.cooma.ext7.impl.Ext7InitErrorImpl) of config file( file:/E:/CodeRepository/open/cooma/target/test-classes/META-INF/services/com.metaframe.cooma.ext7.InitErrorExt) for extension(interface com.metaframe.cooma.ext7.InitErrorExt)"));
            assertThat(expected.getCause().getCause(), instanceOf(ExceptionInInitializerError.class));
        }
    }
}