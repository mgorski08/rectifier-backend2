@echo off
set spring.datasource.url=jdbc:postgresql://localhost:5432/rectifier
set spring.datasource.username=rectifier
set spring.datasource.password=test123
set JWT_SECRET=c2938n4crETREHRwtfefg3456364093245
set spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
set spring.jpa.hibernate.ddl-auto=update
set DEFAULT_PASSWORD=test123
java -jar rectifierBackend-0.0.1-SNAPSHOT.war
pause