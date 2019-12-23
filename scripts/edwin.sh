pwd=`pwd`
cd edwin/
git pull
mvn exec:java -Dexec.mainClass="it.cnr.istc.stlab.edwin.Edwin" -Dexec.args="$pwd/$1" -DjvmArgs="-Xmx32g"
