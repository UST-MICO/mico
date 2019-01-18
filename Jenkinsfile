pipeline {
  agent any
  stages {
    stage('Maven Build') {
      steps {
        sh 'mvn clean compile package -B -DskipTests'
      }
    }
    stage('Maven Test') {
      steps {
        sh 'mvn test'
      }
    }
    stage('Docker Registry') {
      parallel {
        stage('mico-core') {
          steps {
            sh '''ACR_IMAGE_NAME="${ACR_LOGINSERVER}/mico-core:kube${BUILD_NUMBER}"
docker build -t $ACR_IMAGE_NAME -f Dockerfile.mico-core .
docker login ${ACR_LOGINSERVER} -u ${ACR_ID} -p ${ACR_PASSWORD}
docker push $ACR_IMAGE_NAME
DOCKERHUB_IMAGE_NAME="ustmico/mico-core:latest"
docker tag $ACR_IMAGE_NAME $DOCKERHUB_IMAGE_NAME
docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}
docker push $DOCKERHUB_IMAGE_NAME'''
          }
        }
        stage('mico-admin') {
          steps {
            sh '''ACR_IMAGE_NAME="${ACR_LOGINSERVER}/mico-admin:kube${BUILD_NUMBER}"
docker build -t $ACR_IMAGE_NAME -f Dockerfile.mico-admin .
docker login ${ACR_LOGINSERVER} -u ${ACR_ID} -p ${ACR_PASSWORD}
docker push $ACR_IMAGE_NAME
DOCKERHUB_IMAGE_NAME="ustmico/mico-admin:latest"
docker tag $ACR_IMAGE_NAME $DOCKERHUB_IMAGE_NAME
docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}
docker push $DOCKERHUB_IMAGE_NAME'''
          }
        }
      }
    }
    stage('Deploy') {
      parallel {
        stage('kubectl mico-core') {
          steps {
            sh '''ACR_IMAGE_NAME="${ACR_LOGINSERVER}/mico-core:kube${BUILD_NUMBER}"
kubectl set image deployment/mico-core mico-core=$ACR_IMAGE_NAME --kubeconfig /var/lib/jenkins/config'''
          }
        }
        stage('kubectl mico-admin') {
          steps {
            sh '''ACR_IMAGE_NAME="${ACR_LOGINSERVER}/mico-admin:kube${BUILD_NUMBER}"
kubectl set image deployment/mico-admin mico-admin=$ACR_IMAGE_NAME --kubeconfig /var/lib/jenkins/config'''
          }
        }
      }
    }
  }
}