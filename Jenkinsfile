pipeline {
    agent any
    tools{
        maven 'maven'
    }
    stages{
        stage('Build maven'){
            steps{
                checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/amuzcategui/Tingeso1.git']])
                bat 'mvn clean package'
                dir('lab1') {
                                bat 'mvn clean package'
                            }
            }

        }

        stage('Unit Tests') {
            steps {
            dir('lab1') {
                 bat 'mvn clean package'
            }
                // Run Maven 'test' phase. It compiles the test sources and runs the unit tests
                bat 'mvn test' // Use 'bat' for Windows agents or 'sh' for Unix/Linux agents
            }
        }

        stage('Build docker image'){
            steps{
                script{
                    bat 'docker build -t amuzcategui/lab1:latest .'
                }
            }
        }
        stage('Push image to Docker Hub'){
            steps{
                script{
                   withCredentials([string(credentialsId: 'docker-credentials', variable: 'Ambar2004#')]) {
                        bat 'docker login -u amuzcategui -p %Ambar2004#%'
                   }
                   bat 'docker push amuzcategui/lab1:latest'
                }
            }
        }
    }
}