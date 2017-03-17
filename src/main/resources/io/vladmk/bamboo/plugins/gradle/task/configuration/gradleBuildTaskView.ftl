[#-- @ftlvariable name="uiConfigSupport" type="com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport" --]

[@ww.label labelKey='executable.type' name='label' /]
[#--[@ww.label labelKey='builder.gradle.buildFile' name='buildFile' hideOnNull='true' /]--]
[@ww.label labelKey='builder.gradle.target' name='target' /]

[@ui.displayJdk jdkLabel=buildJdk isJdkValid=uiConfigSupport.isJdkLabelValid(buildJdk) /]
[@ww.label labelKey='builder.common.env' name='environmentVariables' hideOnNull='true' /]
[@ww.label labelKey='builder.common.sub' name='workingSubDirectory' hideOnNull='true' /]
[#if hasTests ]
    [@ww.label labelKey='builder.common.tests.directory' name='testResultsDirectory' hideOnNull='true' /]
[/#if]
