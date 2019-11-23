/**
 * @ClassName: CustomerDataSourceConfiguration
 * @Description: 自定义的数据库 链接
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

package com.free.plaform.config;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>自定义的数据库 链接</p >
 *
 * @author guoguo
 * @version 1.0, 2019-04-28 14:24 版本及创建日期
 * @since V1.0
 */

public class CustomerDataSourceConfiguration {


    private Map<String,CustomDataSource> datasource = new HashMap<String,CustomDataSource>();

    public Map<String, CustomDataSource> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, CustomDataSource> datasource) {
        this.datasource = datasource;
    }


}
