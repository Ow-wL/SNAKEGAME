package snakegame.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;
import java.util.function.Consumer;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 600; // 15 * 40 = 600
    private final int BOARD_HEIGHT = 600; // 15 * 40 = 600
    private final int STATUS_HEIGHT = 80;
    private final int UNIT_SIZE = 40; // 600 / 15 = 40
    private final int GRID_SIZE = 15; // 15x15 격자
    private final int DELAY = 100;
    private final int GAME_TIME = 30; // 3분 = 180초

    private ArrayList<Point> snake;
    private Point apple;
    private Point bomb;
    private char direction = 'R';
    private boolean running = false;
    private Timer timer;
    private Timer gameTimer;
    private Random random;
    private int score = 0;
    private int highScore = 0;
    private int timeLeft = GAME_TIME;
    private boolean gameOver = false;
    private boolean hasBomb = false;

    // 이미지 변수들
    private BufferedImage appleImage;
    private BufferedImage bombImage;
    private Graphics2D g;

    // SnakeGame 클래스에 새로운 필드 추가
    private boolean paused = false;
    private JPanel pausePanel;
    private JButton resumeButton;
    private JButton restartButton;
    private JButton exitButton;

    public SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + STATUS_HEIGHT));
        this.setBackground(new Color(245, 245, 245)); // 밝은 회색 배경
        this.setFocusable(true);
        this.addKeyListener(this);

        // 이미지 로드
        loadImages();

        startGame();
    }

    private void loadImages() {
        try {
            // 이미지 파일 로드
            appleImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/apple2.png")));
            bombImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/bomb4.png")));

            // 이미지가 로드되지 않았을 경우 기본 이미지 생성
            if (appleImage == null) {
                appleImage = createDefaultAppleImage();
            }
            if (bombImage == null) {
                bombImage = createDefaultBombImage();
            }

        } catch (IOException e) {
            System.out.println("이미지 로딩 실패: " + e.getMessage());
            // 기본 이미지로 대체
            appleImage = createDefaultAppleImage();
            bombImage = createDefaultBombImage();
        } catch (Exception e) {
            System.out.println("이미지 로딩 중 오류: " + e.getMessage());
            // 기본 이미지로 대체
            appleImage = createDefaultAppleImage();
            bombImage = createDefaultBombImage();
        }
    }

    private BufferedImage createDefaultAppleImage() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 기본 사과 그리기 (기존 코드와 동일)
        GradientPaint appleGradient = new GradientPaint(
                0, 0, new Color(244, 67, 54),
                UNIT_SIZE, UNIT_SIZE, new Color(198, 40, 40)
        );
        g2d.setPaint(appleGradient);
        g2d.fillOval(3, 3, UNIT_SIZE - 6, UNIT_SIZE - 6);

        // 사과 하이라이트
        g2d.setColor(new Color(255, 138, 128));
        g2d.fillOval(8, 8, 12, 10);

        // 사과 꼭지 그리기
        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(UNIT_SIZE - 20, 13, UNIT_SIZE - 20, 3);

        // 사과 잎 그리기
        g2d.setColor(new Color(20, 169, 40));
        g2d.setStroke(new BasicStroke(3));
        g2d.fillOval(UNIT_SIZE - 19, 3, 12, 6);

        g2d.dispose();
        return img;
    }

    private BufferedImage createDefaultBombImage() {
        BufferedImage img = new BufferedImage(UNIT_SIZE, UNIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 기본 폭탄 그리기 (기존 코드와 동일)
        GradientPaint bombGradient = new GradientPaint(
                0, 0, new Color(69, 69, 69),
                UNIT_SIZE, UNIT_SIZE, new Color(33, 33, 33)
        );
        g2d.setPaint(bombGradient);
        g2d.fillOval(3, 3, UNIT_SIZE - 6, UNIT_SIZE - 6);

        // 폭탄 하이라이트
        g2d.setColor(new Color(158, 158, 158));
        g2d.fillOval(10, 10, 10, 8);

        // 심지 그리기
        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(UNIT_SIZE - 8, 5, UNIT_SIZE - 3, 0);

        g2d.dispose();
        return img;
    }

    public void startGame() {
        snake = new ArrayList<>();
        // 초기 뱀 위치 (3개 세그먼트)
        snake.add(new Point(0, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE * 2, STATUS_HEIGHT));

        newApple();
        newBomb();
        hasBomb = true;
        timeLeft = GAME_TIME;
        running = true;

        timer = new Timer(DELAY, this);
        timer.start();

        // 1초마다 시간 감소
        gameTimer = new Timer(1000, e -> {
            timeLeft--;
            if (timeLeft <= 0) {
                running = false;
                gameTimer.stop();
                gameOver = true;
            }
        });
        gameTimer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw(g2d);
    }

    public void draw(Graphics2D g) {
        // 상태창 그리기
        drawStatusBar(g);

        if (running) {
            // 게임 영역 체스판 배경
            drawChessboardBackground(g);

            // 게임 영역 테두리
            g.setColor(new Color(150, 150, 150));
            g.setStroke(new BasicStroke(2));
            g.drawRect(0, STATUS_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);

            // 사과 그리기 (이미지 사용)
            if (appleImage != null) {
                g.drawImage(appleImage, apple.x, apple.y, null);
            }

            // 폭탄 그리기 (이미지 사용)
            if (hasBomb && bombImage != null) {
                g.drawImage(bombImage, bomb.x, bomb.y, null);
            }

            // 뱀 그리기
            for (int i = 0; i < snake.size(); i++) {
                if (i == snake.size() - 1) {
                    // 머리 (하늘색)
                    drawSnakeSegment(g, snake.get(i).x, snake.get(i).y, new Color(81, 147, 212), true);
                } else {
                    // 몸통 (어두운 하늘색)
                    drawSnakeSegment(g, snake.get(i).x, snake.get(i).y, new Color(61, 129, 197), false);
                }
            }
        } else {
            gameOver(g);
        }

        if (paused) {
            // 반투명한 오버레이 추가
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, STATUS_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);
        
            // "일시정지" 텍스트 표시
            g.setColor(Color.WHITE);
            g.setFont(new Font("맑은 고딕", Font.BOLD, 32));
            FontMetrics metrics = g.getFontMetrics();
            String pauseText = "일시정지";
            g.drawString(pauseText, 
                (BOARD_WIDTH - metrics.stringWidth(pauseText)) / 2,
                STATUS_HEIGHT + BOARD_HEIGHT / 4);
        }
    }

    public void drawChessboardBackground(Graphics2D g) {
        Color lightColor = new Color(143, 214, 143); // 연두색
        Color darkColor = new Color(165, 250, 165);  // 초록색

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                // 체스판 패턴 (짝수+짝수 = 밝은색, 홀수+홀수 = 밝은색)
                if ((row + col) % 2 == 0) {
                    g.setColor(lightColor);
                } else {
                    g.setColor(darkColor);
                }

                int x = col * UNIT_SIZE;
                int y = STATUS_HEIGHT + row * UNIT_SIZE;
                g.fillRect(x, y, UNIT_SIZE, UNIT_SIZE);
            }
        }
    }

    public void drawStatusBar(Graphics2D g) {
        // 상태창 배경 (그라데이션)
        GradientPaint gradient = new GradientPaint(0, 0, new Color(63, 81, 181), 0, STATUS_HEIGHT, new Color(125, 100, 237));
        g.setPaint(gradient);
        g.fillRect(0, 0, BOARD_WIDTH, STATUS_HEIGHT);

        // 상태창 테두리
        g.setColor(new Color(48, 63, 159));
        g.setStroke(new BasicStroke(2));
        g.drawRect(0, 0, BOARD_WIDTH, STATUS_HEIGHT);

        // 텍스트 정보
        g.setColor(Color.WHITE);

        // 한글 폰트 설정
        Font koreanFont = new Font("맑은 고딕", Font.BOLD, 14);
        if (!koreanFont.getFamily().equals("맑은 고딕")) {
            // 맑은 고딕이 없으면 다른 한글 폰트 시도
            koreanFont = new Font("굴림", Font.BOLD, 14);
            if (!koreanFont.getFamily().equals("굴림")) {
                // 한글 폰트가 없으면 기본 폰트 사용
                koreanFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
            }
        }
        g.setFont(koreanFont);

        String currentScoreText = "현재점수: " + score;
        String highScoreText = "최고점수: " + highScore;
        String timeText = "남은시간: " + formatTime(timeLeft);

        // 텍스트 위치 계산
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight();

        // 첫 번째 줄
        int y1 = STATUS_HEIGHT / 3 + textHeight / 3;
        g.drawString(currentScoreText, 15, y1);

        // 시간은 오른쪽 정렬
        int timeWidth = fm.stringWidth(timeText);
        g.drawString(timeText, BOARD_WIDTH - timeWidth - 15, y1);

        // 두 번째 줄 (최고점수는 가운데)
        int y2 = STATUS_HEIGHT * 2 / 3 + textHeight / 3;
        int highScoreWidth = fm.stringWidth(highScoreText);
        g.drawString(highScoreText, (BOARD_WIDTH - highScoreWidth) / 2, y2);
    }

    public void drawSnakeSegment(Graphics2D g, int x, int y, Color baseColor, boolean isHead) {
        // 뱀 세그먼트 그라데이션
        Color lightColor = new Color(
                Math.min(255, baseColor.getRed() + 30),
                Math.min(255, baseColor.getGreen() + 30),
                Math.min(255, baseColor.getBlue() + 30)
        );

        GradientPaint segmentGradient = new GradientPaint(
                x, y, lightColor,
                x + UNIT_SIZE, y + UNIT_SIZE, baseColor
        );
        g.setPaint(segmentGradient);
        g.fillRoundRect(x + 2, y + 2, UNIT_SIZE - 4, UNIT_SIZE - 4, 12, 12);

        // 테두리
        g.setColor(new Color(baseColor.getRed() - 20, baseColor.getGreen() - 20, baseColor.getBlue() - 20));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x + 2, y + 2, UNIT_SIZE - 4, UNIT_SIZE - 4, 12, 12);

        // 머리에 눈 추가
        /*if (isHead) {
            g.setColor(Color.WHITE);
            g.fillOval(x + 10, y + 10, 8, 8);
            g.fillOval(x + 22, y + 10, 8, 8);
            g.setColor(Color.BLACK);
            g.fillOval(x + 12, y + 12, 4, 4);
            g.fillOval(x + 24, y + 12, 4, 4);
        }*/
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    public void newApple() {
        int x, y;
        do {
            x = random.nextInt(GRID_SIZE) * UNIT_SIZE;
            y = random.nextInt(GRID_SIZE) * UNIT_SIZE + STATUS_HEIGHT;
        } while (isPositionOccupied(x, y));

        apple = new Point(x, y);
    }

    public void newBomb() {
        int x, y;
        do {
            x = random.nextInt(GRID_SIZE) * UNIT_SIZE;
            y = random.nextInt(GRID_SIZE) * UNIT_SIZE + STATUS_HEIGHT;
        } while (isPositionOccupied(x, y));

        bomb = new Point(x, y);
        hasBomb = true;
    }

    public void moveBomb() {
        if (!hasBomb) return;

        int x, y;
        do {
            x = random.nextInt(GRID_SIZE) * UNIT_SIZE;
            y = random.nextInt(GRID_SIZE) * UNIT_SIZE + STATUS_HEIGHT;
        } while (isPositionOccupied(x, y));

        bomb = new Point(x, y);
    }

    public boolean isPositionOccupied(int x, int y) {
        Point pos = new Point(x, y);

        // 뱀 몸통과 겹치는지 확인
        for (Point segment : snake) {
            if (segment.equals(pos)) {
                return true;
            }
        }

        // 사과와 겹치는지 확인
        if (apple != null && apple.equals(pos)) {
            return true;
        }

        // 폭탄과 겹치는지 확인
        if (hasBomb && bomb != null && bomb.equals(pos)) {
            return true;
        }

        return false;
    }

    public void move() {
        Point head = new Point(snake.get(snake.size() - 1));

        switch (direction) {
            case 'U':
                head.y -= UNIT_SIZE;
                break;
            case 'D':
                head.y += UNIT_SIZE;
                break;
            case 'L':
                head.x -= UNIT_SIZE;
                break;
            case 'R':
                head.x += UNIT_SIZE;
                break;
        }

        snake.add(head);

        // 사과를 먹었는지 확인
        if (head.equals(apple)) {
            score++;
            if (score > highScore) {
                highScore = score;
            }
            newApple();

            // 폭탄이 없으면 생성, 있으면 이동
            if (!hasBomb) {
                newBomb();
            } else {
                moveBomb();
            }
        } else {
            snake.remove(0);
        }
    }

    public void checkCollisions() {
        Point head = snake.get(snake.size() - 1);

        // 자기 몸과 충돌 확인
        for (int i = 0; i < snake.size() - 1; i++) {
            if (head.equals(snake.get(i))) {
                running = false;
            }
        }

        // 벽과 충돌 확인
        if (head.x < 0 || head.x >= BOARD_WIDTH ||
                head.y < STATUS_HEIGHT || head.y >= BOARD_HEIGHT + STATUS_HEIGHT) {
            running = false;
        }

        // 폭탄과 충돌 확인
        if (hasBomb && head.equals(bomb)) {
            running = false;
        }

        if (!running) {
            timer.stop();
            gameTimer.stop();
            gameOver = true;
        }
    }

    public void gameOver(Graphics2D g) {
        // 게임 영역을 반투명하게
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(0, STATUS_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);

        // 한글 폰트 설정
        Font koreanFont = new Font("맑은 고딕", Font.BOLD, 24);
        if (!koreanFont.getFamily().equals("맑은 고딕")) {
            koreanFont = new Font("굴림", Font.BOLD, 24);
            if (!koreanFont.getFamily().equals("굴림")) {
                koreanFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
            }
        }

        // 최종 점수
        g.setColor(new Color(244, 67, 54));
        g.setFont(koreanFont);
        FontMetrics metrics1 = g.getFontMetrics();
        String scoreText = timeLeft <= 0 ? "시간 종료! 점수: " + score : "게임 오버! 점수: " + score;
        g.drawString(scoreText, (BOARD_WIDTH - metrics1.stringWidth(scoreText)) / 2,
                STATUS_HEIGHT + 80);

        // Game Over 텍스트
        Font bigFont = new Font("맑은 고딕", Font.BOLD, 32);
        if (!bigFont.getFamily().equals("맑은 고딕")) {
            bigFont = new Font("굴림", Font.BOLD, 32);
            if (!bigFont.getFamily().equals("굴림")) {
                bigFont = new Font(Font.SANS_SERIF, Font.BOLD, 32);
            }
        }
        g.setColor(new Color(63, 81, 181));
        g.setFont(bigFont);
        FontMetrics metrics2 = g.getFontMetrics();
        String gameOverText = timeLeft <= 0 ? "시간 종료!" : "게임 오버!";
        g.drawString(gameOverText, (BOARD_WIDTH - metrics2.stringWidth(gameOverText)) / 2,
                STATUS_HEIGHT + BOARD_HEIGHT / 2);

        // 재시작 안내
        Font smallFont = new Font("맑은 고딕", Font.BOLD, 16);
        if (!smallFont.getFamily().equals("맑은 고딕")) {
            smallFont = new Font("굴림", Font.BOLD, 16);
            if (!smallFont.getFamily().equals("굴림")) {
                smallFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
            }
        }
        g.setColor(new Color(96, 125, 139));
        g.setFont(smallFont);
        FontMetrics metrics3 = g.getFontMetrics();
        String restartText = "스페이스바를 눌러 재시작";
        g.drawString(restartText, (BOARD_WIDTH - metrics3.stringWidth(restartText)) / 2,
                STATUS_HEIGHT + BOARD_HEIGHT / 2 + 50);
    }

    public void restartGame() {
        score = 0;
        direction = 'R';
        gameOver = false;
        hasBomb = false;
        timeLeft = GAME_TIME;
        snake.clear();
        snake.add(new Point(0, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE * 2, STATUS_HEIGHT));
        newApple();
        running = true;
        timer.restart();
        gameTimer.restart();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollisions();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (direction != 'R') {
                    direction = 'L';
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (direction != 'L') {
                    direction = 'R';
                }
                break;
            case KeyEvent.VK_UP:
                if (direction != 'D') {
                    direction = 'U';
                }
                break;
            case KeyEvent.VK_DOWN:
                if (direction != 'U') {
                    direction = 'D';
                }
                break;
            case KeyEvent.VK_SPACE:
                if (gameOver) {
                    restartGame();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                if(running) {
                    pauseGame();
                }
                break;
        }
    }

    // pauseGame 메소드 구현
    public void pauseGame() {
        if (!gameOver) {
            paused = true;
            timer.stop();
            gameTimer.stop();
        
            // 일시정지 패널 생성
            pausePanel = new JPanel();
            pausePanel.setLayout(new BoxLayout(pausePanel, BoxLayout.Y_AXIS));
            pausePanel.setBackground(new Color(0, 0, 0, 150));
            pausePanel.setBounds(BOARD_WIDTH/4, STATUS_HEIGHT + BOARD_HEIGHT/4, 
                               BOARD_WIDTH/2, BOARD_HEIGHT/2);
        
            // 버튼 스타일 설정을 위한 공통 메소드
            Consumer<JButton> styleButton = button -> {
                button.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                button.setBackground(new Color(63, 81, 181));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.setMaximumSize(new Dimension(200, 40));
            };

            // 재개 버튼
            resumeButton = new JButton("게임 재개");
            styleButton.accept(resumeButton);
            resumeButton.addActionListener(e -> resumeGame());

            // 재시작 버튼
            restartButton = new JButton("게임 재시작");
            styleButton.accept(restartButton);
            restartButton.addActionListener(e -> {
                removeButtons();
                restartGame();
            });

            // 나가기 버튼
            exitButton = new JButton("게임 종료");
            styleButton.accept(exitButton);
            exitButton.addActionListener(e -> System.exit(0));

            // 버튼 사이 여백 추가
            pausePanel.add(Box.createVerticalStrut(20));
            pausePanel.add(resumeButton);
            pausePanel.add(Box.createVerticalStrut(10));
            pausePanel.add(restartButton);
            pausePanel.add(Box.createVerticalStrut(10));
            pausePanel.add(exitButton);
            pausePanel.add(Box.createVerticalStrut(20));

            this.setLayout(null);
            this.add(pausePanel);
            this.revalidate();
            this.repaint();
        }
    }

    // resumeGame 메소드 추가
    private void resumeGame() {
        if (paused) {
            paused = false;
            removeButtons();
            timer.start();
            gameTimer.start();
        }
    }

    // 버튼 제거 메소드 추가
    private void removeButtons() {
        if (pausePanel != null) {
            this.remove(pausePanel);
            this.revalidate();
            this.repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}