/*
 * Copyright 2017 W.UP Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package digital.wup.android_maven_publish

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.plugins.JavaPlugin

final class AndroidLibrary implements SoftwareComponentInternal {

    private final UsageContext compileUsage;
    private final RuntimeUsage runtimeUsage;

    AndroidLibrary(Project project) {
        this.compileUsage = new CompileUsage(project)
        this.runtimeUsage = new RuntimeUsage(project)
    }


    @Override
    Set<UsageContext> getUsages() {
        return Collections.unmodifiableSet([compileUsage, runtimeUsage].toSet())
    }

    @Override
    String getName() {
        return 'android'
    }

    private final static class CompileUsage extends BaseUsage {

        private DependencySet dependencies

        CompileUsage(Project project) {
            super(project)
        }

        @Override
        Usage getUsage() {
            return Usage.FOR_COMPILE
        }

        @Override
        Set<ModuleDependency> getDependencies() {
            if (dependencies == null) {
                def android = project.extensions.getByType(LibraryExtension)
                String publishConfig = android.defaultPublishConfig
                dependencies = configurations.getByName(publishConfig + JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME.capitalize()).allDependencies
            }
            return dependencies.withType(ModuleDependency)
        }
    }

    private final static class RuntimeUsage extends BaseUsage {

        private DependencySet dependencies

        RuntimeUsage(Project project) {
            super(project)
        }

        @Override
        Usage getUsage() {
            return Usage.FOR_RUNTIME
        }

        @Override
        Set<ModuleDependency> getDependencies() {
            if (dependencies == null) {
                def android = project.extensions.getByType(LibraryExtension)
                String publishConfig = android.defaultPublishConfig
                dependencies = configurations.getByName(publishConfig + JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME.capitalize()).allDependencies
            }
            return dependencies.withType(ModuleDependency)
        }
    }

    private static abstract class BaseUsage implements UsageContext {
        protected final Project project
        protected final ConfigurationContainer configurations

        BaseUsage(Project project) {
            this.project = project
            this.configurations = project.configurations
        }

        @Override
        Set<PublishArtifact> getArtifacts() {
            return Collections.unmodifiableSet(configurations.getByName(Dependency.ARCHIVES_CONFIGURATION).allArtifacts.toSet())
        }
    }
}
