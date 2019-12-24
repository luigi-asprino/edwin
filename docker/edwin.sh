cd /opt/lgu-commons/
git pull 
mvn clean install  -Dgpg.skip
cd /opt/edwin/
git pull
mvn exec:java -Dexec.mainClass="it.cnr.istc.stlab.edwin.Edwin" -Dexec.args="/opt/data/conf.properties" -DjvmArgs="-Xmx32g"
