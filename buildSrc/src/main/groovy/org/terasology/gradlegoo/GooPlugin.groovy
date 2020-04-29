package org.terasology.gradlegoo

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.ResolutionStrategy
import org.gradle.api.artifacts.component.ModuleComponentIdentifier


@SuppressWarnings("unused")
class GradleGooPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.extensions.create("gradlegoo", GradleGooExtension)
        target.dependencies {
            components {
                withModule("org.spf4j:spf4j-slf4j-test", Slf4jImplementationRule)
            }
        }
    }
}


class Slf4jImplementationRule implements ComponentMetadataRule {
    void execute(ComponentMetadataContext context) {
        context.details.allVariants {
            withCapabilities {
                addCapability "logging", "slf4j-impl-capability", "0"
            }
        }
    }
}


class GradleGooExtension {
    Class<Slf4jImplementationRule> Slf4jImplementationRule = Slf4jImplementationRule

    /**
     * This is just the code from here:
     * https://docs.gradle.org/current/userguide/dependency_capability_conflict.html#sub:selecting-between-candidates
     *
     * They knew conflicts would happen, that's their motivation for the feature. They even use this *exact thing* in
     * their examples, slf4j bindings. And clearly, once you have conflicts, you'll want a way to specify their
     * resolution. The manual talks about it at great length.
     *
     * So why didn't they make the common-cases a one-liner instead of ten lines to copy and paste?
     *
     * @param group the capability's group name, e.g. `logging`
     * @param capability the capability name, e.g. `slf4j-impl-capability`
     * @param preferredModule the name of the module you want to win, e.g. `logback-classic`
     * @param reason shown in debug messages about conflict resolution
     */
    static Action<ResolutionStrategy> prefers(String group, String capability, String preferredModule, String reason) {
        return { ResolutionStrategy strategy -> prefers(strategy, group, capability, preferredModule, reason) };
    }

    static void prefers(ResolutionStrategy strategy, String group, String capability, String preferredModule,
                        String reason) {
        strategy.capabilitiesResolution.withCapability(group, capability) {
            def toBeSelected = candidates.find {
                it.id instanceof ModuleComponentIdentifier && it.id.module == preferredModule
            }
            if (toBeSelected != null) {
                select(toBeSelected)
            }
            because reason
        }
    }
}
