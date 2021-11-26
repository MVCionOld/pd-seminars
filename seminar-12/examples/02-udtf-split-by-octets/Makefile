build: clear
	mvn -f OctetsSplit/pom.xml package
run: build
	hive -f query.hql
clear:
	rm -rf OctetsSplit/target
