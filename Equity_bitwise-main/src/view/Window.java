package view;
import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public Window() {
        initGUI();
    }

    private void initGUI() {

        setBackground();

        // Establece la operación por defecto al cerrar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Haz visible la ventana
        setVisible(true);
    }

    private void setBackground() {
        ImageIcon BackGroundImage = new ImageIcon("C:\\Users\\laura\\Documents\\GitHub\\poker_software_ucm\\poker\\images\\mesa.jpg");
        Image img = BackGroundImage.getImage();
        // Establecer el contenido de la ventana
        int imgWidth = BackGroundImage.getIconWidth();
        int imgHeight = BackGroundImage.getIconHeight();

        double scaleFactor = 0.5;
        int newWidth = (int) (imgWidth * scaleFactor);
        int newHeight = (int) (imgHeight * scaleFactor);

        // Escalar la imagen
        Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BackGroundImage = new ImageIcon(scaledImg);

        // Establecer el tamaño de la ventana según las nuevas dimensiones
        setSize(newWidth, newHeight);

        JLabel BackGroundLabel = new JLabel(BackGroundImage);

        //BackGroundLabel.setOpaque(true);
        setContentPane(BackGroundLabel);
        BackGroundLabel.setLayout(new BorderLayout());
    }

    public void cartasJugador() {

    }
}


