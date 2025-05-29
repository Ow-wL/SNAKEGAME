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
    private final int STATUS_HEIGHT = 100; // 80에서 100으로 증가
    private final int UNIT_SIZE = 40; // 600 / 15 = 40
    private final int GRID_SIZE = 15; // 15x15 격자
    private final int DELAY = 100;
    private final int GAME_TIME = 30; // 30초

    private ArrayList<Point> snake;
    private ArrayList<Point> apples; // 여러 개의 사과를 담을 리스트
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
    private int appleCount = 3; // 기본 사과 개수

    private BufferedImage appleImage;
    private BufferedImage bombImage;
    private BufferedImage scoreIcon;
    private BufferedImage trophyIcon;
    private BufferedImage timeIcon;
    private Graphics2D g;

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

        // 사과 개수 선택
        selectAppleCount();

        startGame();
    }

    private void selectAppleCount() {
        String[] options = {"1개", "2개", "3개", "4개", "5개"};
        String selected = (String) JOptionPane.showInputDialog(
                null,
                "사과 개수를 선택하세요:",
                "사과 개수 설정",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2] // 기본값: 3개
        );

        if (selected != null) {
            switch (selected) {
                case "1개": appleCount = 1; break;
                case "2개": appleCount = 2; break;
                case "3개": appleCount = 3; break;
                case "4개": appleCount = 4; break;
                case "5개": appleCount = 5; break;
                default: appleCount = 3; break;
            }
        } else {
            appleCount = 3; // 취소 시 기본값
        }
    }

    private void loadImages() {
        try {
            // 게임 이미지 파일 로드
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
            System.out.println("게임 이미지 로딩 실패: " + e.getMessage());
            // 기본 이미지로 대체
            appleImage = createDefaultAppleImage();
            bombImage = createDefaultBombImage();
        } catch (Exception e) {
            System.out.println("게임 이미지 로딩 중 오류: " + e.getMessage());
            // 기본 이미지로 대체
            appleImage = createDefaultAppleImage();
            bombImage = createDefaultBombImage();
        }

        // 상태바 아이콘 로드
        loadStatusIcons();
    }

    private void loadStatusIcons() {
        try {
            // 상태바 아이콘 이미지 파일 로드
            scoreIcon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/Apple.png")));
            trophyIcon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/Trophy.png")));
            timeIcon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/Time.png")));

        } catch (IOException e) {
            System.out.println("상태바 아이콘 로딩 실패: " + e.getMessage());
            // 기본 아이콘으로 대체
            createDefaultStatusIcons();
        } catch (Exception e) {
            System.out.println("상태바 아이콘 로딩 중 오류: " + e.getMessage());
            // 기본 아이콘으로 대체
            createDefaultStatusIcons();
        }
    }

    private void createDefaultStatusIcons() {
        int iconSize = 48; // 32에서 48로 증가

        // 점수 아이콘 (별 모양) - 기본 아이콘
        scoreIcon = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scoreIcon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 별 모양 그리기 (크기에 맞게 조정)
        int[] xPoints = {iconSize/2, iconSize/2 + 6, iconSize - 3, iconSize/2 + 12, iconSize/2 + 18, iconSize/2, iconSize/2 - 18, iconSize/2 - 12, 3, iconSize/2 - 6};
        int[] yPoints = {3, iconSize/2 - 9, iconSize/2 - 9, iconSize/2 + 3, iconSize - 3, iconSize/2 + 12, iconSize - 3, iconSize/2 + 3, iconSize/2 - 9, iconSize/2 - 9};

        GradientPaint starGradient = new GradientPaint(0, 0, new Color(255, 215, 0), iconSize, iconSize, new Color(255, 165, 0));
        g2d.setPaint(starGradient);
        g2d.fillPolygon(xPoints, yPoints, 10);

        g2d.setColor(new Color(218, 165, 32));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawPolygon(xPoints, yPoints, 10);
        g2d.dispose();

        // 트로피 아이콘 (최고점수) - 기본 아이콘
        trophyIcon = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        g2d = trophyIcon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 트로피 컵 (크기 증가)
        GradientPaint trophyGradient = new GradientPaint(0, 0, new Color(255, 215, 0), iconSize, iconSize, new Color(255, 140, 0));
        g2d.setPaint(trophyGradient);
        g2d.fillOval(iconSize/4, iconSize/4, iconSize/2, iconSize/2);

        // 트로피 손잡이 (크기 증가)
        g2d.setStroke(new BasicStroke(4));
        g2d.drawArc(3, iconSize/3, iconSize/5, iconSize/2, 90, 180);
        g2d.drawArc(iconSize - iconSize/5 - 3, iconSize/3, iconSize/5, iconSize/2, 270, 180);

        // 트로피 받침 (크기 증가)
        g2d.fillRect(iconSize/3, iconSize - iconSize/3, iconSize/3, iconSize/6);
        g2d.fillRect(iconSize/4, iconSize - iconSize/6, iconSize/2, iconSize/12);

        g2d.setColor(new Color(218, 165, 32));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(iconSize/4, iconSize/4, iconSize/2, iconSize/2);
        g2d.dispose();

        // 시간 아이콘 (시계) - 기본 아이콘
        timeIcon = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        g2d = timeIcon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 시계 배경 (크기 증가)
        GradientPaint clockGradient = new GradientPaint(0, 0, new Color(100, 149, 237), iconSize, iconSize, new Color(65, 105, 225));
        g2d.setPaint(clockGradient);
        g2d.fillOval(3, 3, iconSize - 6, iconSize - 6);

        // 시계 테두리 (크기 증가)
        g2d.setColor(new Color(25, 25, 112));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(3, 3, iconSize - 6, iconSize - 6);

        // 시계 바늘 (크기 증가)
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        int centerX = iconSize/2;
        int centerY = iconSize/2;
        g2d.drawLine(centerX, centerY, centerX, centerY - iconSize/3); // 시침
        g2d.drawLine(centerX, centerY, centerX + iconSize/4, centerY - iconSize/5); // 분침

        // 중심점 (크기 증가)
        g2d.fillOval(centerX - 3, centerY - 3, 6, 6);
        g2d.dispose();
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
        apples = new ArrayList<>(); // 사과 리스트 초기화

        // 초기 뱀 위치 (3개 세그먼트)
        snake.add(new Point(0, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE * 2, STATUS_HEIGHT));

        // 지정된 개수만큼 사과 생성
        for (int i = 0; i < appleCount; i++) {
            newApple();
        }

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

            // 모든 사과 그리기 (이미지 사용)
            if (appleImage != null) {
                for (Point apple : apples) {
                    g.drawImage(appleImage, apple.x, apple.y, null);
                }
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
                    STATUS_HEIGHT + BOARD_HEIGHT / 4 + 10);
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

        // 한글 폰트 설정 (크기 증가)
        Font koreanFont = new Font("맑은 고딕", Font.BOLD, 24); // 18에서 24로 증가
        if (!koreanFont.getFamily().equals("맑은 고딕")) {
            koreanFont = new Font("굴림", Font.BOLD, 24);
            if (!koreanFont.getFamily().equals("굴림")) {
                koreanFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
            }
        }
        g.setFont(koreanFont);

        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight();
        int iconSize = 48; // 32에서 48로 증가
        int yPos = (STATUS_HEIGHT + textHeight) / 2 - 5;
        int iconYPos = (STATUS_HEIGHT - iconSize) / 2;

        // 3개 영역으로 균등 분할
        int sectionWidth = BOARD_WIDTH / 3;

        // 1. 현재점수 (왼쪽)
        int scoreX = 25; // 여백 증가
        if (scoreIcon != null) {
            g.drawImage(scoreIcon, scoreX, iconYPos, iconSize, iconSize, null);
        }
        String scoreText = String.valueOf(score);
        g.drawString(scoreText, scoreX + iconSize + 15, yPos); // 간격 증가

        // 2. 최고점수 (가운데) - 왼쪽으로 5픽셀 이동
        int highScoreX = sectionWidth + (sectionWidth - iconSize - fm.stringWidth(String.valueOf(highScore)) - 15) / 2 - 5;
        if (trophyIcon != null) {
            g.drawImage(trophyIcon, highScoreX, iconYPos, iconSize, iconSize, null);
        }
        String highScoreText = String.valueOf(highScore);
        g.drawString(highScoreText, highScoreX + iconSize + 15, yPos); // 간격 증가

        // 3. 남은시간 (오른쪽)
        String timeText = formatTime(timeLeft);
        int timeWidth = fm.stringWidth(timeText);
        int timeX = BOARD_WIDTH - timeWidth - iconSize - 40; // 여백 증가
        if (timeIcon != null) {
            g.drawImage(timeIcon, timeX, iconYPos, iconSize, iconSize, null);
        }
        g.drawString(timeText, timeX + iconSize + 15, yPos); // 간격 증가
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

        apples.add(new Point(x, y));
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

        // 모든 사과와 겹치는지 확인
        for (Point apple : apples) {
            if (apple.equals(pos)) {
                return true;
            }
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

        // 모든 사과와 충돌 확인
        Point eatenApple = null;
        for (Point apple : apples) {
            if (head.equals(apple)) {
                eatenApple = apple;
                break;
            }
        }

        if (eatenApple != null) {
            // 사과를 먹었을 때
            score++;
            if (score > highScore) {
                highScore = score;
            }

            // 먹은 사과 제거
            apples.remove(eatenApple);

            // 새 사과 생성
            newApple();

            // 폭탄이 없으면 생성, 있으면 이동
            if (!hasBomb) {
                newBomb();
            } else {
                moveBomb();
            }
        } else {
            // 사과를 먹지 않았으면 꼬리 제거
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
        apples.clear(); // 사과 리스트 초기화

        snake.add(new Point(0, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE, STATUS_HEIGHT));
        snake.add(new Point(UNIT_SIZE * 2, STATUS_HEIGHT));

        // 사과 개수 다시 선택
        selectAppleCount();

        // 지정된 개수만큼 사과 생성
        for (int i = 0; i < appleCount; i++) {
            newApple();
        }

        running = true;
        paused = false;
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
            if (!paused) {
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
                resumeButton = new JButton("돌아가기");
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