/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.project;

import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.Test;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory.buildLifecycleMappingMaven;

class DomainBundleLifecycleMappingTest {

  @Test
  void getPhases() {
    DomainBundleLifecycleMapping mapping = new DomainBundleLifecycleMapping();
    LifecycleMappingMavenVersionless lifecycleMapping = buildLifecycleMappingMaven(mapping);
    assertThat(mapping.getPhases(StringUtils.EMPTY).keySet())
        .as("Phases should be the same")
        .isEqualTo(mapping.getLifecyclePhases(lifecycleMapping).keySet());
  }
}
