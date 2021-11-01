/***********************************************************
 * Copyright 2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/

package org.sonar.plugins.its.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties({"_$dbName"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItsScanRule {
    private String term;
    private String regex;
    private String severity;
    private String replacements;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getReplacements() {
        return replacements;
    }

    public void setReplacements(String replacements) {
        this.replacements = replacements;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

}
