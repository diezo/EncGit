package com.diezo.encgit.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

public class Compressor {
    public static byte[] zlib_deflate(byte[] input) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        deflater.end();
        return outputStream.toByteArray();
    }

    public static byte[] zlib_inflate(byte[] input) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
             InflaterInputStream inflater = new InflaterInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int read;

            while ((read = inflater.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            return baos.toByteArray();
        }
    }
}
