/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

public class ApplicationClassloaderModel {

  private ClassLoaderModel classLoaderModel;
  private Map<ArtifactCoordinates, ClassLoaderModel> nestedClassLoaderModels = new HashMap<>();

  public ApplicationClassloaderModel(ClassLoaderModel classLoaderModel) {
    this.classLoaderModel = classLoaderModel;
  }

  public ClassLoaderModel getClassLoaderModel() {
    return classLoaderModel;
  }

  protected Map<ArtifactCoordinates, ClassLoaderModel> getNestedClassLoaderModels() {
    return nestedClassLoaderModels;
  }

  public void mergeDependencies(Collection<ClassLoaderModel> otherClassloaderModels) {
    doMergeDependencies(otherClassloaderModels, context -> {
    });
  }

  protected final void doMergeDependencies(Collection<ClassLoaderModel> otherClassloaderModels,
                                           Consumer<MergeDependenciesContext> action) {
    for (ClassLoaderModel otherClassLoaderModel : otherClassloaderModels) {
      Artifact artifact = getClassLoaderModel().getDependencies().stream()
          .filter(dependency -> dependency.getArtifactCoordinates().equals(otherClassLoaderModel.getArtifactCoordinates()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(format("Couldn't find an artifact coordinate for '%s' to merge dependencies as inline",
                                                              otherClassLoaderModel.getArtifactCoordinates())));

      if (nestedClassLoaderModels.putIfAbsent(otherClassLoaderModel.getArtifactCoordinates(), otherClassLoaderModel) != null) {
        throw new IllegalArgumentException(format("Duplicated definition of a nested class loader model for artifact coordinates '%s'",
                                                  otherClassLoaderModel.getArtifactCoordinates()));
      }
      action.accept(new MergeDependenciesContext(artifact, otherClassLoaderModel));
    }
  }

  public List<Artifact> getArtifacts() {
    return ImmutableList.<Artifact>builder()
        .addAll(getClassLoaderModel().getArtifacts())
        .addAll(getNestedClassLoaderModels().values().stream()
            .flatMap(nestedClassLoaderModels -> nestedClassLoaderModels.getArtifacts().stream()).collect(toList()))
        .build();
  }

  public void addDirectDependencies(List<Artifact> dependencies) {
    checkArgument(dependencies != null, "dependencies cannot be null");
    for (Artifact artifact : dependencies) {
      if (!classLoaderModel.getDependencies().contains(artifact)) {
        classLoaderModel.getDependencies().add(artifact);
      }
    }
  }

  public ClassLoaderModel getClassLoaderModel(Artifact artifact) {
    return nestedClassLoaderModels.get(artifact.getArtifactCoordinates());
  }

  class MergeDependenciesContext {

    private Artifact artifact;
    private ClassLoaderModel classLoaderModel;

    public MergeDependenciesContext(Artifact artifact, ClassLoaderModel classLoaderModel) {
      this.artifact = artifact;
      this.classLoaderModel = classLoaderModel;
    }

    public Artifact getArtifact() {
      return artifact;
    }

    public ClassLoaderModel getClassLoaderModel() {
      return classLoaderModel;
    }
  }
}
