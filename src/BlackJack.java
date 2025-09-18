import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.Timer;

public class BlackJack {

    int playerCoins = 1000;
    int currentBet = 0;

    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("AJQK".contains(value)) {
                if (value.equals("A")) return 11;
                return 10;
            }
            return Integer.parseInt(value);
        }

        public boolean isAce() {
            return value.equals("A");
        }

        public String getImagePath() {
            return "/cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck;
    Random random = new Random();

    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    int boardWidth = 600;
    int boardHeight = boardWidth;

    int cardWidth = 110;
    int cardHeight = 154;

    String roundMessage = "";
    boolean roundEnded = false;

    JFrame frame = new JFrame("21");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                // carta escondida
                Image hiddenCardImg = new ImageIcon(getClass().getResource("/cards/BACK.png")).getImage();
                if(roundEnded) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImg, 20, 20, cardWidth, cardHeight, null);

                // cartas do dealer
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
                }

                // cartas do jogador
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, 20 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                }

                // saldo e aposta
                g.setFont(new Font("Sans Serif", Font.PLAIN, 20));
                g.setColor(Color.yellow);
                g.drawString("Saldo: " + playerCoins, 400, 280);
                g.drawString("Aposta: " + currentBet, 400, 310);

                
                if(!roundMessage.isEmpty()) {
                    g.setFont(new Font("Arial", Font.BOLD, 30));
                    g.setColor(Color.white);
                    g.drawString(roundMessage, 220, 250);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //botões
    JPanel buttonPanel = new JPanel();
    JButton betMinusButton = new JButton("-");
    JButton betPlusButton = new JButton("+");
    JButton betConfirmButton = new JButton("Confirmar Aposta");
    JButton hitButton =  new JButton("Comprar");
    JButton stayButton = new JButton("Parar");
    JButton restartButton = new JButton("Reiniciar");
    JButton litravButton = new JButton("Litrav");

    BlackJack() {
        startGame();
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        stayButton.setFocusable(false);
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        betMinusButton.setFocusable(false);
        betPlusButton.setFocusable(false);
        betConfirmButton.setFocusable(false);

        buttonPanel.add(hitButton);
        buttonPanel.add(stayButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(betMinusButton);
        buttonPanel.add(betPlusButton);
        buttonPanel.add(betConfirmButton);
        buttonPanel.add(litravButton, BorderLayout.EAST);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        //funções dos botões
        betMinusButton.addActionListener(e -> {
            if (currentBet > 0) currentBet -= 50;
            gamePanel.repaint();
        });

        betPlusButton.addActionListener(e -> {
            if (currentBet + 50 <= playerCoins) currentBet += 50;
            gamePanel.repaint();
        });

        betConfirmButton.addActionListener(e -> {
            if (currentBet > 0) {
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                betMinusButton.setEnabled(false);
                betPlusButton.setEnabled(false);
                betConfirmButton.setEnabled(false);
                gamePanel.repaint();
            }
        });

        restartButton.addActionListener(e -> {
            playerCoins = 1000;
            roundMessage = "";
            restartButton.setVisible(false);
            startGame();
            hitButton.setEnabled(false);
            stayButton.setEnabled(false);
            betMinusButton.setEnabled(true);
            betPlusButton.setEnabled(true);
            betConfirmButton.setEnabled(true);
            gamePanel.repaint();
        });

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                playerSum = reducePlayerAce();
                if(playerSum > 21) hitButton.setEnabled(false);
                gamePanel.repaint();
                checkRoundEnd();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);
                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size()-1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce()? 1 : 0;
                    dealerHand.add(card);
                    dealerSum = reduceDealerAce();
                }
                gamePanel.repaint();
                checkRoundEnd();
            }
        });

        litravButton.setFocusable(false);
        litravButton.addActionListener(e -> {
            try { 
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://github.com/litrav"));
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://linkedin.com/in/pedro-trofino"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        gamePanel.repaint();
    }

    private void checkRoundEnd() {
        roundMessage = "";
        

        playerSum = reducePlayerAce();
        dealerSum = reduceDealerAce();

        if (playerSum > 21) {
            roundMessage = "Você Perdeu!";
            playerCoins -= currentBet;
            roundEnded = true;
        } else if (dealerSum > 21) {
            roundMessage = "Você Ganhou!";
            playerCoins += currentBet;
            roundEnded = true;
        } else if (!hitButton.isEnabled() && !stayButton.isEnabled()) {
            if (playerSum > dealerSum) {
                roundMessage = "Você Ganhou!";
                playerCoins += currentBet;
                roundEnded = true;
            } else if (playerSum < dealerSum) {
                roundMessage = "Você Perdeu!";
                playerCoins -= currentBet;
                roundEnded = true;
            } else {
                roundMessage = "Empate";
                roundEnded = true;
            }
            if (roundEnded) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);
                betMinusButton.setEnabled(true);
                betPlusButton.setEnabled(true);
                betConfirmButton.setEnabled(true);
            }
        }

        gamePanel.repaint();

        if(roundEnded) {
            currentBet = 0;
            betMinusButton.setEnabled(true);
            betPlusButton.setEnabled(true);
            betConfirmButton.setEnabled(true);

            hitButton.setEnabled(false);
            stayButton.setEnabled(false);

            if(playerCoins <= 0) {
                restartButton.setVisible(true);
                roundMessage = "Você ficou sem moedas!";
                gamePanel.repaint();
            } else {
                // timer antes da rodada
                new Timer(3000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        roundMessage = ""; 
                        startGame();
                        gamePanel.repaint();
                        ((Timer)e.getSource()).stop();
                    }
                }).start();
            }
        }
    }

    public void startGame() {
        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size()-1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        playerHand = new ArrayList<>();
        playerSum = 0;
        playerAceCount = 0;
        for(int i=0;i<2;i++){
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce()?1:0;
            playerHand.add(card);
        }
        roundEnded = false;
    }

    public void buildDeck() {
        deck = new ArrayList<>(); 
        String[] values = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] types = {"C","D","H","S"};
        for(String type: types){
            for(String value: values){
                deck.add(new Card(value,type));
            }
        }
    }

    public void shuffleDeck() {
        for(int i=0; i<deck.size(); i++){
            int j = random.nextInt(deck.size());
            Card tmp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, tmp);
        }
    }

    public int reducePlayerAce() {
        while(playerSum>21 && playerAceCount>0){
            playerSum -=10;
            playerAceCount--;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while(dealerSum>21 && dealerAceCount>0){
            dealerSum -=10;
            dealerAceCount--;
        }
        return dealerSum;
    }

    public static void main(String[] args) {
        new BlackJack();
    }
}
