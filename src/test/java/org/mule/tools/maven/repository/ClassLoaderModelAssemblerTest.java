/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mule.tools.maven.dependency.util.DependencyUtils.toDependency;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.maven.dependency.model.ArtifactCoordinates;
import org.mule.tools.maven.dependency.model.ClassLoaderModel;

public class ClassLoaderModelAssemblerTest {

  private static final Object POM_TYPE = "pom";
  private static final String PARENT_VERSION = "2.0.0";
  private static final String PARENT_GROUP_ID = "parent.group.id";
  private static final String USER_REPOSITORY_LOCATION = "/Users/muleuser/.m2/repository";
  private static final String SEPARATOR = "/";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String GROUP_ID_SEPARATOR = ".";
  private File dummyFile;
  private ClassLoaderModelAssembler classLoaderModelAssembler;
  private Log logMock;
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private TemporaryFolder temporaryFolder;
  private Model pomModel;
  private Parent parentProject;


  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private AetherMavenClient aetherMavenClientMock;

  @Before
  public void before() {
    temporaryFolder = new TemporaryFolder();
    dummyFile = new File(temporaryFolder.getRoot(), SEPARATOR);
    logMock = mock(Log.class);
    classLoaderModelAssembler =
        new ClassLoaderModelAssembler(logMock, mock(AetherMavenClient.class));
    pomModel = new Model();
    pomModel.setGroupId(GROUP_ID);
    pomModel.setArtifactId(ARTIFACT_ID);
    pomModel.setVersion(VERSION);
    parentProject = new Parent();
    parentProject.setVersion(PARENT_VERSION);
    parentProject.setGroupId(PARENT_GROUP_ID);
    pomModel.setParent(parentProject);
  }

  @Test
  public void getBundleDescriptorTest() {
    BundleDescriptor actualBundleDescriptor = classLoaderModelAssembler.getBundleDescriptor(pomModel);

    assertThat("Group id is not the expected", actualBundleDescriptor.getGroupId(), equalTo(GROUP_ID));
    assertThat("Artifact id is not the expected", actualBundleDescriptor.getArtifactId(), equalTo(ARTIFACT_ID));
    assertThat("Version is not the expected", actualBundleDescriptor.getVersion(), equalTo(VERSION));
    assertThat("Base version is not the expected", actualBundleDescriptor.getBaseVersion(), equalTo(VERSION));
    assertThat("Type is not the expected", actualBundleDescriptor.getType(), equalTo(POM_TYPE));
  }

  @Test
  public void getBundleDescriptorVersionFromParentTest() {
    pomModel.setVersion(null);
    BundleDescriptor actualBundleDescriptor = classLoaderModelAssembler.getBundleDescriptor(pomModel);
    assertThat("Version is not the expected", actualBundleDescriptor.getVersion(), equalTo(PARENT_VERSION));
    assertThat("Base version is not the expected", actualBundleDescriptor.getBaseVersion(), equalTo(PARENT_VERSION));
  }

  @Test
  public void getBundleDescriptorGroupIdFromParentTest() {
    pomModel.setGroupId(null);
    BundleDescriptor actualBundleDescriptor = classLoaderModelAssembler.getBundleDescriptor(pomModel);
    assertThat("Goup id is not the expected", actualBundleDescriptor.getGroupId(), equalTo(PARENT_GROUP_ID));
  }

  @Test
  public void getClassLoaderModelTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1, StringUtils.EMPTY);
    BundleDependency dependency2 = buildBundleDependency(1, 2, StringUtils.EMPTY);
    appDependencies.add(dependency1);
    appDependencies.add(dependency2);

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();
    BundleDependency firstMulePlugin = buildBundleDependency(2, 3, MULE_PLUGIN_CLASSIFIER);
    BundleDependency secondMulePlugin = buildBundleDependency(2, 4, MULE_PLUGIN_CLASSIFIER);
    appMulePluginDependencies.add(firstMulePlugin);
    appMulePluginDependencies.add(secondMulePlugin);

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    List<BundleDependency> firstMulePluginDependencies = new ArrayList<>();
    BundleDependency dependency5 = buildBundleDependency(1, 5, StringUtils.EMPTY);
    BundleDependency dependency6 = buildBundleDependency(1, 6, StringUtils.EMPTY);
    BundleDependency dependency7 = buildBundleDependency(1, 7, StringUtils.EMPTY);
    BundleDependency dependency8 = buildBundleDependency(1, 8, StringUtils.EMPTY);
    firstMulePluginDependencies.add(secondMulePlugin);
    firstMulePluginDependencies.add(dependency5);
    firstMulePluginDependencies.add(dependency8);

    setPluginDependencyinAetherMavenClientMock(firstMulePlugin, firstMulePluginDependencies);

    List<BundleDependency> secondMulePluginDependencies = new ArrayList<>();
    secondMulePluginDependencies.add(dependency6);
    secondMulePluginDependencies.add(dependency7);

    setPluginDependencyinAetherMavenClientMock(secondMulePlugin, secondMulePluginDependencies);

    ClassLoaderModelAssembler classLoaderModelAssemblerSpy = getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ClassLoaderModel classLoaderModel = classLoaderModelAssemblerSpy.getClassLoaderModel(mock(File.class), mock(File.class));

    assertThat("Application dependencies are not the expected", classLoaderModel.getDependencies(),
               containsInAnyOrder(toDependency(dependency1), toDependency(dependency2)));

    assertThat("Mule plugins are not the expected", classLoaderModel.getMulePlugins().keySet(),
               containsInAnyOrder(toDependency(firstMulePlugin), toDependency(secondMulePlugin)));

    assertThat("First mule plugin dependencies are not the expected",
               classLoaderModel.getMulePlugins().get(toDependency(firstMulePlugin)),
               containsInAnyOrder(toDependency(secondMulePlugin), toDependency(dependency5), toDependency(dependency8)));

    assertThat("Second mule plugin dependencies are not the expected",
               classLoaderModel.getMulePlugins().get(toDependency(secondMulePlugin)),
               containsInAnyOrder(toDependency(dependency6), toDependency(dependency7)));

  }

  private ClassLoaderModelAssembler getClassLoaderModelAssemblySpy(AetherMavenClient aetherMavenClientMock) {
    ClassLoaderModelAssembler classLoaderModelAssemblerSpy = spy(new ClassLoaderModelAssembler(logMock, aetherMavenClientMock));
    ArtifactCoordinates projectArtifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
    doReturn(projectArtifactCoordinates).when(classLoaderModelAssemblerSpy).getArtifactCoordinates(any());
    BundleDescriptor projectBundleDescriptor = mock(BundleDescriptor.class);
    doReturn(projectBundleDescriptor).when(classLoaderModelAssemblerSpy).getProjectBundleDescriptor(any());
    return classLoaderModelAssemblerSpy;
  }

  private void setPluginDependencyinAetherMavenClientMock(BundleDependency mulePlugin,
                                                          List<BundleDependency> mulePluginDependencies) {
    when(aetherMavenClientMock
        .resolveBundleDescriptorDependencies(eq(false), eq(false), eq(mulePlugin.getDescriptor())))
            .thenReturn(mulePluginDependencies);
  }

  @Test
  public void getClassLoaderModelWithOneDependencyThatIsNotMulePluginTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();
    BundleDependency dependency1 = buildBundleDependency(1, 1, StringUtils.EMPTY);
    appDependencies.add(dependency1);

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    when(aetherMavenClientMock
        .resolveBundleDescriptorDependencies(eq(false), eq(false), any()))
            .thenReturn(new ArrayList<>());

    ClassLoaderModelAssembler classLoaderModelAssemblerSpy = getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ClassLoaderModel classLoaderModel = classLoaderModelAssemblerSpy.getClassLoaderModel(mock(File.class), mock(File.class));

    assertThat("Application dependencies are not the expected", classLoaderModel.getDependencies(),
               containsInAnyOrder(toDependency(dependency1)));

    assertThat("The application should have no mule plugin dependencies", classLoaderModel.getMulePlugins().size(),
               equalTo(0));
  }

  @Test
  public void getClassLoaderModelWithOneDependencyThatIsAMulePluginTest() throws URISyntaxException {
    List<BundleDependency> appDependencies = new ArrayList<>();

    List<BundleDependency> appMulePluginDependencies = new ArrayList<>();
    BundleDependency firstMulePlugin = buildBundleDependency(2, 3, MULE_PLUGIN_CLASSIFIER);
    appMulePluginDependencies.add(firstMulePlugin);

    aetherMavenClientMock = getAetherMavenClientMock(appDependencies, appMulePluginDependencies);

    List<BundleDependency> firstMulePluginDependencies = new ArrayList<>();
    BundleDependency mulePluginTransitiveDependency1 = buildBundleDependency(1, 1, StringUtils.EMPTY);
    firstMulePluginDependencies.add(mulePluginTransitiveDependency1);

    setPluginDependencyinAetherMavenClientMock(firstMulePlugin, firstMulePluginDependencies);

    ClassLoaderModelAssembler classLoaderModelAssemblerSpy = getClassLoaderModelAssemblySpy(aetherMavenClientMock);

    ClassLoaderModel classLoaderModel = classLoaderModelAssemblerSpy.getClassLoaderModel(mock(File.class), mock(File.class));

    assertThat("The class loader model should have no elements as dependencies", classLoaderModel.getDependencies().size(),
               equalTo(0));

    assertThat("Mule plugins are not the expected", classLoaderModel.getMulePlugins().keySet(),
               containsInAnyOrder(toDependency(firstMulePlugin)));

    assertThat("First mule plugin dependencies are not the expected",
               classLoaderModel.getMulePlugins().get(toDependency(firstMulePlugin)),
               containsInAnyOrder(toDependency(mulePluginTransitiveDependency1)));
  }

  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier)
      throws URISyntaxException {
    BundleDescriptor bundleDescriptor = buildBundleDescriptor(groupIdSuffix, artifactIdSuffix, classifier);
    URI bundleUri = buildBundleURI(bundleDescriptor);
    return new BundleDependency.Builder().setDescriptor(bundleDescriptor).setBundleUri(bundleUri).build();
  }

  private URI buildBundleURI(BundleDescriptor bundleDescriptor) throws URISyntaxException {
    return new URI(USER_REPOSITORY_LOCATION + SEPARATOR + bundleDescriptor.getGroupId().replace(GROUP_ID_SEPARATOR, SEPARATOR)
        + bundleDescriptor.getArtifactId() + SEPARATOR + bundleDescriptor.getBaseVersion());

  }

  private BundleDescriptor buildBundleDescriptor(int groupIdSuffix, int artifactIdSuffix, String classifier) {
    return new BundleDescriptor.Builder().setGroupId(GROUP_ID + groupIdSuffix).setArtifactId(ARTIFACT_ID + artifactIdSuffix)
        .setVersion(VERSION).setBaseVersion(VERSION).setType(TYPE).setClassifier(classifier).build();
  }

  public AetherMavenClient getAetherMavenClientMock(List<BundleDependency> appDependencies,
                                                    List<BundleDependency> appMulePluginDependencies) {
    AetherMavenClient aetherMavenClientMock = mock(AetherMavenClient.class);
    when(aetherMavenClientMock
        .resolveBundleDescriptorDependenciesWithWorkspaceReader(any(File.class), anyBoolean(), anyBoolean(),
                                                                any(BundleDescriptor.class),
                                                                eq(classLoaderModelAssembler.mulePluginFilter),
                                                                eq(classLoaderModelAssembler.mulePluginFilter)))
                                                                    .thenReturn(appDependencies);

    when(aetherMavenClientMock
        .resolveBundleDescriptorDependenciesWithWorkspaceReader(any(File.class), anyBoolean(), anyBoolean(),
                                                                any(BundleDescriptor.class),
                                                                eq(classLoaderModelAssembler.notMulePluginFilter),
                                                                eq(classLoaderModelAssembler.notMulePluginFilter)))
                                                                    .thenReturn(appMulePluginDependencies);

    return aetherMavenClientMock;
  }
}
