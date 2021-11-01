pwd    
mkdir plugins
mvn package
cp target/vmw-its-scanner*.jar plugins
mvn clean
mvn -f pom-7.9.xml package
cp target/vmw-its-scanner*.jar plugins
ls -lF plugins
