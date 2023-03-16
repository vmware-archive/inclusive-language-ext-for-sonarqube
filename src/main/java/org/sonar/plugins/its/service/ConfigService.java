/***********************************************************
 * Copyright 2023 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.service;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for loading the external configuration for the plugin.
 */
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    public static Map<String, List<String>> loadConfig(SensorContext context) {
        FileSystem fs = context.fileSystem();

        File[] configFiles = fs.baseDir().listFiles((dir, name) -> name.contains("its-sonar-config"));

        Map<String, List<String>> config = new HashMap<>();

        if (configFiles.length > 0) {
            try {
                InputStream is = new FileInputStream(configFiles[0]);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                logger.info("ConfigLoader: " + jsonTxt);
                JSONArray jsonConfig = new JSONArray(jsonTxt);
                for (int i = 0; i < jsonConfig.length(); i++) {
                    JSONObject property = jsonConfig.getJSONObject(i);
                    String filepath = property.getString("filepath");
                    String terms = property.getString("terms");
                    config.put(filepath, Arrays.asList(terms.split(",")));
                }
            } catch (Exception e) {
                logger.error("Config file can't be opened", e);
            }
        }

        return config;
    }
}
