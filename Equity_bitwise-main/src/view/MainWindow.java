package view;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.LinkedList;
import logic.Card;
import static logic.Card.*;

public class MainWindow extends JFrame {

    int cont = 0;

    private LinkedList<Point> coord;

    public MainWindow() {
        initGUI();
    }

    private void initGUI() {

        setBackground();
        setButtons();
        initCoordinates();
        setLayout(null);
        // Establece la operacion por defecto al cerrar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Haz visible la ventana

        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void setBackground() {
        ImageIcon BackGroundImage = scaleImage("C:\\Users\\laura\\Documents\\GitHub\\poker_software_ucm\\poker\\images\\mesa3.jpg", 0.62);

        setSize(BackGroundImage.getIconWidth(), BackGroundImage.getIconHeight());

        JLabel BackGroundLabel = new JLabel(BackGroundImage);

        //BackGroundLabel.setOpaque(true);
        setContentPane(BackGroundLabel);
        BackGroundLabel.setLayout(null);
    }
    
    private void setButtons() {
    	JFrame ventana = new JFrame();
    	
    	/*  ventana.setSize(400, 300);
        	ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	ventana.setLayout(null);

        	// Botón 1
        	JButton boton1 = new JButton("Botón 1");
        	boton1.setBounds(50, 50, 100, 30);
        	boton1.addActionListener(e -> JOptionPane.showMessageDialog(ventana, "¡Botón 1 presionado!"));

        	// Botón 2
        	JButton boton2 = new JButton("Botón 2");
        	boton2.setBounds(200, 50, 100, 30);
        	boton2.addActionListener(e -> JOptionPane.showMessageDialog(ventana, "¡Botón 2 presionado!"));

        	ventana.add(boton1);
        	ventana.add(boton2);

        	ventana.setVisible(true); */
    }

    public ImageIcon scaleImage(String path, double scaleFactor) {
        ImageIcon imageIcon = new ImageIcon(path);
        Image img = imageIcon.getImage();

        int imgWidth = imageIcon.getIconWidth();
        int imgHeight = imageIcon.getIconHeight();

        int newWidth = (int) (imgWidth * scaleFactor);
        int newHeight = (int) (imgHeight * scaleFactor);

        // Escalar la imagen
        Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    public void cartasJugador(int[] mano) { // OJO! : este array de enteros es el return de getCards() de Player 
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        int totalWidth = 0;
        int maxHeight = 0;

        for(int c : mano) {
            if(getRank(c) != '\r') {
                String name = "" + rankToString(c) + suitToString(c) + ".png";
                ImageIcon image = scaleImage("C:\\Users\\laura\\Documents\\GitHub\\poker_software_ucm\\poker\\images\\cartas\\" + name, 0.09);

                JLabel label = new JLabel(image);
                //label.setBounds(50, 50, newWidth, newHeight);
                panel.add(label);
                //add(label);
                totalWidth += image.getIconWidth();
                if (image.getIconHeight() > maxHeight) {
                    maxHeight = image.getIconHeight();
                }
            }
        }

        int widthPanel = totalWidth + 45;
        int heightPanel = maxHeight + 8;
        panel.setOpaque(false);
        mainPanel.setOpaque(false);
        //mainPanel.setPreferredSize(new Dimension(widthPanel*2, heightPanel*3));
        Point coordPanel = coord.get(cont);
        mainPanel.setBounds(coordPanel.x - (widthPanel/2), coordPanel.y - (heightPanel), widthPanel, heightPanel*2 -20);
        mainPanel.add(panel);
        cont++;

        JPanel porcentaje = new JPanel(new BorderLayout());
        porcentaje.setBackground(Color.white);
        JLabel label = new JLabel(Integer.toString(cont), SwingConstants.CENTER);
        porcentaje.setSize(new Dimension(widthPanel - 20, mainPanel.getHeight()));
        porcentaje.setBorder(BorderFactory.createLineBorder(Color.black));

        label.setFont(new Font("Arial", Font.BOLD, 16));
        porcentaje.add(label, BorderLayout.CENTER);
        mainPanel.add(porcentaje, SwingConstants.NORTH);

        add(mainPanel);
        setVisible(true);
    }

    public void cartasMesa(int[] mano) { // OJO! : este array de enteros es el return de getCards() de Table :)
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        int totalWidth = 0;
        int maxHeight = 0;

        for(int c : mano) {
            if(getRank(c) != '\r') {
                String name = "" + rankToString(c) + suitToString(c) + ".png";
                ImageIcon image = scaleImage("C:\\Users\\laura\\Documents\\GitHub\\poker_software_ucm\\poker\\images\\cartas\\" + name, 0.12);

                JLabel label = new JLabel(image);
                //label.setBounds(50, 50, newWidth, newHeight);
                panel.add(label);
                //add(label);
                totalWidth += image.getIconWidth();
                if (image.getIconHeight() > maxHeight) {
                    maxHeight = image.getIconHeight();
                }
            }
        }

        int widthPanel = totalWidth + 45;
        int heightPanel = maxHeight + 8;
        panel.setOpaque(false);

        panel.setBounds(620 - (widthPanel/2), 200, widthPanel, heightPanel);
        add(panel);
        cont++;
        setVisible(true);
    }

    private void initCoordinates() {
        coord = new LinkedList<>();
        //coord.add(new Point(580, 35));
        coord.add(new Point(1000, 120));
        coord.add(new Point(1020, 370));
        coord.add(new Point(770, 490));
        coord.add(new Point(450, 490));
        //coord.add(new Point(360, 435));
        coord.add(new Point(200, 370));
        //coord.add(new Point(110, 285));
        coord.add(new Point(190, 120));
        //coord.add(new Point(500, 300));
    }
}

