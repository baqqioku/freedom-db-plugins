# freedom-db-plugins

#### 介绍
数据库插件
基于SpringBoot2.X以上  集成了 atomikos  解决多数据源的动态切换 同时可以满足对多数据的事务管理

#### 插件配置

.properties 数据源配置

````
# datasource 默认数据源
druid.datasource.url=jdbc:mysql://localhost:3306/keta_custom?useUnicode=true&characterEncoding=UTF-8&useSSL=false
druid.datasource.driverClassName=com.mysql.jdbc.Driver
druid.datasource.username=root
druid.datasource.password=root
druid.datasource.filters=stat
druid.datasource.maxActive=20
druid.datasource.initialSize=1
druid.datasource.maxWait=60000
druid.datasource.minIdle=1
druid.datasource.timeBetweenEvictionRunsMillis=60000
druid.datasource.minEvictableIdleTimeMillis=300000
druid.datasource.validationQuery=SELECT 'x'
druid.datasource.testWhileIdle=true
druid.datasource.testOnBorrow=false
druid.datasource.testOnReturn=false
druid.datasource.poolPreparedStatements=false
druid.datasource.maxOpenPreparedStatements=20
druid.datasource.slowSqlMillis=5000
druid.datasource.isLogSlowSql=true



# 数据源1 依始类推，db2,db3
custom.datasource.db1.name=db1
custom.datasource.db1.url=jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false
custom.datasource.db1.driverClassName=com.mysql.jdbc.Driver
custom.datasource.db1.username=root
custom.datasource.db1.password=root
custom.datasource.db1.validationQuery=SELECT 1

# 集成了atomikos 通过配置启动，如果不启用可以使用
#不启动用是atomikos
platform.noXa.enabled=true
#启用aomikos
platform.xa.enabled=true
````


