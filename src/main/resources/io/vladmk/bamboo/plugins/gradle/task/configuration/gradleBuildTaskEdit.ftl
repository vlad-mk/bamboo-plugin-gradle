[#-- @ftlvariable name="uiConfigSupport" type="com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport" --]

[@ww.textfield labelKey='builder.gradle.target' name='target' required='true' cssClass="long-field" /]

[#assign addExecutableLink][@ui.displayAddExecutableInline executableKey='gradle'/][/#assign]

[@ww.checkbox labelKey='builder.gradle.gradlew.checked' name='wrapperChecked' toggle='true'/]

[@ui.bambooSection dependsOn='wrapperChecked' showOn='false']
    [@ww.select cssClass="builderSelectWidget" labelKey='executable.type' name='label'
        list=uiConfigSupport.getExecutableLabels('gradle') required='true'
        extraUtility=addExecutableLink /]
[/@ui.bambooSection]

[@ui.bambooSection dependsOn='wrapperChecked' showOn='true']
    [@ww.textfield labelKey='builder.gradle.gradlew.executable' name='gradlewExecutable' required='false' cssClass="long-field" /]
[/@ui.bambooSection]

[@ww.textfield labelKey='bulder.gradle.user.home' name='gradleUserHome' cssClass="long-field" /]

[@ww.textfield labelKey='builder.gradle.buildFile' name='buildFile' cssClass="long-field" /]

[@ww.textarea labelKey='builder.gradle.properties' name='properties' rows='4' required='false' cssClass="long-field" /]

[#assign addJdkLink][@ui.displayAddJdkInline /][/#assign]
[@ww.select labelKey='builder.common.jdk' name='buildJdk'
    cssClass="jdkSelectWidget"
    list=uiConfigSupport.jdkLabels required='true'
    extraUtility=addJdkLink]
[/@ww.select]

[#--[@ui.bambooSection titleKey='builder.gradle.compileJDK.description']--]

[@ww.checkbox labelKey='builder.gradle.compileJDK.checked' name='compilerChecked' toggle='true'/]

[@ui.bambooSection dependsOn='compilerChecked' showOn='true']
    [#--[#assign addJdkLink2][@ui.displayAddJdkInline/][/#assign]--]
    [@ww.select labelKey='builder.gradle.compileJDK' name='compileJdk'
        cssClass="jdkSelectWidget"
        list=uiConfigSupport.jdkLabels required='true'
        helpDialogKey='builder.gradle.compilerhelp'
        extraUtility=addJdkLink]
    [/@ww.select]

[/@ui.bambooSection]

[#--[/@ui.bambooSection]--]


[@ww.textfield labelKey='builder.common.env' name='environmentVariables' cssClass="long-field" /]
[@ww.textfield labelKey='builder.common.sub' name='workingSubDirectory' cssClass="long-field" /]
[#--[@ww.textfield labelKey='builder.common.sub' name='workingSubDirectory' helpUri='working-directory.ftl' cssClass="long-field" /]--]

[@ui.bambooSection titleKey='builder.common.tests.directory.description']
    [@ww.checkbox labelKey='builder.common.tests.exists' name='testChecked' toggle='true'/]

    [@ui.bambooSection dependsOn='testChecked' showOn='true']
        [@ww.textfield labelKey='builder.common.tests.directory.custom' name='testResultsDirectory' cssClass="long-field" /]
    [/@ui.bambooSection]
[/@ui.bambooSection]
