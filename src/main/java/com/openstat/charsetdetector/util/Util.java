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
package com.openstat.charsetdetector.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Generic utilities class. Contains everything that does not fit in any more
 * relevant classes.
 */
public final class Util {

    private Util() {
    }

    public static int signToUnsign(byte b) {
        return b & 0xff;
    }

    public static BufferedWriter createWriter(File file) {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedWriter createWriter(String file) {
        return createWriter(new File(file));
    }
}
