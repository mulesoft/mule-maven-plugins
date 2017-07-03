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

import org.apache.maven.MavenExecutionException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.repository.AuthenticationContext;
import org.eclipse.aether.repository.RemoteRepository;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.model.Authentication;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.internal.AetherMavenClientProvider;
import org.mule.maven.client.internal.DefaultLocalRepositorySupplierFactory;
import org.mule.maven.client.internal.DefaultSettingsSupplierFactory;
import org.mule.maven.client.internal.MavenEnvironmentVariables;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


public class MuleMavenPluginClientProvider {

  private List<RemoteRepository> remoteRepositories;

  public MuleMavenPluginClientProvider(List<RemoteRepository> remoteRepositories) {
    this.remoteRepositories = remoteRepositories;
  }

  protected MavenClient buildMavenClient() {
    MavenConfiguration mavenConfiguration = buildMavenConfiguration();
    AetherMavenClientProvider provider = new AetherMavenClientProvider();
    return provider.createMavenClient(mavenConfiguration);
  }

  public MavenConfiguration buildMavenConfiguration() {
    MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder = new MavenConfiguration.MavenConfigurationBuilder();

    DefaultSettingsSupplierFactory settingsSupplierFactory = new DefaultSettingsSupplierFactory(new MavenEnvironmentVariables());
    Optional<File> globalSettings = settingsSupplierFactory.environmentGlobalSettingsSupplier();
    Optional<File> userSettings = settingsSupplierFactory.environmentUserSettingsSupplier();

    globalSettings.ifPresent(mavenConfigurationBuilder::withGlobalSettingsLocation);
    userSettings.ifPresent(mavenConfigurationBuilder::withUserSettingsLocation);

    DefaultLocalRepositorySupplierFactory localRepositorySupplierFactory = new DefaultLocalRepositorySupplierFactory();
    Supplier<File> localMavenRepository = localRepositorySupplierFactory.environmentMavenRepositorySupplier();

    this.remoteRepositories.stream().map(this::toRemoteRepo).forEach(mavenConfigurationBuilder::withRemoteRepository);

    return mavenConfigurationBuilder
        .withLocalMavenRepositoryLocation(localMavenRepository.get())
        .build();
  }

  private org.mule.maven.client.api.model.RemoteRepository toRemoteRepo(RemoteRepository remoteRepository) {
    String id = remoteRepository.getId();
    Optional<Authentication> authentication = getAuthentication(remoteRepository);
    URL url = null;
    try {
      url = getURL(remoteRepository);
    } catch (MavenExecutionException e) {
      e.printStackTrace();
    }
    org.mule.maven.client.api.model.RemoteRepository.RemoteRepositoryBuilder builder =
        new org.mule.maven.client.api.model.RemoteRepository.RemoteRepositoryBuilder();
    authentication.ifPresent(builder::withAuthentication);
    return builder
        .withId(id)
        .withUrl(url)
        .build();
  }

  private URL getURL(RemoteRepository remoteRepository) throws MavenExecutionException {
    try {
      return new URL(remoteRepository.getUrl());
    } catch (MalformedURLException e) {
      throw new MavenExecutionException(e.getMessage(), e.getCause());
    }
  }

  private Optional<Authentication> getAuthentication(RemoteRepository remoteRepository) {
    AuthenticationContext authenticationContext =
        AuthenticationContext.forRepository(new DefaultRepositorySystemSession(), remoteRepository);

    if (authenticationContext == null) {
      return Optional.empty();
    }

    String password = new String(authenticationContext.get(AuthenticationContext.PASSWORD, char[].class));
    String username = new String(authenticationContext.get(AuthenticationContext.USERNAME, char[].class));

    Authentication.AuthenticationBuilder authenticationBuilder = new Authentication.AuthenticationBuilder();
    AuthenticationContext.close(authenticationContext);

    return Optional.of(authenticationBuilder.withPassword(password).withUsername(username).build());
  }
}
