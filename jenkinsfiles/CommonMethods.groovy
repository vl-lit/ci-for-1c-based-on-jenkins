
def exportEnvironmentVariablePrefix() {

    if (isUnix()) {
        return "export"
    } else {  
        return "SET"
    }    
}

def assertWithEcho(booleanExpression, errorMessage, successMessage = "") {    

    if (booleanExpression != true) {        
        echoAndError(errorMessage)
    }
    else if (successMessage != "") {
        echo successMessage
    }

}

def echoAndError(message) {
    echo message
    error message
}

def stdoutDependingOnOS() {

    if (isUnix()) {
        return "/dev/stdout"
    } else {  
        return "CON"
    }    
}

def getTempDirecrotyDependingOnOS() {
    if (isUnix()) {
        return env.TMPDIR != null ? env.TMPDIR : '/tmp'
    } else {
         return env.TEMP
    }
}

def getNullFile() {

    if (isUnix()) {
        return "/dev/null"
    } else {
         return "nul"
    }
}

def concatStringsFromArray(ArrayList command, boolean addSpaces = false) {
    
    resultStr = ""

    command.each { str ->
        resultStr += str
        if (addSpaces) {
            resultStr += " "
        }
    }
        
    return resultStr
}

def concatCommandFromArray(ArrayList command) {

    return concatStringsFromArray(command, true)
}

def cmd(command) {
    
    if (command instanceof ArrayList)
        command = concatCommandFromArray(command)

    if (env.VERBOSE == "true")
        echo command

    if (isUnix()) {
        sh "${command}"
    } else {
        bat """chcp 65001 > nul
            ${command}"""
    }

}

def cmdReturnStatusCode(command) {
    
    if (command instanceof ArrayList)
        command = concatCommandFromArray(command)    

    if (env.VERBOSE == "true")
        echo command

    def statusCode = 0

    if (isUnix()) {
        statusCode = sh script: "${command}",
                        returnStatus: true
    } else {
        statusCode = bat script:
                        """chcp 65001 > null
                        ${command}""",
                     returnStatus: true
    }

    return statusCode
}


def cmdReturnStdout(command) {
    
    if (command instanceof ArrayList)
        command = concatCommandFromArray(command)    

    if (env.VERBOSE == "true")
        echo command

    def output = ""

    if (isUnix()) {
        output = sh script: "${command}",
                        returnStdout: true
    } else {
        output = bat script:
                    """chcp 65001 > null
                    ${command}""",
                    returnStdout: true
    }

    output = output.trim()

    return output
}


def cmdReturnStatusCodeAndStdout(command) {
    
    if (command instanceof ArrayList)
        command = concatCommandFromArray(command) 

    def statusCode = 0
    def stdout = ""
    def tempFileName = 'cmdReturnStatusCodeAndStdout_' + UUID.randomUUID() + '.txt'
    def tempFilePath = getTempDirecrotyDependingOnOS() + "/" + tempFileName
    statusCode = cmdReturnStatusCode(command + ' > ' + tempFilePath)
    stdout = readFile(tempFilePath).trim()
    cmdReturnStatusCode('rm -f ' + tempFilePath)
    
    return [ statusCode, stdout ]
}


def emailJobStatus(status) {

    emailext (
        subject: "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
        body: """<p>BUILD STATUS IS ${currentBuild.result}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
            <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>""",
        recipientProviders: [[$class: 'DevelopersRecipientProvider']],
        to: "${env.EMAIL_ADDRESS_FOR_NOTIFICATIONS}"
    )

}

def hostname() {

    result = cmdReturnStdout("hostname")
    return result    

}


def deleteFileIfExists(filePath) {

    if ( fileExists("${filePath}") ) { cmd "rm -f ${filePath}" }
    if ( fileExists("${filePath}") ) { error "Failed to delete file ${filePath}" }

}


// Needs -Dpermissive-script-security.enabled=true to be set in jenkins.xml
def isTimeoutException(excp) {
    
    result = false;

    excp.causes.each { item ->
        if ("${item}".contains("org.jenkinsci.plugins.workflow.steps.TimeoutStepExecution"))
            result = true;
    }

    return result
}

def throwTimeoutException(stageName) {
    error "TIMEOUT ON STAGE '${stageName}'"
}

def readFileWithoutBOM(fileName) {

    def text = cmdReturnStdout("sed '1s/^\\xEF\\xBB\\xBF//;1s/^\\xFE\\xFF//;1s/^\\xFF\\xFE//' '${fileName}'")
    return text
}

def getFirstLineOfTextFileLowerCase(fileName) {

    result = "" 
    text = readFileWithoutBOM(fileName)
    lines = text.split("\n")

    if (lines.length > 0) {        
        result = lines[0]
        result = result.trim().toLowerCase()
    }

    return result
}

def getLastLineOfTextFileLowerCase(fileName) {

    result = ""
    text = readFileWithoutBOM(fileName)
    lines = text.split("\n")

    if (lines.length > 0) {        
        result = lines[lines.length-1]        
        result = result.trim().toLowerCase()        
    }

    return result
}

def getNumberFromNumberFile(fileName, throwExceptionIfFileNotExists = false, throwExceptionIfFileIsEmpty = false) {

    number = 0

    if (!fileExists(fileName)) {

        if (env.VERBOSE == "true") {
            echo "getNumberFromNumberFile: File not exists: ${fileName}"
        }        

        if (throwExceptionIfFileNotExists) {
            echoAndError("Exception from getNumberFromNumberFile: file not exists: ${fileName}")
        }
    }
    else {

        if (env.VERBOSE == "true") {
            echo "getNumberFromNumberFile: File exists: ${fileName}"
        }        

        str = getLastLineOfTextFileLowerCase(fileName)
        if (str == "") {
            if (throwExceptionIfFileIsEmpty) {
                echoAndError("Exception from getNumberFromNumberFile: file is empty: ${fileName}")
            }
        }
        else {

            if (env.VERBOSE == "true") {
                echo "getNumberFromNumberFile: Got string representation of number from file: ${str}"
            }

            number = str.toInteger()
        }
    }

    return number
}

def killProcessesByRegExp(mask) {

    def command = "ps aux | grep '${mask}' | grep -v grep | tr -s ' ' | cut -d ' ' -f 2 | while read line; do kill \$line; done"
    cmd(command)

}

def max(ArrayList numbers) {

    def maxVal = numbers[0]

    numbers.each { item ->
        if (item > maxVal) maxVal = item
    }

    return maxVal

}

// Return this module as Groovy object 
return this