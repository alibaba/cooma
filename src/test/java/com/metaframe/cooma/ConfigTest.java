/*
 * Copyright 2012-2013 Cooma Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metaframe.cooma;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 */
public class ConfigTest {
    @Test
    public void testFromString() throws Exception {
        Config config = Config.fromString("k1=v1&k2=v2");

        assertTrue(config.contains("k1"));
        assertTrue(config.contains("k2"));
        assertFalse(config.contains("k3"));

        assertEquals("v1", config.get("k1"));
        assertEquals("v2", config.get("k2"));
    }

    @Test
    public void testFromMap() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Config config = Config.fromMap(map);

        assertTrue(config.contains("k1"));
        assertTrue(config.contains("k2"));
        assertFalse(config.contains("k3"));

        assertEquals("v1", config.get("k1"));
        assertEquals("v2", config.get("k2"));
    }

    @Test
    public void testFromKv() throws Exception {
        Config config = Config.fromKv("k1", "v1", "k2", "v2");

        assertTrue(config.contains("k1"));
        assertTrue(config.contains("k2"));
        assertFalse(config.contains("k3"));

        assertEquals("v1", config.get("k1"));
        assertEquals("v2", config.get("k2"));
    }

    @Test
    public void testAddConfig_String() throws Exception {
        Config config = Config.fromKv("k1", "v1");

        assertTrue(config.contains("k1"));
        assertFalse(config.contains("k2"));
        assertFalse(config.contains("k3"));

        assertEquals("v1", config.get("k1"));

        config = config.addConfig("k2", "v2");

        assertTrue(config.contains("k1"));
        assertTrue(config.contains("k2"));
        assertFalse(config.contains("k3"));

        assertEquals("v1", config.get("k1"));
        assertEquals("v2", config.get("k2"));
    }

    @Test
    public void testAddConfig_Map() throws Exception {
        Config config = Config.fromKv("k1", "v1");

        assertTrue(config.contains("k1"));
        assertFalse(config.contains("k2"));
        assertFalse(config.contains("k3"));

        assertEquals("v1", config.get("k1"));

        Map<String, String> map = new HashMap<String, String>();
        map.put("k2", "v2");
        config = config.addConfig(map);

        assertTrue(config.contains("k1"));
        assertTrue(config.contains("k2"));
        assertFalse(config.contains("k3"));

        assertEquals("v1", config.get("k1"));
        assertEquals("v2", config.get("k2"));

    }

    @Test
    public void testToMap() throws Exception {
        Config config = Config.fromString("k1=v1&k2=v2");

        Map<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        assertEquals(map, config.toMap());
    }

    @Test
    public void testGetBoolean() throws Exception {
        Config config = Config.fromString("k1=true&k2=false&k3=v3");

        assertTrue(config.getBoolean("k1"));
        assertFalse(config.getBoolean("k2"));
        assertFalse(config.getBoolean("k3")); // FIXME 不是Boolean值，当False？！
        assertFalse(config.getBoolean("k4"));

        assertTrue(config.getBoolean("k1", false));
        assertFalse(config.getBoolean("k2", true));
        assertFalse(config.getBoolean("k3", true)); // FIXME 不是Boolean值，当False？！
        assertTrue(config.getBoolean("k4", true));
    }

    @Test
    public void testGetInt() throws Exception {
        Config config = Config.fromString("k1=33&k2=-13&k3=v3");

        assertEquals(33, config.getInt("k1"));
        assertEquals(-13, config.getInt("k2"));
        try {
            assertEquals(0, config.getInt("k3"));
        } catch (NumberFormatException expected) {
            // Ignore
        }
        assertEquals(0, config.getInt("k4")); // 没有值，使用缺省值0！

        assertEquals(33, config.getInt("k1", 333));
        assertEquals(-13, config.getInt("k2", 333));
        try {
            assertEquals(0, config.getInt("k3", 333));
        } catch (NumberFormatException expected) {
            // Ignore
        }
        assertEquals(333, config.getInt("k4", 333));
    }

    @Test
    public void testGetLong() throws Exception {
        Config config = Config.fromString("k1=1234567890123456789&k2=-13&k3=v3");

        assertEquals(1234567890123456789L, config.getLong("k1"));
        assertEquals(-13, config.getLong("k2"));
        try {
            assertEquals(0, config.getLong("k3"));
        } catch (NumberFormatException expected) {
            // Ignore
        }
        assertEquals(0, config.getLong("k4")); // 没有值，使用缺省值0！

        assertEquals(1234567890123456789L, config.getLong("k1", 333));
        assertEquals(-13, config.getLong("k2", 333));
        try {
            assertEquals(0, config.getLong("k3", 333));
        } catch (NumberFormatException expected) {
            // Ignore
        }
        assertEquals(333, config.getLong("k4", 333));

    }
}
