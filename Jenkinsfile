pipeline {
    agent any
    tools {
        jdk "JAVA17"
    }
    stages {
        stage("Build") {
            steps {
                sh "chmod +x ./gradlew"
                sh "./gradlew :Fabric:build :Forge:build"
            }
        }
    }
    post {
        always {
            sh "./gradlew --stop"
        }
    }
}
