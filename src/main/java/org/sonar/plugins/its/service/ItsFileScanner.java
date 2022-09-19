/***********************************************************
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.plugins.its.java.ITSRulesDefinition;
import org.sonar.plugins.its.java.SourceComment;
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

        List<SourceComment> comments = new ArrayList<SourceComment>();

        // TODO add support for comments in Python, Ruby, HTML, JSON
        // Check file extension and get the regex for the different file
        String slComment = "//[^\r\n]*";
        String mlComment = "/\\*[\\s\\S]*?\\*/";
//        String strLit = "\"(?:\\\\.|[^\\\\\"\r\n])*\"";
//        String chLit = "'(?:\\\\.|[^\\\\'\r\n])+'";
//        String any = "[\\s\\S]";

        Pattern p = Pattern.compile(
                String.format("(%s)|(%s)", slComment, mlComment)); //strLit, chLit, any));

        Path path = file.file().toPath();
        String entireFileAsString;
        try {
            entireFileAsString = FileIOUtil.readFileAsString(path, MAX_CHARACTERS_SCANNED);
        } catch (LargeFileEncounteredException ex) {
            System.out.println("Skipping file. Its scanner (" +
                    this.getClass().getSimpleName() + ") maximum file size ( " +
                    (MAX_CHARACTERS_SCANNED - 1) + " chars) encountered for file '" +
                    path + "'. Did not check this file AT ALL.");
            return;
        }


        // Finds all comments in the source code
        Matcher m = p.matcher(entireFileAsString);

        while (m.find()) {
            if (m.group(1) != null) {
                comments.add(new SourceComment(m.start(), m.end()));
            }
            if (m.group(2) != null) {
                comments.add(new SourceComment(m.start(), m.end()));
            }
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

                // checks if in comment and sets different rule
                if (isInComment(matcher, comments)) {
                    String message = "Offensive term: " + rule.getTerm() + ", replace with: " + rule.getReplacements();
                    logger.info("COMMENT Offensive Term "+path + ":" + lineNumberOfTriggerMatch + " - " + message);
                    createViolation(context, file, rule, lineNumberOfTriggerMatch, message, true);
                } else {
                    String message = "SOURCE Offensive term: " + rule.getTerm() + ", replace with: " + rule.getReplacements();
                    logger.info("SOURCE Offensive Term "+path + ":" + lineNumberOfTriggerMatch + " - " + message);
                    createViolation(context, file, rule, lineNumberOfTriggerMatch, message, false);
                }
            }
        }
    }

    private boolean isInComment(Matcher matcher, List<SourceComment> comments) {
        int positionOfMatch = matcher.start();
        int positionOfEnd = matcher.end();

        for (int commentIndex = 0; commentIndex < comments.size(); commentIndex++) {
            SourceComment currentComment = comments.get(commentIndex);

            if (currentComment.getStart() <= positionOfMatch && currentComment.getEnd() >= positionOfEnd) {
                return true;
            }
        }
        return false;
    }

    private void createViolation(SensorContext context, InputFile file, ItsScanRule rule, int line, String message,
            boolean commentRule) {
        // no need to define the severity as it is automatically set according to the
        // configured Quality profile
        NewIssue issue;

        if (commentRule) {
            issue = context.newIssue().forRule(ITSRulesDefinition.ITS_COMMENT_RULE_KEY);
        } else {
            issue = context.newIssue().forRule(ITSRulesDefinition.ITS_RULE_KEY);
        }

        // Inclusive Terminology issues do NOT lead to security bugs or potential of
        // maintainer to introduce new bugs in the future,
        // therefore according to Severity ratings on
        // https://docs.sonarqube.org/7.3/RulesTypesandSeverities.html all these issues
        // are Minor except those in source comments

        NewIssueLocation location = issue.newLocation()
                .on(file)
                .at(file.selectLine(line))
                .message(message);
        issue.at(location);
        issue.save();

    }
}
