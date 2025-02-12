/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.standalone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.Prober;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StandaloneArtifactDeployerTest {

  private static final String ARTIFACT_NAME = "artifact";
  private static final String ARTIFACT_FILENAME = ARTIFACT_NAME + ".jar";
  private static final String MULE_HOME_DIRECTORY = "mule_home";
  private StandaloneArtifactDeployer deployer;
  private StandaloneDeployment deploymentMock;
  private MuleProcessController controllerMock;
  private DeployerLog logMock;
  private Prober proberMock;
  private StandaloneArtifactDeployer deployerSpy;
  private File artifactFile;
  private File muleHome;

  @TempDir
  Path temporaryFolder;

  @BeforeEach
  public void setUp() throws DeploymentException, IOException {
    deploymentMock = mock(StandaloneDeployment.class);
    artifactFile = temporaryFolder.resolve((ARTIFACT_FILENAME)).toFile();
    muleHome = Files.createDirectories(temporaryFolder.resolve(MULE_HOME_DIRECTORY)).toFile();
    doReturn(artifactFile).when(deploymentMock).getArtifact();
    doReturn(muleHome).when(deploymentMock).getMuleHome();
    doReturn(ARTIFACT_NAME).when(deploymentMock).getApplicationName();
    controllerMock = mock(MuleProcessController.class);
    logMock = mock(DeployerLog.class);
    proberMock = mock(Prober.class);
    deployer = new StandaloneArtifactDeployer(deploymentMock, controllerMock, logMock, proberMock);
    deployerSpy = spy(deployer);
    doNothing().when(deployerSpy).renameApplicationToApplicationName();
    doNothing().when(deployerSpy).addDomainFromStandaloneDeployment(any());
  }

  @Test
  public void deployDomainTest() {}

  @Test
  public void undeployDomainTest() {
    assertThatThrownBy(() -> deployer.undeployDomain())
        .isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void deployApplicationNullFileTest() {
    assertThatThrownBy(() -> {
      doReturn(null).when(deploymentMock).getArtifact();
      deployerSpy.deployApplication();
    }).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void deployApplicationTest() throws DeploymentException {
    deployerSpy.deployApplication();
    verify(controllerMock, times(1)).deploy(artifactFile.getAbsolutePath());
  }

  @Test
  public void deployApplicationMuleControllerExceptionTest() {
    assertThatThrownBy(() -> {
      doThrow(new MuleControllerException()).when(controllerMock).deploy(artifactFile.getAbsolutePath());
      deployer.deployApplication();
    }).isExactlyInstanceOf(DeploymentException.class);

  }

  @Test
  public void undeployApplicationNotExistentMuleHomeTest() {
    assertThatThrownBy(() -> {
      muleHome.delete();
      deployer.undeployApplication();
    }).isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    doNothing().when(deployerSpy).undeploy(muleHome);

    deployerSpy.undeployApplication();

    verify(deployerSpy, times(1)).undeploy(muleHome);
  }


  @Test
  public void undeployTest() throws DeploymentException, IOException {
    File appsFolder = new File(muleHome, "apps");
    assertThat(appsFolder.mkdir()).describedAs("Directory should have been created").isTrue();

    File deployedFile = new File(appsFolder, ARTIFACT_FILENAME);
    assertThat(deployedFile.createNewFile()).describedAs("File should have been created").isTrue();

    deployer.undeploy(muleHome);

    assertThat(deployedFile.exists()).describedAs("File should have been deleted").isFalse();
    assertThat(appsFolder.exists()).describedAs("Folder shouldn't be deleted").isTrue();
  }

  @Test
  public void undeployNotFoundTest() {
    assertThatThrownBy(() -> {
      File appsFolder = new File(muleHome, "apps");
      assertThat(appsFolder.mkdir()).describedAs("Directory should have been created").isTrue();

      File deployedFile = new File(appsFolder, ARTIFACT_FILENAME);
      assertThat(deployedFile.exists()).describedAs("File should have been deleted").isFalse();

      deployer.undeploy(muleHome);
    }).isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void verifyMuleIsStartedTest() {
    doReturn(true).when(controllerMock).isRunning();
    deployer.verifyMuleIsStarted();
    verify(controllerMock, times(1)).isRunning();
  }

  @Test
  public void verifyMuleIsStartedExceptionTest() {
    assertThatThrownBy(() -> {
      doReturn(false).when(controllerMock).isRunning();
      deployer.verifyMuleIsStarted();
      verify(controllerMock, times(1)).isRunning();
    }).isExactlyInstanceOf(MuleControllerException.class);
  }

  @Test
  void waitForDeploymentsTest() throws DeploymentException {
    File artifact = mock(File.class);
    when(deploymentMock.getArtifact()).thenReturn(artifact);
    when(deploymentMock.getPackaging()).thenReturn("mule-application");
    when(deploymentMock.getDeploymentTimeout()).thenReturn(Optional.of(10L));
    when(artifact.exists()).thenReturn(true);
    when(artifact.getName()).thenReturn("artifact.jar");
    // TIMEOUT
    StandaloneArtifactDeployer deployer00 = new StandaloneArtifactDeployer(deploymentMock, logMock);
    assertThatThrownBy(deployer00::waitForDeployments).isExactlyInstanceOf(DeploymentException.class)
        .hasMessageContaining("Application deployment timeout");

    // ARTIFACT NOT EXISTS
    when(artifact.exists()).thenReturn(false);
    StandaloneArtifactDeployer deployer01 = new StandaloneArtifactDeployer(deploymentMock, logMock);
    assertThatThrownBy(deployer01::waitForDeployments).isExactlyInstanceOf(DeploymentException.class)
        .hasMessageContaining("Application does not exist: ");
  }

}
