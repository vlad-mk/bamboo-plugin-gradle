package io.vladmk.bamboo.plugins.gradle.decorator;

import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.plugins.TaskProcessCommandDecorator;
import com.atlassian.bamboo.utils.SystemProperty;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

@Slf4j
public class IsolatedTmpDirCommandDecorator implements TaskProcessCommandDecorator
{
//    @SuppressWarnings("UnusedDeclaration")
//    private static final Logger log = Logger.getLogger(IsolatedTmpDirCommandDecorator.class);

    @NotNull
    @Override
    public List<String> decorate(@NotNull TaskContext taskContext, @NotNull List<String> command)
    {
        final List<String> decoratedCommand = Lists.newArrayList(command);

        final File isolatedTmpDir = new File(SystemProperty.BUILD_PARENT_JAVA_IO_TMPDIR.getValue(), taskContext.getBuildContext().getPlanKey());
        if (isolatedTmpDir.exists() || isolatedTmpDir.mkdirs())
        {
            decoratedCommand.add(1, "-Djava.io.tmpdir=" + isolatedTmpDir.getAbsolutePath());
        }

        return decoratedCommand;
    }
}
