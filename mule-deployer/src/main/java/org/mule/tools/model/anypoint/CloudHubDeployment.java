/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.core.exception.DeploymentException;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CloudHubDeployment extends AnypointDeployment {

  @Parameter
  protected Integer workers;

  @Parameter
  protected String workerType;

  @Parameter
  protected String region;

  @Parameter(defaultValue = "true")
  protected Boolean overrideProperties = true;

  /**
   * Region to deploy the application in Cloudhub.
   *
   * @since 2.0
   */
  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  /**
   * Number of workers for the deploymentConfiguration of the application in Cloudhub.
   *
   * @since 2.0
   */
  public Integer getWorkers() {
    return workers;
  }

  public void setWorkers(Integer workers) {
    this.workers = workers;
  }

  /**
   * Flag to override properties defined on the application in Cloudhub.
   *
   * @since 2.0
   */
  public Boolean getOverrideProperties() {
    return overrideProperties;
  }

  public void setOverrideProperties(Boolean overrideProperties) {
    this.overrideProperties = overrideProperties;
  }

  /**
   * Type of workers for the deploymentConfiguration of the application in Cloudhub.
   *
   * @since 2.0
   */
  public String getWorkerType() {
    return workerType;
  }

  public void setWorkerType(String workerType) {
    this.workerType = workerType;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();

    String cloudHubWorkers = getProperty("cloudhub.workers");
    if (isNotBlank(cloudHubWorkers)) {
      setWorkers(Integer.valueOf(cloudHubWorkers));
    }

    String cloudHubWorkerType = getProperty("cloudhub.workerType");
    if (isNotBlank(cloudHubWorkerType)) {
      setWorkerType(cloudHubWorkerType);
    }

  }

}
