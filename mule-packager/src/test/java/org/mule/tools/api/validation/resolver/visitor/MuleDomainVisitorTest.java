/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.validation.resolver.visitor;

import org.junit.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static util.ResolverTestHelper.buildDependencies;
import static util.ResolverTestHelper.createProjectDependencyNodeSpy;

public class MuleDomainVisitorTest {

  private static final int NUMBER_DEPENDENCIES = 10;
  private ProjectDependencyNode nodeSpy;
  private MuleDomainVisitor visitor = new MuleDomainVisitor();

  @Test
  public void collectDependenciesTest() throws ValidationException {
    nodeSpy = createProjectDependencyNodeSpy();
    Set<ArtifactCoordinates> dependencies = buildDependencies(NUMBER_DEPENDENCIES);
    doReturn(dependencies).when(nodeSpy).getDependencies(any());

    visitor.collectDependencies(nodeSpy);

    assertThat("Collected dependencies should be empty", visitor.getCollectedDependencies().isEmpty(), is(true));
  }
}
