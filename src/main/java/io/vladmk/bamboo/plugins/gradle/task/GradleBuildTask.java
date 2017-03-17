package io.vladmk.bamboo.plugins.gradle.task;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.interceptors.ErrorMemorisingInterceptor;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.ExternalProcessBuilder;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.utils.SystemProperty;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.utils.process.ExternalProcess;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class GradleBuildTask implements TaskType
{
  @SuppressWarnings( "unused" )
  private static final Logger log = Logger.getLogger( GradleBuildTask.class );
  // ------------------------------------------------------------------------------------------------------- Constants
  private static final String BUILD_SUCCESSFUL_MARKER = SystemProperty.BUILD_SUCCESSFUL_MARKER.getValue( "[success]" );
  private static final String BUILD_FAILED_MARKER = SystemProperty.BUILD_FAILED_MARKER.getValue( "[error]" );
  private static final boolean SEARCH_BUILD_SUCCESS_FAIL_MESSAGE_EVERYWHERE = SystemProperty.SEARCH_BUILD_SUCCESS_FAIL_MESSAGE_EVERYWHERE.getValue( false );
  private static final int LINES_TO_PARSE_FOR_ERRORS = 200;
  private static final int FIND_SUCCESS_MESSAGE_IN_LAST = SystemProperty.FIND_SUCCESS_MESSAGE_IN_LAST.getValue( 250 );
  private static final int FIND_FAILURE_MESSAGE_IN_LAST = FIND_SUCCESS_MESSAGE_IN_LAST;
  // ------------------------------------------------------------------------------------------------- Type Properties
  // ---------------------------------------------------------------------------------------------------- Dependencies
  private final CapabilityContext capabilityContext;
  private final EnvironmentVariableAccessor environmentVariableAccessor;
  private final ProcessService processService;
  private final TestCollationService testCollationService;
  // ---------------------------------------------------------------------------------------------------- Constructors

  public GradleBuildTask(final CapabilityContext capabilityContext,
                         final EnvironmentVariableAccessor environmentVariableAccessor,
                         final ProcessService processService,
                         final TestCollationService testCollationService )
  {
    this.capabilityContext = capabilityContext;
    this.environmentVariableAccessor = environmentVariableAccessor;
    this.processService = processService;
    this.testCollationService = testCollationService;
  }

  // ----------------------------------------------------------------------------------------------- Interface Methods
  // -------------------------------------------------------------------------------------------------- Action Methods
  // -------------------------------------------------------------------------------------------------- Public Methods

  @NotNull
  @Override
  public TaskResult execute( @NotNull final TaskContext taskContext ) throws TaskException
  {
    final BuildLogger buildLogger = taskContext.getBuildLogger();
    final CurrentBuildResult currentBuildResult = taskContext.getBuildContext().getBuildResult();

    GradleConfig config = new GradleConfig( taskContext, capabilityContext, environmentVariableAccessor );

    ErrorMemorisingInterceptor errorLines = ErrorMemorisingInterceptor.newInterceptor();

    buildLogger.getInterceptorStack().add( errorLines );

    try
    {
      final ExternalProcess externalProcess = processService.executeExternalProcess(
        taskContext,
        new ExternalProcessBuilder()
          .workingDirectory( config.getWorkingDirectory() )
          .env( config.getExtraEnvironment() )
          .command( config.getCommandline() )
      );

      if ( externalProcess.getHandler().isComplete() )
      {
        final TaskResultBuilder taskResultBuilder =
          TaskResultBuilder
            .newBuilder( taskContext )
            .checkReturnCode( externalProcess );

        if ( config.isHasTests() && config.getTestResultsFilePattern() != null )
        {
          testCollationService.collateTestResults( taskContext, config.getTestResultsFilePattern() );
          taskResultBuilder.checkTestFailures();
        }

        return taskResultBuilder.build();
      }

      throw new TaskException( "Failed to execute command, external process not completed?" );
    }
    catch ( Throwable e )
    {
      throw new TaskException( "Failed to execute task", e );
    }
    finally
    {
      currentBuildResult.addBuildErrors( errorLines.getErrorStringList() );
    }
  }

  // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
