package visualizabio;

import javax.swing.JOptionPane;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

public class MessageBox {
	public static void BadQuality(Reader.CaptureQuality q) {
		JOptionPane.showMessageDialog(null, q.toString(), "Má qualidade",
				JOptionPane.WARNING_MESSAGE);
	}

	public static void BadStatus(Reader.Status s) {
		String str = String.format("Status de leitura: %s", s.toString());
		JOptionPane.showMessageDialog(null, str, "Status de leitura",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void DpError(String strFunctionName, UareUException e) {
		String str = String.format("%s Erro no leitor %d \n%s",
				strFunctionName, (e.getCode() & 0xffff), e.toString());
		JOptionPane.showMessageDialog(null, str, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void Warning(String strText) {
		JOptionPane.showMessageDialog(null, strText, "Cuidado",
				JOptionPane.WARNING_MESSAGE);
	}
}
