import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Objects;

public class MainWindow extends JFrame {

    private boolean isOmaha = false; // Indicates whether Omaha mode is active
    private int cont = 0;
    private LinkedList<Point> coord;
    Game game = new Game();
    boolean[] fase = new boolean[3];
    // Pannello principale per le carte
    private JPanel cardsPanel;

    public MainWindow() {
        initCoordinates();
        initGUI();
    }

    private JPanel createBackgroundPanel() {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                String path = new File("images/mesa3.jpg").getAbsolutePath();
                ImageIcon backgroundImage = scaleImage(path, 0.62);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        backgroundPanel.setLayout(null); // Allows custom positioning
        backgroundPanel.setOpaque(true); // Ensure the background image is visible

        // Add the cardsPanel over the background
        cardsPanel = new JPanel();
        cardsPanel.setLayout(null); // Allows absolute positioning of cards
        cardsPanel.setOpaque(false); // Transparency for overlaying
        cardsPanel.setBounds(0, 0, 1350, 800); // Match size of the main window
        backgroundPanel.add(cardsPanel);

        return backgroundPanel;
    }



    private void initGUI() {
        // Set main layout
        setLayout(new BorderLayout());
        // Add the toolbar to the top
        JToolBar toolBar = getjToolBar();
        add(toolBar, BorderLayout.NORTH);

        // Add the lateral menu to the right
        JPanel cardSelectionPanel = createCardSelectionPanel();
        JScrollPane scrollPane = new JScrollPane(cardSelectionPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.EAST);

        // Add the center panel with the background image
        add(createBackgroundPanel(), BorderLayout.CENTER);

        // Configure the window
        setMinimumSize(new Dimension(1350, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private JToolBar getjToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // Toolbar cannot be moved
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT)); // Buttons aligned to the left

        // Start Game Button
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(_ -> {
            try {
                game.calculatePreFlopEquity();
                updatePlayersEquity();
                startButton.setEnabled(false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // Continue Game Button
        JButton continueButton = new JButton("Continue Game");
        continueButton.addActionListener(_ -> {
            if (fase[0]) {
                if (fase[1]) {
                    if (fase[2]) {
                        JOptionPane.showMessageDialog(this, "Game is finished!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        game.calculateRiverEquity();
                        fase[2] = true;
                    }
                } else {
                    game.calculateTurnEquity();
                    fase[1] = true;
                }
            } else {
                game.calculateFlopEquity();
                fase[0] = true;
            }
            cartasMesa(game.getTable());
            updatePlayersEquity();
        });

        // Add buttons to the toolbar
        toolBar.add(startButton);
        toolBar.add(continueButton);
        return toolBar;
    }


    private JPanel createCardSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 1, 5, 5)); // Layout with 8 rows (6 players + 1 table card section)
        panel.setBorder(BorderFactory.createTitledBorder("Hand Distribution"));

        JComboBox<String>[] cardBoxes = new JComboBox[12]; // For 6 players, 2 cards each
        for (int i = 0; i < 6; i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            playerPanel.setBorder(BorderFactory.createTitledBorder("Player " + (i + 1)));

            // Dropdown for card selection
            JComboBox<String> card1Box = createCardComboBox();
            JComboBox<String> card2Box = createCardComboBox();
            cardBoxes[i * 2] = card1Box;
            cardBoxes[i * 2 + 1] = card2Box;

            // Button for dealing the cards (R)
            JButton distributeButton = new JButton("R");

            int finalI = i;
            distributeButton.addActionListener(_ -> {
                try {
                    int card1, card2;
                    boolean used = false;
                    // Handle random card selection
                    if ("R".equals(cardBoxes[finalI * 2].getSelectedItem())) {
                        card1 = game.randomCard();
                    } else {
                        card1 = parseCard((String) cardBoxes[finalI * 2].getSelectedItem());
                        if(!game.presentInDeck(card1)) {
                            JOptionPane.showMessageDialog(this, "Card already used!", "Error", JOptionPane.ERROR_MESSAGE);
                            used = true;
                        }
                        else
                            if(game.presentInDeck(parseCard((String) cardBoxes[finalI * 2 + 1].getSelectedItem())))
                                game.removeFromDeck(card1);

                    }
                    if ("R".equals(cardBoxes[finalI * 2 + 1].getSelectedItem())) {
                        card2 = game.randomCard();
                        game.removeFromDeck(card2);
                    } else {
                        card2 = parseCard((String) cardBoxes[finalI * 2 + 1].getSelectedItem());
                        if(!game.presentInDeck(card2)) {
                            JOptionPane.showMessageDialog(this, "Card already used!", "Error", JOptionPane.ERROR_MESSAGE);
                            used = true;
                        }
                        else
                            game.removeFromDeck(card2);
                    }

                    // Assign the cards to the player
                    if(!used){
                    game.getPlayers()[finalI] = new Player(card1, card2);
                    cartasJugador(game.getPlayers()[finalI]);
                    distributeButton.setEnabled(false);
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid card selection!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            playerPanel.add(card1Box);
            playerPanel.add(card2Box);
            playerPanel.add(distributeButton);

            panel.add(playerPanel);
        }

        // Table Cards Selection Section
        JPanel tableCardPanel = new JPanel();
        tableCardPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        tableCardPanel.setBorder(BorderFactory.createTitledBorder("Table Cards"));

        // Dropdown to select a table card
        JComboBox<String> tableCardBox = createCardComboBox();
        tableCardPanel.add(tableCardBox);

        // Button to add selected card to the table
        JButton addTableCardButton = new JButton("Add Table Card");
        addTableCardButton.addActionListener(_ -> {
            try {
                String selectedCard = (String) tableCardBox.getSelectedItem();
                if (selectedCard == null || "R".equals(selectedCard)) {
                    JOptionPane.showMessageDialog(this, "Select a valid card!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int card = parseCard(selectedCard);
                // Add the card to the table (assuming game.addToTable handles the addition)
                if (game.addTableCard(card)) {
                    // Update the table cards display
                    cartasMesa(game.getTable()); // Redraw the table cards
                } else {
                    JOptionPane.showMessageDialog(this, "Card already used.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid card selection!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        tableCardPanel.add(addTableCardButton);
        panel.add(tableCardPanel);

        return panel;
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

    public ImageIcon scaleImage(String path, double scaleFactor) {
        ImageIcon imageIcon = new ImageIcon(path);
        Image img = imageIcon.getImage();

        int imgWidth = imageIcon.getIconWidth();
        int imgHeight = imageIcon.getIconHeight();
        System.out.println(imgWidth + " " + imgHeight);
        int newWidth = (int) (imgWidth * scaleFactor);
        int newHeight = (int) (imgHeight * scaleFactor);

        Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    public void cartasJugador(Player player) {
        int[] mano = player.getCards();
        double equity = player.getEquity();

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


        // Assing a button "Fold"
        JButton foldButton = new JButton("Fold");
        foldButton.setBounds(widthPanel + 5, heightPanel / 2 - 15, 80, 30);

        foldButton.addActionListener(_ -> {
            System.out.println("Player folded");
            player.fold();
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
        panel.setBounds(560 - (widthPanel / 2), 250, widthPanel, heightPanel);

        // Aggiungi il nuovo pannello delle carte al pannello principale
        cardsPanel.add(panel);

        // Aggiorna il layout e ridisegna
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }


    private void initCoordinates() {
        coord = new LinkedList<>();
        coord.add(new Point(950, 200));
        coord.add(new Point(950, 500));
        coord.add(new Point(730, 620));
        coord.add(new Point(420, 620));
        coord.add(new Point(190, 500));
        coord.add(new Point(190, 200));
    }

    private JComboBox<String> createCardComboBox() {
        String[] cards = new String[53];
        cards[0] = "R";
        String[] suits = {"c", "d", "h", "s"}; // Clubs, Diamonds, Hearts, Spades
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};

        int index = 1;
        for (String rank : ranks) {
            for (String suit : suits) {
                cards[index++] = rank + suit;
            }
        }

        return new JComboBox<>(cards);
    }


    private int parseCard(String card) throws IllegalArgumentException {
        if (card == null || card.length() != 2) {
            throw new IllegalArgumentException("Invalid card format: " + card);
        }
        char rank = card.charAt(0);
        char suit = card.charAt(1);
        String a = new StringBuilder().append(rank).append(suit).toString();
        return Card.cardFromChar(a); // Implement `stringToCard` in your `Card` class.
    }


    public static void main(String[] args) {
        // Avvia l'applicazione
        SwingUtilities.invokeLater(MainWindow::new);
    }
}
