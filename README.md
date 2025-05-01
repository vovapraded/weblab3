# Гайд по запуску
1.  В директории с проектом:  
`docker compose up -d`  
`mvn package`
2. Переместить в war в wildfly-preview-26.1.3.Final/standalone/deployments
3. в папке wildfly: `./bin/standalone.sh -c standalone.xml`  
или `bin\standalone.bat -c standalone.xml` на windows
4. Приложение запущено по адресу: http://localhost:8080/