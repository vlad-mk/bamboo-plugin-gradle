package io.vladmk.bamboo.plugins.maven.decorator;

import io.vladmk.bamboo.plugins.gradle.decorator.IsolatedTmpDirCommandDecorator;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.plugins.TaskProcessCommandDecorator;
import com.atlassian.bamboo.utils.SystemProperty;
import com.atlassian.bamboo.v2.build.BuildContext;
import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IsolatedTmpDirCommandDecoratorTest
{
    private static final String PLAN_KEY = "FOO-BAR";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private TaskContext taskContext;
    private TaskProcessCommandDecorator decorator;

    @Before
    public void setUp() throws Exception
    {
        BuildContext mockBuildContext = mock(BuildContext.class);
        when(mockBuildContext.getPlanKey()).thenReturn(PLAN_KEY);

        when(taskContext.getBuildContext()).thenReturn(mockBuildContext);

        decorator = new IsolatedTmpDirCommandDecorator();
    }

    @After
    public void tearDown() throws Exception
    {
        SystemProperty.BUILD_PARENT_JAVA_IO_TMPDIR.setValue(System.getProperty("java.io.tmpdir"));
    }

    @Test
    public void testDecoratorWithNonDefaultJavaIoTmpdir() throws Exception
    {
        final String newJavaIoTmpdir = new File(SystemUtils.getJavaIoTmpDir(), "NON_DEFAULT").getAbsolutePath();
        SystemProperty.BUILD_PARENT_JAVA_IO_TMPDIR.setValue(newJavaIoTmpdir);

        final List<String> command = Arrays.asList("gradle", "clean", "test");
        final List<String> expectedCommand = Arrays.asList(
                "gradle",
                "-Djava.io.tmpdir=" + new File(newJavaIoTmpdir, PLAN_KEY).getAbsolutePath(),
                "clean", "test");

        final List<String> decoratedCommand = decorator.decorate(taskContext, command);
        assertEquals(expectedCommand, decoratedCommand);
    }

    @Test
    public void testDecoratorWithNonDefaultContainingSpaceJavaIoTmpdir() throws Exception
    {
        final String newJavaIoTmpdir = new File(SystemUtils.getJavaIoTmpDir(), "NON DEFAULT WITH SPACE").getAbsolutePath();
        SystemProperty.BUILD_PARENT_JAVA_IO_TMPDIR.setValue(newJavaIoTmpdir);

        final List<String> command = Arrays.asList("gradle", "clean", "test");
        final List<String> expectedCommand = Arrays.asList(
                "gradle",
                "-Djava.io.tmpdir=" + new File(newJavaIoTmpdir, PLAN_KEY).getAbsolutePath(),
                "clean", "test");

        final List<String> decoratedCommand = decorator.decorate(taskContext, command);
        assertEquals(expectedCommand, decoratedCommand);
    }
}
