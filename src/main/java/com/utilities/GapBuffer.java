package com.utilities;

import java.util.Arrays;

/**
 * GapBuffer implementation for providing efficient text editing - faster insertion at the pointer
 */
public class GapBuffer {
    // current chars buffer pointer
    private char[] buffer;

    private int gapLeft, gapRight, size;

    public GapBuffer(int buffSize) {
        buffer = new char[buffSize];
        for(int i=0;i < buffSize;i++) buffer[i] = '\0';

        size = buffSize;
        gapLeft = 0;
        gapRight = size - gapLeft - 1;
    }

    // move the cursor to the left until left gap end is hit while also shifting chars to the right
    private void left(int pos) {
        while (pos < gapLeft) {
            gapLeft--;
            gapRight--;
            buffer[gapRight+1] = buffer[gapLeft];
            buffer[gapLeft] = '\0';
        }
    }

    // move the cursor to the right until right gap end is hit while shifting chars to the left
    private void right(int pos) {
        while (pos > gapLeft) {
            gapLeft++;
            gapRight++;
            buffer[gapLeft-1] = buffer[gapRight];
            buffer[gapRight] = '\0';
        }
    }

    // move the gap location to the start of the provided position
    private void moveTo(int position) {
        if (position < gapLeft) {
            left(position);
        }
        else {
            right(position);
        }
    }

    // grow the buffer at location @position by size @k
    private void grow(int k, int position) {
        size = size + k;
        char[] extendedBuffer = new char[size];

        gapRight = gapLeft + k;

        int i;
        for(i = 0; i < gapLeft; i++) {
            extendedBuffer[i] = buffer[i];
        }
        while (i < gapRight) {
            extendedBuffer[i] = '\0';
            i++;
        }
        for(i = gapRight+1; i < size; i++) {
            extendedBuffer[i] = buffer[i - k];
        }

        buffer = extendedBuffer;
    }

    public int getGapSize() {
        return gapRight - gapLeft + 1;
    }

    public int bufferSize() {
        return size - getGapSize();
    }

    public void printBuffer() {
        System.out.print(toString());
    }

    // insert string input at location @position in the current buffer
    public void insert(String input, int position) {
        int i = 0,
            len = input.length();

        if (position != gapLeft) moveTo(position);

        while ( i < len ) {
            if (gapLeft == gapRight) {
                grow(size, position);
            }

            // insert the character in the gap and move the gap
            buffer[gapLeft] = input.charAt(i);
            gapLeft++;
            i++;
            position++;
        }
    }

    @Override
    public String toString() {
        char[] b = new char[bufferSize()];
        int i = 0, j = 0;
        while (i < size) {
            if (buffer[i] != '\0') b[j++] = buffer[i];
            i++;
        }

        return new String(b);
    }
}
