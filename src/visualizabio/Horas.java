package visualizabio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Horas {

    Calendar hoje = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/2018 - HH:mm:ss");

    Date horaManhaInicial = new Date(2018, hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH), 8, 0, 0);
    String manhaInicial = sdf.format(horaManhaInicial);

    Date horaManhaFinal = new Date(2018, hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH), 10, 0, 0);
    String manhaFinal = sdf.format(horaManhaFinal);

    Date horaTardeInicial = new Date(2018, hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH), 10, 59, 0);
    String tardeInicial = sdf.format(horaTardeInicial);

    Date horaTardeFinal = new Date(2018, hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH), 16, 0, 0);
    String tardeFinal = sdf.format(horaTardeFinal);

    Date horaNoiteInicial = new Date(2018, hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH), 17, 0, 0);
    String noiteInicial = sdf.format(horaNoiteInicial);

    Date horaNoiteFinal = new Date(2018, hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH), 20, 0, 0);
    String noiteFinal = sdf.format(horaNoiteFinal);

    Date horaManhaInicialC = new Date();
    Date horaManhaFinalC = new Date();

    Date horaTardeInicialC = new Date();
    Date horaTardeFinalC = new Date();

    Date horaNoiteInicialC = new Date();
    Date horaNoiteFinalC = new Date();

    Date horaAtual = new Date();
    Date horaAtualC = new Date();

    public void InicializaHoras() {
        horaAtual = hoje.getTime();

        try {
            horaAtualC = sdf.parse(sdf.format(horaAtual));

        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        try {
            horaNoiteFinalC = sdf.parse(noiteFinal);
        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            horaNoiteInicialC = sdf.parse(noiteInicial);
        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            horaTardeFinalC = sdf.parse(tardeFinal);
        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            horaTardeInicialC = sdf.parse(tardeInicial);
        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            horaManhaFinalC = sdf.parse(manhaFinal);
        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            horaManhaInicialC = sdf.parse(manhaInicial);
        } catch (ParseException ex) {
            Logger.getLogger(Verificacao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
