package visualizabio;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jdk.nashorn.internal.objects.annotations.Where;

public class FingerDB {

    private static final String tableName = "participante";
    private static final String userColumn = "cpf";
    private static final String userColumn2 = "nome";
    private static final String print1Column = "biometria";

    private String URL = "jdbc:mysql://localhost:3306/";
    private String host;
    private String database;
    private String userName;
    private String pwd;
    private java.sql.Connection connection = null;
    private String preppedStmtInsert = null;
    private String preppedStmtUpdate = null;
    private String preppedStmtUpdatePres = null;
    private String preppedStmtUpdateLimite = null;

    public class Record {

        String userID;
        byte[] fmdBinary;
        int presenca;
        int identificacao;
        String data;

        Record(String ID, byte[] fmd, int quant_pres, int ident, String hora) {
            userID = ID;
            fmdBinary = fmd;
            presenca = quant_pres;
            identificacao = ident;
            data = hora;
        }
    }

    public FingerDB() {
        database = "bdsinfcor";
        userName = "bdsinfcor";
        pwd = "anjofelipe";
        host = "bdsinfcor.mysql.dbaas.com.br";

        URL = "jdbc:mysql://" + host + "/";
        preppedStmtUpdate = "UPDATE participante SET biometria = ?, horapresenca = ?, quantpresenca = 1  WHERE cpf = ?";
        preppedStmtUpdatePres = "UPDATE participante SET quantpresenca = ?, horapresenca = ? WHERE idparticipante = ?";

    }

    @Override
    public void finalize() {
        try {
            connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void Open() throws SQLException {
        connection = DriverManager.getConnection(URL + database, userName, pwd);
    }

    public void Close() throws SQLException {
        connection.close();
    }

    public boolean UserExists(String userID) throws SQLException {
        String sqlStmt = "Select " + userColumn + " from " + tableName
                + " WHERE " + userColumn + "='" + userID + "'";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sqlStmt);
        return rs.next();
    }

    public boolean BioExists(String userID) throws SQLException {
        String sqlStmt = "Select * from " + tableName
                + " WHERE " + userColumn + "='" + userID + "'";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sqlStmt);
        boolean existe = false;
        while (rs.next()) {
            if (rs.getBytes(print1Column) != null || rs.getInt("pagamento") == 0) {
                existe = true;
            }
        }
        return existe;
    }

    public String BuscaNome(String userID) throws SQLException {
        String sqlStmt = "Select * from " + tableName
                + " WHERE " + userColumn + "='" + userID + "'";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sqlStmt);
        while (rs.next()) {
            return rs.getString("nome");
        }

        return null;
    }

    public void Insert(String userID, byte[] print1) throws SQLException {
        Calendar hoje = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/2018 - HH:mm:ss");
        Date horaAtual = new Date();

        horaAtual = hoje.getTime();
        String horaAtualC = sdf.format(horaAtual);

        java.sql.PreparedStatement pst = connection.prepareStatement(preppedStmtUpdate);
        pst.setBytes(1, print1);
        pst.setString(2, horaAtualC);
        pst.setString(3, userID);
        pst.execute();
    }

    public void InserePresenca(int quantPresenca, int id) throws SQLException {
        java.sql.PreparedStatement pst = connection.prepareStatement(preppedStmtUpdatePres);
        pst.setInt(1, quantPresenca);
        Calendar c = Calendar.getInstance();
        Date data = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        //System.out.println(sdf.format(data));
        pst.setString(2, sdf.format(data));
        pst.setInt(3, id);
        pst.execute();
    }

    public List<Record> GetAllFPData() throws SQLException {
        List<Record> listUsers = new ArrayList<Record>();
        String sqlStmt = "Select * from " + tableName;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sqlStmt);
        while (rs.next()) {
            if (rs.getBytes(print1Column) != null) {
                listUsers.add(new Record(rs.getString(userColumn2), rs
                        .getBytes(print1Column),
                        rs.getInt("quantpresenca"), rs.getInt("idparticipante"), rs.getString("horapresenca")));
            }
        }
        return listUsers;
    }

    public String GetConnectionString() {
        return URL + " User: " + this.userName;
    }

    public String GetExpectedTableSchema() {
        return "Table: " + tableName + " PK(VARCHAR(32)): " + userColumn
                + "VARBINARY(4000): " + print1Column;
    }
}
