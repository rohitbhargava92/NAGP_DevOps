pipeline {
    agent any
    tools {
        maven "Maven3"
    }
    stages {
        stage('Clone') {
            steps {
                echo 'Cloning Repo from git Branch Master'
                git branch: 'master', url: 'https://github.com/rohitbhargava92/NAGP_DevOps.git'
            }
        }
        stage('Test') {
            steps {
                echo 'Running Smoke Test to check Major Functionalities'
                bat 'mvn clean test'
            }
        }
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv("SonarQube_jenkinsIntegration") {
                    bat 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar'
                }
            }
        }
        stage("Publish to Artifactory") {
            steps {
                rtMavenDeployer(
                    id: 'deployer',
                    serverId: '3161671_Rohit@artifactory',
                    releaseRepo: 'org.nagp.declarativePipeline',
                    snapshotRepo: 'org.nagp.declarativePipeline'
                )
                rtPublishBuildInfo(
                    serverId: '3161671_Rohit@artifactory',
                )
            }
        }
    }
    post {
        success {
            echo 'Build Sucess. Creating Junit Report'
            junit '**/target/surefire-reports/TEST-*.xml'
            testNG()
        }
        failure {
            echo 'Build Failed. Creating Junit Report'
            junit '**/target/surefire-reports/TEST-*.xml'
            testNG()
        }
    }
}
