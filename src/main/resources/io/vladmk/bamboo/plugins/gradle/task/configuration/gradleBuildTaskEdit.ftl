[#-- @ftlvariable name="uiConfigSupport" type="com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport" --]

[#assign addExecutableLink][@ui.displayAddExecutableInline executableKey='gradle'/][/#assign]
[@ww.select cssClass="builderSelectWidget" labelKey='executable.type' name='label'
    list=uiConfigSupport.getExecutableLabels('gradle')
    extraUtility=addExecutableLink /]

[#--[@ww.textfield labelKey='builder.gradle.buildFile' name='buildFile' cssClass="long-field" /]--]
[@ww.textfield labelKey='builder.gradle.target' name='target' required='true' cssClass="long-field" /]

[#assign addJdkLink][@ui.displayAddJdkInline /][/#assign]
[@ww.select labelKey='builder.common.jdk' name='buildJdk'
    cssClass="jdkSelectWidget"
    list=uiConfigSupport.jdkLabels required='true'
    extraUtility=addJdkLink]
[/@ww.select]

[@ww.textfield labelKey='builder.common.env' name='environmentVariables' cssClass="long-field" /]
[@ww.textfield labelKey='builder.common.sub' name='workingSubDirectory' helpUri='working-directory.ftl' cssClass="long-field" /]

[@ui.bambooSection titleKey='builder.common.tests.directory.description']
    [@ww.checkbox labelKey='builder.common.tests.exists' name='testChecked' toggle='true'/]

    [@ui.bambooSection dependsOn='testChecked' showOn='true']
        [@ww.textfield labelKey='builder.common.tests.directory.custom' name='testResultsDirectory' cssClass="long-field" /]
    [/@ui.bambooSection]
[/@ui.bambooSection]
