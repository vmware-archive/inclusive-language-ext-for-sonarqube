/***********************************************************
 * Copyright 2023 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.java.checks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;

/**
 * Rule to detect all non inclusive terms in the source comments.
 * It should detect master, slave, kill, etc
 */
@Rule(key = ITSCommentsRule.KEY, name = "Inclusive Terminology Comments Scanner",
        description = "Scan text/source files comments for presence of offensive terms",
        priority = Priority.MAJOR)
public class ITSCommentsRule extends BaseTreeVisitor implements JavaFileScanner {

    public static final String KEY = "ITSCommentsRule";

    public ITSCommentsRule() {
        super();
        logger.info("ITSCommentsRule: created");
    }

    private static final Logger logger = LoggerFactory.getLogger(ITSCommentsRule.class);

    protected static final String COMPANY_NAME = "VMware";

    @Override
    public void scanFile(JavaFileScannerContext context) {
        logger.info("ITSCommentsRule: scanning " + context.getInputFile().filename());
    }

}
