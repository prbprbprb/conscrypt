/*
 * Copyright 2014 The Android Open Source Project
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

package org.conscrypt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compatibility utility for Arrays.
 */
final class ArrayUtils {
    private ArrayUtils() {}

    /**
     * Checks that the range described by {@code offset} and {@code count}
     * doesn't exceed {@code arrayLength}.
     */
    static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException("length=" + arrayLength + "; regionStart="
                    + offset + "; regionLength=" + count);
        }
    }

    // Set.of only available in Java 9+ and Android API 30+.
    @SafeVarargs
    static <T> Set<T> toSet(T... array) {
        Set<T> result = new HashSet<>(array.length);
        result.addAll(Arrays.asList(array));
        return result;
    }

    // Returns a new String array with unwanted values filtered out.
    // XXX Optimise
    static String[] filter(String[] input, Set<String> toFilter) {
        List<String> result = new ArrayList<>(Arrays.asList(input));
        result.removeAll(toFilter);
        return result.toArray(new String[0]);
    }

    static String[] filter(String[] input, String... toFilter) {
        return filter(input, toSet(toFilter));
    }
}
