package example;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.openstat.charsetdetector.CharsetDetector;
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
