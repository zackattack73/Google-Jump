package jvn;

public enum JvnStateLock {
    N, // No lock
    R, // Read
    W, // Write
    RC, // Read cached
    WC, // Write cached
    RWC // Read lock and write cached
}
