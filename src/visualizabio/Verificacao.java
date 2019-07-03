package visualizabio;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Engine.Candidate;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Fmd.Format;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

public class Verificacao extends JPanel implements ActionListener {

    private static final long serialVersionUID = 6;
    private static final String ACT_BACK = "back";
    private static final String ACT_LOAD = "load";
    private static final String ACT_LOAD_FROM_DB = "load_from_db";

    private CaptureThread m_capture;
    private Reader m_reader;
    private Fmd[] m_fmds;
    private JDialog m_dlgParent;
    public FingerDB db = new FingerDB();
    public List<FingerDB.Record> m_listOfRecords = new ArrayList<FingerDB.Record>();
    public List<Fmd> m_fmdList = new ArrayList<Fmd>();
    public Fmd[] m_fmdArray = null;
    public String nome;
    private JLabel m_text;
    public Fmd m_enrollmentFmd;
    private ImagePanel m_imagePanel;
    private int presenca;
    private final String m_strPrompt1 = "<html><div align=\"center\"><div color=yellow>COLOQUE O DEDO NO LEITOR\n\n";
    private static ReaderCollection m_collection;

    private Verificacao(Reader reader) {

        try {
            m_collection = UareUGlobal.GetReaderCollection();
            m_collection.GetReaders();

        } catch (UareUException ex) {
            Logger.getLogger(Verificacao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        m_reader = reader;

        m_fmds = new Fmd[2]; // two FMDs to perform comparison

        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);
        setBackground(new java.awt.Color(31, 36, 40));
        //setPreferredSize(new Dimension(400, 400));
        add(Box.createVerticalStrut(20));

        Font fonteLbl = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        Font fonteTxt = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        Font fonteBtn = new Font(Font.DIALOG, Font.PLAIN, 15);

        Dimension lbl = new Dimension(400, 30);
        Dimension img = new Dimension(400, 340);
        Dimension txt = new Dimension(400, 40);
        Dimension dmbtn = new Dimension(90, 30);
        Dimension dm = new Dimension(400, 40);

        JLabel lblReader = new JLabel("<html><div color=\"yellow\">REGISTRO DE PRESENÇA");

        lblReader.setForeground(Color.WHITE);
        lblReader.setBackground(Color.WHITE);
        lblReader.setFont(fonteLbl);


        JPanel painelLbl = new JPanel();
        painelLbl.setPreferredSize(lbl);
        painelLbl.setAlignmentX(CENTER_ALIGNMENT);
        painelLbl.setBackground(Color.darkGray);
        painelLbl.add(lblReader);
        add(painelLbl);

        add(Box.createVerticalStrut(20));

        JPanel painelImg = new JPanel();
        painelImg.setPreferredSize(img);
        painelImg.setBackground(new java.awt.Color(31, 36, 40));
        m_imagePanel = new ImagePanel();
        m_imagePanel.setPreferredSize(new Dimension(290, 318));
        painelImg.add(m_imagePanel);
        add(painelImg);
        add(Box.createVerticalStrut(20));

        JPanel painelTextArea = new JPanel();
        painelTextArea.setPreferredSize(txt);
        painelTextArea.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        m_text = new JLabel();
        m_text.setFont(fonteTxt);
        painelTextArea.setBackground(Color.darkGray);
        painelTextArea.add(m_text);
        add(painelTextArea);
        //paneReader.setSize(100, 300);

        add(Box.createVerticalStrut(20));

        JPanel painelBtnExt = new JPanel();
        painelBtnExt.setPreferredSize(dm);
        painelBtnExt.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnBack = new JButton("SAIR");
        btnBack.setActionCommand(ACT_BACK);
        btnBack.addActionListener(this);
        btnBack.setFont(fonteBtn);
        btnBack.setPreferredSize(dmbtn);
        btnBack.setBackground(new java.awt.Color(255, 102, 102));
        btnBack.setForeground(Color.white);
        painelBtnExt.setBackground(Color.darkGray);
        painelBtnExt.add(btnBack);

        add(painelBtnExt);
        //add(Box.createVerticalStrut(20));
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
        if (e.getActionCommand().equals(ACT_BACK)) {
            // cancel capture
            StopCaptureThread();
            m_dlgParent.setVisible(false);

        } else if (e.getActionCommand().equals(ACT_LOAD)) {
            byte[] fmdFromFile = loadDataFile();
            try {
                this.m_enrollmentFmd = UareUGlobal.GetImporter().ImportFmd(fmdFromFile, Format.DP_REG_FEATURES, Format.DP_REG_FEATURES);
//                this.m_loadFromDB.setEnabled(false);
                this.m_listOfRecords.clear();
            } catch (UareUException e1) {
                // TODO Auto-generated catch block
                MessageBox.DpError("ImportFmd", e1);
            }
        }
        if (e.getActionCommand().equals(CaptureThread.ACT_CAPTURE)) {
            // process result

            CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent) e;
            if (evt.capture_result.image != null) {
                if (ProcessCaptureResult(evt)) {
                    // restart capture thread
                    WaitForCaptureThread();
                    StartCaptureThread();

                } else {
                    // destroy dialog
                    m_dlgParent.setVisible(false);
                }
            }

        }
    }

    private void LoadDB() {
        try {
            db.Open();
            this.m_listOfRecords = db.GetAllFPData();
            for (FingerDB.Record record : this.m_listOfRecords) {
                Fmd fmd;
                fmd = UareUGlobal.GetImporter().ImportFmd(record.fmdBinary, com.digitalpersona.uareu.Fmd.Format.DP_REG_FEATURES, com.digitalpersona.uareu.Fmd.Format.DP_REG_FEATURES);
                this.m_fmdList.add(fmd);

            }
            m_fmdArray = new Fmd[this.m_fmdList.size()];

            this.m_fmdList.toArray(m_fmdArray);
            System.out.println("Busca feita no BD");
            this.m_enrollmentFmd = null;

        } catch (SQLException ex) {
            Logger.getLogger(Verificacao.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (UareUException ex) {
            Logger.getLogger(Verificacao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void StartCaptureThread() {
        m_capture = new CaptureThread(m_reader, false, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
        m_capture.start(this);

    }

    private void StopCaptureThread() {
        if (null != m_capture) {
            m_capture.cancel();
        }
    }

    private void WaitForCaptureThread() {
        if (null != m_capture) {
            m_capture.join(1000);
        }
    }

    private boolean ProcessCaptureResult(CaptureThread.CaptureEvent evt) {
        boolean bCanceled = false;

        if (this.m_enrollmentFmd == null && this.m_listOfRecords.size() == 0) {
            MessageBox.Warning("Não foi possivel verificar a biometria.");
            return !bCanceled;
        }

        if (null != evt.capture_result) {
            if (null != evt.capture_result.image && Reader.CaptureQuality.GOOD == evt.capture_result.quality) {
                // extract features
                Engine engine = UareUGlobal.GetEngine();

                try {
                    m_imagePanel.showImage(evt.capture_result.image);
                    Fmd fmd = engine.CreateFmd(evt.capture_result.image,
                            Fmd.Format.DP_VER_FEATURES);
                    m_fmds[0] = fmd;

                    int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000;
                    Candidate[] matches = engine.Identify(m_fmds[0], 0, m_fmdArray, target_falsematch_rate, 1);
                    if (matches.length == 1) {

                        presenca = this.m_listOfRecords.get(matches[0].fmd_index).presenca;
                        presenca++;
                        String data = this.m_listOfRecords.get(matches[0].fmd_index).data;

                        int id = this.m_listOfRecords.get(matches[0].fmd_index).identificacao;

                        String str = ("" + this.m_listOfRecords.get(matches[0].fmd_index).userID + "\n\n");

                        if (VerificaHora(data)) {
                            try {
                                db.InserePresenca(presenca, id);
                                m_text.setText("<html><div align=\"center\"><div color=\"green\">" + str + "</div></br>PRESENÇA REGISTRADA");
                                LoadDB();

                            } catch (SQLException ex) {
                                Logger.getLogger(Verificacao.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "PRESENÇA JÁ REGISTRADA", " ", JOptionPane.WARNING_MESSAGE);
                            m_text.setBackground(Color.white);
                            LoadDB();
                        }

                    } else {
                        String str2 = ("<html><div color=\"red\">NÃO IDENTIFICADO");

                        m_text.setText(str2);

                    }
                    if (!TesteHora()) {
                        JOptionPane.showMessageDialog(null, "FORA DO HORÁRIO DE REGISTRO",
                                " ", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (UareUException e) {
                    MessageBox.DpError("Engine.CreateFmd()", e);
                }
            } else {
                // the loop continues
//                m_text.append(m_strPrompt2);
            }
        } else if (Reader.CaptureQuality.CANCELED == evt.capture_result.quality) {
            // capture or streaming was canceled, just quit
            bCanceled = true;
        } else if (null != evt.exception) {
            // exception during capture
            MessageBox.DpError("Capture", evt.exception);
            bCanceled = true;
        } else if (null != evt.reader_status) {
            // reader failure
            MessageBox.BadStatus(evt.reader_status);
            bCanceled = true;
        } else {
            // bad quality
            MessageBox.BadQuality(evt.capture_result.quality);
        }

        return !bCanceled;
    }

    private boolean VerificaHora(String data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/2018 - HH:mm:ss");

        Horas horas = new Horas();
        horas.InicializaHoras();
        Date ultimaPresenca = new Date();
        try {
            ultimaPresenca = sdf.parse(data);

        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        boolean cond1 = (horas.horaAtualC.compareTo(horas.horaManhaInicialC) >= 0 && horas.horaAtualC.compareTo(horas.horaManhaFinalC) <= 0);
        boolean cond2 = (horas.horaAtualC.compareTo(horas.horaTardeInicialC) >= 0 && horas.horaAtualC.compareTo(horas.horaTardeFinalC) <= 0);
        boolean cond3 = (horas.horaAtualC.compareTo(horas.horaNoiteInicialC) >= 0 && horas.horaAtualC.compareTo(horas.horaNoiteFinalC) <= 0);

        if (cond1) {
            System.out.println("teste 01");
            return !(ultimaPresenca.compareTo(horas.horaManhaInicialC) >= 0 && ultimaPresenca.compareTo(horas.horaManhaFinalC) <= 0);
        } else if (cond2) {
            System.out.println("teste 02");
            return !(ultimaPresenca.compareTo(horas.horaTardeInicialC) >= 0 && ultimaPresenca.compareTo(horas.horaTardeFinalC) <= 0);
        } else {
            System.out.println("teste 03");
            return !(ultimaPresenca.compareTo(horas.horaNoiteInicialC) >= 0 && ultimaPresenca.compareTo(horas.horaNoiteFinalC) <= 0);
        }
    }

    private void doModal(JDialog dlgParent) {
        // open reader
        try {
            m_reader.Open(Reader.Priority.COOPERATIVE);
        } catch (UareUException e) {
            MessageBox.DpError("Reader.Open()", e);
        }

        // start capture thread
        StartCaptureThread();

        // put initial prompt on the screen
        m_text.setText(m_strPrompt1);

        // bring up modal dialog
        m_dlgParent = dlgParent;
        m_dlgParent.setContentPane(this);
        m_dlgParent.pack();
        m_dlgParent.setLocationRelativeTo(null);
        m_dlgParent.toFront();
        m_dlgParent.setVisible(true);
        m_dlgParent.dispose();

        // cancel capture
        StopCaptureThread();

        // wait for capture thread to finish
        WaitForCaptureThread();

        // close reader
        try {
            m_reader.Close();
        } catch (UareUException e) {
            MessageBox.DpError("Reader.Close()", e);
        }

    }

    private byte[] loadDataFile() {
        // TODO Auto-generated method stub
        JFileChooser fc = new JFileChooser(new File("test"));

        fc.showOpenDialog(this);
        if (fc.getSelectedFile() != null) {
            InputStream input = null;
            try {
                input = new BufferedInputStream(new FileInputStream(fc.getSelectedFile()));
                byte[] data = new byte[input.available()];
                input.read(data, 0, input.available());
                input.close();
                return data;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "Erro no salvamento do arquivo.");
            }
        }
        return null;
    }

    private static boolean TesteHora() {
        Horas horas = new Horas();
        horas.InicializaHoras();

        boolean cond1 = (horas.horaAtualC.compareTo(horas.horaManhaInicialC) >= 0 && horas.horaAtualC.compareTo(horas.horaManhaFinalC) <= 0);
        boolean cond2 = (horas.horaAtualC.compareTo(horas.horaTardeInicialC) >= 0 && horas.horaAtualC.compareTo(horas.horaTardeFinalC) <= 0);
        boolean cond3 = (horas.horaAtualC.compareTo(horas.horaNoiteInicialC) >= 0 && horas.horaAtualC.compareTo(horas.horaNoiteFinalC) <= 0);
        return (cond1 || cond2 || cond3);
    }

    public static void Run(Reader reader, Fmd _enrolledFmd) {
        if (TesteHora()) {
            JDialog dlg = new JDialog((JDialog) null, "VERIFICAÇÃO", true);
            Icone icone = new Icone();
            dlg.setIconImage(icone.imagemTitulo);
            Verificacao verification = new Verificacao(reader);
            verification.LoadDB();
            verification.m_enrollmentFmd = _enrolledFmd;
            verification.doModal(dlg);
        } else {
            JOptionPane.showMessageDialog(null, "FORA DO HORÁRIO DE REGISTRO",
                    " ", JOptionPane.WARNING_MESSAGE);
        }

    }
}
