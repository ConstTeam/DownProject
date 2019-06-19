echo "create database, using sh."

DBHost=127.0.0.1 
DBName=down_log
DBRoot=uroot
DBP1=p1

echo "drop ${DBName} database..."
echo "drop database if exists ${DBName};" | mysql -${DBRoot} -${DBP1} -h${DBHost}

echo "create ${DBName} database... "
echo "create database ${DBName};" | mysql -${DBRoot} -${DBP1} -h${DBHost}

echo "import ${DBName} sql setence..."
mysql -${DBRoot} -${DBP1} -h${DBHost} ${DBName} < ${DBName}.sql

echo "import ${DBName} over"

#echo "drop database if exists ${DBHotGameName}_log;" | /usr/local/mysql/bin/mysql -${DBRoot} -${DBP1} -h${DBHost}
#echo "create database ${DBHotGameName}_log;" | /usr/local/mysql/bin/mysql -${DBRoot} -${DBP1} -h${DBHost}
#/usr/local/mysql/bin/mysql -${DBRoot} -${DBP1} -h${DBHost} ${DBHotGameName}_log < log.sql
