/***********************************************************
 * Copyright 2023 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.java;

/**
 * Class describing the start and the end of a comment.
 * The term master shouldn't be detected here because of the external configuration.
 */
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
