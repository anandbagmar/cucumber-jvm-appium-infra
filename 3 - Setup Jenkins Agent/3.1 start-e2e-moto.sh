#!/usr/bin/env bash
set -e

NODE_NAME="<NAME_OF_THE_NODE>"
SECRET="<JENKINS_NODE's_SECRET_KEY>"

echo "********************************************"
echo Starting Jenkins Slave
echo User=$USER
echo Node name=$NODE_NAME
echo Machine=$HOSTNAME
echo JAVA_HOME=$JAVA_HOME
echo ANDROID_HOME=$ANDROID_HOME
echo "********************************************"

java -jar ~/jenkinsslave/slave.jar -jnlpUrl http://<JENKINS_SERVER_NAME>/computer/$NODE_NAME/slave-agent.jnlp -secret $SECRET &

