rm -rf $JENKINS_HOME/plugins/findbugs*

mvn clean install
cp -f target/*.hpi $JENKINS_HOME/plugins/

cd $JENKINS_HOME
./go.sh
