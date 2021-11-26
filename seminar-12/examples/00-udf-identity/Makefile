build: clear
	mvn -f Identity/pom.xml package
run: build
	hive -f query.hql
clear:
	rm -rf Identity/target