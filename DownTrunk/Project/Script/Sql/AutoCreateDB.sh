echo "create database, using sh."

DBHost=127.0.0.1 
DBName=down_trunk
DBRoot=uroot
DBP1=p1

echo "drop ${DBName} database..."
echo "drop database if exists ${DBName};"
mysql -${DBRoot} -${DBP1} -h${DBHost} -e "drop database if exists ${DBName}"

echo "create ${DBName} database... "
echo "create database ${DBName};"
mysql -${DBRoot} -${DBP1} -h${DBHost} -e "create database ${DBName}"

echo "import ${DBName} sql setence..."
mysql -${DBRoot} -${DBP1} -h${DBHost} ${DBName} < ${DBName}.sql

echo "import ${DBName} over"