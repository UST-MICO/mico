#!/usr/bin/env groovy

def COLOR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger', 'UNSTABLE': 'danger', 'ABORTED': 'danger']

pipeline {
    environment {
        BUILD_USER = ''
        micoAdminRegistry = "ustmico/mico-admin"
        micoCoreRegistry = "ustmico/mico-core"
        registryCredential = 'dockerhub'
        micoAdminDockerImage = ''
        micoCoreDockerImage = ''
    }
    agent any
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/UST-MICO/mico.git'
            }
        }
        stage('Docker build') {
            parallel {
                stage('mico-core') {
                    steps {
                        script {
                            micoCoreDockerImage = docker.build(micoCoreRegistry, "-f Dockerfile.mico-core .")
                        }
                    }
                }
                stage('mico-admin') {
                    steps {
                        script {
                            micoAdminDockerImage = docker.build(micoAdminRegistry, "-f Dockerfile.mico-admin .")
                        }
                    }
                }
            }
        }
        stage('Push images') {
            steps {
                script{
                    docker.withRegistry('', 'dockerhub') {
                        micoCoreDockerImage.push("kube$BUILD_NUMBER")
                        micoAdminDockerImage.push("kube$BUILD_NUMBER")
                        micoCoreDockerImage.push("latest")
                        micoAdminDockerImage.push("latest")
                    }
                }
            }
        }
        stage('Deploy on Kubernetes') {
            parallel {
                stage('mico-core') {
                    steps{
                        sh '''IMAGE_NAME="ustmico/mico-core:kube${BUILD_NUMBER}"
                        kubectl set image deployment/mico-core mico-core=$IMAGE_NAME -n mico-system --kubeconfig /var/lib/jenkins/config'''
                    }
                }
                stage('mico-admin') {
                    steps{
                        sh '''IMAGE_NAME="ustmico/mico-admin:kube${BUILD_NUMBER}"
                        kubectl set image deployment/mico-admin mico-admin=$IMAGE_NAME -n mico-system --kubeconfig /var/lib/jenkins/config'''
                    }
                }
            }
        }
        stage('Docker clean up') {
            steps {
                // Delete all images that are older than 10 days
                sh '''docker image prune -a --force --filter "until=240h"'''
            }
        }
    }

    post {
        always {
            wrap([$class: 'BuildUser']) {
    	       slackSend channel: '#ci-pipeline',
                    color: COLOR_MAP[currentBuild.currentResult],
                    message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${BUILD_USER}\n More info at: ${env.BUILD_URL}"
    
                // Clean workspace
                cleanWs()
            }
        }
    }
}
