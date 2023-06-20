/*
 * Copyright 2023 The Android Open Source Project
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;


@RunWith(JUnit4.class)
public class ArrayUtilsTest {

    @Test
    public void checkOffsetAndCount_Fail() {
        assertInvalidOffsetOrCount(0, 0, 1);
        assertInvalidOffsetOrCount(0, 1, 0);
        assertInvalidOffsetOrCount(0, 1, 1);
        assertInvalidOffsetOrCount(0, 0, -1);
        assertInvalidOffsetOrCount(0, -1, 0);
        assertInvalidOffsetOrCount(0, -1, -1);
        assertInvalidOffsetOrCount(10, 0, 11);
        assertInvalidOffsetOrCount(10, 11, 0);
        assertInvalidOffsetOrCount(10, 11, 11);
        assertInvalidOffsetOrCount(10, 0, -1);
        assertInvalidOffsetOrCount(10, -1, 0);
    }

    private static void assertInvalidOffsetOrCount(int arrayLength, int offset, int count) {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> ArrayUtils.checkOffsetAndCount(arrayLength, offset, count));
    }

    @Test
    public void checkOffsetAndCount_Success() {
        ArrayUtils.checkOffsetAndCount(0, 0, 0);
        ArrayUtils.checkOffsetAndCount(10, 0, 5);
        ArrayUtils.checkOffsetAndCount(10, 5, 0);
        ArrayUtils.checkOffsetAndCount(10, 5, 4);
    }

    @Test
    public void toSet() {
        assertEquals(new HashSet<String>(), ArrayUtils.toSet(new String[0]));
        assertEquals(new HashSet<Integer>(), ArrayUtils.toSet(new Integer[0]));

        Set<String> expectedStrings = new HashSet<>();
        expectedStrings.add("a");
        expectedStrings.add("b");
        expectedStrings.add("c");
        assertEquals(expectedStrings, ArrayUtils.toSet(new String[]{ "a", "b", "c" }));
        assertEquals(expectedStrings, ArrayUtils.toSet(new String[]{ "c", "b", "a" }));
        assertEquals(expectedStrings, ArrayUtils.toSet(new String[]{ "a", "b", "c", "c", "c", "c" }));
        assertEquals(expectedStrings, ArrayUtils.toSet(new String[]{ "a", "a", "b", "b", "c", "c" }));
        assertEquals(expectedStrings, ArrayUtils.toSet("a", "b", "c"));

        Set<Integer> expectedInts = new HashSet<>();
        expectedInts.add(1);
        expectedInts.add(2);
        expectedInts.add(3);
        assertEquals(expectedInts, ArrayUtils.toSet(new Integer[]{ 1, 2, 3 }));
        assertEquals(expectedInts, ArrayUtils.toSet(new Integer[]{ 3, 2, 1 }));
        assertEquals(expectedInts, ArrayUtils.toSet(new Integer[] { 1, 2, 3,3 ,3 ,3 ,3 }));
    }

    @Test
    public void filter() {
        String[] input = new String[] { "a", "b", "c", "d", "e" };
        String[] abc = new String[] { "a", "b", "c" };

        assertArrayEquals(abc, ArrayUtils.filter(input, "d", "e"));
        assertArrayEquals(abc, ArrayUtils.filter(input, "e", "d"));
        assertArrayEquals(abc, ArrayUtils.filter(input, "d", "d", "e", "e"));
        assertArrayEquals(new String[0], ArrayUtils.filter(input, input));
    }
}
