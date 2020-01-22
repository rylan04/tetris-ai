import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

public class Game extends JFrame implements ActionListener, KeyListener {

    private boolean keyArray[] = new boolean[7];

    public char[][] board = new char[15][20];

    Robot robot = null;

    public int rotation = 0;
    public int piece;
    public Point pointer = new Point(5, 2);
    public int ticker = 0;
    public int widthLeft[] = new int[4];
    public int widthRight[] = new int[4];
    public int speed = 20;
    public int cooldownMove = 60;
    public int cooldownRotate = 30;
    public int cooldownDrop = 100;
    public boolean canHold = true;
    public boolean moveable;

    public boolean aiPlaying = false;
    public int testMoveX = 1;
    public int testRot = 0;
    public boolean decisionMade = false;
    public int decisionMoveX = 1;
    public int decisionRot = 0;

    public int holdPiece;

    private final char fill = 'Y';
    private final char[] fillType = {'a','b','c','d','e','f','g'};
    private final char empty = 'x';

    // tracks the lowest point of the block
    // private int lowestPoint = pointer.y;
    // private int leftestPoint = pointer.x;
    // private int rightestPoint = pointer.x;
    private int lowestPoint[][] = new int[7][4];
    private int leftestPoint[][] = new int[7][4];
    private int rightestPoint[][] = new int[7][4];

    static Timer timer;

    public JLabel[][] tiles = new JLabel[10][20];

    // position, rotation
    private int scores[][] = new int[15][4];

    // if a block is being held
    boolean held = false;

    // holds all subsequent blocks (4 at a time)
    ArrayList<Integer> blocks = new ArrayList<Integer>();

    public Game() {

        for (int i = 0; i < 4; i++) {
            scores[0][i] = -10000;
        }

        for (int i = 0; i < 10; i++) {

            for (int j = 0; j < 20; j++) {

                tiles[i][j] = new JLabel();
                tiles[i][j].setBounds(20 * i, 20 * j, 20, 20);
                add(tiles[i][j]);
            }
        }

        for (int piece = 0; piece < 7; piece++) {

            for (int rotation = 0; rotation < 4; rotation++) {

                if ((piece == 0 && rotation == 1) || (piece == 0 && rotation == 3) || (piece == 1 && rotation == 1)
                        || (piece == 2 && rotation == 3) || (piece == 5 && rotation == 1)) {
                    // left 0
                    leftestPoint[piece][rotation] = 0;
                } else {
                    leftestPoint[piece][rotation] = -1;
                }

                if ((piece == 0 && rotation == 1) || (piece == 0 && rotation == 3) || (piece == 1 && rotation == 3)
                        || (piece == 2 && rotation == 1) || (piece == 3 && rotation == 0)
                        || (piece == 3 && rotation == 1) || (piece == 3 && rotation == 2)
                        || (piece == 3 && rotation == 3) || (piece == 4 && rotation == 1)
                        || (piece == 4 && rotation == 3) || (piece == 5 && rotation == 3)
                        || (piece == 6 && rotation == 1) || (piece == 6 && rotation == 3)) {
                    rightestPoint[piece][rotation] = 0;
                } else if ((piece == 0 && rotation == 0) || (piece == 0 && rotation == 2)) {
                    rightestPoint[piece][rotation] = 2;
                } else {
                    rightestPoint[piece][rotation] = 1;
                }

                // starred pieces (this includes squares)
                if ((piece == 0 && rotation == 0) || (piece == 0 && rotation == 2) || (piece == 1 && rotation == 0)
                        || (piece == 2 && rotation == 2) || (piece == 5 && rotation == 0) || piece == 3
                        || (piece == 6 && rotation == 0) || (piece == 6 && rotation == 2)
                        || (piece == 4 && rotation == 0) || (piece == 4 && rotation == 2)) {

                    // System.out.println("ASUODHASD");
                    lowestPoint[piece][rotation] = 0;

                } else if ((piece == 0 && rotation == 1) || (piece == 0 && rotation == 3)) {
                    lowestPoint[piece][rotation] = 2;
                } else {

                    lowestPoint[piece][rotation] = 1;
                }

            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 7; j++) {

                System.out.print(leftestPoint[j][i] + ":" + rightestPoint[j][i] + "    ");

            }
            System.out.println();
        }

        for (int i = 0; i < 4; i++) {
            blocks.add((int) (Math.random() * 7));
        }
        piece = blocks.get(0);

        // System.out.println(Arrays.toString(blocks.toArray()));

        // setPiece();
        newPiece();
        updateBoard();

        try {
            robot = new Robot();
        } catch (AWTException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        setSize(500, 900);
        setVisible(true);
        setLayout(null);
        addKeyListener(this);

        timer = new Timer(1, this);

        timer.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public void updateBoard() {

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                Font myFont = new Font("Serif",Font.BOLD, 20);

                if (board[i + 2][j] == 'x') {
                    // tiles[i][j].setIcon(new ImageIcon(new ImageIcon
                    // ("Images/GreySquareResized.jpg").getImage().getScaledInstance (20, 20, 0)));
                    tiles[i][j].setForeground(Color.gray);
                    tiles[i][j].setFont(myFont);
                    tiles[i][j].setText("□");

//					tiles[i][j].setText("⬛");
                } else {
                    tiles[i][j].setFont(myFont);
                    if (board[i+2][j] == 'a' || (board[i+2][j] == 'O' && piece == 0)) {
                        tiles[i][j].setForeground(new Color(0,240,240));
                    } else if (board[i+2][j] == 'b' || (board[i+2][j] == 'O' && piece == 1)) {
                        tiles[i][j].setForeground(new Color(240,160,0));
                    } else if (board[i+2][j] == 'c' || (board[i+2][j] == 'O' && piece == 2)) {
                        tiles[i][j].setForeground(new Color(0,0,240));
                    } else if (board[i+2][j] == 'd' || (board[i+2][j] == 'O' && piece == 3)) {
                        tiles[i][j].setForeground(new Color(255,215,0));
                    } else if (board[i+2][j] == 'e' || (board[i+2][j] == 'O' && piece == 4)) {
                        tiles[i][j].setForeground(new Color(240,0,0));
                    } else if (board[i+2][j] == 'f' || (board[i+2][j] == 'O' && piece == 5)) {
                        tiles[i][j].setForeground(new Color(160,0,240));
                    } else if (board[i+2][j] == 'g' || (board[i+2][j] == 'O' && piece == 6)) {
                        tiles[i][j].setForeground(new Color(0,240,0));
                    }
//					tiles[i][j].setText("□");
                    tiles[i][j].setText("⬛");
                    // tiles[i][j].setIcon(new ImageIcon(new ImageIcon
                    // ("Images/RedSquare.png").getImage().getScaledInstance (20, 20, 0)));

                }

                // tiles[i][j].setText(Character.toString(board[i][j]));
            }
        }
        repaint();

    }

    private final Point[][][] minos = {
            // I-Piece
            { { new Point(-1, 0), new Point(0, 0), new Point(1, 0), new Point(2, 0) },
                    { new Point(0, -1), new Point(0, 0), new Point(0, 1), new Point(0, 2) },
                    { new Point(-1, 0), new Point(0, 0), new Point(1, 0), new Point(2, 0) },
                    { new Point(0, -1), new Point(0, 0), new Point(0, 1), new Point(0, 2) } },

            // J-Piece
            { { new Point(-1, 0), new Point(0, 0), new Point(1, 0), new Point(1, -1) },
                    { new Point(0, -1), new Point(0, 0), new Point(0, 1), new Point(1, 1) },
                    { new Point(-1, 0), new Point(0, 0), new Point(1, 0), new Point(-1, 1) },
                    { new Point(0, -1), new Point(0, 0), new Point(0, 1), new Point(-1, -1) } },

            // L-Piece
            { { new Point(-1, 0), new Point(0, 0), new Point(1, 0), new Point(1, 1) },
                    { new Point(0, -1), new Point(0, 0), new Point(0, 1), new Point(-1, 1) },
                    { new Point(-1, 0), new Point(0, 0), new Point(1, 0), new Point(-1, -1) },
                    { new Point(0, -1), new Point(0, 0), new Point(0, 1), new Point(1, -1) } },

            // O-Piece
            { { new Point(-1, -1), new Point(-1, 0), new Point(0, -1), new Point(0, 0) },
                    { new Point(-1, -1), new Point(-1, 0), new Point(0, -1), new Point(0, 0) },
                    { new Point(-1, -1), new Point(-1, 0), new Point(0, -1), new Point(0, 0) },
                    { new Point(-1, -1), new Point(-1, 0), new Point(0, -1), new Point(0, 0) } },

            // S-Piece
            { { new Point(0, -1), new Point(1, -1), new Point(-1, 0), new Point(0, 0) },
                    { new Point(-1, -1), new Point(-1, 0), new Point(0, 0), new Point(0, 1) },
                    { new Point(0, -1), new Point(1, -1), new Point(-1, 0), new Point(0, 0) },
                    { new Point(-1, -1), new Point(-1, 0), new Point(0, 0), new Point(0, 1) } },

            // T-Piece
            { { new Point(0, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0) },
                    { new Point(0, -1), new Point(0, 0), new Point(1, 0), new Point(0, 1) },
                    { new Point(-1, 0), new Point(0, 0), new Point(1, 0), new Point(0, 1) },
                    { new Point(0, -1), new Point(-1, 0), new Point(0, 0), new Point(0, 1) } },

            // Z-Piece
            { { new Point(-1, -1), new Point(0, -1), new Point(0, 0), new Point(1, 0) },
                    { new Point(0, -1), new Point(-1, 0), new Point(0, 0), new Point(-1, 1) },
                    { new Point(-1, -1), new Point(0, -1), new Point(0, 0), new Point(1, 0) },
                    { new Point(0, -1), new Point(-1, 0), new Point(0, 0), new Point(-1, 1) } } };

    public void setPiece() {

        for (int i = 2; i < 12; i++) {
            for (int j = 0; j < 20; j++) {

                // if board is Y, then skip, else:
                if (board[i][j] != fillType[0] && board[i][j] !=fillType[1] && board[i][j] !=fillType[2]
                        && board[i][j] !=fillType[3] && board[i][j] !=fillType[4] && board[i][j] !=fillType[5]
                        && board[i][j] !=fillType[6]) {
                    board[i][j] = 'x';
                }
            }
        }

        if (aiPlaying == false) {

            board[pointer.x][pointer.y] = 'O';

            for (int i = 0; i < 4; i++) {

                board[pointer.x + minos[piece][rotation][i].x][pointer.y + minos[piece][rotation][i].y] = 'O';
            }
        } else if (decisionMade == true) {

            pointer.x = decisionMoveX;
            rotation = decisionRot;
            board[pointer.x][pointer.y] = 'O';

            for (int i = 0; i < 4; i++) {

                board[pointer.x + minos[piece][rotation][i].x][pointer.y + minos[piece][rotation][i].y] = 'O';
            }

        }

        else { ////////// AI IS PLAYING

            board[pointer.x][pointer.y] = 'Y';

            for (int i = 0; i < 4; i++) {

                board[pointer.x + minos[piece][rotation][i].x][pointer.y + minos[piece][rotation][i].y] = 'Z';
            }

        }

    }

    int decisionMadeBuffer = 0;

    public void newPiece() {

        if (aiPlaying == false || decisionMadeBuffer == 1) {
            decisionMadeBuffer = 0;
            decisionMade = false;
            cooldownDrop = 100;

            if (held == false) {
                System.out.println("random block");
                piece = blocks.get(0);
                blocks.remove(0);
                blocks.add((int) (Math.random() * 7));
            } else if (held == true) {
                System.out.println("held block");
                piece = holdPiece;
                held = false;
            }
            pointer.x = 5;

            for (int i = 0; i < 14; i++) {
                for (int j = 0; j < 4; j++) {
                    scores[i][j] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                scores[0][i] = -10000;
            }

        }
        if (decisionMade) {
            decisionMadeBuffer = 1;
        }

        switch (piece) {
            case 0:
                rotation = 0;
                pointer.y = 0;
                break;
            case 1:
                rotation = 2;
                pointer.y = 1;
                break;
            case 2:
                rotation = 0;
                pointer.y = 1;
                break;
            case 3:
                rotation = 0;
                pointer.y = 0;
                break;
            case 4:
                rotation = 0;
                pointer.y = 0;
                break;
            case 5:
                rotation = 2;
                pointer.y = 1;
                break;
            case 6:
                rotation = 0;
                pointer.y = 0;
                break;

        }

        // System.out.println(Arrays.toString(blocks.toArray()));

    }

    int countdrops = 0;

    @Override
    public void actionPerformed(ActionEvent e) {

        // piece = 4;
        // rotation = 1;
        //
        // System.out.println(pointer.x + " posx");
        // System.out.println(moveable);

        ticker++;

        if (ticker % speed == 0) {

            pointer.y++;
            setPiece();
            updateBoard();

        }

        // System.out.println(decisionMade);
        // decisionMade = true;
        if (aiPlaying == true && decisionMade == false) {
            pointer.x = testMoveX;
            rotation = testRot;
            // robot.keyPress(KeyEvent.VK_SHIFT); ////// USE THIS ONE IF YOU WANNA SEE STUFF
            // HAPPEN SLOWLY SO YOU CAN SEE
            ////// IT
            robot.keyPress(KeyEvent.VK_SPACE); /////// AND USE THIS ONE IF YOU JUST WANNA
            // ZOOOOOOOM CYCLE THROUGH ALL
            /////// (this the one we acc gonna use)
        } else if (aiPlaying == true) {
            robot.keyRelease(KeyEvent.VK_SPACE); /////// AND USE THIS ONE IF YOU JUST WANNA

        }

        // if the piece cannot move further
        boolean ready = false;

        // if there is a Y below any of the pieces

        if (lowestPoint[piece][rotation] + pointer.y < 19) {
            for (int i = 0; i < 4; i++) {

                if (board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 1] == fillType[0]
                        ||board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 1] == fillType[1]
                        ||board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 1] == fillType[2]
                        ||board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 1] == fillType[3]
                        ||board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 1] == fillType[4]
                        ||board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 1] == fillType[5]
                        ||board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 1] == fillType[6]) {
                    ready = true;
                }
            }
        }
        // if == 17 OR the subsequent character in the array is a filled piece

        if (lowestPoint[piece][rotation] + pointer.y == 19 || ready == true) {

            canHold = true;

            if (aiPlaying == false || decisionMade == true) {
                fillSpaces();
                boolean[] clear = new boolean[20];
                for (int i = 0; i < 10; i++) {

                    clear[i] = false;

                }

                for (int y = 0; y < 20; y++) {

                    int increment = 2;
                    while (board[increment][y] == fillType[0] || board[increment][y] == fillType[1] || board[increment][y] == fillType[2]
                            || board[increment][y] == fillType[3] || board[increment][y] == fillType[4] || board[increment][y] == fillType[5]
                            || board[increment][y] == fillType[6]) {
                        increment++;
                        if (increment == 12) {
                            clear[y] = true;
                            System.out.println(y + "   clear");

                            for (int i = y; i > 0; i--) {
                                for (int j = 2; j < 12; j++) {

                                    board[j][i] = board[j][i - 1];
                                }
                            }

                            break;
                        }
                    }

                }
            } else { /////////////////// AI IS PLAYING

                // for (int i = 0; i < 20; i++) {
                // for (int j = 0; j < 10; j++) {
                // System.out.print(board[j][i]);
                // }
                // System.out.println();
                // }
                // System.out.println(pointer.x + " : " + rotation);

                // countdrops++;
                // System.out.println(countdrops);

                //////////// CALCULATE SCORE FOR THE MOVE

                /*
                 * SCORES (for now): +1 - adjacent to another block | +2 - clears a line | -3 -
                 * creates a hole
                 *
                 */

                // Checks for vertical adjacent blocks
                if (pointer.y + lowestPoint[piece][testRot] < 19) {
                    for (int i = 0; i < 4; i++) {

                        if (board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y) + 1] == fillType[0]
                                || board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y) + 1] == fillType[1]
                                || board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y) + 1] == fillType[2]
                                || board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y) + 1] == fillType[3]
                                || board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y) + 1] == fillType[4]
                                || board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y) + 1] == fillType[5]
                                || board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y) + 1] == fillType[6]) {

                            scores[testMoveX][testRot] += 1;
                        }
                    }
                }

                // Checks for horizontal adjacent blocks
                // if block is all the way to the left
                if (pointer.x + leftestPoint[piece][testRot] <= 0) {
                    System.out.println("block is touching left wall");

                    for (int i = 0; i < 4; i++) {

                        if (board[pointer.x + minos[piece][testRot][i].x + 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[0]
                                || board[pointer.x + minos[piece][testRot][i].x + 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[1]
                                || board[pointer.x + minos[piece][testRot][i].x + 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[2]
                                || board[pointer.x + minos[piece][testRot][i].x + 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[3]
                                || board[pointer.x + minos[piece][testRot][i].x + 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[4]
                                || board[pointer.x + minos[piece][testRot][i].x + 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[5]
                                || board[pointer.x + minos[piece][testRot][i].x + 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[6]) {

                            scores[testMoveX][testRot] += 1;
                        }
                    }

                    // if block is all the way to the right
                } else if (testMoveX + rightestPoint[piece][testRot] >= 20) {

                    System.out.println("block is touching right wall");

                    for (int i = 0; i < 4; i++) {

                        if (board[pointer.x + minos[piece][testRot][i].x - 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[0]
                                || board[pointer.x + minos[piece][testRot][i].x - 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[1]
                                || board[pointer.x + minos[piece][testRot][i].x - 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[2]
                                || board[pointer.x + minos[piece][testRot][i].x - 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[3]
                                || board[pointer.x + minos[piece][testRot][i].x - 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[4]
                                || board[pointer.x + minos[piece][testRot][i].x - 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[5]
                                || board[pointer.x + minos[piece][testRot][i].x - 1][(pointer.y + minos[piece][testRot][i].y)] == fillType[6]) {

                            scores[testMoveX][testRot] += 1;
                        }

                    }
                    // if block is not touching any walls
                } else {

                    System.out.println("block is not touching a wall");

                    for (int j = -1; j < 1; j++) {

                        for (int i = 0; i < 4; i++) {

                            if (board[pointer.x + minos[piece][testRot][i].x + j][(pointer.y + minos[piece][testRot][i].y)] == fillType[0]
                                    || board[pointer.x + minos[piece][testRot][i].x + j][(pointer.y + minos[piece][testRot][i].y)] == fillType[1]
                                    || board[pointer.x + minos[piece][testRot][i].x + j][(pointer.y + minos[piece][testRot][i].y)] == fillType[2]
                                    || board[pointer.x + minos[piece][testRot][i].x + j][(pointer.y + minos[piece][testRot][i].y)] == fillType[3]
                                    || board[pointer.x + minos[piece][testRot][i].x + j][(pointer.y + minos[piece][testRot][i].y)] == fillType[4]
                                    || board[pointer.x + minos[piece][testRot][i].x + j][(pointer.y + minos[piece][testRot][i].y)] == fillType[5]
                                    ||board[pointer.x + minos[piece][testRot][i].x + j][(pointer.y + minos[piece][testRot][i].y)] == fillType[6]) {

                                scores[testMoveX][testRot] += 1;
                            }

                        }
                    }
                }

                // Checks for holes
                bigLoop: for (int i = 0; i < 4; i++) {

                    if ((pointer.y + minos[piece][testRot][i].y) + 1 < 20) {

                        if (board[pointer.x + minos[piece][testRot][i].x][(pointer.y + minos[piece][testRot][i].y)
                                + 1] == empty) {

                            System.out.println("there's a hole");
                            scores[testMoveX][testRot] -= 100;
                            break bigLoop;

                        }
                    }

                }

                // Checks for cleared lines

                boolean[] clear = new boolean[20];
                for (int i = 2; i < 12; i++) {

                    clear[i] = false;

                }

                for (int y = 0; y < 20; y++) {

                    int increment = 2;
                    while (board[increment][y] == fillType[0] || board[increment][y] == fillType[1] || board[increment][y] == fillType[2]
                            || board[increment][y] == fillType[3] || board[increment][y] == fillType[4] || board[increment][y] == fillType[5]
                            || board[increment][y] == fillType[6] || board[increment][y] == 'Z') {
                        increment++;
                        if (increment == 12) {
                            clear[y] = true;
                            System.out.println(y + "   clear");

                            // for (int i = y; i > 0; i--) {
                            // for (int j = 0; j < 10; j++) {
                            //
                            // board[j][i] = board[j][i - 1];
                            // }
                            // }

                            System.out.println("possible cleared line(s)");
                            scores[testMoveX][testRot] += 2;
                            break;
                        }
                    }

                }

                // lower is more points
                for (int i = 19; i > 0; i--) {
                    if (pointer.y == i) {
                        scores[testMoveX][testRot] += i * 2;
                    }
                }

                if (leftestPoint[piece][testRot] + testMoveX < 2) {
                    scores[testMoveX][testRot] = -1000000;
                    scores[testMoveX - 1][testRot] = -1000000;
                }

                if (rightestPoint[piece][testRot] + testMoveX > 9) {

                    for (int f = 2; testMoveX + f <= 14; f++) {
                        scores[testMoveX + f][testRot] = -1000000;
                    }

                    System.out.println("Score for " + testMoveX + ", " + testRot + ": " + scores[testMoveX][testRot]);

                    System.out.println("finished move");

                    for (int i = 2; i < 12; i++) {
                        for (int j = 0; j < 20; j++) {
                            if (board[i][j] == 'Z') {
                                board[i][j] = 'x';
                            }
                        }
                    }

                    // do {
                }

                testRot++;
                // if (testRot >3) {
                // break;
                // }
                // System.out.println(testMoveX + rightestPoint[piece][testRot] + " place");
                // System.out.println(testRot + " testrot");
                //
                // } while (testMoveX + rightestPoint[piece][testRot] > 9);

                if (testRot > 3) {
                    testRot = 0;
                    testMoveX++;

                    System.out.println(testMoveX + rightestPoint[piece][testRot] + "      AT THIS POINT");
                    if (testMoveX + rightestPoint[piece][testRot] > 13) {
                        testMoveX = 1;
                        /////////////////////// MAKE A DECISION ON THE PIECE TO DROP BASED ON SCORES

                        // finds the highest score and which X and dir it is
                        int highest = scores[0][3];
                        int x = 1;
                        int r = 0;
                        for (int i = 0; i < 14; i++) {
                            for (int j = 0; j < 4; j++) {
                                if (scores[i][j] > highest) {
                                    highest = scores[i][j];
                                    x = i;
                                    r = j;
                                }
                            }
                        }

                        decisionMoveX = x;
                        decisionRot = r;
                        System.out.println("Highest score is: " + highest + ", Move is: " + x + "," + r);
                        decisionMade = true;
                        // timer.stop();
                    }
                }
                // decisionMade = false;
            }
            newPiece();

        }

        moveable = true;

        if (lowestPoint[piece][rotation] + pointer.y < 17) {
            for (int i = 0; i < 4; i++) {

                if (board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 2] == fillType[0]
                        || board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 2] == fillType[1]
                        || board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 2] == fillType[2]
                        || board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 2] == fillType[3]
                        || board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 2] == fillType[4]
                        || board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 2] == fillType[5]
                        || board[pointer.x + minos[piece][rotation][i].x][(pointer.y + minos[piece][rotation][i].y) + 2] == fillType[6]) {
                    moveable = false;
                }
            }
        }

        if (moveable = true && pointer.y > 1) {

            if (keyArray[0] && cooldownRotate < 0) {
                cooldownRotate = 100;
                if (rotation < 3) {
                    rotation += 1;
                } else {
                    rotation = 0;
                }

                try {

                    if (leftestPoint[piece][rotation] + pointer.x < 0) {
                        pointer.x += 1;
                    }
                    if (rightestPoint[piece][rotation] + pointer.x > 9) {
                        if (piece == 0) {
                            pointer.x -= 2;
                        } else {
                            pointer.x -= 1;

                        }
                    }

                } catch (Exception error) {

                }

                setPiece();

            }
            if (keyArray[1] && cooldownRotate < 0) {
                cooldownRotate = 100;
                if (rotation > 0) {
                    rotation -= 1;
                } else {
                    rotation = 3;
                }

                try {

                    if (leftestPoint[piece][rotation] + pointer.x < 0) {
                        pointer.x += 1;
                    }
                    if (rightestPoint[piece][rotation] + pointer.x > 9) {
                        if (piece == 0) {
                            pointer.x -= 2;
                        } else {
                            pointer.x -= 1;

                        }
                    }

                } catch (Exception error) {

                }

                setPiece();

            }

            if (keyArray[2] && cooldownMove < 0) {
                cooldownMove = 80;
                boolean moveable = true;
                if (canMove(-1)) {
                    checkColumn: for (int i = 0; i < 20; i++) {
                        if (board[2][i] == 'O') {
                            moveable = false;
                            break checkColumn;
                        }
                    }
                    if (moveable) {
                        pointer.x -= 1;
                    }
                }
            }
            if (keyArray[3] && cooldownMove < 0) {
                cooldownMove = 80;
                boolean moveable = true;
                if (canMove(1)) {
                    checkColumn: for (int i = 0; i < 20; i++) {
                        if (board[11][i] == 'O' && canMove(1)) {
                            moveable = false;
                            break checkColumn;
                        }
                    }
                    if (moveable) {
                        pointer.x += 1;
                    }
                }
            }
            if (keyArray[5] && canHold == true) {

                canHold = false;
                holdPiece = piece;
                newPiece();

                held = true;

                System.out.println("hold piece: " + holdPiece);
            }

            if (keyArray[6]) {
                speed = 30;
            } else if (keyArray[4] && cooldownDrop < 0) {
                speed = 1;
            } else {
                speed = 80;
            }
        }

        updateBoard();
        cooldownMove--;
        cooldownDrop--;
        cooldownRotate--;

    }

    // replaces X char with Y char when board piece position is final
    private void fillSpaces() {

        if (pointer.y > 0) {
            // re sets characters
            for (int i = 0; i < 4; i++) {

                System.out.println(pointer.y);
                System.out.println(minos[piece][rotation][i].y);

                board[pointer.x + minos[piece][rotation][i].x][pointer.y + minos[piece][rotation][i].y] = fillType[piece];
            }

            System.out.println("filled spaces");
        } else {
            endGame();
        }
    }

    private boolean canMove(int dir) {
        System.out.println("direction: " + dir);

        if (leftestPoint[piece][rotation] + pointer.x > 0 && rightestPoint[piece][rotation] + pointer.x < 9) {
            for (int i = 0; i < 4; i++) {

                if (board[(pointer.x + minos[piece][rotation][i].x) + dir][(pointer.y + minos[piece][rotation][i].y)] == fillType[0]
                        || board[(pointer.x + minos[piece][rotation][i].x) + dir][(pointer.y + minos[piece][rotation][i].y)] == fillType[1]
                        || board[(pointer.x + minos[piece][rotation][i].x) + dir][(pointer.y + minos[piece][rotation][i].y)] == fillType[2]
                        || board[(pointer.x + minos[piece][rotation][i].x) + dir][(pointer.y + minos[piece][rotation][i].y)] == fillType[3]
                        || board[(pointer.x + minos[piece][rotation][i].x) + dir][(pointer.y + minos[piece][rotation][i].y)] == fillType[4]
                        || board[(pointer.x + minos[piece][rotation][i].x) + dir][(pointer.y + minos[piece][rotation][i].y)] == fillType[5]
                        || board[(pointer.x + minos[piece][rotation][i].x) + dir][(pointer.y + minos[piece][rotation][i].y)] == fillType[6]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void endGame() {

        timer.stop();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_UP) {
            keyArray[0] = true;
            aiPlaying = true;
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            keyArray[1] = true;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            keyArray[2] = true;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            keyArray[3] = true;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            keyArray[4] = true;
        }
        if (keyCode == KeyEvent.VK_H) {
            keyArray[5] = true;
        }
        if (keyCode == KeyEvent.VK_SHIFT) {
            keyArray[6] = true;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {

        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_UP) {
            keyArray[0] = false;
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            keyArray[1] = false;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            keyArray[2] = false;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            keyArray[3] = false;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            keyArray[4] = false;
        }
        if (keyCode == KeyEvent.VK_H) {
            keyArray[5] = false;
        }
        if (keyCode == KeyEvent.VK_SHIFT) {
            keyArray[6] = false;
        }

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

}







