/**
 *    Copyright (c) 2011 Openstat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.openstat.charsetdetector;

import org.testng.annotations.Test;
import java.nio.charset.Charset;
import static org.testng.Assert.*;

@Test
public final class UTF8CharsetDetectorTests {

    @Test
    public void testIsUTF8() {
        byte[] b = "asdadasdWWWW123123".getBytes(Charset.forName("UTF-8"));
        assertTrue(UTF8CharsetDetector.isUTF8(b));

        b = "asdadЯЯЯВВВВWУУУУфффф23ййй23".getBytes(Charset.forName("UTF-8"));
        assertTrue(UTF8CharsetDetector.isUTF8(b));

        b = "中国　朝鮮半島の六カ国協議再開を呼びかけ".getBytes(Charset.forName("UTF-8"));
        assertTrue(UTF8CharsetDetector.isUTF8(b));

        b = "asdadЯЯЯВВВВWУУУУфффф23ййй23".getBytes(Charset.forName("KOI8-R"));
        assertFalse(UTF8CharsetDetector.isUTF8(b));

        b = "asdadasdWWWW123123".getBytes(Charset.forName("KOI8-R"));
        assertTrue(UTF8CharsetDetector.isUTF8(b));
        assertEquals(new String(b, Charset.forName("UTF-8")), "asdadasdWWWW123123");

        b = "asdadЯЯЯВВВВWУУУУфффф23ййй23".getBytes(Charset.forName("windows-1251"));
        assertFalse(UTF8CharsetDetector.isUTF8(b));

        b = "asdadasdWWWW123123".getBytes(Charset.forName("windows-1251"));
        assertTrue(UTF8CharsetDetector.isUTF8(b));
        assertEquals(new String(b, Charset.forName("UTF-8")), "asdadasdWWWW123123");

        b = "asdadasdWWWW12!!!@@@###$$3123".getBytes(Charset.forName("ISO-8859-1"));
        assertTrue(UTF8CharsetDetector.isUTF8(b));
        assertEquals(new String(b, Charset.forName("UTF-8")), "asdadasdWWWW12!!!@@@###$$3123");

    }
}
