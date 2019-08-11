/**
 * @ClassName: DataSourceNoXaDynamicConfig
 * @Description: 没有启动Atomic的数据源
 * @author guoguo
 * @date 2019/5/13
 * @version V1.0
 * @since JDK 1.8
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ---------------------------------------------------------
 * 2019/5/13      guoguo          v1.0.0
 */

package com.free.plaform;

import com.free.plaform.dynamic.DynamicDataSourceRegister;
import com.free.plaform.dynamic.MultiDataSouceTransactionFactory;
import com.free.plaform.mybatis.page.PaginationInterception;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>没有启动Atomic的数据源</p >
 *
 * @author guoguo
 * @version 1.0, 2019-05-13 9:31 版本及创建日期
 * @since V1.0
 */

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Import({DynamicDataSourceRegister.class})
@ConditionalOnProperty(prefix = "platform.noXa", value = "enabled")
public class DataSourceNoXaDynamicConfig {

    @Bean
    @ConditionalOnMissingBean(SqlSessionFactoryBean.class)
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

    @Bean
    @ConditionalOnMissingBean(JtaTransactionManager.class)
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


}
