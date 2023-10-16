package pine.log.monitor.engine.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import pine.log.monitor.LogGlobalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

@Component
public class BaseOptional {

    @Autowired(required = false)
    private DruidDataSource dataSource;

    /**
     *  可关闭的执行语法块
     * @param sql
     * @param statementConsumer
     * @return
     * @throws SQLException
     */
    public void execute(String sql, Consumer<PreparedStatement> statementConsumer){
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            statementConsumer.accept(preparedStatement);
        }catch (SQLException e){
            throw new LogGlobalException(e);
        }
        finally {
            if (preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
