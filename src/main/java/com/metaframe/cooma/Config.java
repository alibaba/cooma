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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Configuration info of extensions, pass among extensions.
 * <p/>
 * {@link Config} are <b>immutable</b> instance, so thread-safe.
 *
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 * @since 0.1.0
 */
public final class Config {

    private final Map<String, String> configs;

    private Config(Map<String, String> configs, boolean deepCopy) {
        if (deepCopy) {
            this.configs = new HashMap<String, String>(configs.size());
            for (Map.Entry<String, String> c : configs.entrySet()) {
                this.configs.put(c.getKey(), c.getValue());
            }
        } else {
            this.configs = configs;
        }
    }

    private static final Pattern PAIR_SEPARATOR = Pattern.compile("\\s*[&]\\s*");
    private static final Pattern KV_SEPARATOR = Pattern.compile("\\s*[=]\\s*");

    /**
     * Parse config string to {@link Config} instance.
     * <p/>
     * a config string like <code>key1=value1&key2=value2</code>.
     *
     * @param configString config string.
     * @since 0.1.0
     */
    public static Config fromString(String configString) {
        if (configString == null || (configString = configString.trim()).length() == 0) {
            return new Config(new HashMap<String, String>(0), false);
        }

        HashMap<String, String> cs = new HashMap<String, String>();
        String[] pairs = PAIR_SEPARATOR.split(configString);
        for (String pair : pairs) {
            if (pair.length() == 0) continue;

            String[] kv = KV_SEPARATOR.split(pair);
            switch (kv.length) {
                case 1:
                    cs.put(kv[0], "");
                    break;
                case 2:
                    cs.put(kv[0], kv[1]);
                    break;
                default:
                    throw new IllegalArgumentException("input config(" + configString +
                            ") is illegal: key(" + kv[0] + ") has more than 1 value!");
            }
        }

        return new Config(cs, false);
    }

    /**
     * @since 0.1.0
     */
    public static Config fromMap(Map<String, String> configs) {
        return new Config(configs, true);
    }

    static Map<String, String> kv2Map(String... kv) {
        Map<String, String> cs = new HashMap<String, String>();

        for (int i = 0; i < kv.length; i += 2) {
            String key = kv[i];
            if (key == null) {
                throw new IllegalArgumentException("Key must not null!");
            }
            if (i + 1 < kv.length) {
                cs.put(key, kv[i + 1]);
            } else {
                cs.put(key, null);
            }
        }

        return cs;
    }

    /**
     * @since 0.1.0
     */
    public static Config fromKv(String... kvPairs) {
        return new Config(kv2Map(kvPairs), false);
    }

    /**
     * @since 0.1.0
     */
    public Config addConfig(String... kvPairs) {
        Map<String, String> cs = new HashMap<String, String>(this.configs);
        cs.putAll(kv2Map(kvPairs));
        return new Config(cs, false);
    }

    /**
     * @since 0.1.0
     */
    public Config addConfig(Map<String, String> configs) {
        Map<String, String> cs = new HashMap<String, String>(this.configs);
        cs.putAll(configs);
        return new Config(cs, false);
    }

    /**
     * @since 0.1.0
     */
    public Map<String, String> toMap() {
        return new HashMap<String, String>(configs);
    }

    /**
     * @since 0.1.0
     */
    public boolean contains(String key) {
        return configs.containsKey(key);
    }

    /**
     * @since 0.1.0
     */
    public String get(String key) {
        return configs.get(key);
    }

    /**
     * @since 0.1.0
     */
    public String get(String key, String defaultValue) {
        if (contains(key)) {
            return configs.get(key);
        } else {
            return defaultValue;
        }
    }

    // the util methods!

    /**
     * @since 0.1.0
     */
    public boolean getBoolean(String key) {
        return Boolean.valueOf(get(key));
    }

    /**
     * @since 0.1.0
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if (contains(key)) {
            return Boolean.valueOf(get(key));
        } else {
            return defaultValue;
        }
    }

    /**
     * @since 0.1.0
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * @since 0.1.0
     */
    public int getInt(String key, int defaultValue) {
        if (contains(key)) {
            return Integer.parseInt(get(key));
        } else {
            return defaultValue;
        }
    }

    /**
     * @since 0.1.0
     */
    public long getLong(String key) {
        return getLong(key, 0);
    }

    /**
     * @since 0.1.0
     */
    public long getLong(String key, long defaultValue) {
        if (contains(key)) {
            return Long.parseLong(get(key));
        } else {
            return defaultValue;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configs == null) ? 0 : configs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Config other = (Config) obj;
        if (configs == null) {
            if (other.configs != null)
                return false;
        } else if (!configs.equals(other.configs))
            return false;
        return true;
    }

    private transient volatile String toString;

    @Override
    public String toString() {
        if (toString != null) return toString;

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> c : configs.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("&");
            }

            sb.append(c.getKey().trim());

            String value = c.getValue();
            if (value != null && (value = value.trim()).length() > 0) {
                sb.append("=").append(value);
            }
        }
        return toString = sb.toString();
    }
}
