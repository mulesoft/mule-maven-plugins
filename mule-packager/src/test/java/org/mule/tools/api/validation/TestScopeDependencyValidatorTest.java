/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.validation;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestScopeDependencyValidatorTest {

  private static final String GROUP_ID = "org.mule.test";
  private static final String SECOND_GROUP_ID = "org.mule.testgroup";
  private static final String ARTIFACT_ID = "artifact";

  private DependencyValidator testScopeDependencyValidator;

  @Before
  public void setUp() {
    this.testScopeDependencyValidator =
        new TestScopeDependencyValidator(singletonList(new TestScopeDependencyValidator.Dependency(GROUP_ID, ARTIFACT_ID)),
                                         singletonList(SECOND_GROUP_ID));
  }

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void testExceptionWhenInvalidDependencyValidatingMultiple() throws Exception {
    List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(GROUP_ID, ARTIFACT_ID, "compile"));
    expectedException.expect(ValidationException.class);
    testScopeDependencyValidator.areDependenciesValid(dependencies);
  }

  @Test
  public void testExceptionWhenInvalidGroupValidatingMultiple() throws Exception {
    List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(SECOND_GROUP_ID, ARTIFACT_ID, "compile"));
    expectedException.expect(ValidationException.class);
    testScopeDependencyValidator.areDependenciesValid(dependencies);
  }

  @Test
  public void validatorDoesNotFailIfScopeIsTests() throws Exception {
    List<ArtifactCoordinates> dependencies = singletonList(createCoordinates(GROUP_ID, ARTIFACT_ID, "test"));
    assertThat(testScopeDependencyValidator.areDependenciesValid(dependencies), is(true));
  }


  private ArtifactCoordinates createCoordinates(String groupId, String artifactId, String scope) {
    return new ArtifactCoordinates(groupId, artifactId, "0.0.0", "jar", "", scope);
  }

}
