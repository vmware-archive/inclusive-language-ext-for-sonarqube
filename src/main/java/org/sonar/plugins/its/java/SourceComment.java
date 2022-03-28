package org.sonar.plugins.its.java;

public class SourceComment {
    private long start;
    private long end;

    public SourceComment(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }
}
