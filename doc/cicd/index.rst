Continous Integration & Continous Deployment
============================================

Describes setup of a Continous Integration and Continous Deployment Pipeline with Kubernetes, Jenkins, and Docker hosted on Microsoft Azure. 

.. role:: bash(code)
    :language: bash

Prerequisites
-------------

* Install Docker (on Ubuntu: `<https://docs.docker.com/install/linux/docker-ce/ubuntu/>`_ )
* Install unzip :bash:`sudo apt-get install unzip`
* Install Maven
    * :bash:`sudo mkdir /opt/maven/`
    * :bash:`cd /opt/maven/`
    * :bash:`wget http://ftp.wayne.edu/apache/maven/maven-3/<VERSION>/binaries/apache-maven-<VERSION>-bin.zip`
    * :bash:`unzip apache-maven-<VERSION>`
    * :bash:`sudo nano /etc/profile.d/maven.sh`
    * Add the following lines to maven.sh:
        .. code-block:: bash
        
            export JAVA_HOME=<PATH_TO_YOUR_JAVA_HOME>
            export M2_HOME=/opt/maven
            export MAVEN_HOME=/opt/maven
            export PATH=${M2_HOME}/bin:${PATH}
    * :bash:`sudo source /etc/profile.d/maven.sh`

Kubernetes Setup
----------------

* Create Azure Kubernetes Service (AKS) on Microsoft Azure (`<https://docs.microsoft.com/en-us/azure/aks/kubernetes-walkthrough>`_)
* Create Azure Container Registry (ACR) (`<https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-prepare-acr>`_
* Provide AKS access to ACR to be able to read containers (`<https://docs.microsoft.com/de-de/azure/container-registry/container-registry-auth-aks#grant-aks-access-to-acr>`_) 
* Access Kubernetes Dashboard: 
    * Install Kubernetes-CLI: :bash:`sudo az aks install-cli`
    * Go to Kubernetes Cluster Resource in Microsoft Azure and follow the instructions for "Kubernetes Dashboard"
* If Cluster uses RBAC: use of Kubernetes Dashboard is restricted and most of the operations are not permitted. To be able to operate through the dashboard, execute the following command: :bash:`kubectl create clusterrolebinding kubernetes-dashboard --clusterrole=cluster-admin --serviceaccount=kube-system:kubernetes-dashboard` 
* WARNING: This configuration of a ClusterRoleBinding may lead to security issues, there is no additional authentication and the dashboard is publicly accessable! (`<https://docs.microsoft.com/en-us/azure/aks/kubernetes-dashboard>`_)

Jenkins Setup
-------------
* Deploy a Jenkins VM through the Azure Marketplace
* Create Jenkins environment variable to hold ACR login server with the name: ACR_LOGINSERVER (`<https://docs.microsoft.com/en-us/azure/aks/jenkins-continuous-deployment#create-a-jenkins-environment-variable>`_)
* Create Jenkins credentials to access ACR: `<https://docs.microsoft.com/en-us/azure/aks/jenkins-continuous-deployment#create-a-jenkins-credential-for-acr>`_
* Create a new Jenkins project: `<https://docs.microsoft.com/en-us/azure/aks/jenkins-continuous-deployment#create-a-jenkins-project>`_
* Create a Github Webhook: `<https://docs.microsoft.com/en-us/azure/aks/jenkins-continuous-deployment#create-a-github-webhook>`_
* Push the Docker image(s) to Azure Container Registry (ACR) manually (only first time)
* Add the following build scripts:
    * Build multi-module MICO project with maven: :bash:`mvn clean compile package -DskipTests`
    * Build and push MICO-Core Docker image: 
        .. code-block:: bash

            WEB_IMAGE_NAME="${ACR_LOGINSERVER}/mico-core:kube${BUILD_NUMBER}"
            docker build -t $WEB_IMAGE_NAME ./mico-core
            docker login ${ACR_LOGINSERVER} -u ${ACR_ID} -p ${ACR_PASSWORD}
            docker push $WEB_IMAGE_NAME
    * Deploy MICO-Core to Kubernetes:
        .. code-block:: bash

            WEB_IMAGE_NAME="${ACR_LOGINSERVER}/mico-core:kube${BUILD_NUMBER}"
            kubectl set image deployment/mico-core mico-core=$WEB_IMAGE_NAME --kubeconfig /var/lib/jenkins/config
    * Build and push MICO-Frontend Docker image:
        .. code-block:: bash

            WEB_IMAGE_NAME="${ACR_LOGINSERVER}/mico-admin:kube${BUILD_NUMBER}"
            docker build -t $WEB_IMAGE_NAME ./mico-admin
            docker login ${ACR_LOGINSERVER} -u ${ACR_ID} -p ${ACR_PASSWORD}
            docker push $WEB_IMAGE_NAME

    * Deploy MICO-Frontend to Kubernetes:
        .. code-block:: bash

            WEB_IMAGE_NAME="${ACR_LOGINSERVER}/mico-admin:kube${BUILD_NUMBER}"
            kubectl set image deployment/mico-admin mico-admin=$WEB_IMAGE_NAME --kubeconfig /var/lib/jenkins/config
