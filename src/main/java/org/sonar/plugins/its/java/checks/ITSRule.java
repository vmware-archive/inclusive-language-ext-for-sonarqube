/***********************************************************
 * Copyright 2021 VMware, Inc.
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

@Rule(key = ITSRule.KEY,
    name = "Inclusive Terminology Scanner",
    description = "Scan text/source files for presense of offensive terms",
    priority = Priority.MINOR)
public class ITSRule extends BaseTreeVisitor implements JavaFileScanner {

    public static final String KEY = "ITSRule";

    public ITSRule() {
        super();
        logger.info("ITSRule: created");
    }

    private static final Logger logger = LoggerFactory.getLogger(ITSRule.class);

    protected static final String COMPANY_NAME = "VMware";

    @Override
    public void scanFile(JavaFileScannerContext context) {
        logger.info("ITSRule: scanning " + context.getInputFile().filename());
    }

}
