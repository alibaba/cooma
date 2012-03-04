package com.oldratlee.cooma;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Configuration info of extensions. pass among the extension.
 * <p>
 * {@link Configs} are <b>immutable</b> instance, so can be used in
 * multithreading case safely.
 * 
 * @author oldratlee
 * @since 0.1.0
 */
public final class Configs {
    
    private final Map<String, String> configs;
    
    private Configs(Map<String, String> configs, boolean deepCopy) {
        if(deepCopy) {
            this.configs = new HashMap<String, String>(configs.size());
            for (Map.Entry<String, String> c : configs.entrySet()) {
                this.configs.put(c.getKey().toString(), c.getValue().toString());
            }
        }
        else {
            this.configs = configs;
        }
    }

    private static final Pattern PAIR_SEPERATOR = Pattern.compile("\\s*[&]\\s*");
    private static final Pattern KV_SEPERATOR = Pattern.compile("\\s*[=]\\s*");
    
    /**
     * Parse config string to {@link Configs} instance.
     * <p>
     * a config string like <code>key1=value1&key2=value2</code>. 
     * 
     * @param configString config string.
     */
    public static Configs fromConfigString(String configString) {
        if(configString == null || (configString = configString.trim()).length() == 0) {
            return new Configs(new HashMap<String, String>(0), false);
        }
        
        HashMap<String, String> cs = new HashMap<String, String>();
        String[] pairs = PAIR_SEPERATOR.split(configString);
        for(String pair : pairs) {
            if(pair.length() == 0) continue;
            
            String[] kv = KV_SEPERATOR.split(pair);
            switch (kv.length) {
                case 1:
                    cs.put(kv[0], "");
                    break;
                case 2:
                    cs.put(kv[0], kv[1]);
                default:
                    throw new IllegalArgumentException("input config(" + configString +
                    		") is illegal: key(" + kv[0] + ") has more than 1 value!");
            }
        }
        
        return new Configs(cs, false);
    }
    
    public static Configs fromMap(Map<String, String> configs) {
        return new Configs(configs, true);
    }
    
    static Map<String, String> kv2Map(String... kv) {
        Map<String, String> cs = new HashMap<String, String>();
        
        for(int i = 0; i < kv.length; i += 2) {
            String key = kv[i];
            if(key == null) throw new IllegalArgumentException("Key must not null!");
            if(i + 1 < kv.length) {
                cs.put(key, kv[i+1]);
            }
            else {
                cs.put(key, null);
            }
        }
        
        return cs;
    }
    
    public static Configs fromKv(String... kvPairs) {  
        return new Configs(kv2Map(kvPairs), false);
    }
    
    public Configs addConfig(String... kvPairs) {
        Map<String, String> cs = new HashMap<String, String>(this.configs);
        cs.putAll(kv2Map(kvPairs));
        return new Configs(cs, false);
    }
    
    public Configs addConfig(Map<String, String> configs) {
        Map<String, String> cs = new HashMap<String, String>(this.configs);
        cs.putAll(configs);
        return new Configs(cs, false);
    }

    public Map<String, String> toMap() {
        return new HashMap<String, String>(configs);
    }
    

    public boolean contains(String key) {
        return configs.containsKey(key);
    }
    
    
    public String get(String key) {
        return configs.get(key);
    }

    // the util methods!

    public boolean getBoolean(String key) {
        return Boolean.valueOf(get(key));
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        if(contains(key)) {
            return Boolean.valueOf(get(key));
        }
        else {
            return defaultValue;
        }
    }
    
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }
    
    public int getInt(String key, int defaultValue) {
        if(contains(key)) {
            return Integer.parseInt(get(key));
        }
        else {
            return defaultValue;
        }
    }
    
    public long getLong(String key) {
        return Long.parseLong(get(key));
    }
    
    public long getLong(String key, long defaultValue) {
        if(contains(key)) {
            return Long.parseLong(get(key));
        }
        else {
            return defaultValue;
        }
    }
}
