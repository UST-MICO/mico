pipeline {
  environment {
    micoAdminRegistry = "ustmico/mico-admin"
    micoCoreRegistry = "ustmico/mico-core"
    registryCredential = 'dockerhub'
    micoAdminDockerImage = ''
    micoCoreDockerImage = ''
  }
  agent any
  stages {
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
    stage('Deploy on kubernetes') {
        parallel {
            stage('mico-core') {
                steps{
                    sh '''ACR_IMAGE_NAME="ustmico/mico-core:kube${BUILD_NUMBER}"
                    kubectl set image deployment/mico-core mico-core=$ACR_IMAGE_NAME --kubeconfig /var/lib/jenkins/config'''
                }
            }
            stage('mico-admin') {
                steps{
                    sh '''ACR_IMAGE_NAME="ustmico/mico-admin:kube${BUILD_NUMBER}"
                    kubectl set image deployment/mico-admin mico-admin=$ACR_IMAGE_NAME --kubeconfig /var/lib/jenkins/config'''
                }
            }
        }
    }
    stage('Remove unused docker images') {
        parallel {
            stage('mico-core') {
                steps{
                    sh "docker rmi $micoCoreRegistry:latest"
                    sh "docker rmi $micoCoreRegistry:kube${BUILD_NUMBER}"
                }
            }
            stage('mico-admin') {
                steps{
                    sh "docker rmi $micoAdminRegistry:latest"
                    sh "docker rmi $micoAdminRegistry:kube${BUILD_NUMBER}"
                }
            }
        }
    }
  }
}
