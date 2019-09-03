package com.lucidcore;

public class Guard {
    public static Guard newGuard() {
        return new Guard();
    }

    private Guard() {}

    public void nullCheck(String arg, String message) {
        if(arg == null || arg.equals("")) {
            throw new IllegalArgumentException(message);
        }
    }
}
