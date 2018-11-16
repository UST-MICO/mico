Continous Integration
=====================

.. role:: bash(code)
    :language: bash

Prerequisites
-------------

* Install Docker (on Ubuntu: `<https://docs.docker.com/install/linux/docker-ce/ubuntu/>`_ )
* Install unzip :bash:`sudo apt-get install unzip`
* Install Maven
    * :bash:`sudo mkdir /opt/maven/`
    * :bash:`cd /opt/maven/`
    * :bash:`wget http://ftp.wayne.edu/apache/maven/maven-3/<VERSION/binaries/apache-maven-<VERSION>-bin.zip`
    * :bash:`unzip apache-maven-<VERSION>`
    * :bash:`sudo nano /etc/profile.d/maven.sh`
    * Add the following lines to maven.sh:
        .. code-block:: bash

            export JAVA_HOME=<PATH_TO_YOUR_JAVA_HOME>
            export M2_HOME=/opt/maven
            export MAVEN_HOME=/opt/maven
            export PATH=${M2_HOME}/bin:${PATH}
    * :bash:`sudo source /etc/profile.d/maven.sh`


        
        

Jenkins Setup
-------------