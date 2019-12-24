cd /opt/
git clone https://github.com/rdfhdt/hdt-java.git
cd /opt/hdt-java/ 
mvn clean install
cd /opt/ 
git clone https://github.com/luigi-asprino/lgu-commons.git
cd /opt/lgu-commons/
git checkout dev 
mvn clean install  -Dgpg.skip
cd /opt/
git clone https://github.com/luigi-asprino/edwin.git 
cd /opt/edwin/
git checkout dev-multiplefiles
mvn clean install
echo "\n\n\nDONE\n\n\n"