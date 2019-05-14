/**
 * @ClassName: MultiDataSouceTransactionFactory
 * @Description:
 * @author guoguo
 * @date 2019/5/10
 * @version V1.0
 * @since JDK 1.8
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ---------------------------------------------------------
 * 2019/5/10      guoguo          v1.0.0
 */

package com.free.plaform.dynamic;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

import javax.sql.DataSource;

/**
 * <p></p >
 *
 * @author guoguo
 * @version 1.0, 2019-05-10 16:21 版本及创建日期
 * @since V1.0
 */
public class MultiDataSouceTransactionFactory extends SpringManagedTransactionFactory {

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new MultiDataSourceTransaction(dataSource);
    }
}
