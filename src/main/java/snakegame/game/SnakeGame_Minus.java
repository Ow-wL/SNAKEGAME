package snakegame.game;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class SnakeGame_Minus {

    static class GameFrame extends JFrame {

        static class XY {
            int x;
            int y;
            public XY(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }
        static JPanel panelTitle;
        static JPanel panelGame;
        static JLabel labelTitle;
        static JLabel labelMessage;
        static JPanel[][] panels = new JPanel[20][20];
        static int[][] map = new int[20][20]; // Fruit 9, Bomb 8, 0 Blank
        static LinkedList<XY> snake = new LinkedList<>();
        static int dir = 3; // move direction 0:up 1:down 2:left 3:right
        static int score = 0;
        static int time = 0; // game time ( unit 1 second )
        static int timeTickCount = 0; // per 200ms;
        static Timer timer = null;
        private static Font emojiFont;

        public GameFrame(String title) {
            super(title);
            this.setSize(400,500);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initUI(); // init UI
            //makeSnakeList(); // make snake body
            //startTimer(); // start timer
            //setKeyListener(); // listen key event
            //makeFruit(); // make fruit
        }

        public void initUI() {
            this.setLayout(new BorderLayout());

            panelTitle = new JPanel();
            panelTitle.setPreferredSize(new Dimension(400, 100));
            panelTitle.setBackground(Color.BLACK);
            panelTitle.setLayout(new FlowLayout());

            labelTitle = new JLabel("Score: 0, Time: 0 sec");
            labelTitle.setPreferredSize(new Dimension(400, 50));
            labelTitle.setFont(new Font("Ariel", Font.BOLD, 20));
            labelTitle.setForeground(Color.WHITE);
            labelTitle.setHorizontalAlignment(JLabel.CENTER);
            panelTitle.add(labelTitle);


            labelMessage = new JLabel("Eat Fruit!");
            labelMessage.setSize(new Dimension(400, 20));
            labelMessage.setPreferredSize(new Dimension(400, 20));
            labelMessage.setFont(new Font("Ariel", Font.BOLD, 20));
            labelMessage.setForeground(Color.CYAN);
            labelMessage.setHorizontalAlignment(JLabel.CENTER);
            panelTitle.add(labelMessage);


            this.add(panelTitle);

            panelGame = new JPanel();
            panelGame.setLayout(new GridLayout(20, 20));
            for (int i = 0; i < 20; i++) { // i Loop : Row
                for (int j = 0; j < 20; j++) { // j Loop : Column
                    map[i][j] = 0; // init 0 : Blank
                    panels[i][j] = new JPanel();
                    panels[i][j].setPreferredSize(new Dimension(20, 20));
                    panels[i][j].setBackground(Color.GREEN);
                    panelGame.add(panels[i][j]);
                }
            }
            this.add(panelGame);
            this.pack(); // Remove Empty Space
        }
    }

    public static void main(String[] args) {

        new GameFrame("SnakeGame");
    }
}
