import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

def commonMethods


def clusterIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterName1C) {
    
    def command = "${env.INSTALLATION_DIR_1C}/rac  ${rasHostnameOrIP}:${rasPort} cluster list | grep -B1 '${clusterName1C}' | head -1 | tr -d ' ' | cut -d ':' -f 2"
    def clusterId = commonMethods.cmdReturnStdout(command)
    clusterId = clusterId.trim()

    if (env.VERBOSE == "true")
        echo clusterId

    return clusterId

}


def databaseIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterId, databaseName1C) {
    
    def command = "${env.INSTALLATION_DIR_1C}/rac ${rasHostnameOrIP}:${rasPort} infobase --cluster ${clusterId} summary list | grep -B1 '${databaseName1C}' | head -1 | tr -d ' ' | cut -d ':' -f 2"
    def databaseId = commonMethods.cmdReturnStdout(command)
    databaseId = databaseId.trim()

    if (env.VERBOSE == "true")
        echo databaseId

    return databaseId
}


def dropDatabaseViaRAS(rasHostnameOrIP, 
                       rasPort, 
                       clusterName, 
                       databaseName, 
                       dropSQLDatabase = true,
                       databaseUser = "", 
                       databasePassword = "",
                       raiseExceptionIfNotDeleted = true) {

    if (env.VERBOSE == "true") { 
        echo "Trying to drop database ${databaseName} via RAS"
    }

    def clusterId = clusterIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterName)
    def databaseId = databaseIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterId, databaseName)

    if (databaseId != "") {

        def command = "${env.INSTALLATION_DIR_1C}/rac ${rasHostnameOrIP}:${rasPort} infobase --cluster ${clusterId} drop --infobase=${databaseId}  --infobase-user=\"${databaseUser}\" --infobase-pwd=\"${databasePassword}\""
        
        if (dropSQLDatabase) {
            command += " --drop-database"
        }

        def statusCode = commonMethods.cmdReturnStatusCode(command)
        
        if (statusCode != 0 && raiseExceptionIfNotDeleted) {
            commonMethods.echoAndError("Database ${databaseName} was not deleted")
        }

        if (raiseExceptionIfNotDeleted) {     
            databaseId = databaseIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterId, databaseName)
            if (databaseId != "") {
                commonMethods.echoAndError("Database ${databaseName} was not deleted")
            }
        }

    }
}


def forbidScheduledJobsViaRas(rasHostnameOrIP, rasPort, clusterName, databaseName, databaseUser = "", databasePassword = "") {

    def clusterId = clusterIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterName)
    def databaseId = databaseIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterId, databaseName)    

    def command = "${env.INSTALLATION_DIR_1C}/rac ${rasHostnameOrIP}:${rasPort} infobase --cluster ${clusterId} update --infobase=${databaseId}  --infobase-user=\"${databaseUser}\" --infobase-pwd=\"${databasePassword}\" --scheduled-jobs-deny=on"
    commonMethods.cmd(command)
}


def permitScheduledJobsViaRas(rasHostnameOrIP, rasPort, clusterName, databaseName, databaseUser, databasePassword) {

    def clusterId = clusterIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterName)
    def databaseId = databaseIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterId, databaseName)    

    def command = "${env.INSTALLATION_DIR_1C}/rac ${rasHostnameOrIP}:${rasPort} infobase --cluster ${clusterId} update --infobase=${databaseId}  --infobase-user=\"${databaseUser}\" --infobase-pwd=\"${databasePassword}\" --scheduled-jobs-deny=off"
    commonMethods.cmd(command)
}


def deleteConnectionsViaRas(rasHostnameOrIP, rasPort, clusterName, databaseName, kill1CProcesses = true) {

    if (env.VERBOSE == "true") { 
        echo "Trying to delete connections to database ${databaseName} via RAS"
    }

    def clusterId = clusterIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterName)
    def databaseId = databaseIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterId, databaseName)    

    if (databaseId != "") {
        def command = "${env.INSTALLATION_DIR_1C}/rac ${rasHostnameOrIP}:${rasPort} session --cluster ${clusterId} list --infobase=${databaseId}  | grep 'session ' | tr -d ' ' | cut -d ':' -f 2 | while read line ; do  ${env.INSTALLATION_DIR_1C}/rac session --cluster ${clusterId} terminate --session=\$line; done"
        commonMethods.cmd(command)
    }

    if (kill1CProcesses) {
        commonMethods.killProcessesByRegExp("${env.INSTALLATION_DIR_1C}/1cv8")
    }   

    sleep(5)

}


def dropSQLand1CDatabaseIfExists(serverSQL,
                                 userSQL,
                                 passwordSQL,
                                 rasHostnameOrIP,
                                 rasPort,
                                 clusterName1C,
                                 databaseNameSQL,
                                 databaseName1C,
                                 kill1CProcesses = true) {
    
    if (env.VERBOSE == "true") { 
        echo "Trying to drop database ${databaseNameSQL} on SQL server and ${databaseName1C} on 1C server"
    }

    deleteConnectionsViaRas(rasHostnameOrIP, rasPort, clusterName1C, databaseName1C, kill1CProcesses)

    def command = """export PGPASSWORD=${passwordSQL}
    psql -h ${serverSQL} -U ${userSQL} -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${databaseNameSQL}'"
    psql -h ${serverSQL} -U ${userSQL} -c "DROP DATABASE IF EXISTS ${databaseNameSQL}"
    """    
    commonMethods.cmd(command)

    dropDatabaseViaRAS(rasHostnameOrIP, rasPort, clusterName1C, databaseName1C, false)

}


def createDatabase(serverSQL,
                   userSQL,
                   passwordSQL,
                   rasHostnameOrIP,
                   rasPort,
                   clusterName1C,
                   databaseNameSQL,
                   databaseName1C) {

    def clusterId = clusterIdentifierFromRAS(rasHostnameOrIP, rasPort, clusterName1C)
    def command = "${env.INSTALLATION_DIR_1C}/rac ${rasHostnameOrIP}:${rasPort} infobase --cluster ${clusterId} create --create-database --name=${databaseName1C} --dbms=PostgreSQL --db-server=${serverSQL} --db-name=${databaseNameSQL} --locale=ru --db-user=${userSQL} --db-pwd=${passwordSQL} --license-distribution=allow"
    commonMethods.cmd(command)

}


def createFileDatabase(pathTo1CThickClient, databaseDirectory, deleteIfExits) {
    
    def commonErrorMsg = "Exception from DBManage.createFileDatabase:"

    if ( fileExists(databaseDirectory) ) {

        if (deleteIfExits) {

            commonMethods.cmdReturnStatusCode("rm -rf ${databaseDirectory}")

            if ( fileExists(databaseDirectory) ) {
                commonMethods.echoAndError("${commonErrorMsg} failed to remove directory ${databaseDirectory}")
            }

        }
        else {
            commonMethods.echoAndError("${commonErrorMsg} directory ${databaseDirectory} already exists")
        }
    }    

    def command = "\"${pathTo1CThickClient}\" CREATEINFOBASE File=\"${databaseDirectory}\""  
    commonMethods.cmd(command)

    if ( !fileExists("${databaseDirectory}/1Cv8.1CD") ) {
        commonMethods.echoAndError("${commonErrorMsg} Failed to create new file database in directory ${databaseDirectory}")
    }

}


def storageConnectionString(pathTo1CThickClient, 
                            databaseConnectionString, 
                            storageAddress,
                            storageUser,
                            storagePassword) {

    def userAndPassword =  "/ConfigurationRepositoryN \"${storageUser}\" /ConfigurationRepositoryP \"${storagePassword}\""
    def storageAuthParams = "/ConfigurationRepositoryF \"${storageAddress}\" ${userAndPassword}"    
    def resultString = "\"${pathTo1CThickClient}\" DESIGNER ${databaseConnectionString} ${storageAuthParams}"

    return resultString

}


def getLastStorageVersion(storageConnectionString,
                          directoryToDumpHistoryReport,
                          intervalFirstVersion,
                          intervalLastVersion = null) {

    def versionNumber = 0

    def outFileName = "${directoryToDumpHistoryReport}/storage_history_out_log.txt"
    def reportFileName = "${directoryToDumpHistoryReport}/storage_history.txt"
    def intervalParams = "-NBegin ${intervalFirstVersion}"

    commonMethods.deleteFileIfExists(outFileName)
    commonMethods.deleteFileIfExists(reportFileName)

    if (intervalLastVersion != null) {
        intervalParams += " -NEnd ${intervalLastVersion}"
    }

    def getReportParams = "/ConfigurationRepositoryReport \"${reportFileName}\" ${intervalParams} /Out \"${outFileName}\""

    def command = "${storageConnectionString} ${getReportParams}"
    def statusCode = commonMethods.cmdReturnStatusCode(command)

    commonMethods.cmdReturnStatusCode("tail -n 5 ${outFileName}")

    def outputLine = commonMethods.getLastLineOfTextFileLowerCase(outFileName)
    def success = statusCode == 0 && outputLine.isEmpty() 
    commonMethods.assertWithEcho(success, 
            "Could not get configuration repository history report", 
            "Configuration repository history report was successfully dumped")
    
    def reportText = readFile(reportFileName)
    def versionBlockMarker = '{"#","Версия:"}'
    def positionOfVersionBlockMarker = reportText.lastIndexOf(versionBlockMarker)

    if(positionOfVersionBlockMarker != -1) {
        
        def lengthOfVersionBlockMarker = versionBlockMarker.length()
        def firstPositionAfterVersionBlockMarker = positionOfVersionBlockMarker + lengthOfVersionBlockMarker
        def versionNumberMarker = '{"#","';
        def lengthOfVersionNumberMarker = versionNumberMarker.length()
        def firstPositionOfVersion = reportText.indexOf(versionNumberMarker, firstPositionAfterVersionBlockMarker) + lengthOfVersionNumberMarker
        def lastPositionOfVersion =  reportText.indexOf('"}', firstPositionOfVersion + 1)
        def versionText = reportText.substring(firstPositionOfVersion, lastPositionOfVersion)

        if (env.VERBOSE == "true") {
            echo "versionText = ${versionText}"
        }

        versionText = versionText.replace(",", "")
        versionText = versionText.replace(".", "")
        versionText = versionText.replace(" ", "") // пробел
        versionText = versionText.replace(" ", "") // неразрывный пробел

        versionNumber = versionText.toInteger()
    }

    if (env.VERBOSE == "true") {
        echo "versionNumber = ${versionNumber}"
    }

    return versionNumber

}


def publishDatabaseOnApache24(databaseName, user = null, password = null) {

    def connectionString = "Srvr=${env.CLUSTER_1C_HOST}:${env.CLUSTER_1C_MANAGER_PORT};Ref=${databaseName};"

    if (user != null) {
        command += "usr=\"${user}\";"
    }

    if (password != null) {
        command += "pwd=\"${password}\";"
    }

    commonMethods.cmdReturnStatusCode("rm -rf /var/www/${databaseName}/*")
    def command = "sudo ${env.INSTALLATION_DIR_1C}/webinst -apache24 -wsdir ${databaseName} -dir \"/var/www/${databaseName}\" -connStr \"${connectionString}\""
    commonMethods.cmd(command)

}


def placeDefaultVrdToPublishDirectory(databaseName, pathToSourceDefaultVrdFile) {

    def destinationFile = "/var/www/${databaseName}/default.vrd"
    commonMethods.cmdReturnStatusCode("sudo rm -f ${destinationFile}")
    commonMethods.cmd("sudo cp ${pathToSourceDefaultVrdFile} ${destinationFile}")
    commonMethods.cmd("sudo chown usr1cv8:www-data ${destinationFile}")
}


def restartApache() {
    commonMethods.cmd("sudo systemctl restart apache2.service")
}


this.commonMethods = load "./jenkinsfiles/CommonMethods.groovy"

// return this module as Groovy object
return this