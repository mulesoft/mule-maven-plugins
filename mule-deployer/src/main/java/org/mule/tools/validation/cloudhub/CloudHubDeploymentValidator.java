/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.validation.cloudhub;

import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.SupportedVersion;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates if the mule runtime version is valid in a CloudHub deployment scenario.
 */
public class CloudHubDeploymentValidator extends AbstractDeploymentValidator {

  public CloudHubDeploymentValidator(Deployment deployment) {
    super(deployment);
  }

  @Override
  public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
    CloudHubClient client = getCloudHubClient();
    List<SupportedVersion> supportedMuleVersions = client.getSupportedMuleVersions();
    return new EnvironmentSupportedVersions(supportedMuleVersions.stream().map(sv -> sv.getVersion())
        .collect(Collectors.toSet()));
  }

  /**
   * Creates a CloudHub client based on the deployment configuration.
   * 
   * @return The generated CloudHub client.
   */
  private CloudHubClient getCloudHubClient() {
    CloudHubClient client = new CloudHubClient((CloudHubDeployment) deployment, null);
    return client;
  }
}
