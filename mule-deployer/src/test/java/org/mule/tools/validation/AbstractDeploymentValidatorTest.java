/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractDeploymentValidatorTest {

  private static final String CANNOT_RESOLVE_ENVIRONMENT_VERSION_MESSAGE = "Cannot resolve environment runtime version";
  private static final String MULE_VERSION = "4.1.0";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private DeploymentValidatorMock validator;
  private Deployment deploymentMock;

  @Before
  public void setUp() {
    expectedException.expect(DeploymentException.class);
  }

  @Test
  public void validateMuleVersionAgainstEnvironmentMissingMuleVersionTest() throws DeploymentException {
    expectedException.expectMessage("muleVersion is not present in deployment configuration");
    deploymentMock = createDeploymentMock(null);
    validator = new DeploymentValidatorMock(deploymentMock);
    validator.validateMuleVersionAgainstEnvironment();
  }

  private Deployment createDeploymentMock(String version) {
    Deployment deploymentMock = mock(Deployment.class);
    when(deploymentMock.getMuleVersion()).thenReturn(Optional.ofNullable(version));
    return deploymentMock;
  }

  @Test
  public void validateMuleVersionAgainstEnvironmentCannotResolveEnvironmentMuleVersionTest() throws DeploymentException {
    expectedException.expectMessage(CANNOT_RESOLVE_ENVIRONMENT_VERSION_MESSAGE);
    Deployment deploymentMock = createDeploymentMock(MULE_VERSION);
    validator = new DeploymentValidatorMock(deploymentMock);
    validator.validateMuleVersionAgainstEnvironment();
  }

  class DeploymentValidatorMock extends AbstractDeploymentValidator {

    public DeploymentValidatorMock(Deployment deployment) {
      super(deployment);
    }

    @Override
    public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
      throw new DeploymentException(CANNOT_RESOLVE_ENVIRONMENT_VERSION_MESSAGE);
    }
  }
}
