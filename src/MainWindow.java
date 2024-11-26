import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;

public class MainWindow extends JFrame {

    private int cont = 0;
    private LinkedList<Point> coord;
    Game game = new Game();
    boolean[] fase = new boolean[2];
    // Pannello principale per le carte
    private JPanel cardsPanel;

    public MainWindow() {
        initGUI();
    }

    private void initGUI() {
        setBackground();
        initCoordinates();

        // Imposta il layout principale della finestra
        setLayout(new BorderLayout());

        // Barra degli strumenti
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // La barra non può essere spostata
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT)); // Pulsanti allineati a sinistra

        // Pulsante Start Game
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(_ -> {
            game.insertPlayerCards();
            try {
                game.calculatePreFlopEquity();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (Player p : game.getPlayers()) {
                cartasJugador(p.getCards(), p.getEquity());
            }
        });

        // Pulsante Continue Game
        JButton continueButton = new JButton("Continue Game");
        continueButton.addActionListener(_ -> {
            // Azione per "Continue Game
            if(fase[0])
                if(fase[1]){
                    game.calculateRiverEquity();
                    }
                else{ game.calculateTurnEquity();
                    fase[1] = true;
                }
            else{
                game.calculateFlopEquity();
                fase[0] = true;}
            cartasMesa(game.getTable());
            updatePlayersEquity();
            System.out.println("Continue game clicked.");
        });

        // Aggiungi i pulsanti alla barra degli strumenti
        toolBar.add(startButton);
        toolBar.add(continueButton);

        // Aggiungi la barra degli strumenti nella parte superiore della finestra
        add(toolBar, BorderLayout.NORTH);

        // Pannello per le carte
        cardsPanel = new JPanel();
        cardsPanel.setLayout(null); // Layout null per posizionamento assoluto
        cardsPanel.setOpaque(false); // Sfondo trasparente
        add(cardsPanel, BorderLayout.CENTER);

        // Configura la finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true); // Mostra la finestra
    }

    // Metodo per aggiornare l'equity dei giocatori
    private void updatePlayersEquity() {
        Component[] components = cardsPanel.getComponents(); // Ottieni tutti i componenti nel pannello
        int i = 0;
        for (Component component : components) {
            if (component instanceof JPanel playerPanel) {

                // Cerca il JLabel che mostra l'equity del giocatore
                for (Component subComponent : playerPanel.getComponents()) {
                    if (subComponent instanceof JLabel equityLabel) {

                        // Verifica se il JLabel rappresenta l'equity
                        if (equityLabel.getText().startsWith("Equity:")) {
                            // Aggiorna l'equity con il nuovo valore
                            double newEquity = game.getPlayers()[i].getEquity();
                            equityLabel.setText("Equity: " + String.format("%.2f", newEquity) + "%");
                            i++;
                            // Passa al giocatore successivo
                            break;
                        }
                    }
                }
            }
        }

        // Ridisegna il pannello per applicare i cambiamenti
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void setBackground() {
        // Immagine di sfondo

        String path = new File("images/mesa3.jpg").getAbsolutePath();
        ImageIcon BackGroundImage = scaleImage(path, 0.62);
        setSize(BackGroundImage.getIconWidth(), BackGroundImage.getIconHeight());

        JLabel BackGroundLabel = new JLabel(BackGroundImage);
        setContentPane(BackGroundLabel); // Imposta lo sfondo
        BackGroundLabel.setLayout(null);
    }

    public ImageIcon scaleImage(String path, double scaleFactor) {
        ImageIcon imageIcon = new ImageIcon(path);
        Image img = imageIcon.getImage();

        int imgWidth = imageIcon.getIconWidth();
        int imgHeight = imageIcon.getIconHeight();

        int newWidth = (int) (imgWidth * scaleFactor);
        int newHeight = (int) (imgHeight * scaleFactor);

        Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    public void cartasJugador(int[] mano, double equity) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null); // Posizionamento assoluto
        mainPanel.setOpaque(false);

        JPanel cardsPanel = new JPanel(new FlowLayout());
        cardsPanel.setOpaque(false);

        int totalWidth = 0;
        int maxHeight = 0;

        for (int j : mano) {
            if (Card.getRank(j) != '\r') {
                String path = new File("images/cartas").getAbsolutePath();
                String name = Card.cardToString(j) + ".png";
                ImageIcon image = scaleImage(path + "\\" + name, 0.09);

                JLabel label = new JLabel(image);
                cardsPanel.add(label);
                totalWidth += image.getIconWidth();
                if (image.getIconHeight() > maxHeight) {
                    maxHeight = image.getIconHeight();
                }
            }
        }

        int widthPanel = totalWidth + 45;
        int heightPanel = maxHeight + 8;

        Point coordPanel = coord.get(cont);
        mainPanel.setBounds(coordPanel.x - (widthPanel / 2), coordPanel.y - (heightPanel), widthPanel + 100, heightPanel * 2 + 20);

        // Posiziona il pannello delle carte
        cardsPanel.setBounds(0, 0, widthPanel, heightPanel);
        mainPanel.add(cardsPanel);

        // Aggiungi un pulsante "Fold"
        JButton foldButton = new JButton("Fold");
        foldButton.setBounds(widthPanel + 5, heightPanel / 2 - 15, 80, 30);
        foldButton.addActionListener(_ -> {
            System.out.println("Player folded");
            mainPanel.setVisible(false); // Nascondi il pannello del giocatore
        });
        mainPanel.add(foldButton);

        // Mostra l'equity del giocatore
        JLabel equityLabel = new JLabel("Equity: " + String.format("%.2f", equity) + "%");
        equityLabel.setForeground(Color.WHITE);
        equityLabel.setBounds(10, heightPanel + 10, 100, 20);
        mainPanel.add(equityLabel);

        // Aggiungi il pannello principale al pannello delle carte
        this.cardsPanel.add(mainPanel);
        this.cardsPanel.revalidate(); // Aggiorna il layout del pannello
        this.cardsPanel.repaint();    // Ridisegna il pannello

        cont++;
    }

    public void cartasMesa(int[] mesa) {
        // Rimuovi il vecchio pannello delle carte del tavolo, se esiste
        for (Component component : cardsPanel.getComponents()) {
            if (component instanceof JPanel panel) {
                if ("tableCards".equals(panel.getName())) { // Controlla il nome del pannello
                    cardsPanel.remove(panel);
                    break; // Rimuovi solo il pannello delle carte del tavolo
                }
            }
        }

        // Crea un nuovo pannello per le carte del tavolo
        JPanel panel = new JPanel();
        panel.setName("tableCards"); // Identifica questo pannello come quello delle carte del tavolo
        panel.setLayout(new FlowLayout());

        int totalWidth = 0;
        int maxHeight = 0;

        // Aggiungi le carte al pannello
        for (int i : mesa) {
            if (i != 0) {
                if (Card.getRank(i) != '\r') {
                    String path = new File("images/cartas").getAbsolutePath();
                    String name = Card.cardToString(i) + ".png";
                    ImageIcon image = scaleImage(path + "\\" + name, 0.12);

                    JLabel label = new JLabel(image);
                    panel.add(label);
                    totalWidth += image.getIconWidth();
                    if (image.getIconHeight() > maxHeight) {
                        maxHeight = image.getIconHeight();
                    }
                }
            }
        }

        int widthPanel = totalWidth + 45;
        int heightPanel = maxHeight + 8;
        panel.setOpaque(false);

        // Posiziona il pannello delle carte sul tavolo
        panel.setBounds(620 - (widthPanel / 2), 200, widthPanel, heightPanel);

        // Aggiungi il nuovo pannello delle carte al pannello principale
        cardsPanel.add(panel);

        // Aggiorna il layout e ridisegna
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }


    private void initCoordinates() {
        coord = new LinkedList<>();
        coord.add(new Point(1000, 120));
        coord.add(new Point(1020, 370));
        coord.add(new Point(770, 490));
        coord.add(new Point(450, 490));
        coord.add(new Point(200, 370));
        coord.add(new Point(190, 120));
    }

    public static void main(String[] args) {
        // Avvia l'applicazione
        SwingUtilities.invokeLater(MainWindow::new);
    }
}
