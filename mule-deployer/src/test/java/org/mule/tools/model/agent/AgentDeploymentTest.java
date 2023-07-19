/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.model.agent;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.core.exception.DeploymentException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.spy;

public class AgentDeploymentTest {

  private AgentDeployment deploymentSpy;

  @Before
  public void setUp() {
    deploymentSpy = spy(AgentDeployment.class);
  }

  @Test
  public void setAgentDeploymentValuesAnypointUriSetSystemPropertyTest() throws DeploymentException {
    String anypointUri = "www.lala.com";
    System.setProperty("anypoint.baseUri", anypointUri);
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The anypoint baseUri was not resolved by system property",
               deploymentSpy.getUri(), equalTo(anypointUri));
    System.clearProperty("anypoint.baseUri");
  }

  @Test
  public void setAgentDeploymentValuesAnypointUriNotSetTest() throws DeploymentException {
    String anypointUriDefaultValue = "https://anypoint.mulesoft.com";
    deploymentSpy.setEnvironmentSpecificValues();
    assertThat("The anypoint baseUri was not resolved to the default value",
               deploymentSpy.getUri(), equalTo(anypointUriDefaultValue));
  }
}
