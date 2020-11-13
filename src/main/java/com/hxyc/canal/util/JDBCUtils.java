package com.hxyc.canal.util;

import org.springframework.core.io.ClassPathResource;

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
 * @Description JDBC工具类
 * @Author admin
 * @Date 2020/11/5 15:07
 **/
public class JDBCUtils {

    private static String url;
    private static String driver;
    private static String username;
    private static String password;

    static {
        try {
            Properties pro = new Properties();
            // 获取src路径下的文件的方式 --->ClassLoader
            InputStream is = JDBCUtils.class.getClassLoader().getResourceAsStream("application.properties");
            pro.load(is);
            username = pro.getProperty("spring.datasource.username");
            password = pro.getProperty("spring.datasource.password");
            driver = pro.getProperty("spring.datasource.driver-class-name");
            url = pro.getProperty("spring.datasource.url");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url,username,password);
    }

    public static void close(Statement stmt,Connection conn){
        if (stmt != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
