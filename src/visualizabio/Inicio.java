package visualizabio;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import java.awt.Color;
import static java.awt.Component.CENTER_ALIGNMENT;
import java.awt.Font;
import java.awt.Image;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Inicio extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1;

    private static final String ACT_SELECTION = "selection";
    private static final String ACT_CAPTURE = "capture";
    private static final String ACT_STREAMING = "streaming";
    private static final String ACT_VERIFICATION = "verification";
    private static final String ACT_IDENTIFICATION = "identification";
    private static final String ACT_ENROLLMENT = "enrollment";
    private static final String ACT_EXIT = "exit";

    private JDialog m_dlgParent;
    private JTextArea m_textReader;

    private static ReaderCollection m_collection;
    private static Reader m_reader;
    private static Fmd enrollmentFMD;

    private Inicio() {

        BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(layout);
        setBackground(new java.awt.Color(31, 36, 40));
        Font fonteLbl = new Font(Font.MONOSPACED, Font.BOLD, 20);
        
        Font fonteBtn = new Font(Font.MONOSPACED, Font.BOLD, 18);

        Dimension dm = new Dimension(400, 45);
        
        //*	138, 138, 138*//

        Dimension dmbtn = new Dimension(150, 35);

        Icone icone = new Icone();

        Dimension lbl1 = new Dimension(400, 150);
        Dimension lbl = new Dimension(400, 40);
        JLabel lblReader = new JLabel(icone.icon2);

        JLabel lblReader2 = new JLabel("REGISTRO E PRESENÇA");
        lblReader.setFont(fonteLbl);
        lblReader2.setFont(fonteLbl);
        lblReader.setForeground(Color.YELLOW);
        lblReader2.setForeground(Color.YELLOW);

        
        add(Box.createVerticalStrut(25));

        JPanel painelLbl = new JPanel();
        painelLbl.setPreferredSize(lbl1);
        painelLbl.setAlignmentX(CENTER_ALIGNMENT);
        painelLbl.setBackground(new java.awt.Color(31, 36, 40));

        JPanel painelLbl2 = new JPanel();
        painelLbl2.setPreferredSize(lbl);
        painelLbl2.setAlignmentX(CENTER_ALIGNMENT);
        painelLbl2.setBackground(new java.awt.Color(31, 36, 40));

        painelLbl.add(lblReader);
        painelLbl2.add(lblReader2);

        add(painelLbl);
        add(Box.createVerticalStrut(10));
        add(painelLbl2);
        add(Box.createVerticalStrut(15));

        JPanel painelBtnCad = new JPanel();
        painelBtnCad.setPreferredSize(dm);
        painelBtnCad.setBackground(Color.DARK_GRAY);
        painelBtnCad.setAlignmentX(CENTER_ALIGNMENT);
        

        JPanel painelBtnVer = new JPanel();
        painelBtnVer.setPreferredSize(dm);
        painelBtnVer.setBackground(Color.DARK_GRAY);
        painelBtnVer.setAlignmentX(CENTER_ALIGNMENT);

        JPanel painelBtnEx = new JPanel();
        painelBtnEx.setPreferredSize(dm);
        painelBtnEx.setBackground(Color.DARK_GRAY);
        painelBtnEx.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnCadastrar = new JButton("REGISTRO");
        btnCadastrar.setBackground(new java.awt.Color(0, 102, 153));
        btnCadastrar.setActionCommand(ACT_ENROLLMENT);
        btnCadastrar.addActionListener(this);
        //btnCadastrar.setBackground(Color.LIGHT_GRAY);
        
        btnCadastrar.setFont(fonteBtn);
        btnCadastrar.setPreferredSize(dmbtn);
        
        painelBtnCad.add(btnCadastrar);

        JButton btnVerificacao = new JButton("PRESENÇA");
        btnVerificacao.setBackground(new java.awt.Color(51, 153, 51));
        btnVerificacao.setActionCommand(ACT_VERIFICATION);
        btnVerificacao.addActionListener(this);
        btnVerificacao.setFont(fonteBtn);
        btnVerificacao.setPreferredSize(dmbtn);
        painelBtnVer.add(btnVerificacao);

        JButton btnExit = new JButton("SAIR");
        btnExit.setBackground(new java.awt.Color(255, 102, 102));
        btnExit.setActionCommand(ACT_EXIT);
        btnExit.addActionListener(this);
        btnExit.setFont(fonteBtn);
        btnExit.setPreferredSize(dmbtn);
        painelBtnEx.add(btnExit);
        
        
        add(painelBtnCad);
        add(Box.createVerticalStrut(10));
        add(painelBtnVer);
        add(Box.createVerticalStrut(10));
        add(painelBtnEx);
        add(Box.createVerticalStrut(35));
        Font fonteCredito = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Dimension dm2 = new Dimension(400, 25);
        JPanel painelCreditos = new JPanel();
        painelCreditos.setPreferredSize(dm2);
        painelCreditos.setAlignmentX(CENTER_ALIGNMENT);
        painelCreditos.setBackground(Color.black);
        
        JLabel lblCreditos = new JLabel("© Copyright ADS/IFPI ");
        lblCreditos.setFont(fonteCredito);
        lblCreditos.setForeground(Color.white);
        painelCreditos.add(lblCreditos);

        add(painelCreditos);

        setOpaque(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals(ACT_VERIFICATION)) {

            try {
                this.m_collection = UareUGlobal.GetReaderCollection();
                m_collection.GetReaders();
            } catch (UareUException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "ERRO NA BUSCA POR LEITORES");
                return;
            }

            if (m_collection.size() == 0) {
                MessageBox.Warning("NENHUM LEITOR BIOMÉTRICO CONECTADO");
                return;
            }

            m_reader = m_collection.get(0);

            if (null == m_reader) {
                MessageBox.Warning("NENHUM LEITOR BIOMÉTRICO SELECIONADO");
            } else {
                Verificacao.Run(m_reader, this.enrollmentFMD);

            }
        } else if (e.getActionCommand().equals(ACT_ENROLLMENT)) {

            try {
                this.m_collection = UareUGlobal.GetReaderCollection();
                m_collection.GetReaders();
            } catch (UareUException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "ERRO NA BUSCA POR LEITORES");
                return;
            }

            if (m_collection.size() == 0) {
                MessageBox.Warning("NENHUM LEITOR BIOMÉTRICO CONECTADO");
                return;
            }

            m_reader = m_collection.get(0);

            if (null == m_reader) {
                MessageBox.Warning("NENHUM LEITOR BIOMÉTRICO SELECIONADO");
            } else {

                this.enrollmentFMD = Cadastro.Run(m_reader);
            }
        } else if (e.getActionCommand().equals(ACT_EXIT)) {
            System.exit(0);
            m_dlgParent.setVisible(false);
        }
    }

    private void doModal(JDialog dlgParent) {
        m_dlgParent = dlgParent;
        m_dlgParent.setContentPane(this);
        m_dlgParent.pack();
        m_dlgParent.setLocationRelativeTo(null);
        m_dlgParent.setVisible(true);
        m_dlgParent.dispose();
    }

    private static void createAndShowGUI() {

        Inicio paneContent = new Inicio();
        // initialize capture library by acquiring reader collection
        try {
            paneContent.m_collection = UareUGlobal.GetReaderCollection();
        } catch (UareUException e) {
            MessageBox.DpError("UareUGlobal.getReaderCollection()", e);
            return;
        }
        // run dialog
        JDialog dlg = new JDialog((JDialog) null, "SINFCOR 2018", true);
        Icone icone = new Icone();
        dlg.setIconImage(icone.imagemTitulo);
        paneContent.doModal(dlg);
        // release capture library by destroying reader collection
        try {
            UareUGlobal.DestroyReaderCollection();
        } catch (UareUException e) {
            MessageBox.DpError("UareUGlobal.destroyReaderCollection()", e);
        }
    }

    public static void main(String[] args) throws IOException {
        //SwingUtilities.invokeLater(new WebcamViewerExample());
        createAndShowGUI();
        try {
            m_collection = UareUGlobal.GetReaderCollection();
            m_collection.GetReaders();
        } catch (UareUException e1) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, "Error getting collection");
            return;
        }
        m_reader = m_collection.get(0);
    }

    private void add(Image imagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
