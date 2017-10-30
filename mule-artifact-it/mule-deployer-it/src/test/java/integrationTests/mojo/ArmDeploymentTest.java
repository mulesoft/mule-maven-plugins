/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package integrationTests.mojo;

import integrationTests.ProjectFactory;
import integrationTests.mojo.environment.setup.ArmEnvironment;
import integrationTests.mojo.environment.verifier.ArmDeploymentVerifier;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static integrationTests.mojo.environment.setup.ArmEnvironment.PRODUCTION_ENVIROMENT;

@Ignore
public class ArmDeploymentTest implements SettingsConfigurator {

  private static Logger log;
  private static Verifier verifier;
  private static File projectBaseDirectory;
  private static ProjectFactory builder;
  private static final String INSTALL = "install";
  private static final String MULE_DEPLOY = "mule:deploy";
  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String INSTANCE_NAME = RandomStringUtils.randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();
  private static final String APPLICATION_NAME = RandomStringUtils.randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();
  private ArmDeploymentVerifier armDeploymentVerifier;

  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-mule-deploy-arm-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
  }

  @Before
  public void before() throws VerificationException, InterruptedException, IOException, TimeoutException {
    armDeploymentVerifier = new ArmDeploymentVerifier();
    log = LoggerFactory.getLogger(this.getClass());
    log.info("Initializing context...");
    initializeContext();
    verifier.executeGoal(INSTALL);
    verifier.setEnvironmentVariable("username", System.getProperty("username"));
    verifier.setEnvironmentVariable("password", System.getProperty("password"));
    verifier.setEnvironmentVariable("target", INSTANCE_NAME);
    verifier.setEnvironmentVariable("target.type", "server");
    verifier.setEnvironmentVariable("environment", PRODUCTION_ENVIROMENT);
    verifier.setEnvironmentVariable("arm.application.name", APPLICATION_NAME);
    ArmEnvironment armEnvironment = new ArmEnvironment("4.0.0-SNAPSHOT", INSTANCE_NAME);
    armDeploymentVerifier.killMuleProcesses();
    armEnvironment.start();
  }

  @Test
  public void testArmDeploy() throws VerificationException, InterruptedException, TimeoutException {
    log.info("Executing mule:deploy goal...");
    verifier.setEnvironmentVariable("MAVEN_OPTS", "-agentlib:jdwp=transport=dt_socket,server=y,address=8002,suspend=y");

    verifier.executeGoal(MULE_DEPLOY);
    armDeploymentVerifier.verifyIsDeployed(INSTANCE_NAME);
    log.info("Application " + APPLICATION_NAME + " successfully deployed to ARM.");
    verifier.verifyErrorFreeLog();
  }

  @After
  public void after() throws IOException {
    armDeploymentVerifier.killMuleProcesses();
  }
}
