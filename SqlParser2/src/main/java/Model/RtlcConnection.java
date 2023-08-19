package Model;

import oracle.jdbc.OracleConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class RtlcConnection {
    private static Connection connection;

    public static Connection getConnection(){
        if (connection==null){
            makeOracleConnection();
        }
        return connection;
    }

    private static void makeOracleConnection() {
        //String connectionUrl = "jdbc:oracle:thin:@ng-inf-etl-5.dh.rt.ru:1521/ETLINUAT";//"jdbc:oracle:thin:@vm-dm-lab01.dh.rt.ru:1521/SDEV_ADHOC";
        String connectionUrl = "jdbc:oracle:thin:@vip-ora-etlprod.dh.rt.ru:1521/ETLPROD";//"jdbc:oracle:thin:@vm-dm-lab01.dh.rt.ru:1521/SDEV_ADHOC";
        try {
            Class.forName("oracle.jdbc.OracleDriver").newInstance();
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            Connection connectionOra = (OracleConnection) DriverManager.getConnection(connectionUrl
                    ,"VADIM.PETROV"
                    ,"");
            connectionOra.setAutoCommit(false);
            connection = connectionOra;
            //connectionOra.createStatement().executeQuery("ALTER SESSION SET NLS_NUMERIC_CHARACTERS = '. '");
            System.out.println("Oracle connected.." + !connectionOra.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }
    }



}
