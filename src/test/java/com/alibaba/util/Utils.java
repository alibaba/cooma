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

package com.alibaba.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Utils {
    public static Map<String, String> kv2Map(String... kv) {
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

}
