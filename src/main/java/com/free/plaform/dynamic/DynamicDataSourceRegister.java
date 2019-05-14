package com.free.plaform.dynamic;

import com.alibaba.druid.pool.DruidDataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.free.plaform.config.CustomDataSource;
import com.free.plaform.config.CustomerDataSourceConfiguration;
import com.free.plaform.config.DruidSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar,EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceRegister.class);

    private ConversionService conversionService = new DefaultConversionService();

    private Map dataSourcePropertyValues;

    private DataSource defaultDataSource;

    private Map<String, DataSource> customerDataSources = new HashMap<String, DataSource>();

    @Override
    public void setEnvironment(Environment environment) {

        initDefaultDataSource(environment);
        initCustomDataSources(environment);


    }

    /**
     * @Description 初始化主数据源
     * @author 王鑫
     * @param env
     */
    private void initDefaultDataSource(Environment env) {
       // 读取主数据源
        Binder binder = Binder.get(env); //绑定简单配置
        DruidSettings propertyResolver = binder.bind("druid.datasource", Bindable.of(DruidSettings.class)).get();
        dataSourcePropertyValues = binder.bind("druid.datasource", Map.class).get();
        Map<String, Object> dsMap = new HashMap<String, Object>();
        dsMap.put("driverClassName", propertyResolver.getDriverClassName());
        dsMap.put("url", propertyResolver.getUrl());
        dsMap.put("username", propertyResolver.getUsername());
        dsMap.put("password", propertyResolver.getPassword());
        dsMap.put("validationQuery", propertyResolver.getValidationQuery());
        defaultDataSource = buildDataSource(dsMap, env);
        //dataBinder(defaultDataSource, env);
    }


    /**
     * @Description 初始化更多数据源
     * @author 王鑫
     * @param env
     */
    private void initCustomDataSources(Environment env) {
        // 读取配置文件获取更多数据源，也可以通过defaultDataSource读取数据库获取更多数据源
        Binder binder = Binder.get(env); //绑定简单配置
        CustomerDataSourceConfiguration propertyResolver = binder.bind("custom", Bindable.of(CustomerDataSourceConfiguration.class)).get();
        Map<String, CustomDataSource> customDataSourceMap = propertyResolver.getDatasource();
        for(Map.Entry entry : customDataSourceMap.entrySet()){
            CustomDataSource customDataSource = (CustomDataSource) entry.getValue();
            DataSource ds = builCustomerdDataSource(customDataSource, env);
            customerDataSources.put((String) entry.getKey(), ds);
        }

    }



    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<Object,Object> targetDataSources = new HashMap<Object,Object>();

        targetDataSources.put("dataSource",defaultDataSource);
        DynamicDataSourceContextHolder.dataSourceIds.add("dataSource");
        targetDataSources.putAll(customerDataSources);

        for(String key:customerDataSources.keySet()){
            DynamicDataSourceContextHolder.dataSourceIds.add(key);
        }
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicDataSource.class);

        beanDefinition.setSynthetic(true);
        MutablePropertyValues mpv = beanDefinition.getPropertyValues();
        mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
        mpv.addPropertyValue("targetDataSources", targetDataSources);
        registry.registerBeanDefinition("dataSource", beanDefinition);
        logger.info("Dynamic DataSource Registry");
    }

    /**
     * @Description 创建DataSource
     * @author 王鑫
     * @param dsMap
     * @return
     */
    public DataSource buildDataSource(Map<String, Object> dsMap, Environment env) {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dsMap.get("url").toString());
        dataSource.setDriverClassName(dsMap.get("driverClassName").toString());
        dataSource.setUsername(dsMap.get("username").toString());
        dataSource.setPassword(dsMap.get("password").toString());

        Binder binder = Binder.get(env); //绑定简单配置
        DruidSettings propertyResolver = binder.bind("druid.datasource", Bindable.of(DruidSettings.class)).get();
        dataSource.setInitialSize(Integer.parseInt(propertyResolver.getInitialSize().toString()));
        dataSource.setMaxActive(Integer.parseInt(propertyResolver.getMaxActive().toString()));
        dataSource.setMinIdle(Integer.parseInt(propertyResolver.getMinIdle().toString()));
        dataSource.setMaxWait(Integer.parseInt(propertyResolver.getMaxWait().toString()));
        //dataSource.setUseUnfairLock(Boolean.valueOf(propertyResolver.get("useUnfairLock")));
        dataSource.setValidationQuery(propertyResolver.getValidationQuery());
        //dataSource.setTestOnBorrow(Boolean.valueOf(propertyResolver.get("testOnBorrow")));
        //dataSource.setTestOnReturn(Boolean.valueOf(propertyResolver.getProperty("testOnReturn")));
        //dataSource.setTestWhileIdle(Boolean.valueOf(propertyResolver.getProperty("testWhileIdle")));
        dataSource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(propertyResolver.getTimeBetweenEvictionRunsMillis().toString()));
        dataSource.setMinEvictableIdleTimeMillis(Integer.parseInt(propertyResolver.getMinEvictableIdleTimeMillis().toString()));
        try {
            dataSource.setFilters(propertyResolver.getFilters());
            return dataSource;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DataSource builCustomerdDataSource(CustomDataSource customDataSource, Environment env) {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(customDataSource.getUrl());
        dataSource.setDriverClassName(customDataSource.getDriverClassName());
        dataSource.setUsername(customDataSource.getUsername().toString());
        dataSource.setPassword(customDataSource.getPassword().toString());

        Binder binder = Binder.get(env); //绑定简单配置
        DruidSettings propertyResolver = binder.bind("druid.datasource", Bindable.of(DruidSettings.class)).get();
        dataSource.setInitialSize(Integer.parseInt(propertyResolver.getInitialSize().toString()));
        dataSource.setMaxActive(Integer.parseInt(propertyResolver.getMaxActive().toString()));
        dataSource.setMinIdle(Integer.parseInt(propertyResolver.getMinIdle().toString()));
        dataSource.setMaxWait(Integer.parseInt(propertyResolver.getMaxWait().toString()));
        //dataSource.setUseUnfairLock(Boolean.valueOf(propertyResolver.get("useUnfairLock")));
        dataSource.setValidationQuery(propertyResolver.getValidationQuery());
        //dataSource.setTestOnBorrow(Boolean.valueOf(propertyResolver.get("testOnBorrow")));
        //dataSource.setTestOnReturn(Boolean.valueOf(propertyResolver.getProperty("testOnReturn")));
        //dataSource.setTestWhileIdle(Boolean.valueOf(propertyResolver.getProperty("testWhileIdle")));
        dataSource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(propertyResolver.getTimeBetweenEvictionRunsMillis().toString()));
        dataSource.setMinEvictableIdleTimeMillis(Integer.parseInt(propertyResolver.getMinEvictableIdleTimeMillis().toString()));
        try {
            dataSource.setFilters(propertyResolver.getFilters());
            return dataSource;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    /*@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        defaultDataSource  = (DataSource) applicationContext.getBean("defaultDataSource");
    }*/
}
