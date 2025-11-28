pipeline {
    agent any
    tools {
        maven 'maven'
    }
    stages {
        stage("Build JAR File"){
                    steps{
                        checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/amuzcategui/Tingeso1.git']])
                        dir("lab1"){
                            bat "mvn clean install"
                        }
                    }
                }

        stage('Unit Tests') {
            steps {

                dir('lab1') {

                    bat 'mvn test'
                }
            }
        }

        stage("Build and Push Docker Image"){
            steps{
                dir("lab1"){
                    script{
                        withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                             bat 'echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin'
                             bat "docker build -t amuzcategui/lab1:latest ."
                             bat "docker push amuzcategui/lab1:latest"
                        }                    
                    }
                }
            }
        }    
    }
}