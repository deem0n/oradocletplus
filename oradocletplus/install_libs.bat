call mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=repo/com-sun-javadoc-1.0.jar -DgroupId=com.sun -DartifactId=com-sun-javadoc -Dversion=1.0 -Dpackaging=jar

call mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=repo/ojdbc6-11.2.0.1.0.jar -DgroupId=oracle.jdbc -DartifactId=ojdbc6 -Dversion=11.2.0.1.0 -Dpackaging=jar
