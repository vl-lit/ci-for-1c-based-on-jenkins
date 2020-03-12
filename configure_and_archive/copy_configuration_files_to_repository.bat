chcp 65001

REM Main parameters
SET APACHE_PATH=C:/Apache24
SET JENKINS_PATH=C:/Program Files (x86)/jenkins
SET JENKINS_JOBS_FOLDER_NAME=ci_for_1c

REM Dependent variables
SET EXTERNAL_CONF_DIR=%~dp0/external_configuration_files
SET JOBS_FOLDER_SOURCE_PATH=%JENKINS_PATH%/jobs/%JENKINS_JOBS_FOLDER_NAME%
SET JOBS_FOLDER_DESTINATION_PATH=%EXTERNAL_CONF_DIR%/jenkins/jobs/%JENKINS_JOBS_FOLDER_NAME%
SET NODES_PATH=%JENKINS_PATH%/nodes

REM Copying Jenkins config files
rm -rf "%EXTERNAL_CONF_DIR%/jenkins"
mkdir "%EXTERNAL_CONF_DIR%/jenkins/nodes"
mkdir "%JOBS_FOLDER_DESTINATION_PATH%"

cp "%JENKINS_PATH%/jenkins.xml" "%EXTERNAL_CONF_DIR%/jenkins/jenkins.xml"
REM cp "%JENKINS_PATH%/hudson.plugins.emailext.ExtendedEmailPublisher.xml" "%EXTERNAL_CONF_DIR%/jenkins/hudson.plugins.emailext.ExtendedEmailPublisher.xml"

REM The -a option is an improved recursive option, that preserve all file attributes, and also preserve symlinks.
REM The . at end of the source path is a specific cp syntax that allow to copy all files and folders, included hidden ones.
cp -a "%JENKINS_PATH%/nodes/." "%EXTERNAL_CONF_DIR%/jenkins/nodes"
cp "%JOBS_FOLDER_SOURCE_PATH%/config.xml" "%JOBS_FOLDER_DESTINATION_PATH%/config.xml"

for /F %%i IN ('ls -1 "%JOBS_FOLDER_SOURCE_PATH%/jobs"') DO (
    mkdir "%JOBS_FOLDER_DESTINATION_PATH%/jobs/%%i"
    cp "%JOBS_FOLDER_SOURCE_PATH%/jobs/%%i/config.xml" "%JOBS_FOLDER_DESTINATION_PATH%/jobs/%%i/config.xml"
)

REM Copying Apache config files
rm -rf "%EXTERNAL_CONF_DIR%/apache"
mkdir "%EXTERNAL_CONF_DIR%/apache"
cp "%APACHE_PATH%/conf/httpd.conf" "%EXTERNAL_CONF_DIR%/apache/httpd.conf"
