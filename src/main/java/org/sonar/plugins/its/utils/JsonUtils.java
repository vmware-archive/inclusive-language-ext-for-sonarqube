/***********************************************************
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/

package org.sonar.plugins.its.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    // Converts string to T
    public static <T> T convert(String jsonString, TypeReference<T> typeRef) throws IOException {
        if (StringUtils.isEmpty(jsonString))
            return null;
        try {
            T result = getMapper().readValue(jsonString, typeRef);
            return result;
        } catch (IOException e) {
            logger.error("Unable to convert {} to {}", jsonString, typeRef.toString());
            throw e;
        }
    }

    // Converts JsonNode to T
    public static <T> T convert(JsonNode json, TypeReference<T> typeRef) throws IOException {
        if (json == null)
            return null;
        try {
            T result = getMapper().readerFor(typeRef).readValue(json);
            return result;
        } catch (IOException e) {
            logger.error("Unable to convert {} to {}", getString(json), typeRef.toString());
            throw e;
        }
    }

    // Converts string to class T
    public static <T> T convert(String jsonString, Class<T> type) throws Exception {
        if (StringUtils.isEmpty(jsonString))
            return null;
        try {
            T result = getMapper().readValue(jsonString, type);
            return result;
        } catch (Exception e) {
            logger.error("Unable to convert {} to {}", jsonString, type);
            throw e;
        }
    }

    // Converts JsonNode to T
    public static <T> T convert(JsonNode json, Class<T> type) throws Exception {
        if (json == null)
            return null;
        try {
            if (type.equals(String.class))
                return (T)json.toString();
            T result = getMapper().convertValue(json, type);
            return result;
        } catch (Exception e) {
            logger.error("Unable to convert {} to {}", getString(json), type);
            throw e;
        }
    }

    // Converts T to JsonNode
    public static JsonNode toJsonNode(Object obj) throws Exception {
        return obj == null ? null: getMapper().valueToTree(obj);
    }

    public static ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore extra fields in node
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        return mapper;
    }

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return " Stack Trace:" + sw.toString();
    }

    /**
     * Converts a Java Object to JSON string
     * @param obj
     * @return
     * @throws JsonProcessingException
     */
    public static String toJsonString(Object obj) throws JsonProcessingException {
        return obj == null ? "" : getMapper().writeValueAsString(obj);
    }

    public static String getString(Object obj) {
        if (obj == null) {
            return "null";
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "Unable to convert object to string.";
        }
    }

    public static String prettyPrintJsonString(JsonNode jsonNode) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonNode.toString(), Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return "Unable to convert JsonNode to string.";
        }
    }

//    public static boolean isDevEnv() {
//        String mode = new PlayConfigurationPropertiesProvider().getProperties().getString("application.environment.mode", "DEV");
//        return "DEV".equals(mode);
//    }
//
//    public static boolean isProductionEnv() {
//        String mode = new PlayConfigurationPropertiesProvider().getProperties().getString("application.environment.mode", "DEV");
//        return "PROD".equals(mode);
//    }
//
//    public static boolean isStagingEnv() {
//        String mode = new PlayConfigurationPropertiesProvider().getProperties().getString("application.environment.mode", "DEV");
//        return "STG".equals(mode);
//    }
//
//    public static boolean isTestEnv() {
//        String mode = new PlayConfigurationPropertiesProvider().getProperties().getString("application.environment.mode", "DEV");
//        return mode.equalsIgnoreCase("test");
//    }

    public static <T> List<T> paginateCollection(List<T> allRecords, int start, int size) throws IOException {
        int nCases = allRecords.size();

        if (start < 0 || size < 0 || start > nCases) {
            throw new IOException("Illegal value of start or size.");
        }

        if (start + size > nCases) {
            size = nCases - start;
        }

        ArrayList<T> paginatedRecords = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            T caseId = allRecords.get(i + start);
            paginatedRecords.add(caseId);
        }

        return paginatedRecords;
    }

    /** return list of user project which also match list of artifact permission groups */
    public static List<String> matchUserAndArtifactProjects(List<String> userProjects, List<String>artifactGroups) {
        ArrayList<String> result = new ArrayList<String>();
        if (userProjects == null || artifactGroups == null)
            return result;
        for (int i = 0; i < userProjects.size(); i++) {
            String userProject = userProjects.get(i);
            for (int j = 0; j < artifactGroups.size(); j++) {
                String artifactGroup = artifactGroups.get(j);
                // use regular expression match if artifact group contains pattern
                boolean match = false;
                if (artifactGroup.contains("%")) {
                    artifactGroup = artifactGroup.replaceAll("%", ".*");
                    match = Pattern.matches(artifactGroup, userProject);
                }
                else {
                    match = artifactGroup.equalsIgnoreCase(userProject);
                }
                if (match) {
                    result.add(userProject);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Receives a list of byte array contents and file names. Contents and names are associated based on position.
     * Returns the zipped contents in a single zip file.
     *
     * @param contents - The contents of each file is a single element in this list.
     * @param fileNames - How do we want the files in the zip to be named.
     * @return ByteArrayOutputStream - Byte array stream representation of the zip file.
     * @throws IOException
     */
    public static ByteArrayOutputStream zipToByteArray(List<byte[]> contents, List<String> fileNames) throws IOException {
        if (contents.size() != fileNames.size()) {
            throw new IOException("Number of contents should match number of file names");
        }

        ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(zipOutput)) {
            for (int i = 0; i < fileNames.size(); i++) {
                ZipEntry entry = new ZipEntry(fileNames.get(i));

                zos.putNextEntry(entry);
                zos.write(contents.get(i));
                zos.closeEntry();
            }

            return zipOutput;
        }
    }

    public static String convertBooleanToEnglishText(boolean binaryValue) {
        return binaryValue ? "Yes" : "No";
    }

    public static String convertBooleanToEnglishText(String binaryValue) {
        return convertBooleanToEnglishText("true".equalsIgnoreCase(binaryValue));
    }

    public static JsonNode downloadJson(String jsonUrl) throws Exception {
        try (InputStream is = new URL(jsonUrl).openStream()) {
            String jsonConfigStr = IOUtils.toString(is, StandardCharsets.UTF_8);

            return getMapper().readTree(jsonConfigStr);
        }
    }

    public static String downloadFile(String url) throws Exception {
        try (InputStream is = new URL(url).openStream()) {
            String content = IOUtils.toString(is, StandardCharsets.UTF_8);
            return content;
        }
    }

    public static boolean isBundleFile(String fileName) {
        String ext = "";
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            ext = fileName.substring(i + 1);
        }
        return (ext.equals("zip") || ext.equals("tar")) ? true : false;
    }

    /**
     * Instantiates an object and sets its properties to the values from the map.
     *
     * @param keyValue A map with keys - name of the properties of the class and values the values for these properties.
     * @param target The target class
     * @param <T> Class type of the target class
     * @return An instance of type T with its properties set from the map.
     */
    public static <T> T convertMapToInstance(Map<String, String> keyValue, Class<T> target) throws Exception {
        if (target == null) {
            return null;
        }

        if (keyValue == null) {
            return target.newInstance();
        }

        return convert(toJsonNode(keyValue), target);
    }

    public static <K, V> Map<K, V> pluckFirst(Map<K, V[]> map) {
        if (map == null) {
            return null;
        }

        Map<K, V> newMap = new HashMap<>();

        map.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().length > 0)
                .forEach(e -> newMap.put(e.getKey(), e.getValue()[0]));

        return newMap;
    }

    public static <T> T queryParamsToFilterInstance(Map<String, String[]> queryParams, Class<T> target) throws Exception {
        return convertMapToInstance(pluckFirst(queryParams), target);
    }

}
