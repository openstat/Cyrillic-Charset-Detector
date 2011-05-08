Cyrillic Charset Detector
=========================

License
-------

Copyright  &copy;  2011 Openstat

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

>   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


Description
-----------

This is the library for detecting cyrillic character sets that was used to encode the string to byte array.

It is optimized for detecting character sets of short phrases.

Supported character sets are: `utf-8`, `win-1251`, `koi8-r`, `iso-8859-5`, `ibm855`.

Note that CharsetDetector class is non thread safe.


How to build the JAR
--------------------

To build the project you need Ant (http://ant.apache.org/bindownload.cgi)
and JDK (http://www.oracle.com/technetwork/java/javase/downloads/index.html) to be installed.

In project directory run:

      $ ant jar

The jar file can be found in `[project directory]/target`.

If you have made some changes then run:

      $ ant

to check if you have not broken the test or style checks.


Example of usage
----------------

    package example;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.nio.charset.Charset;

    import com.openstat.csdetecting.CharsetDetector;
    /**
     * Prints the content of file with unknown encoding.
     */
    public final class Example {
        private Example() { }

        /**
         * @param args - the first argument is path to file with unknown encoding
         * @throws IOException
         */
        public static void main(String[] args) throws IOException {
            String path = args.length == 1 ? args[0] : Example.class.getResource("file.txt").getPath();
            // read byte array from file
            File file = new File(path);
            FileInputStream stream = new FileInputStream(file);
            byte[] b = new byte[(int) file.length()];
            stream.read(b);
            stream.close();

            // init charset detector
            CharsetDetector detector = new CharsetDetector();
            // detect the charset
            Charset charset = detector.detectNioCharset(b);

            // print
            System.out.println("Not decoded: " + new String(b));
            System.out.println("Decoded: " + new String(b, charset));
        }
    }


How to train the algorithm with your own learning set
-----------------------------------------------------

This algorithm collects some linguistic statistics for detecting one-byte character sets
(for detecting UTF-8 it looks at the bit format).

In order to customize the algorithm you may want to train it again with your own learning set
(for example you may want the algorithm to detect the encoding of some special abbreviation -
so you can easily add it to your learning set)

To train the algorithm run the ant target "train" with several arguments:

      $ ant train -Dlearningset.path=<path to learning set> -Dlearningset.encoding=<encoding>

Command line arguments:

 - **learningset.path** is path to the learning set file. By default it is
       `[project directory]/learning-set/data.txt`

 - **learningset.encoding** is encoding of the learning set file. It can be `WIN_1251`, `KOI8_R`, `ISO_8859_5`
        or `IBM855`. By default it is `WIN_1251`

After you ran "ant train" run:

      $ ant

