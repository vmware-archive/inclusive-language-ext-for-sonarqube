/***********************************************************
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.its.utils.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;

public class ItsRulesManager  {
    private static final Logger logger = LoggerFactory.getLogger(ItsRulesManager.class);

    private static List<ItsScanRule> fgRules;
    private static long fgLastRulesUpdateTime;
    private static final long RulesUpdateIntervalMins = 5;
    private static final String ItsServiceRulesUrlPublic = "https://vmw-itsx-resources.s3.us-west-2.amazonaws.com/public/its-rules.json";

    private static String defaultScanRulesJson = "[ " +
     "{\"term\":\"abort\",\"regex\":\"\\\\babort\\\\b\",\"replacements\":\"stop, cancel, halt prematurely, end prematurely, stop prematurely\"}, " +
     "{\"term\":\"black hat\",\"regex\":\"black[\\\\s-_]*hat\",\"replacements\":\"unethical(adj)\"},                                           " +
     "{\"term\":\"blacklist\",\"regex\":\"black[\\\\s-_]*list\",\"replacements\":\"denylist(n), block(v), ban(v)\"},                           " +
     "{\"term\":\"blackout\",\"regex\":\"black[\\\\s-_]*out\",\"replacements\":\"restrict(v), restriction(n), outage(n)\"},                    " +

    "{\"term\":\"disable\",\"regex\":\"\\\\bdisable[d]*\\\\b\",\"replacements\":\"deactivate\"},                                                " +
    "{\"term\":\"evict\",\"regex\":\"\\\\bevict\\\\b\",\"replacements\":\"remove(v), eject(v)\"},                                               " +
    "{\"term\":\"eviction\",\"regex\":\"eviction\",\"replacements\":\"removal(n)\"},                                                        " +
    "{\"term\":\"execute\",\"regex\":\"\\\\bexecute[d]*\\\\b\",\"replacements\":\"run\"},               " +
    "{\"term\":\"female\",\"regex\":\"\\\\bfemale\\\\b\",\"replacements\":\"jack(n), socket(n)\"},      " +
    "{\"term\":\"he\",\"regex\":\"\\\\bhe\\\\b\",\"replacements\":\"they\"},                            " +
    "{\"term\":\"kill\",\"regex\":\"\\\\bkill\\\\b\",\"replacements\":\"stop, halt\"},                  " +
    "{\"term\":\"male\",\"regex\":\"\\\\bmale\\\\b\",\"replacements\":\"plug\"},                        " +

    "{\"term\":\"master\",\"regex\":\"\\\\bmaster\\\\b\",\"replacements\":\"primary, controller, control plane\"},                              " +
    "{\"term\":\"rule-of-thumb\",\"regex\":\"rule[\\\\s-_]*of[\\\\s-_]*thumb\",\"replacements\":\"rule, guideline\"},          " +

    "{\"term\":\"segregate\",\"regex\":\"\\\\bsegregate\",\"replacements\":\"separate\"},             " +
    "{\"term\":\"segregation\",\"regex\":\"\\\\bsegregation\\\\b\",\"replacements\":\"separation\"},    " +
    "{\"term\":\"she\",\"regex\":\"\\\\bshe\\\\b\",\"replacements\":\"they\"},                          " +
    "{\"term\":\"slave\",\"regex\":\"\\\\bslave\\\\b\",\"replacements\":\"secondary, worker, replica\"}," +
    "{\"term\":\"suffer\",\"regex\":\"\\\\bsuffer\\\\b\",\"replacements\":\"decrease, lessen, shrink\"}," +
    "{\"term\":\"white hat\",\"regex\":\"white[\\\\s-_]*hat\",\"replacements\":\"ethical(adj)\"},     " +

     "{\"term\":\"whitelist\",\"regex\":\"white[\\\\s-_]*list\",\"replacements\":\"allowlist(n), allow(v), safelist(n), acceptlist(n)\"}       " +
     "]";

    /** Return list of scan rules */
    public static List<ItsScanRule> getRules() {

        // check if rules need to be updated
        long updateInterval = TimeUnit.MINUTES.toMillis(RulesUpdateIntervalMins);
        boolean needUpdate = System.currentTimeMillis() - fgLastRulesUpdateTime > updateInterval;
        if (needUpdate) {
            loadRulesFromItsServer(ItsServiceRulesUrlPublic);
        }

        if (fgRules != null)
            return fgRules;

        List<ItsScanRule> rules = null;
        try {
            rules = JsonUtils.convert(defaultScanRulesJson, new TypeReference<List<ItsScanRule>>() {});
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        fgRules = rules;
        return rules;
    }

    /** Load ITS rules configuration from URL */
    public static synchronized boolean loadRulesFromItsServer(String rulesUrl) {
        try {
            URL url = new URL(rulesUrl);
            logger.info("loading ITS rules from {}", url);
            long start = System.currentTimeMillis();

            String rulesJson = IOUtils.toString(url);
            List<ItsScanRule> rules = JsonUtils.convert(rulesJson, new TypeReference<List<ItsScanRule>>() {});
            fgRules = rules;
            fgLastRulesUpdateTime = System.currentTimeMillis();
            long elapsed = fgLastRulesUpdateTime - start;

            logger.info("loaded {} ITS rules in {} ms", rules.size(), elapsed);
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
