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

import static com.openstat.charsetdetector.util.Util.signToUnsign;

public final class UTF8CharsetDetector {

    private UTF8CharsetDetector() {
    }

    /**
     * Looks if bytes' bit format corresponds to UTF-8 specification (http://tools.ietf.org/html/rfc3629#section-3)
     *
     * @param bytes array of bytes
     * @return if bytes are encoded UTF-8 string
     */
    public static boolean isUTF8(byte[] bytes) {
        try {
        for (int i = 0; i < bytes.length;) {
            byte b = bytes[i];
            if (isSingleByteChar(b)) {
                i++;
            } else if (isDoubleByteCharHead(b)) {
                i++;
                if (!isCharTail(bytes[i])) {
                    return false;
                }
                i++;
            } else if (isTripleByteCharHead(b)) {
                i++;
                for (int j = 0; j < 2; j++) {
                    if (!isCharTail(bytes[i])) {
                        return false;
                    }
                    i++;
                }
            } else if (isQuaternaryByteCharHead(b)) {
                i++;
                for (int j = 0; j < 3; j++) {
                    if (!isCharTail(bytes[i])) {
                        return false;
                    }
                    i++;
                }
            } else {
                return false;
            }
        }
        return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * is byte matches 0xxxxxxx.
     * @param b
     * @return
     */
    static boolean isSingleByteChar(byte b) {
        return (signToUnsign(b) >> 7) == 0;
    }

    /**
     * is byte matches 110xxxxx.
     * @param b
     * @return
     */
    static boolean isDoubleByteCharHead(byte b) {
        return ((signToUnsign(b) >> 5) == 0x6);
    }

    /**
     * is byte matches 1110xxxx.
     * @param b
     * @return
     */
    static boolean isTripleByteCharHead(byte b) {
        return (signToUnsign(b) >> 4) == 0xE;
    }

    /**
     * is byte matches 11110xxx.
     * @param b
     * @return
     */
    static boolean isQuaternaryByteCharHead(byte b) {
        return (signToUnsign(b) >> 3) == 0x1E;
    }

    /**
     * is byte matches 10xxxxxx.
     * @param b
     * @return
     */
    static boolean isCharTail(byte b) {
        return (signToUnsign(b) >> 6) == 0x2;
    }
}
