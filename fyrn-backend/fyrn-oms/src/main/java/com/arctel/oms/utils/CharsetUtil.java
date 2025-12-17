package com.arctel.oms.utils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharsetUtil {

    public static String detectCharset(File file) throws Exception {
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);

        try (FileInputStream fis = new FileInputStream(file)) {
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
        }

        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();

        return encoding == null ? "UTF-8" : encoding;
    }


    public static File convertToUtf8(File src, String srcCharset) throws IOException {
        File target = new File(src.getParent(), src.getName() + ".utf8.txt");

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(src), Charset.forName(srcCharset)));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }

        return target;
    }
}