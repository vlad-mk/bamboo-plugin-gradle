package io.vladmk.bamboo.plugins.gradle.task.configuration;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.BuildTaskRequirementSupport;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskTestResultsSupport;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.atlassian.struts.TextProvider;
import com.google.common.collect.Iterables;
import io.vladmk.bamboo.plugins.gradle.task.GradleConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GradleBuildTaskConfigurator extends AbstractTaskConfigurator implements BuildTaskRequirementSupport, TaskTestResultsSupport
{
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(GradleBuildTaskConfigurator.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // context keys
    private static final String CTX_UI_CONFIG_SUPPORT = "uiConfigSupport";

    private static final List<String> FIELDS_TO_COPY = Arrays.asList(
            GradleConfig.CFG_BUILD_FILE,
            GradleConfig.CFG_TARGETS
    );

    private static final String DEFAULT_TARGET = "clean test";
    private static final String DEFAULT_TEST_RESULTS_PATTERN =  "**/test-reports/*.xml";
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private TextProvider textProvider;
    private UIConfigSupport uiConfigSupport;
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    @NotNull
    public Set<Requirement> calculateRequirements(@NotNull final TaskDefinition taskDefinition, @NotNull final Job job)
    {
        final Set<Requirement> requirements = new HashSet<>();
        taskConfiguratorHelper.addJdkRequirement(requirements, taskDefinition, TaskConfigConstants.CFG_JDK_LABEL);
        taskConfiguratorHelper.addSystemRequirementFromConfiguration(requirements, taskDefinition,
                                                                     TaskConfigConstants.CFG_BUILDER_LABEL, GradleConfig.GRADLE_CAPABILITY_PREFIX );

        return requirements;
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(
                context, taskDefinition,
                Iterables.concat(TaskConfigConstants.DEFAULT_BUILDER_CONFIGURATION_KEYS, FIELDS_TO_COPY));
        context.put(TaskConfigConstants.CFG_HAS_TESTS_BOOLEAN, Boolean.valueOf(taskDefinition.getConfiguration().get(TaskConfigConstants.CFG_HAS_TESTS)));
        context.put(CTX_UI_CONFIG_SUPPORT, uiConfigSupport);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(
                context, taskDefinition,
                Iterables.concat(TaskConfigConstants.DEFAULT_BUILDER_CONFIGURATION_KEYS, FIELDS_TO_COPY));
        context.put(TaskConfigConstants.CFG_HAS_TESTS_BOOLEAN, Boolean.valueOf(taskDefinition.getConfiguration().get(TaskConfigConstants.CFG_HAS_TESTS)));
        context.put(CTX_UI_CONFIG_SUPPORT, uiConfigSupport);
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        context.put( GradleConfig.CFG_TARGETS, DEFAULT_TARGET);
        context.put(TaskConfigConstants.CFG_TEST_RESULTS_FILE_PATTERN, DEFAULT_TEST_RESULTS_PATTERN);
        context.put(TaskConfigConstants.CFG_HAS_TESTS_BOOLEAN, Boolean.TRUE);
        context.put(TaskConfigConstants.CFG_HAS_TESTS, Boolean.TRUE);
        context.put(CTX_UI_CONFIG_SUPPORT, uiConfigSupport);
        context.put(TaskConfigConstants.CFG_JDK_LABEL, uiConfigSupport.getDefaultJdkLabel());
    }

    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        taskConfiguratorHelper.validateBuilderLabel(params, errorCollection);
        taskConfiguratorHelper.validateJdk(params, errorCollection);
        taskConfiguratorHelper.validateTestResultsFilePattern(params, errorCollection);
        
        if (StringUtils.isEmpty(params.getString( GradleConfig.CFG_TARGETS)))
        {
            errorCollection.addError( GradleConfig.CFG_TARGETS, textProvider.getText("task.gradle.validate.goals.mandatory"));
        }
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(
                config, params,
                Iterables.concat(TaskConfigConstants.DEFAULT_BUILDER_CONFIGURATION_KEYS, FIELDS_TO_COPY));

        return config;
    }

    @Override
    public boolean taskProducesTestResults(@NotNull final TaskDefinition taskDefinition)
    {
        return Boolean.parseBoolean(taskDefinition.getConfiguration().get(TaskConfigConstants.CFG_HAS_TESTS));
    }
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public void setTextProvider(TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }

    public void setUiConfigSupport(UIConfigSupport uiConfigSupport)
    {
        this.uiConfigSupport = uiConfigSupport;
    }
}
