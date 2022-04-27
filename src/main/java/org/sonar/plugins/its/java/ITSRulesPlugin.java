/***********************************************************
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.plugins.its.java;

import org.sonar.api.Plugin;
import org.sonar.plugins.its.java.checks.ITSJavaFilesSensor;

/**
 * Entry point of your plugin containing your custom rules
 */
public class ITSRulesPlugin implements Plugin {

  @Override
  public void define(Context context) {

    // server extensions -> objects are instantiated during server startup
    context.addExtension(ITSRulesDefinition.class);

    // batch extensions -> objects are instantiated during code analysis
    context.addExtensions(ITSFileCheckRegistrar.class, ITSJavaFilesSensor.class);

  }

}
