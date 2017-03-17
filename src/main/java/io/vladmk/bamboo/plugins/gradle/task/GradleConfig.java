package io.vladmk.bamboo.plugins.gradle.task;

import com.atlassian.bamboo.process.CommandlineStringUtils;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.ExecutablePathUtils;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradleConfig
{
  @SuppressWarnings( "UnusedDeclaration" )
  private static final Logger log = Logger.getLogger( GradleConfig.class );
  // ------------------------------------------------------------------------------------------------------- Constants
  public static final String CFG_BUILD_FILE = "buildFile";
  public static final String CFG_BUILDER_LABEL = TaskConfigConstants.CFG_BUILDER_LABEL;
  protected static final String CFG_ENVIRONMENT_VARIABLES = TaskConfigConstants.CFG_ENVIRONMENT_VARIABLES;
  protected static final String CFG_HAS_TESTS = TaskConfigConstants.CFG_HAS_TESTS;
  public static final String CFG_JDK_LABEL = TaskConfigConstants.CFG_JDK_LABEL;
  public static final String CFG_TARGETS = "target";
  protected static final String CFG_TEST_RESULTS_FILE_PATTERN = TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN;


  public static final String GRADLE_CAPABILITY_PREFIX = CapabilityDefaultsHelper.CAPABILITY_BUILDER_PREFIX + ".gradle";
  protected static final String GRADLE_EXECUTABLE_NAME = "gradle";
  // ------------------------------------------------------------------------------------------------- Type Properties

  protected final String builderLabel;
  protected final String builderPath;
  protected final String buildFile;
  protected final String environmentVariables;
  protected final boolean hasTests;
  protected final String jdkLabel;
  protected final List<String> targets;
  protected final String testResultsFilePattern;
  protected final File workingDirectory;

  protected final Map<String, String> extraEnvironment = new HashMap<>();


  // ---------------------------------------------------------------------------------------------------- Dependencies
  // ---------------------------------------------------------------------------------------------------- Constructors

  public GradleConfig(@NotNull TaskContext taskContext, @NotNull CapabilityContext capabilityContext, @NotNull EnvironmentVariableAccessor environmentVariableAccessor )
  {
    buildFile = taskContext.getConfigurationMap().get( CFG_BUILD_FILE );

    targets = CommandlineStringUtils.tokeniseCommandline( StringUtils.replaceChars( taskContext.getConfigurationMap().get( CFG_TARGETS ), "\r\n", "  " ) );

    builderLabel = Preconditions.checkNotNull( taskContext.getConfigurationMap().get( CFG_BUILDER_LABEL ), "Builder label is not defined" );
    builderPath = Preconditions.checkNotNull( capabilityContext.getCapabilityValue( GRADLE_CAPABILITY_PREFIX + "." + builderLabel ), "Builder path is not defined" );

    environmentVariables = taskContext.getConfigurationMap().get( CFG_ENVIRONMENT_VARIABLES );
    hasTests = taskContext.getConfigurationMap().getAsBoolean( CFG_HAS_TESTS );
    jdkLabel = taskContext.getConfigurationMap().get( CFG_JDK_LABEL );
    testResultsFilePattern = taskContext.getConfigurationMap().get( CFG_TEST_RESULTS_FILE_PATTERN );
    workingDirectory = taskContext.getWorkingDirectory();

    // set extraEnvironment
    extraEnvironment.putAll( environmentVariableAccessor.splitEnvironmentAssignments( environmentVariables, false ) );
    extraEnvironment.put( "GRADLE_HOME", builderPath );
  }

  // ----------------------------------------------------------------------------------------------- Interface Methods
  // -------------------------------------------------------------------------------------------------- Helper Methods

  @NotNull
  protected String getGradleExecutableName()
  {
    return ExecutablePathUtils.makeBatchIfOnWindows(GRADLE_EXECUTABLE_NAME);
  }

  protected String getGradleExecutablePath(final String homePath )
  {
    String pathToExecutable = StringUtils.join( new String[]{ homePath, "bin", getGradleExecutableName() }, File.separator );

    if ( StringUtils.contains( pathToExecutable, " " ) )
    {
      try
      {
        File f = new File( pathToExecutable );
        pathToExecutable = f.getCanonicalPath();
      }
      catch ( IOException e )
      {
        log.warn( "IO Exception trying to get executable", e );
      }
    }
    return pathToExecutable;
  }
  // -------------------------------------------------------------------------------------------------- Public Methods

  public String getFirstTarget()
  {
    return targets.iterator().next();
  }

  public List<String> getCommandline()
  {
    final List<String> arguments = new ArrayList<>();

    arguments.add( getGradleExecutablePath( builderPath ) );

    if ( StringUtils.isNotEmpty( buildFile ) )
    {
      arguments.addAll( Arrays.asList( "-f", buildFile ) );
    }

    arguments.addAll( targets );

    return arguments;
  }
  // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

  public Map<String, String> getExtraEnvironment()
  {
    return extraEnvironment;
  }

  public File getWorkingDirectory()
  {
    return workingDirectory;
  }

  public boolean isHasTests()
  {
    return hasTests;
  }

  public String getTestResultsFilePattern()
  {
    return testResultsFilePattern;
  }
}
