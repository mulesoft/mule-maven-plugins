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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

public class AppClassLoaderModel extends ClassLoaderModel {

  private List<Plugin> pluginsWithAdditionalDependencies;

  public AppClassLoaderModel(String version, ArtifactCoordinates artifactCoordinates) {
    super(version, artifactCoordinates);
    pluginsWithAdditionalDependencies = new ArrayList<>();
  }

  @Override
  protected ClassLoaderModel doGetParameterizedUriModel() {
    AppClassLoaderModel copy = new AppClassLoaderModel(getVersion(), getArtifactCoordinates());
    List<Plugin> pluginsCopy =
        pluginsWithAdditionalDependencies.stream().map(Plugin::copyWithParameterizedDependenciesUri).collect(toList());
    copy.setPluginsWithAdditionalDependencies(pluginsCopy);
    return copy;
  }

  public List<Plugin> getPluginsWithAdditionalDependencies() {
    return pluginsWithAdditionalDependencies;
  }

  public void setPluginsWithAdditionalDependencies(List<Plugin> pluginsWithAdditionalDependencies) {
    this.pluginsWithAdditionalDependencies = pluginsWithAdditionalDependencies;
  }
}
