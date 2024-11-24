import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class HigherLowerGameGUI {
    private List<Card> deck;
    private Card currentCard;
    private int score;
    private int lives;
    private int timeLeft;
    private int streak;
    private int longestStreak;

    private JLabel cardLabel;
    private JLabel messageLabel;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel timerLabel;
    private JLabel streakLabel;
    private JButton restartButton;
    private JButton higherButton;
    private JButton lowerButton;
    private Timer countdownTimer;
    private Random random;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HigherLowerGameGUI::new);
    }

    public HigherLowerGameGUI() {
        // Ask the player whether they want Jokers included
        int includeJokers = JOptionPane.showConfirmDialog(null, "Do you want to include Jokers in the deck?", "Include Jokers", JOptionPane.YES_NO_OPTION);
        boolean jokersIncluded = (includeJokers == JOptionPane.YES_OPTION);

        // Initialize the deck and shuffle it
        initializeDeck(jokersIncluded);
        shuffleDeck();
        currentCard = drawCard();
        score = 0;
        lives = 3;
        timeLeft = 10;
        streak = 0;
        longestStreak = 0;
        random = new Random();

        // Setup UI Components
        JFrame frame = new JFrame("Higher or Lower Card Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        cardLabel = new JLabel("Current Card: " + currentCard, SwingConstants.CENTER);
        messageLabel = new JLabel("Make your guess!", SwingConstants.CENTER);
        scoreLabel = new JLabel("Score: " + score, SwingConstants.CENTER);
        livesLabel = new JLabel("Lives: " + lives, SwingConstants.CENTER);
        timerLabel = new JLabel("Time Left: " + timeLeft, SwingConstants.CENTER);
        streakLabel = new JLabel("Streak: " + streak, SwingConstants.CENTER);

        higherButton = new JButton("Higher");
        lowerButton = new JButton("Lower");
        restartButton = new JButton("Restart");
        restartButton.setEnabled(false);

        higherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lives > 0) {
                    handleGuess(true);
                    if (lives > 0) startCountdown();
                }
            }
        });

        lowerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lives > 0) {
                    handleGuess(false);
                    if (lives > 0) startCountdown();
                }
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
                restartButton.setEnabled(false);
                higherButton.setEnabled(true);
                lowerButton.setEnabled(true);
                startCountdown();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(cardLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(higherButton, gbc);

        gbc.gridx++;
        panel.add(lowerButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(messageLabel, gbc);

        gbc.gridy++;
        panel.add(scoreLabel, gbc);

        gbc.gridy++;
        panel.add(livesLabel, gbc);

        gbc.gridy++;
        panel.add(timerLabel, gbc);

        gbc.gridy++;
        panel.add(streakLabel, gbc);

        gbc.gridy++;
        panel.add(restartButton, gbc);

        frame.add(panel);
        frame.setVisible(true);

        startCountdown();
    }

    // Initialize a standard 52-card deck with optional Jokers
    private void initializeDeck(boolean includeJokers) {
        deck = new LinkedList<>();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(suit, rank));
            }
        }

        // Optionally add Jokers to the deck
        if (includeJokers) {
            deck.add(new Card("Joker", "Black Joker"));
            deck.add(new Card("Joker", "Red Joker"));
        }
    }

    // Shuffle the deck
    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    // Draw a card from the deck
    private Card drawCard() {
        if (!deck.isEmpty()) {
            return deck.remove(0);
        }
        return null;
    }

    // Handle the user's guess
    private void handleGuess(boolean isHigher) {
        countdownTimer.stop();
        moveToNextCard(isHigher, true);
    }

    private void moveToNextCard(boolean isHigher, boolean isPlayerGuess) {
        Card nextCard = drawCard();
        if (nextCard == null) {
            messageLabel.setText("No more cards! Final Score: " + score + " | Longest Streak: " + longestStreak);
            restartButton.setEnabled(true);
            higherButton.setEnabled(false);
            lowerButton.setEnabled(false);
            return;
        }

        boolean correctGuess;
        if (currentCard.getSuit().equals("Joker") || nextCard.getSuit().equals("Joker")) {
            correctGuess = false; // Automatically lose if a Joker is drawn
            messageLabel.setText("Joker drawn! You lost a life.");
            lives--;
            streak = 0;
        } else {
            correctGuess = (isHigher && compareCards(nextCard, currentCard) > 0) ||
                    (!isHigher && compareCards(nextCard, currentCard) < 0);

            if (correctGuess && isPlayerGuess) {
                score++;
                streak++;
                if (streak > longestStreak) {
                    longestStreak = streak;
                }
                messageLabel.setText("Correct! Keep going!");
                if (streak % 5 == 0) {
                    lives++;
                    messageLabel.setText("Correct! Streak bonus: +1 life!");
                }
            } else if (!isPlayerGuess) {
                messageLabel.setText("Time's up! You lost a life.");
                lives--;
                streak = 0;
            } else {
                lives--;
                streak = 0;
                messageLabel.setText("Wrong guess! You lost a life.");
            }
        }

        if (lives <= 0) {
            lives = 0;
            updateLabels();
            messageLabel.setText("Game over! Final Score: " + score + " | Longest Streak: " + longestStreak);
            restartButton.setEnabled(true);
            higherButton.setEnabled(false);
            lowerButton.setEnabled(false);
            timeLeft = 0;
            timerLabel.setText("Time Left: " + timeLeft);
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
        } else {
            currentCard = nextCard;
            cardLabel.setText("Current Card: " + currentCard);
            updateLabels();
        }
    }

    // Restart the game
    private void restartGame() {
        // Ask the player whether they want Jokers included
        int includeJokers = JOptionPane.showConfirmDialog(null, "Do you want to include Jokers in the deck?", "Include Jokers", JOptionPane.YES_NO_OPTION);
        boolean jokersIncluded = (includeJokers == JOptionPane.YES_OPTION);

        initializeDeck(jokersIncluded);
        shuffleDeck();
        currentCard = drawCard();
        score = 0;
        lives = 3;
        timeLeft = 10;
        streak = 0;
        longestStreak = 0;
        cardLabel.setText("Current Card: " + currentCard);
        messageLabel.setText("Make your guess!");
        updateLabels();
    }

    // Start countdown timer for each turn
    private void startCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        timeLeft = 10;
        timerLabel.setText("Time Left: " + timeLeft);
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("Time Left: " + timeLeft);
                if (timeLeft <= 0) {
                    countdownTimer.stop();
                    moveToNextCard(false, false); // Automatically move to the next card without player input
                    updateLabels();
                    startCountdown();
                    if (lives <= 0) {
                        lives = 0;
                        updateLabels();
                        messageLabel.setText("Game over! Final Score: " + score + " | Longest Streak: " + longestStreak);
                        higherButton.setEnabled(false);
                        lowerButton.setEnabled(false);
                        restartButton.setEnabled(true);
                        timeLeft = 0;
                        timerLabel.setText("Time Left: " + timeLeft);
                    }
                }
            }
        });
        countdownTimer.start();
    }

    // Update the labels
    private void updateLabels() {
        livesLabel.setText("Lives: " + lives);
        scoreLabel.setText("Score: " + score);
        streakLabel.setText("Streak: " + streak);
    }

    // Compare two cards to determine their rank
    private int compareCards(Card card1, Card card2) {
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        int rank1 = -1, rank2 = -1;
        for (int i = 0; i < ranks.length; i++) {
            if (card1.getRank().equals(ranks[i])) {
                rank1 = i;
            }
            if (card2.getRank().equals(ranks[i])) {
                rank2 = i;
            }
        }
        return Integer.compare(rank1, rank2);
    }

    // Card class representing a playing card
    private static class Card {
        private final String suit;
        private final String rank;

        public Card(String suit, String rank) {
            this.suit = suit;
            this.rank = rank;
        }

        public String getSuit() {
            return suit;
        }

        public String getRank() {
            return rank;
        }

        @Override
        public String toString() {
            return rank + " of " + suit;
        }
    }
}



















