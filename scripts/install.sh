git clone https://github.com/luigi-asprino/lgu-commons.git
cd lgu-commons/
git checkout dev 
mvn clean install
cd ../
git clone https://github.com/luigi-asprino/edwin.git 
cd edwin/
git checkout dev-multiplefiles
mvn clean install
cd ../
echo "\n\n\nDONE\n\n\n"