A Web Application that reports Choco performance over a set of fzn problems.
(The database is private, though).

Pre-requisites: a file name 'mysql.properties' must be set in /src/main/resources/ and shoud contain :
```
mysql.url=...
mysql.dbname=...
mysql.user=...
mysql.pwd=...
```

The main commands are:
$ mvn clean vaadin:update-widgetset gwt:compile install
$ mvn tomcat:run

