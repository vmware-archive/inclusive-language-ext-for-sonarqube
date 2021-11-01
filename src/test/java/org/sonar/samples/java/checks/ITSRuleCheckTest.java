/***********************************************************
 * Copyright 2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2
 ***********************************************************/
package org.sonar.samples.java.checks;

public class ITSRuleCheckTest {

  // @Test
  public void detected() {
      // this does not compile for Sonar 7.9, disabling this test

//    JavaCheckVerifier.newVerifier()
//      .onFile("src/test/files/ITSRuleCheck.java")
//      .withCheck(new ITSRule())
//      .verifyIssues();
  }
}
