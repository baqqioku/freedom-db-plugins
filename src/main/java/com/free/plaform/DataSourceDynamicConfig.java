/**
 * @ClassName: DataSourceDynamicConfig
 * @Description: 动态设置数据源
 * @author guoguo
 * @date 2019/4/28
 * @version V1.0
 * @since JDK 1.8
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ---------------------------------------------------------
 * 2019/4/28      guoguo          v1.0.0
 */

package com.free.plaform;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.free.plaform.dynamic.DynamicAtomikDataSouceRegister;
import com.free.plaform.dynamic.MultiDataSouceTransactionFactory;
import com.free.plaform.mybatis.page.PaginationInterception;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>动态设置数据源</p >
 *
 * @author guoguo
 * @version 1.0, 2019-04-28 14:30 版本及创建日期
 * @since V1.0
 */

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Import({DynamicAtomikDataSouceRegister.class})
@ConditionalOnProperty(prefix = "platform.xa", value = "enabled")
public class DataSourceDynamicConfig {


    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws SQLException, IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTransactionFactory(new MultiDataSouceTransactionFactory());
        Properties properties = new Properties();
        properties.setProperty("sqlType", "mysql");
        sqlSessionFactoryBean.setConfigurationProperties(properties);
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{new PaginationInterception()});
        return sqlSessionFactoryBean;
    }

    /*@Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }*/

    /*
    * 使用这个来做总事务 后面的数据源就不用设置事务了
    * */
    @Bean(name = "transactionManager")
    @Primary
    public JtaTransactionManager regTransactionManager () {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        UserTransaction userTransaction = new UserTransactionImp();
        return new JtaTransactionManager(userTransaction, userTransactionManager);
    }









}
