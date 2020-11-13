package com.hxyc.canal.util;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

/**
 * @ClassName CanalUtil
 * @Description
 * @Author admin
 * @Date 2020/11/10 14:19
 **/
public class CanalUtils {

    //canal server 地址
    public static String SERVER_ADDRESS;

    //canal server 端口
    private static Integer PORT;

    private static String DESTINATION = "example";

    //canal server 的用户名
    private static String USERNAME = "";

    //canal server 的密码
    private static String PASSWORD = "";

    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(DruidUtils.getDataSource());

    static {
        try {
            Properties pro = new Properties();
            // 获取src路径下的文件的方式 --->ClassLoader
            InputStream is = JDBCUtils.class.getClassLoader().getResourceAsStream("application.properties");
            pro.load(is);
            SERVER_ADDRESS = pro.getProperty("canal.server.address");
            PORT = Integer.parseInt(pro.getProperty("canal.server.port"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 同步数据方法
     */
    public static void executeSyncSql(){
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(SERVER_ADDRESS, PORT), DESTINATION, USERNAME, PASSWORD);
        canalConnector.connect();
        //订阅 所有库所有表
        canalConnector.subscribe(".*\\..*");
        canalConnector.rollback();
        //死循环
        for(;;){
            // 获取指定数量的数据
            Message message = canalConnector.getWithoutAck(100);
            long batchId = message.getId();
            if(batchId != -1){
                try {
                    dataHandle(message.getEntries());
                    canalConnector.ack(batchId); // 提交确认
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    //处理失败 回滚数据
                    //canalConnector.rollback(batchId);
                }
            }

        }
    }

    /**
     * 数据处理
     *
     * @param entrys
     */
    private static void dataHandle(List<CanalEntry.Entry> entrys) throws InvalidProtocolBufferException {
        for (CanalEntry.Entry entry : entrys) {
            if (CanalEntry.EntryType.ROWDATA == entry.getEntryType()) {
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                CanalEntry.EventType eventType = rowChange.getEventType();
                if (eventType == CanalEntry.EventType.DELETE) {
                    saveDeleteSql(entry);
                } else if (eventType == CanalEntry.EventType.UPDATE) {
                    saveUpdateSql(entry);
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    saveInsertSql(entry);
                }
            }
        }
    }

    /**
     * 保存更新语句
     *
     * @param entry
     */
    private static void saveUpdateSql(CanalEntry.Entry entry) {
        try {
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
            for (CanalEntry.RowData rowData : rowDatasList) {
                List<CanalEntry.Column> newColumnList = rowData.getAfterColumnsList();
                StringBuffer sql = new StringBuffer("update " + entry.getHeader().getSchemaName() + "." + entry.getHeader().getTableName() + " set ");
                for (int i = 0; i < newColumnList.size(); i++) {
                    sql.append(" " + newColumnList.get(i).getName()
                            + " = '" + newColumnList.get(i).getValue() + "'");
                    if (i != newColumnList.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(" where ");
                List<CanalEntry.Column> oldColumnList = rowData.getBeforeColumnsList();
                for (CanalEntry.Column column : oldColumnList) {
                    if (column.getIsKey()) {
                        //暂时只支持单一主键
                        sql.append(column.getName() + "=" + column.getValue());
                        break;
                    }
                }
                jdbcTemplate.update(sql.toString());

            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存删除语句 rowData.getAfterColumnsList()
     *
     */
    private static void saveDeleteSql(CanalEntry.Entry entry) {
        try {
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
            for (CanalEntry.RowData rowData : rowDatasList) {
                List<CanalEntry.Column> columnList = rowData.getBeforeColumnsList();
                StringBuffer sql = new StringBuffer("delete from " + entry.getHeader().getSchemaName() + "." + entry.getHeader().getTableName() + " where ");
                for (CanalEntry.Column column : columnList) {
                    if (column.getIsKey()) {
                        //暂时只支持单一主键
                        sql.append(column.getName() + "=" + column.getValue());
                        break;
                    }
                }
                jdbcTemplate.update(sql.toString());

            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存插入语句
     *
     * @param entry
     */
    private static void saveInsertSql(CanalEntry.Entry entry) {
        try {
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
            for (CanalEntry.RowData rowData : rowDatasList) {
                List<CanalEntry.Column> columnList = rowData.getAfterColumnsList();
                StringBuffer sql = new StringBuffer("insert into " + entry.getHeader().getSchemaName() + "." + entry.getHeader().getTableName() + " (");
                for (int i = 0; i < columnList.size(); i++) {
                    if(StringUtils.isNotBlank(columnList.get(i).getValue())){
                        sql.append(columnList.get(i).getName());
                        if (i != columnList.size() - 1) {
                            sql.append(",");
                        }
                    }

                }
                //处理“)”前有“，”
                if(sql.toString().endsWith(",")){
                    sql.deleteCharAt(sql.length()-1);
                }
                sql.append(") VALUES (");
                for (int i = 0; i < columnList.size(); i++) {
                    if(StringUtils.isNotBlank(columnList.get(i).getValue())){
                        sql.append("'" + columnList.get(i).getValue() + "'");
                        if (i != columnList.size() - 1) {
                            sql.append(",");
                        }
                    }

                }
                //处理“)”前有“，”
                if(sql.toString().endsWith(",")){
                    sql.deleteCharAt(sql.length()-1);
                }
                sql.append(")");
                jdbcTemplate.update(sql.toString());

            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
