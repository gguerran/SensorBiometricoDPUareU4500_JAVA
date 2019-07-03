package visualizabio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.sun.media.rtsp.protocol.PauseMessage;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingConstants;

public class Cadastro extends JPanel implements ActionListener {

    FingerDB db = new FingerDB();

    public class EnrollmentThread extends Thread implements
            Engine.EnrollmentCallback {

        public static final String ACT_PROMPT = "enrollment_prompt";
        public static final String ACT_CAPTURE = "enrollment_capture";
        public static final String ACT_FEATURES = "enrollment_features";
        public static final String ACT_DONE = "enrollment_done";
        public static final String ACT_CANCELED = "enrollment_canceled";
        public static final String ACT_SAVE = "save";

        public class EnrollmentEvent extends ActionEvent {

            private static final long serialVersionUID = 102;

            public Reader.CaptureResult capture_result;
            public Reader.Status reader_status;
            public UareUException exception;
            public Fmd enrollment_fmd;

            public EnrollmentEvent(Object source, String action, Fmd fmd,
                    Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
                super(source, ActionEvent.ACTION_PERFORMED, action);
                capture_result = cr;
                reader_status = st;
                exception = ex;
                enrollment_fmd = fmd;
            }
        }

        private final Reader m_reader;
        private CaptureThread m_capture;
        private final ActionListener m_listener;
        private boolean m_bCancel;

        protected EnrollmentThread(Reader reader, ActionListener listener) {
            m_reader = reader;
            m_listener = listener;
        }

        @Override
        public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) {
            Engine.PreEnrollmentFmd prefmd = null;

            while (null == prefmd && !m_bCancel) {
                // start capture thread
                m_capture = new CaptureThread(m_reader, false,
                        Fid.Format.ISO_19794_4_2005,
                        Reader.ImageProcessing.IMG_PROC_DEFAULT);
                m_capture.start(null);

                // prompt for finger
                SendToListener(ACT_PROMPT, null, null, null, null);

                // wait till done
                m_capture.join(0);

                // check result
                CaptureThread.CaptureEvent evt = m_capture
                        .getLastCaptureEvent();
                if (null != evt.capture_result) {
                    if (Reader.CaptureQuality.CANCELED == evt.capture_result.quality) {
                        // capture canceled, return null
                        break;
                    } else if (null != evt.capture_result.image
                            && Reader.CaptureQuality.GOOD == evt.capture_result.quality) {
                        // Send image
                        SendToListener(ACT_CAPTURE, null, evt.capture_result,
                                null, null);

                        // acquire engine
                        Engine engine = UareUGlobal.GetEngine();

                        try {
                            // extract features

                            Fmd fmd = engine.CreateFmd(
                                    evt.capture_result.image,
                                    Fmd.Format.DP_PRE_REG_FEATURES);

                            // return prefmd
                            prefmd = new Engine.PreEnrollmentFmd();
                            prefmd.fmd = fmd;
                            prefmd.view_index = 0;

                            // send success
                            SendToListener(ACT_FEATURES, null, null, null, null);
                        } catch (UareUException e) {
                            // send extraction error
                            SendToListener(ACT_FEATURES, null, null, null, e);
                        }
                    } else {
                        // send quality result
                        SendToListener(ACT_CAPTURE, null, evt.capture_result,
                                evt.reader_status, evt.exception);
                    }
                } else {
                    // send capture error
                    SendToListener(ACT_CAPTURE, null, evt.capture_result,
                            evt.reader_status, evt.exception);
                }
            }

            return prefmd;
        }

        public void cancel() {
            m_bCancel = true;
            if (null != m_capture) {
                m_capture.cancel();
            }
        }

        private void SendToListener(String action, Fmd fmd,
                Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
            if (null == m_listener || null == action || action.equals("")) {
                return;
            }

            final EnrollmentEvent evt = new EnrollmentEvent(this, action, fmd,
                    cr, st, ex);

            // invoke listener on EDT thread
            try {
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        m_listener.actionPerformed(evt);
                    }
                });
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // acquire engine
            Engine engine = UareUGlobal.GetEngine();

            try {
                m_bCancel = false;
                while (!m_bCancel) {
                    // run enrollment
                    Fmd fmd = engine.CreateEnrollmentFmd(
                            Fmd.Format.DP_REG_FEATURES, this);

                    // send result
                    if (null != fmd) {

                        SendToListener(ACT_DONE, fmd, null, null, null);
                    } else {
                        SendToListener(ACT_CANCELED, null, null, null, null);
                        break;
                    }
                }
            } catch (UareUException e) {
                JOptionPane.showMessageDialog(null,
                        "Erro durante o carregamento dos dados");
                SendToListener(ACT_DONE, null, null, null, e);
            }
        }
    }

    private static final long serialVersionUID = 6;
    private static final String ACT_BACK = "back";
    private static final String ACT_SAVE = "save";
    private static final String ACT_SAVE_DB = "save2db";

    public com.digitalpersona.uareu.Fmd enrollmentFMD;
    private final EnrollmentThread m_enrollment;
    private final Reader m_reader;
    private JDialog m_dlgParent;
    private final JLabel m_text;
    // private final JLabel infoUsername_text;
    private final JLabel username_text;
    private final JLabel username_texta;
    private boolean m_bJustStarted;
    private final JButton m_save2DB;

    private Cadastro(Reader reader, String cpf) {

        m_reader = reader;
        m_bJustStarted = true;
        m_enrollment = new EnrollmentThread(m_reader, this);
        setBackground(new java.awt.Color(31, 36, 40));

        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);

        Font fonteLbl = new Font(Font.SANS_SERIF, Font.BOLD, 28);
        Font fonteCpf = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        Font fonteTxt = new Font(Font.SANS_SERIF, Font.BOLD, 17);
        Font fonteBtn = new Font(Font.DIALOG, Font.PLAIN, 15);
        Font fonteNome = new Font(Font.DIALOG, Font.BOLD, 16);

        JLabel lblReader = new JLabel("CADASTRO INICIADO");
        lblReader.setForeground(Color.blue);
        lblReader.setBackground(Color.WHITE);
        lblReader.setFont(fonteLbl);

        Dimension lbl = new Dimension(400, 50);
        Dimension lbla = new Dimension(380, 80);
        Dimension txt = new Dimension(400, 100);
        Dimension dmcpf = new Dimension(400, 40);
        Dimension dmbtn = new Dimension(90, 30);
        Dimension dm = new Dimension(400, 45);

        JPanel painelLbl = new JPanel();
        painelLbl.setPreferredSize(lbl);
        painelLbl.setAlignmentX(CENTER_ALIGNMENT);
        painelLbl.setBackground(Color.darkGray);

        painelLbl.add(lblReader);
        add(Box.createVerticalStrut(20));
        add(painelLbl);
        add(Box.createVerticalStrut(20));

        JPanel painelTextArea = new JPanel();
        painelTextArea.setPreferredSize(txt);
        painelTextArea.setAlignmentX(CENTER_ALIGNMENT);
        painelTextArea.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 10));

        JPanel painelCpf = new JPanel();
        painelCpf.setPreferredSize(dmcpf);
        painelCpf.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        m_text = new JLabel();
        m_text.setFont(fonteTxt);
        m_text.setForeground(Color.yellow);
        m_text.setPreferredSize(lbla);
        m_text.setBorder(null);
        m_text.setHorizontalAlignment(SwingConstants.CENTER);
        painelTextArea.add(m_text);
        painelTextArea.setBackground(Color.darkGray);
        add(painelTextArea);

        add(Box.createVerticalStrut(20));

        username_text = new JLabel();
        username_texta = new JLabel();
        username_texta.setText(cpf);

        username_text.setFont(fonteNome);
        try {
            //db.Close();
            db.Open();
            username_text.setText("<html><div align=\"center\"><div color=#d9f2d9>"+db.BuscaNome(cpf));
        } catch (SQLException ex) {
            Logger.getLogger(Cadastro.class.getName()).log(Level.SEVERE, null, ex);
        }

        //JScrollPane paneReader2 = new JScrollPane(username_text);
        painelCpf.add(username_text);
        painelCpf.setBackground(Color.darkGray);
        add(painelCpf);
        add(Box.createVerticalStrut(10));

        JPanel painelBtnCad = new JPanel();
        painelBtnCad.setPreferredSize(dm);
        painelBtnCad.setBackground(Color.darkGray);
        painelBtnCad.setAlignmentX(CENTER_ALIGNMENT);

        JPanel painelBtnEx = new JPanel();

        painelBtnEx.setPreferredSize(dm);
        painelBtnEx.setAlignmentX(CENTER_ALIGNMENT);

        m_save2DB = new JButton("SALVAR");
        m_save2DB.setBackground(new java.awt.Color(51, 153, 51));
        m_save2DB.setActionCommand(ACT_SAVE_DB);
        m_save2DB.addActionListener(this);
        m_save2DB.setEnabled(false);
        m_save2DB.setPreferredSize(dmbtn);
        m_save2DB.setFont(fonteBtn);
        painelBtnCad.add(m_save2DB);
        painelBtnCad.setBackground(Color.darkGray);
        add(painelBtnCad);
        add(Box.createVerticalStrut(10));

        JButton btnBack = new JButton("SAIR");
        btnBack.setBackground(new java.awt.Color(255, 102, 102));
        btnBack.setActionCommand(ACT_BACK);
        btnBack.addActionListener(this);
        btnBack.setPreferredSize(dmbtn);
        btnBack.setFont(fonteBtn);
        painelBtnEx.add(btnBack);
        painelBtnEx.setBackground(Color.darkGray);
        add(painelBtnEx);
        add(Box.createVerticalStrut(30));
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
            // destroy dialog to cancel enrollment
            m_dlgParent.setVisible(false);

            return;
        } else if (e.getActionCommand().equals(ACT_SAVE_DB)) {

            try {
                db.Open();
                if (this.username_texta.getText().isEmpty() == false) {
                    // Check if user already exists
                    if ((db.UserExists(this.username_texta.getText()) == true)) {
                        // Save user to database along with fingerprint minutiae
                        db.Insert(this.username_texta.getText(),
                                this.enrollmentFMD.getData());
                        System.out.println("Salvo");
                        m_dlgParent.setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "CPF NÃO ENCONTRADO.");
                        this.username_texta.requestFocusInWindow();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "DIGITE UM CPF");
                    this.username_texta.requestFocusInWindow();
                }

            } catch (SQLException e3) {
                JOptionPane.showMessageDialog(null, e3.getMessage());
            }
            try {
                db.Close();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } else if (e.getActionCommand().equals(ACT_SAVE)) {
            saveDataToFile(enrollmentFMD.getData());
//            this.m_save.setEnabled(false);
            return;
        } else {

            EnrollmentThread.EnrollmentEvent evt = (EnrollmentThread.EnrollmentEvent) e;

            if (e.getActionCommand().equals(EnrollmentThread.ACT_PROMPT)) {
                if (m_bJustStarted) {
                    m_text.setText("COLOQUE O DEDO NO LEITOR");

                } else {
                    //
                    try {
                        new Thread().sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Cadastro.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    m_text.setText("");
                    m_text.setText("COLOQUE NOVAMENTE O DEDO NO LEITOR");
                }
                m_bJustStarted = false;
            } else if (e.getActionCommand()
                    .equals(EnrollmentThread.ACT_CAPTURE)) {

                if (null != evt.capture_result) {
                    if (evt.capture_result.image != null) {
                    }
                }
                System.out.println("Pontuação: " + evt.capture_result.score);
                System.out.println("Qualidade: " + evt.capture_result.quality);

                if (null != evt.capture_result) {
                    // MessageBox.BadQuality(evt.capture_result.quality);
                } else if (null != evt.exception) {

                    // MessageBox.DpError("Capture", evt.exception);
                } else if (null != evt.reader_status) {
                    // MessageBox.BadStatus(evt.reader_status);
                }

                m_bJustStarted = false;
            } else if (e.getActionCommand().equals(
                    EnrollmentThread.ACT_FEATURES)) {
                if (null == evt.exception) {
                    m_text.setText("");
                    try {
                        new Thread().sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Cadastro.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    m_text.setText("BIOMETRIA CAPTURADA");

                } else {
                    MessageBox.DpError("ERRO NA EXTRAÇÃO", evt.exception);

                }
                m_bJustStarted = false;
            } else if (e.getActionCommand().equals(EnrollmentThread.ACT_DONE)) {
                if (null == evt.exception) {

                    enrollmentFMD = evt.enrollment_fmd;
                    m_enrollment.cancel();
                    try {
                        new Thread().sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Cadastro.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    this.m_save2DB.setEnabled(true);

                    m_text.setText("<html><div align=\"center\">REGISTRO FINALIZADO<br><div color=\"green\">SALVE PARA VERIFICAÇÂO");

                } else {
                    MessageBox.DpError("CRIAÇÃO DE BIOMETRIA", evt.exception);
                }
                m_bJustStarted = true;
            } else if (e.getActionCommand().equals(
                    EnrollmentThread.ACT_CANCELED)) {
                // canceled, destroy dialog
                m_dlgParent.setVisible(false);
            }

            // cancel enrollment if any exception or bad reader status
            if (null != evt.exception) {
                m_dlgParent.setVisible(false);
            } else if (null != evt.reader_status
                    && Reader.ReaderStatus.READY != evt.reader_status.status
                    && Reader.ReaderStatus.NEED_CALIBRATION != evt.reader_status.status) {
                m_dlgParent.setVisible(false);
            }
        }
    }

    private void saveDataToFile(byte[] data) {

        System.out.println(new String(data));

        // TODO Auto-generated method stub
        JFileChooser fc = new JFileChooser(new File("test"));

        fc.showSaveDialog(this);
        if (fc.getSelectedFile() != null) {
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(
                        fc.getSelectedFile()));
                output.write(data);
                output.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "Erro ao Salvar arquivo.");
            }
        }
    }

    private void doModal(JDialog dlgParent) {
        // open reader
        try {
            m_reader.Open(Reader.Priority.EXCLUSIVE);
        } catch (UareUException e) {
            MessageBox.DpError("Reader.Open()", e);
        }

        // start enrollment thread
        m_enrollment.start();

        // bring up modal dialog
        m_dlgParent = dlgParent;
        m_dlgParent.setContentPane(this);
        m_dlgParent.pack();
        m_dlgParent.setLocationRelativeTo(null);
        m_dlgParent.setVisible(true);
        m_dlgParent.dispose();

        // stop enrollment thread
        m_enrollment.cancel();

        // close reader
        try {
            m_reader.Close();
        } catch (UareUException e) {
            MessageBox.DpError("Reader.Close()", e);
        }
    }

    public static Fmd Run(Reader reader) {

        String cpf = JOptionPane.showInputDialog(null, "DIGITE O CPF: ");
        FingerDB db = new FingerDB();
        try {
            db.Open();
            if (cpf.isEmpty() == false) {
                // Check if user already exists
                if ((db.UserExists(cpf) == true)) {
                    if (db.BioExists(cpf) == false) {

                        JDialog dlg = new JDialog((JDialog) null, "CADASTRO", true);
                        Icone icone = new Icone();
                        dlg.setIconImage(icone.imagemTitulo);
                        Cadastro enrollment = new Cadastro(reader, cpf);
                        enrollment.doModal(dlg);
                        return enrollment.enrollmentFMD;
                    } else {
                        JOptionPane.showMessageDialog(null, "IMPOSSÍVEL DE CADASTRAR", "", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "CPF NÃO ENCONTRADO.");
                    // this.username_text.requestFocusInWindow();
                }

            } else {
                JOptionPane.showMessageDialog(null, "DIGITE UM CPF");
                //this.username_text.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Cadastro.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
