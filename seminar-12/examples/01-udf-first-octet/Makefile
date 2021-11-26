build: clear
	mvn -f FirstOctet/pom.xml package
run: build
	hive -f query.hql
clear:
	rm -rf FirstOctet/target