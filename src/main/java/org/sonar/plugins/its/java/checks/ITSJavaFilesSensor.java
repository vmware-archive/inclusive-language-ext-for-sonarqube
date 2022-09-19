/***********************************************************
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.java.checks;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.plugins.its.service.ItsFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;

/**
 * Generates issues on all java files at line 1. This rule must be activated in
 * the Quality profile.
 */
public class ITSJavaFilesSensor implements Sensor {

    private static final Logger logger = LoggerFactory.getLogger(ITSJavaFilesSensor.class);

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("Inclusive Terminology Sensor (ITS)");

        // optimization to suppress execution of sensor if project does
        // not contain Java files or if the example rule is not activated
        // in the Quality profile

        // Default is to execute sensor for all languages.
        // descriptor.onlyOnLanguage("java");

        // descriptor.createIssuesForRuleRepositories(ITSRulesDefinition.REPOSITORY_KEY);
    }

    @Override
    public void execute(SensorContext context) {
        FileSystem fs = context.fileSystem();
        Iterable<InputFile> files = fs.inputFiles(fs.predicates().all());
        for (InputFile file : files) {
            try {
                String filename = file.filename();
                String fileExt = FilenameUtils.getExtension(filename).toLowerCase();
                if (fileExt.toLowerCase().equalsIgnoreCase("jar") || fileExt.toLowerCase().equalsIgnoreCase("zip")) {
                    logger.info("ITSRule: skipping binary file " + filename);
                    continue;
                }
                logger.info("Scanning " + file.filename());

                ItsFileScanner scanner = new ItsFileScanner();
                scanner.scanFile(context, this, file);
            } catch (Exception e) {
                logger.error("An error occurred with file " + file.filename());
                logger.error(e.getMessage(), e.getStackTrace());
            }
        }
    }

    public void scanFile(JavaFileScannerContext context) {
        // The call to the scan method on the root of the tree triggers the visit of the
        // AST by this visitor

        InputFile file = context.getInputFile();
        logger.info("ITSRule: scanning " + file.filename());

    }

}
