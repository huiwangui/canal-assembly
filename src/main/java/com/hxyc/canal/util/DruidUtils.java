package com.hxyc.canal.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @ClassName JDBCUtils
 * @Description Druid连接池的工具类
 * @Author admin
 * @Date 2020/11/5 15:07
 **/
public class DruidUtils {

    private static DataSource ds;

    static {
        try {
            //1、加载配置文件
            Properties pro = new Properties();
            // 获取src路径下的文件的方式 --->ClassLoader
            InputStream is = DruidUtils.class.getClassLoader().getResourceAsStream("druid.properties");
            pro.load(is);
            //2、获取DataSource
            ds = DruidDataSourceFactory.createDataSource(pro);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取连接
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /**
     * 释放资源
     * @param stmt
     * @param conn
     */
    public static void close(Statement stmt,Connection conn){
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (conn != null) {
            try {
                conn.close();//归还连接到连接池
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 获取连接池方法
     * @return
     */
    public static DataSource getDataSource(){
        return ds;
    }

    public static void main(String[] args) {
        // JdbcTemplate会自动获取连接及归还连接
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DruidUtils.getDataSource());
        String sql ="update t_user set salt =1 where user_id=66";
        int update = jdbcTemplate.update(sql);//返回影响的行数
    }


}
