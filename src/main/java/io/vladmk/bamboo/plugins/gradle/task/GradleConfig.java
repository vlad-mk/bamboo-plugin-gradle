package io.vladmk.bamboo.plugins.gradle.task;

import com.atlassian.bamboo.configuration.Jdk;
import com.atlassian.bamboo.process.CommandlineStringUtils;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.v2.build.agent.capability.*;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.vladmk.bamboo.plugins.gradle.task.configuration.GradleBuildTaskConfigurator.DEFAULT_WRAPPER;

@Slf4j
public class GradleConfig
{
//  @SuppressWarnings( "UnusedDeclaration" )
//  private static final Logger log = Logger.getLogger( GradleConfig.class );
  // ------------------------------------------------------------------------------------------------------- Constants
  public static final String CFG_BUILD_FILE = "buildFile";
  public static final String CFG_BUILDER_LABEL = TaskConfigConstants.CFG_BUILDER_LABEL;
  protected static final String CFG_ENVIRONMENT_VARIABLES = TaskConfigConstants.CFG_ENVIRONMENT_VARIABLES;
  protected static final String CFG_HAS_TESTS = TaskConfigConstants.CFG_HAS_TESTS;
  public static final String CFG_JDK_LABEL = TaskConfigConstants.CFG_JDK_LABEL;
  public static final String CFG_TARGETS = "target";
  public static final String CFG_PROPERTIES = "properties";
  public static final String CFG_COMPILER_CHECKED = "compilerChecked";
  public static final String CFG_JDK_COMPILER_LABEL = "compileJdk";
  public static final String CFG_JAVAC_GRADLE_VARIABLE = "-PjavaCompiler=\"%s\"";
  public static final String CFG_BOOT_CP_GRADLE_VARIABLE = "-PbootClasspath=\"%s\"";
  protected static final String CFG_TEST_RESULTS_FILE_PATTERN = TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN;

  public static final String CFG_WRAPPER_CHECKED = "wrapperChecked";
  public static final String CFG_WRAPPER_EXECUTABLE = "gradlewExecutable";

  public static final String CFG_GRADLE_USER_HOME = "gradleUserHome";


  public static final String GRADLE_CAPABILITY_PREFIX = CapabilityDefaultsHelper.CAPABILITY_BUILDER_PREFIX + ".gradle";
  protected static final String GRADLE_EXECUTABLE_NAME = "gradle";
  // ------------------------------------------------------------------------------------------------- Type Properties

  protected final String builderLabel;
  protected final String builderPath;
  protected final String buildFile;
  protected final String environmentVariables;
  protected final boolean hasTests;
  protected final String jdkLabel;
  protected final String compileJdkLabel;
  protected final List<String> targets;
  protected final List<String> properies;
  protected final boolean compilerChecked;

  protected final String testResultsFilePattern;
  protected final File workingDirectory;

  protected final boolean wrapperChecked;
  protected final String wrapperExecutable;

  protected final String gradleUserHome;


  protected final Map<String, String> extraEnvironment = new HashMap<>();


  // ---------------------------------------------------------------------------------------------------- Dependencies
  // ---------------------------------------------------------------------------------------------------- Constructors

  public GradleConfig(@NotNull TaskContext taskContext, @NotNull CapabilityContext capabilityContext, @NotNull EnvironmentVariableAccessor environmentVariableAccessor )
  {
    buildFile = taskContext.getConfigurationMap().get( CFG_BUILD_FILE );
    gradleUserHome = taskContext.getConfigurationMap().get( CFG_GRADLE_USER_HOME );
    if(StringUtils.isNotBlank(gradleUserHome))
      extraEnvironment.put( "GRADLE_USER_HOME", gradleUserHome );

    targets = CommandlineStringUtils.tokeniseCommandline( StringUtils.replaceChars( taskContext.getConfigurationMap().get( CFG_TARGETS ), "\r\n", "  " ) );

    if(taskContext.getConfigurationMap().containsKey(CFG_PROPERTIES))
      properies = CommandlineStringUtils.tokeniseCommandline( StringUtils.replaceChars( taskContext.getConfigurationMap().get( CFG_PROPERTIES ), "\r\n", "  " ) );
    else
      properies = null;

    wrapperChecked = taskContext.getConfigurationMap().getAsBoolean(CFG_WRAPPER_CHECKED);
    if(!wrapperChecked) {
      builderLabel = Preconditions.checkNotNull(taskContext.getConfigurationMap().get(CFG_BUILDER_LABEL), "Builder label is not defined");
      builderPath = Preconditions.checkNotNull(capabilityContext.getCapabilityValue(GRADLE_CAPABILITY_PREFIX + "." + builderLabel), "Builder path is not defined");
      wrapperExecutable = taskContext.getConfigurationMap().get( CFG_WRAPPER_EXECUTABLE );
      extraEnvironment.put( "GRADLE_HOME", builderPath );
    } else {
      builderLabel = taskContext.getConfigurationMap().get(CFG_BUILDER_LABEL);
      builderPath = capabilityContext.getCapabilityValue(GRADLE_CAPABILITY_PREFIX + "." + builderLabel);
//      wrapperExecutable = Preconditions.checkNotNull(taskContext.getConfigurationMap().get( CFG_WRAPPER_EXECUTABLE ), "Builder wrapper is not defined");
      String wrapperExecutable = taskContext.getConfigurationMap().get( CFG_WRAPPER_EXECUTABLE );
      this.wrapperExecutable = StringUtils.isNotBlank(wrapperExecutable) ? wrapperExecutable : DEFAULT_WRAPPER ;
    }

    environmentVariables = taskContext.getConfigurationMap().get( CFG_ENVIRONMENT_VARIABLES );
    hasTests = taskContext.getConfigurationMap().getAsBoolean( CFG_HAS_TESTS );
    compilerChecked = taskContext.getConfigurationMap().getAsBoolean(CFG_COMPILER_CHECKED);
    jdkLabel = taskContext.getConfigurationMap().get( CFG_JDK_LABEL );
    compileJdkLabel = taskContext.getConfigurationMap().get( CFG_JDK_COMPILER_LABEL );
    testResultsFilePattern = taskContext.getConfigurationMap().get( CFG_TEST_RESULTS_FILE_PATTERN );
    workingDirectory = taskContext.getWorkingDirectory();

    // set extraEnvironment
    extraEnvironment.putAll( environmentVariableAccessor.splitEnvironmentAssignments( environmentVariables, false ) );
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


  public static String  getJdkPath(@Nullable String buildJdk, @NotNull ReadOnlyCapabilitySet capabilitySet) {
    final String jdkCapabilityKey =
    Jdk.CAPABILITY_JDK_PREFIX + "." + (StringUtils.isNotBlank(buildJdk) ? buildJdk : CFG_JDK_LABEL);

    final Capability capability = capabilitySet.getCapability(jdkCapabilityKey);
    if (capability != null) {
      return capability.getValue();
    } else {
      return null;
    }
  }

  public static String getWrapperPath(String currentDir, String wrapperExecutable) {
    return StringUtils.join( new String[]{ currentDir, wrapperExecutable }, File.separator );
  }


  public List<String> getCommandline(ReadOnlyCapabilitySet capabilitySet)
  {
    final List<String> arguments = new ArrayList<>();

    if(!wrapperChecked)
      arguments.add( getGradleExecutablePath( builderPath ) );
    else
      arguments.add( getWrapperPath(".", wrapperExecutable ));

    if ( StringUtils.isNotEmpty( buildFile ) )
    {
      arguments.addAll( Arrays.asList( "-b", buildFile ) );
    }

    arguments.addAll( targets );

    if(properies != null)
      arguments.addAll(properies);

    if(compilerChecked) {
      String jdkPath = getJdkPath(compileJdkLabel, capabilitySet);
      log.info("jdkPath {} for {}", jdkPath, compileJdkLabel);
      if(StringUtils.isNotBlank(jdkPath)) {
        //javacPath = new File(new File(javacPath, "bin"), "javac").getPath();
        Optional<File> javacOption = ExecutablePathUtils.getExistingExecutable(jdkPath, "javac");
        if(javacOption.isPresent()) {
          String javacPath = javacOption.get().getPath();
          log.info("found javac: {}", javacPath);
          arguments.add(String.format(CFG_JAVAC_GRADLE_VARIABLE, javacPath));
        } else
          log.error("javac path not found in: {}", compileJdkLabel);

        Path rtPath = Paths.get(jdkPath, "jre", "lib", "rt.jar");
//        String rtJar = StringUtils.join(new String[]{jdkPath, "jre", "lib", "rt.jar"}, File.separator);
        File rt = rtPath.toFile();
        if(rt.exists() && rt.isFile())
          arguments.add(String.format(CFG_BOOT_CP_GRADLE_VARIABLE, rtPath.toString()));
        else
          log.error("rt.jar not found in: {}", rtPath.toString());


      }
    }

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
