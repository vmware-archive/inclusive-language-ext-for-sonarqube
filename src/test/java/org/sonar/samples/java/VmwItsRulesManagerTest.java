/***********************************************************
 * Copyright 2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.samples.java;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.sonar.plugins.its.service.ItsRulesManager;
import org.sonar.plugins.its.service.ItsScanRule;

public class VmwItsRulesManagerTest {

  @Test
  public void test() {
    List<ItsScanRule> rules = ItsRulesManager.getRules();

    assertThat(rules.size()).isGreaterThan(0);
  }

}
