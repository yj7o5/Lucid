package com.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Helper {
    public static String readProcessOutput(InputStream stream) {
        StringBuilder output = new StringBuilder();
        byte[] buffer = new byte[1024];

        try {
            int lastRead;
            while ((lastRead = stream.read(buffer, 0, buffer.length)) > 0) {
                if (lastRead < buffer.length)
                    buffer = Arrays.copyOfRange(buffer, 0, lastRead);
                output.append(new String(buffer, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.err.println(String.format("Error reading compilation output: %s\nTrace: %s", e.getMessage(), e.getStackTrace()));
        }

        return output.toString();
    }
}
