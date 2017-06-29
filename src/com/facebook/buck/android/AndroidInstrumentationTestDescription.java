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

package com.facebook.buck.android;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.jvm.java.JavaOptions;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildRules;
import com.facebook.buck.rules.CellPathResolver;
import com.facebook.buck.rules.CommonDescriptionArg;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.HasContacts;
import com.facebook.buck.rules.HasTestTimeout;
import com.facebook.buck.rules.TargetGraph;
import com.facebook.buck.util.HumanReadableException;
import com.facebook.buck.util.immutables.BuckStyleImmutable;
import java.util.Optional;
import org.immutables.value.Value;

public class AndroidInstrumentationTestDescription
    implements Description<AndroidInstrumentationTestDescriptionArg> {

  private final JavaOptions javaOptions;
  private final Optional<Long> defaultTestRuleTimeoutMs;

  public AndroidInstrumentationTestDescription(
      JavaOptions javaOptions, Optional<Long> defaultTestRuleTimeoutMs) {
    this.javaOptions = javaOptions;
    this.defaultTestRuleTimeoutMs = defaultTestRuleTimeoutMs;
  }

  @Override
  public Class<AndroidInstrumentationTestDescriptionArg> getConstructorArgType() {
    return AndroidInstrumentationTestDescriptionArg.class;
  }

  @Override
  public AndroidInstrumentationTest createBuildRule(
      TargetGraph targetGraph,
      ProjectFilesystem projectFilesystem,
      BuildRuleParams params,
      BuildRuleResolver resolver,
      CellPathResolver cellRoots,
      AndroidInstrumentationTestDescriptionArg args) {
    BuildRule apk = resolver.getRule(args.getApk());
    if (!(apk instanceof HasInstallableApk)) {
      throw new HumanReadableException(
          "In %s, instrumentation_apk='%s' must be an android_binary(), apk_genrule() or "
              + "android_instrumentation_apk(), but was %s().",
          params.getBuildTarget(), apk.getFullyQualifiedName(), apk.getType());
    }

    return new AndroidInstrumentationTest(
        params.copyAppendingExtraDeps(BuildRules.getExportedRules(params.getDeclaredDeps().get())),
        (HasInstallableApk) apk,
        args.getLabels(),
        args.getContacts(),
        javaOptions.getJavaRuntimeLauncher(),
        args.getTestRuleTimeoutMs().map(Optional::of).orElse(defaultTestRuleTimeoutMs));
  }

  @BuckStyleImmutable
  @Value.Immutable
  interface AbstractAndroidInstrumentationTestDescriptionArg
      extends CommonDescriptionArg, HasContacts, HasTestTimeout {
    BuildTarget getApk();
  }
}
