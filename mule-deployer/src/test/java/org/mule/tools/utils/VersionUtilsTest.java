/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.tools.utils.VersionUtils.isSameVersion;

@RunWith(Parameterized.class)
public class VersionUtilsTest {

  private static final String VERSION_A = "4.0.0";
  private static final String VERSION_B = "4.0.1";

  private final String version1;
  private final String version2;
  private final Boolean expectedResult;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {VERSION_A, VERSION_A, Boolean.TRUE},
        {VERSION_A, VERSION_B, Boolean.FALSE},
        {VERSION_B, VERSION_A, Boolean.FALSE},
        {null, VERSION_A, Boolean.FALSE},
        {VERSION_A, null, Boolean.FALSE},
        {null, null, Boolean.TRUE}
    });
  }

  public VersionUtilsTest(String version1, String version2, Boolean expectedResult) {
    this.version1 = version1;
    this.version2 = version2;
    this.expectedResult = expectedResult;
  }

  @Test
  public void isSameVersionTest() {
    assertThat("isSameVersion didn't return the expected return value when called with parameters " + version1 + " and "
        + version2, isSameVersion(version1, version2), equalTo(expectedResult));
  }

}
