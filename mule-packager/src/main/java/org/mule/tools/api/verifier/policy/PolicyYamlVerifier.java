/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.verifier.policy;

import static java.lang.String.format;

import static com.google.common.base.Preconditions.checkArgument;

import org.mule.tools.api.exception.ValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;

public class PolicyYamlVerifier {

  private final String yamlFileName;
  private final String path;

  public PolicyYamlVerifier(String path, String yamlFileName) {
    this.path = path;
    this.yamlFileName = yamlFileName;
  }

  public void validate() throws ValidationException {
    try {
      Representer representer = new Representer(new DumperOptions());
      representer.getPropertyUtils().setSkipMissingProperties(true);

      Yaml yamlParser = new Yaml(new Constructor(PolicyYaml.class, new LoaderOptions()), representer);
      File yamlFile = new File(path, yamlFileName);
      checkNotNullFields(yamlParser.load(new FileInputStream(yamlFile)));

    } catch (Exception e) {
      throw new ValidationException(format("Error validating '%s'. %s", yamlFileName, e.getMessage()));
    }
  }

  private void checkNotNullFields(PolicyYaml policy) {
    checkArgument(policy.id != null, "Missing required creator property 'id'");
    checkArgument(policy.name != null, "Missing required creator property 'name'");
    checkArgument(policy.description != null, "Missing required creator property 'description'");
    checkArgument(policy.category != null, "Missing required creator property 'category'");
    checkArgument(policy.type != null, "Missing required creator property 'type'");
    checkArgument(policy.resourceLevelSupported != null, "Missing required creator property 'resourceLevelSupported'");
    checkArgument(policy.standalone != null, "Missing required creator property 'standalone'");
    checkArgument(policy.requiredCharacteristics != null, "Missing required creator property 'requiredCharacteristics'");
    checkArgument(policy.providedCharacteristics != null, "Missing required creator property 'providedCharacteristics'");
    checkArgument(policy.configuration != null, "Missing required creator property 'configuration'");
    validateConfigurationProperties(policy.configuration);
  }

  private void validateConfigurationProperties(List<ConfigurationProperty> configuration) {
    configuration.forEach(p -> {
      checkArgument(p.propertyName != null, "Missing required creator property 'propertyName'");
      checkArgument(p.type != null, "Missing required creator property 'type'");
    });
  }
}
