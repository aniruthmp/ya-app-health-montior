lib = library identifier: 'jenkins-pipeline-scripts-pcf@pcf-pipeline', retriever: legacySCM([$class: 'GitSCM', branches: [[name: 'pcf-pipeline']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'jenkins-ghe-private-key-for-releases', url: 'git@ghe.aa.com:TechOps-SOA/jenkins-pipeline-scripts.git']]])

deployable = "yapper"

try {
    def GIT_COMMIT = ''
    def artifact = ''
    def repoUrl = ''
    def pcf = lib.com.aa.techops.jenkins.PCF.new()
    def notifications = lib.com.aa.techops.jenkins.Notifications.new()
    //projectName = lib.com.aa.techops.jenkins.Jenkins().new().getProjectNameFromJobName(env.JOB_NAME)

    node('Builder') {
        stage("Checkout") {
            deleteDir()
            checkout scm
            GIT_COMMIT = sh(returnStdout: true, script: 'git show -s --format=%H').trim()
        }

        stage("Test") {
            def mvnHome = tool 'Maven339'
            sh "${mvnHome}/bin/mvn -U clean package"
        }

        pcfConfig = lib.com.aa.techops.jenkins.PCFConfig.DEV_CONFIG
        lock("${env.JOB_NAME}-${pcfConfig.env}"){
            stage("Deploy to ${pcfConfig.env}") {
                pcf.push(deployable, pcfConfig)
                notifications.notifyThatBuildDeployed(deployable, env.BUILD_URL, pcfConfig.env)
           }
        }

        pcfConfig = lib.com.aa.techops.jenkins.PCFConfig.QA_CONFIG
        lock("${env.JOB_NAME}-${pcfConfig.env}"){
            stage("Deploy to ${pcfConfig.env}") {
                pcf.push(deployable, pcfConfig)
                notifications.notifyThatBuildDeployed(deployable, env.BUILD_URL, pcfConfig.env)
           }
        }

        pcfConfig = lib.com.aa.techops.jenkins.PCFConfig.PERF_CONFIG
        lock("${env.JOB_NAME}-${pcfConfig.env}"){
            stage("Deploy to ${pcfConfig.env}") {
                pcf.push(deployable, pcfConfig)
                notifications.notifyThatBuildDeployed(deployable, env.BUILD_URL, pcfConfig.env)
           }
        }
    }
    
} catch (Throwable err) {
    echo "Failed: ${err}"

    // def projectName = new com.aa.techops.jenkins.Jenkins().getProjectNameFromJobName(env.JOB_NAME)
    // notifications = new com.aa.techops.jenkins.Notifications()	
    // notifications.notifyThatBuildFailed(projectName, env.BUILD_URL)        
    throw err
}
