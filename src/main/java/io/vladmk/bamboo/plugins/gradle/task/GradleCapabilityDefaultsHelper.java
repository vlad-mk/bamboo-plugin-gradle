package io.vladmk.bamboo.plugins.gradle.task;

import com.atlassian.bamboo.v2.build.agent.capability.AbstractHomeDirectoryCapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.ExecutablePathUtils;
import org.jetbrains.annotations.NotNull;

public class GradleCapabilityDefaultsHelper extends AbstractHomeDirectoryCapabilityDefaultsHelper
{
    private static final String GRADLE_EXE_NAME = "gradle";

    @NotNull
    @Override
    protected String getExecutableName()
    {
        return ExecutablePathUtils.makeBatchIfOnWindows(GRADLE_EXE_NAME);
    }

    @NotNull
    @Override
    protected String getCapabilityKey()
    {
        return GradleConfig.GRADLE_CAPABILITY_PREFIX + "." + "gradle";
    }
}
