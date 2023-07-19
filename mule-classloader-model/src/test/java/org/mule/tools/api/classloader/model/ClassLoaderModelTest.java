/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.classloader.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ClassLoaderModelTest {

  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";

  private ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void checkNullVersionTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(null, artifactCoordinates);
  }

  @Test
  public void checkNullArtifactCoordinatesTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(VERSION, null);
  }

  @Test
  public void checkNullArgumentsTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(null, null);
  }
}
