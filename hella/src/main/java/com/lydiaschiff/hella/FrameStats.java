package com.lydiaschiff.hella;

/**
 * Fps and dropped frame logger interface.
 */
public interface FrameStats {
    void logFrame(String tag, int nDropped, int totalDropped, int total);
    void clear();
}
