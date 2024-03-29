/*
 * Copyright 2015-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.jvm.groovy;

import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.jvm.java.DefaultJavaLibraryRules;
import com.facebook.buck.jvm.java.HasJavaAbi;
import com.facebook.buck.jvm.java.JavaBuckConfig;
import com.facebook.buck.jvm.java.JavaLibraryDescription;
import com.facebook.buck.jvm.java.JavacOptions;
import com.facebook.buck.jvm.java.JavacOptionsFactory;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.CellPathResolver;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.TargetGraph;
import com.facebook.buck.util.immutables.BuckStyleImmutable;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.immutables.value.Value;

public class GroovyLibraryDescription implements Description<GroovyLibraryDescriptionArg> {

  private final GroovyBuckConfig groovyBuckConfig;
  private final JavaBuckConfig javaBuckConfig;
  // For cross compilation
  private final JavacOptions defaultJavacOptions;

  public GroovyLibraryDescription(
      GroovyBuckConfig groovyBuckConfig,
      JavaBuckConfig javaBuckConfig,
      JavacOptions defaultJavacOptions) {
    this.groovyBuckConfig = groovyBuckConfig;
    this.javaBuckConfig = javaBuckConfig;
    this.defaultJavacOptions = defaultJavacOptions;
  }

  @Override
  public Class<GroovyLibraryDescriptionArg> getConstructorArgType() {
    return GroovyLibraryDescriptionArg.class;
  }

  @Override
  public BuildRule createBuildRule(
      TargetGraph targetGraph,
      BuildTarget buildTarget,
      ProjectFilesystem projectFilesystem,
      BuildRuleParams params,
      BuildRuleResolver resolver,
      CellPathResolver cellRoots,
      GroovyLibraryDescriptionArg args) {
    JavacOptions javacOptions =
        JavacOptionsFactory.create(
            defaultJavacOptions, buildTarget, projectFilesystem, resolver, args);
    DefaultJavaLibraryRules defaultJavaLibraryRules =
        new DefaultJavaLibraryRules.Builder(
                buildTarget,
                projectFilesystem,
                params,
                resolver,
                new GroovyConfiguredCompilerFactory(groovyBuckConfig),
                javaBuckConfig,
                args)
            .setJavacOptions(javacOptions)
            .build();

    return HasJavaAbi.isAbiTarget(buildTarget)
        ? defaultJavaLibraryRules.buildAbi()
        : resolver.addToIndex(defaultJavaLibraryRules.buildLibrary());
  }

  public interface CoreArg extends JavaLibraryDescription.CoreArg {
    // Groovyc may not play nice with this, so turning it off
    @Override
    default Optional<Boolean> getGenerateSourceOnlyAbi() {
      return Optional.of(false);
    }

    ImmutableList<String> getExtraGroovycArguments();
  }

  @BuckStyleImmutable
  @Value.Immutable
  interface AbstractGroovyLibraryDescriptionArg extends CoreArg {}
}
