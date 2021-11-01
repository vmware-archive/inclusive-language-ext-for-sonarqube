/***********************************************************
 * Copyright 2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.service;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.plugins.its.java.ITSRulesDefinition;
import org.sonar.plugins.its.utils.FileIOUtil;
import org.sonar.plugins.its.utils.LargeFileEncounteredException;
import org.sonar.plugins.its.utils.LineNumberFinderUtil;

/** Implementation of regex scanner using ITS rules */
public class ItsFileScanner {

    private static final Logger logger = LoggerFactory.getLogger(ItsFileScanner.class);
    protected static final int MAX_CHARACTERS_SCANNED = 10 * 1024 * 1024; // 10 Mb limit

    /** Scan file for offensive terms */
    public void scanFile(SensorContext context, Sensor sensor, InputFile file) {
        int lineNumberOfTriggerMatch = -1;

        Path path = file.file().toPath();
        String entireFileAsString;
        try {
            entireFileAsString = FileIOUtil.readFileAsString(path, MAX_CHARACTERS_SCANNED);
        } catch (LargeFileEncounteredException ex) {
             System.out.println("Skipping file. Its scanner (" +
             this.getClass().getSimpleName() + ") maximum file size ( " +
             (MAX_CHARACTERS_SCANNED-1) + " chars) encountered for file '" +
             path + "'. Did not check this file AT ALL.");
            return;
        }

        List<ItsScanRule> rules = ItsRulesManager.getRules();
        if (rules == null)
            return;

        for (int i = 0; i < rules.size(); i++) {
            ItsScanRule rule = rules.get(i);
            String triggerExpression = rule.getRegex();

            Pattern regexp = Pattern.compile(triggerExpression, Pattern.DOTALL);
            Matcher matcher = regexp.matcher(entireFileAsString);
            while (matcher.find()) {
                int positionOfMatch = matcher.start();
                lineNumberOfTriggerMatch = LineNumberFinderUtil.countLines(entireFileAsString, positionOfMatch);
                String message = "Offensive term: " + rule.getTerm() + ", replace with: " + rule.getReplacements();
                logger.info(path + ":" + lineNumberOfTriggerMatch + " - " + message);
                createViolation(context, file, rule, lineNumberOfTriggerMatch, message);
            }
        }
    }

    private void createViolation(SensorContext context, InputFile file, ItsScanRule rule, int line, String message) {
        // no need to define the severity as it is automatically set according
        // to the configured Quality profile
        NewIssue issue = context.newIssue().forRule(ITSRulesDefinition.ITS_RULE_KEY);

        Severity severity = Severity.MAJOR;
        if (rule.getSeverity() != null && rule.getSeverity().equalsIgnoreCase("high"))
            severity = Severity.CRITICAL;

        NewIssueLocation location = issue.newLocation()
             .on(file)
             .at(file.selectLine(line))
             .message(message);
        issue.at(location);
        issue.overrideSeverity(severity);
        issue.save();

    }
}
