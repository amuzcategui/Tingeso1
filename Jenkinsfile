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
                       
                           withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {

                                // Ahora usamos esas variables para el login
                                bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                           }

                           bat 'docker push amuzcategui/lab1:latest'
                        }
                    }
                }
    }
}