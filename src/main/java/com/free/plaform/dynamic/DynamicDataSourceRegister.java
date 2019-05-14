package com.free.plaform.dynamic;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;

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

public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar,EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceRegister.class);

    private ConversionService conversionService = new DefaultConversionService();

    private PropertyValues dataSourcePropertyValues;

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
        //Map<String,Object> propertyResolver = env.getProperty("druid.datasource.",Map.class);


        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "druid.datasource.");
        Map<String, Object> dsMap = new HashMap<String, Object>();
        dsMap.put("driverClassName", propertyResolver.getProperty("driverClassName"));
        dsMap.put("url", propertyResolver.getProperty("url"));
        dsMap.put("username", propertyResolver.getProperty("username"));
        dsMap.put("password", propertyResolver.getProperty("password"));
        dsMap.put("validationQuery", propertyResolver.getProperty("validationQuery"));
        defaultDataSource = buildDataSource(dsMap, env);
        dataBinder(defaultDataSource, env);
    }

    /**
     * @Description 初始化更多数据源
     * @author 王鑫
     * @param env
     */
    private void initCustomDataSources(Environment env) {
        // 读取配置文件获取更多数据源，也可以通过defaultDataSource读取数据库获取更多数据源
        // Map<String,String> propertyResolver = env.getProperty("custom.datasource.",Map.class);


        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "custom.datasource.");
        String dsPrefixs = propertyResolver.getProperty("names");
        if (StringUtils.isBlank(dsPrefixs)) return;
        for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
            Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
            DataSource ds = buildDataSource(dsMap, env);
            customerDataSources.put(dsPrefix, ds);
            dataBinder(ds, env);
        }
    }


    /**
     * @Description 为DataSource绑定更多数据
     * @author 王鑫
     * @param dataSource
     * @param env
     */
    private void dataBinder(DataSource dataSource, Environment env) {

        /*Binder binder = Binder.get(env); //绑定简单配置
         ReadDataSourceProperties foo = binder.bind("spring.read", Bindable.of(ReadDataSourceProperties.class)).get();
         ReadDataSourceProperties readDataSourceProperties = binder.bind("spring.read",ReadDataSourceProperties.class).get();*/


        RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);
        // dataBinder.setValidator(new LocalValidatorFactory().run(this.applicationContext));
        dataBinder.setConversionService(conversionService);
        dataBinder.setIgnoreNestedProperties(false);// false
        dataBinder.setIgnoreInvalidFields(false);// false
        dataBinder.setIgnoreUnknownFields(true);// true
        if (dataSourcePropertyValues == null) {
            Map<String, Object> rpr = new RelaxedPropertyResolver(env, "spring.datasource").getSubProperties(".");
            Map<String, Object> values = new HashMap<String, Object>(rpr);
            // 排除已经设置的属性
            values.remove("driverClassName");
            values.remove("url");
            values.remove("username");
            values.remove("password");
            dataSourcePropertyValues = new MutablePropertyValues(values);
        }
        dataBinder.bind(dataSourcePropertyValues);
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

        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "druid.datasource.");
        dataSource.setInitialSize(Integer.parseInt(propertyResolver.getProperty("initialSize")));
        dataSource.setMaxActive(Integer.parseInt(propertyResolver.getProperty("maxActive")));
        dataSource.setMinIdle(Integer.parseInt(propertyResolver.getProperty("minIdle")));
        dataSource.setMaxWait(Integer.parseInt(propertyResolver.getProperty("maxWait")));
        dataSource.setUseUnfairLock(Boolean.valueOf(propertyResolver.getProperty("useUnfairLock")));
        dataSource.setValidationQuery(propertyResolver.getProperty("validationQuery"));
        dataSource.setTestOnBorrow(Boolean.valueOf(propertyResolver.getProperty("testOnBorrow")));
        dataSource.setTestOnReturn(Boolean.valueOf(propertyResolver.getProperty("testOnReturn")));
        dataSource.setTestWhileIdle(Boolean.valueOf(propertyResolver.getProperty("testWhileIdle")));
        dataSource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(propertyResolver.getProperty("timeBetweenEvictionRunsMillis")));
        dataSource.setMinEvictableIdleTimeMillis(Integer.parseInt(propertyResolver.getProperty("minEvictableIdleTimeMillis")));
        try {
            dataSource.setFilters(propertyResolver.getProperty("Filters"));
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
