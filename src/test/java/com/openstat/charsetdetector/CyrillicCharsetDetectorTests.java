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
public final class CyrillicCharsetDetectorTests {

    @Test
    public void testDetectEncodingSearch() {
        CyrillicCharsetDetector detector = CharsetDetector.getCyrDetector();

        byte[] b = "как жрать суши".getBytes(Charset.forName("windows-1251"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);

        b = "как жрать суши".getBytes(Charset.forName("KOI8-R"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.KOI8_R);

        b = "море! ~~".getBytes(Charset.forName("windows-1251"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);

        b = "море! ~~".getBytes(Charset.forName("KOI8-R"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.KOI8_R);

        b = "затем".getBytes(Charset.forName("Cp866"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.CP866);

        b = "конь".getBytes(Charset.forName("KOI8-R"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.KOI8_R);

        b = "что+посмотреть".getBytes(Charset.forName("Cp866"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.CP866);

        b = "И+что?!".getBytes(Charset.forName("KOI8-R"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.KOI8_R);

        b = "мой+самолет+был".getBytes(Charset.forName("windows-1251"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);

        b = "КУДА ПОЙТИ".getBytes(Charset.forName("Cp866"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.CP866);

        b = "Я ПЛАКАЛ".getBytes(Charset.forName("KOI8-R"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.KOI8_R);

        b = "С УМА СОЙТИ!".getBytes(Charset.forName("windows-1251"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);
    }

    @Test
    public void testDetectEncodingSpecial() {
        CyrillicCharsetDetector detector = CharsetDetector.getCyrDetector();

        byte[] b;
        b = "э".getBytes(Charset.forName("windows-1251"));
        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);

        b = "".getBytes(Charset.forName("windows-1251"));
        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);
    }

    @Test
    public void testDetectEncodingDlinnosheee() {
        CyrillicCharsetDetector detector = CharsetDetector.getCyrDetector();

        String str = "длинношеее";

        for (CyrillicCharset cs : CyrillicCharset.values()) {
            byte[] b = str.getBytes(cs.getNioCharset());
            assertEquals(new String(b, detector.detectCyrillicCharset(b).getNioCharset()), str);
        }

    }

    @Test
    public void testDetectEncodingFaildInAccess2RawTest() {
        CyrillicCharsetDetector detector = CharsetDetector.getCyrDetector();
        byte[] b;
        b = "r[]=Футбол".getBytes(Charset.forName("windows-1251"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);

        b = "r[]=Футбол".getBytes(Charset.forName("KOI8-R"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.KOI8_R);

        b = "r[]=Регби-лига".getBytes(Charset.forName("windows-1251"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.WIN_1251);

        b = "r[]=Регби-лига".getBytes(Charset.forName("KOI8-R"));

        assertEquals(detector.detectCyrillicCharset(b), CyrillicCharset.KOI8_R);
    }
}
