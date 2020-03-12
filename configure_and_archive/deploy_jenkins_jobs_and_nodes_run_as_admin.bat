chcp 65001
SET EXTERNAL_CONF_DIR=%~dp0\external_configuration_files
SET JENKINS_PATH=C:\Program Files (x86)\jenkins

REM Alternative command : xcopy /Y /i /E /Q "%EXTERNAL_CONF_DIR%\jenkins\nodes" "%JENKINS_PATH%\nodes"
cp -a "%EXTERNAL_CONF_DIR%\jenkins\nodes\." "%JENKINS_PATH%\nodes"
cp -a "%EXTERNAL_CONF_DIR%\jenkins\jobs\." "%JENKINS_PATH%\jobs"
