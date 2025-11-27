pipeline {
    agent any
    tools {
        maven 'maven'
    }
    stages {
        stage('Build maven') {
            steps {
                
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/amuzcategui/Tingeso1.git']])


                dir('lab1') {

                    bat 'mvn clean package'
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

        stage('Build docker image') {
            steps {
                script {

                    dir('lab1') {
                        bat 'docker build -t amuzcategui/lab1:latest .'
                    }
                }
            }
        }

        stage('Push image to Docker Hub') {
            steps {
                script {

                   withCredentials([string(credentialsId: 'docker-credentials', variable: 'pass')]) {
                        bat 'docker login -u amuzcategui -p %Ambar2004#%'
                   }
                   bat 'docker push amuzcategui/lab1:latest'
                }
            }
        }
    }
}