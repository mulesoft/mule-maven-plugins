/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package integration.test.mojo;

import static integration.FileTreeMatcher.hasSameTreeStructure;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;

public class CompileMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String GOAL = "compile";

  public CompileMojoTest() {
    this.goal = GOAL;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testCompile() throws IOException, VerificationException {
    verifier.executeGoal(GOAL);
    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludesCompile));
    verifier.verifyErrorFreeLog();
  }
}
