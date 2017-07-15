package utils;

import java.sql.*;

/**
 * Created by Yangjiali on 2017/3/26 0026.
 * Version 1.0
 */
public class DBHelper {
    public static final String url = "jdbc:mysql://127.0.0.1:3306/hetrec2011movielens?serverTimezone=UTC";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "131429";

    public Connection conn = null;
    public PreparedStatement pst = null;

    public DBHelper(String sql)
    {
        try {
            Class.forName(name);
            conn = DriverManager.getConnection(url,user,password);
            pst = conn.prepareStatement(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void close()
    {
        try {
            this.conn.close();
            this.pst.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
