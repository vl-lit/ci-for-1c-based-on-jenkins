
def getJenkinsMaster() {
    // env.BUILD_URL contains address which is specified in Jenkins global settings:
    // Jenkins Location -> Jenkins URL
    return env.BUILD_URL.split('/')[2].split(':')[0]
}


env.VERBOSE = "true"
env.EMAIL_ADDRESS_FOR_NOTIFICATIONS = "my-email@gmail.com"

if (isUnix()) {
    env.INSTALLATION_DIR_1C = "/opt/1C/v8.3/x86_64"
    env.THICK_CLIENT_1C = env.INSTALLATION_DIR_1C + "/1cv8"
    env.THICK_CLIENT_1C_FOR_STORAGE = env.THICK_CLIENT_1C
    // env.ONE_SCRIPT_PATH="/usr/bin/oscript"
} else {
    env.PLATFORM_1C_VERSION = "8.3.14.1779"
    env.PLATFORM_1C_VERSION_FOR_STORAGE = "8.3.14.1779"
    env.INSTALLATION_DIR_1C = "C:/Program Files/1cv8"
    // env.ONE_SCRIPT_PATH="C:/Program Files (x86)/OneScript/oscript.exe"
    env.THICK_CLIENT_1C = env.INSTALLATION_DIR_1C + "/" + env.PLATFORM_1C_VERSION + "/bin/1cv8.exe"
    env.THICK_CLIENT_1C_FOR_STORAGE = env.INSTALLATION_DIR_1C + "/" + env.PLATFORM_1C_VERSION_FOR_STORAGE + "/bin/1cv8.exe"
}

// Здесь можно написать например  if(env.HOSTNAME == "node1") { env.SQL_SERVER = "node2" }
env.SQL_SERVER = "localhost"
env.SQL_USER = "postgres"
env.SQL_PASSWORD = "vagrant"

env.CLUSTER_1C_HOST = "localhost"
env.CLUSTER_1C_MANAGER_PORT = "1541"
env.CLUSTER_1C_AGENT_PORT = "1540"
env.RAS_HOST = "localhost"
env.RAS_PORT = "1545"

env.INIT_TEMPLATE_DATABASE_WITH_EPF_INSTEAD_OF_EXTENTION = "true"
env.INIT_TEST_DATABASE_WITH_EPF_INSTEAD_OF_EXTENTION = "false"

env.TEMPLATE_FILE_FOR_EMPTY_DT = "/home/vagrant/shared_ci/cf_template/1cv8.cf"

env.DIRECTORY_TO_DUMP_EMPTY_DT = "/home/vagrant/shared_ci/database_images"
env.FILE_NAME_POSTFIX_FOR_EMPTY_DT = ""

env.NAME_OF_1C_ADMIN_USER = "_autotest_admin"
env.NAME_OF_1C_REGULAR_USER = "_autotest_user"
env.PASSWORD_FOR_1C_USERS = "123"

env.TEST_EXTENSION_NAME = "testing"
env.VANESSA_EPF = "/home/vagrant/shared_ci/vanessa-automation/vanessa-automation.epf"

// На разных окружениях может быть разный путь к хранилищу, 
// Окружение можно определять по имени мастер-узла Jenkins, если оно разное на разных хостовых серверах
if (getJenkinsMaster().toLowerCase().contains("host")) {
    env.CONFIGURATION_STORAGE_ADDRESS = "tcp://host.local:1642/storate_test"
}
else {
    env.CONFIGURATION_STORAGE_ADDRESS = "tcp://some-other-host:1742/storate_test"
}

env.CONFIGURATION_STORAGE_USER = "ci_automation"
env.CONFIGURATION_STORAGE_PASSWORD = "ci_automation"
env.FIRST_CONFIGURATION_STORAGE_VERSION_TO_DUMP = "4"
env.DIRECTORY_WITH_STORAGE_VERSIONS = "/home/vagrant/shared_ci/storage_versions"

env.GET_STORAGE_VERSIONS_FROM_CF_FILES = "true"
env.MAXIMUM_NUMBER_OF_CF_FILES_TO_STORE_IN_STORAGE_DUMP_DIRECTORY = "3"
env.ALWAYS_DELETE_STORAGE_VERSION_CF_FILE_AS_POST_ACTION = "true"

env.MAIN_TESTING_JOB_NAME = "main_build"
env.PERSISTENT_ALLURE_REPORT_DIRECTORY = "/home/vagrant/shared_ci/allure/ci_for_1c"

env.TIMEOUT_FOR_CREATE_EMPTY_DT_STAGES = "60"
env.TIMEOUT_FOR_STARTING_BUILD_JOB = "120"
env.TIMEOUT_FOR_DETECTING_VERSION_TO_BUILD = "5"
env.TIMEOUT_FOR_CHECKING_CHANGES_IN_STORAGE_IN_MINUTES = "60"
env.TIMEOUT_FOR_DUMPING_CF_FILE_FROM_STORAGE_IN_MINUTES = "120"
env.TIMEOUT_FOR_LOADING_DT_FILE = "30"
env.TIMEOUT_FOR_LOADING_STORAGE_VERSION = "60"
env.TIMEOUT_FOR_LOADING_TEST_EXTENSION = "30"
env.TIMEOUT_FOR_DATABASE_INITIALIZATION = "60"
env.TIMEOUT_FOR_PREPARATORY_SCENARIOS = "60"
env.TIMEOUT_FOR_MAIN_SCENARIOS = "180"
