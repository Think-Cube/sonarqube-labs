# Jenkins LTS with Maven, Python and the SonarQube plugin pre-installed
FROM jenkins/jenkins:lts-jdk21

USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends maven python3 python3-venv python3-pip git \
 && rm -rf /var/lib/apt/lists/*
USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Admin user and basic settings via Configuration as Code - no setup wizard
COPY casc.yaml /usr/share/jenkins/ref/casc.yaml
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc.yaml
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
