/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizabio;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 *
 * @author Gustavo Guerra
 */
public class Icone {

    
    URL url = this.getClass().getResource("icone.png");
    Image imagemTitulo = Toolkit.getDefaultToolkit().getImage(url);
    Image nova = imagemTitulo.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
    ImageIcon icon = new ImageIcon(nova);
    
     URL url2 = this.getClass().getResource("banner.png");
    Image imagemTitulo2 = Toolkit.getDefaultToolkit().getImage(url2);
    Image nova2 = imagemTitulo2.getScaledInstance(400, 150, java.awt.Image.SCALE_SMOOTH);
    ImageIcon icon2 = new ImageIcon(nova2);

}
