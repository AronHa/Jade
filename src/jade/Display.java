package jade;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.ImageObserver;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Uses JSwing to create the Display for Jade
 * @author Aron Harder
 * @author Mark Harder
 * @since 2014/07/25
 */

public class Display extends Thread implements MouseListener {
	private JFrame frame; //the frame for the graphics
	private Canvas canvas; //The canvas on which everything is painted
	private BufferStrategy strategy; //The strategy used to update the graphics
	private Graphics graphics; //The graphics
	private boolean isRunning; //Whether the game is running
	
	private int distmoved = 0;
	private int[] swap = {-1,-1,-1,-1}; //The gems being swapped
	private boolean allowReverse = true; //allowReverse whether to allow switching gems back if no match
	private int stage = 0; //0 is player move, 2 is enemy move, 1 and 3 are board move
	
	private Color[] colors = {
		Color.green,
		Color.yellow,
		Color.red,
		Color.cyan,
		Color.magenta,
		Color.gray,
		Color.blue
	};
	private Image[] gems = {
	Toolkit.getDefaultToolkit().getImage("gems/air.png"),
	Toolkit.getDefaultToolkit().getImage("gems/earth.png"),
	Toolkit.getDefaultToolkit().getImage("gems/fire.png"),
	Toolkit.getDefaultToolkit().getImage("gems/ice.png"),
	Toolkit.getDefaultToolkit().getImage("gems/rad.png"), //radiation, poison, etc.
	Toolkit.getDefaultToolkit().getImage("gems/stone.png"),
	Toolkit.getDefaultToolkit().getImage("gems/water.png")
	}; //All of the gem images
	private ImageObserver observer;
	
	private static final int WIDTH = 600; //The width of the window
	private static final int HEIGHT = 400; //The height of the window
	private static final int BARSIZE = 22; //The size of the top bar
	private static final int GEMSIZE = 50; //The size of each gem
	private static final int SPEED = 8;

	private Board board;
	private Player player;
	private Opponent enemy;
	private Player ghost; //Lets the game check removing gems and stabalize in the beginning
	
	public Display(){
		player = new Player();
		enemy = new Opponent();
		ghost = new Player();
		board = new Board();
		//int[][] debug = {{1,2,3,4,5,6,7,1},{2,3,4,5,6,7,1,2},{3,4,5,6,7,1,2,3},{4,5,6,7,1,2,3,4},{5,6,7,1,2,3,4,5},{3,7,1,2,3,4,5,6},{7,1,2,3,4,5,6,7},{7,4,5,6,1,2,3,4}};
		//int[][] debug = {{1,4,5,6,7,3,4,5},{5,1,7,3,4,5,6,7},{1,3,4,5,6,7,3,4},{4,5,6,7,3,4,5,6},{6,7,2,4,5,6,7,3},{3,4,5,2,2,3,4,5},{5,6,2,3,4,5,6,7},{2,2,4,5,6,7,3,4}};
		//int[][] debug = {{2,6,5,7,6,5,7,6},{6,2,4,6,5,4,6,5},{2,4,3,5,4,3,5,4},{4,3,7,4,3,7,4,3},{3,7,6,3,7,6,3,7},{7,6,5,7,6,5,7,6},{6,5,4,6,5,4,1,5},{5,4,3,5,1,1,5,1}};
		//int[][] debug = {{2,6,5,7,6,5,7,6},{6,2,4,6,5,4,6,5},{2,4,3,5,4,3,5,4},{4,3,7,4,3,7,4,3},{3,7,6,3,7,6,3,7},{7,6,5,7,6,5,7,6},{6,5,4,6,5,4,1,2},{5,4,3,5,1,1,2,1}};
		//board = new Board(debug);
		System.out.println(board);

		runBoard();
		
		frame = new JFrame("Jade");
		frame.addWindowListener(new FrameClose());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(WIDTH,HEIGHT+BARSIZE);
		
		canvas = new Canvas();
		canvas.setSize(WIDTH,HEIGHT+BARSIZE);
		frame.add(canvas);
		canvas.addMouseListener(this);

		canvas.createBufferStrategy(2);
		do {
			strategy = canvas.getBufferStrategy();
		} while (strategy == null);
		
		isRunning = true;
	}
	
	/**
	 * A class to close the window
	 * @author aronharder
	 *
	 */
	public class FrameClose extends WindowAdapter {
		@Override
		public void windowClosing(final WindowEvent e){
			System.exit(0);
		}
	}
	
	/**
	 * Returns the graphics from the buffer
	 * @return graphics
	 */
	public Graphics getBuffer(){
		if (graphics == null){
			try{
				graphics = strategy.getDrawGraphics();
			} catch(IllegalStateException e){
				return null;
			}
		}
		return graphics;
	}
	
	/**
	 * Updates the screen to create changes
	 * @return whether the contents were lost
	 */
	private boolean updateScreen(){
		graphics.dispose();
		graphics = null;
		try {
			strategy.show();
			Toolkit.getDefaultToolkit().sync();
			return (!strategy.contentsLost());
		} catch (NullPointerException e){
			return true;
		} catch (IllegalStateException e){
			return true;
		}
	}
	
	/**
	 * runs the graphics, updating with 34 fps
	 */
	public void run(){
		long fpsWait = (long) (1.0/30 * 1000);
		main: while (isRunning){
			long renderStart = System.nanoTime();
			updateGame();
			
			do {
				Graphics bg = getBuffer();
				if (!isRunning){
					break main;
				}
				renderGame(bg);
				bg.dispose();
			} while (!updateScreen());
			
			long renderTime = (System.nanoTime() - renderStart) / 1000000;
			try {
				Thread.sleep(Math.max(0,fpsWait - renderTime));
			} catch (InterruptedException e){
				Thread.interrupted();
				break;
			}
			renderTime = (System.nanoTime() - renderStart) / 1000000;
		}
		frame.dispose();
	}
	
	/**
	 * Draws on the canvas
	 * @param g
	 */
	public void renderGame(Graphics g){
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 100, 400);
		g.setColor(Color.BLACK);
		g.fillRect(100, 0, 400, 400); //The game board
		g.setColor(Color.WHITE);
		g.fillRect(500, 0, 100, 400);
		Gem[][] grid = board.getBoard();
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[i].length; j++){
				if (grid[i][j].isSelected()){
					g.setColor(Color.YELLOW);
					g.fillRect(j*GEMSIZE+100,i*GEMSIZE,GEMSIZE,GEMSIZE);
				}
				if (grid[i][j].getValue() > 0){
					g.drawImage(gems[grid[i][j].getValue()-1],j*GEMSIZE+(distmoved*grid[i][j].getxMove())+100,i*GEMSIZE+(distmoved*grid[i][j].getyMove()),observer);
				}
			}
		}
		int[] vals = player.getGems();
		for (int i = 0; i < vals.length; i++){
			g.setColor(colors[i]);
			g.drawString(String.valueOf(vals[i]), 50, 50*(i+1));
		}
		vals = enemy.getGems();
		for (int i = 0; i < vals.length; i++){
			g.setColor(colors[i]);
			g.drawString(String.valueOf(vals[i]), 550, 50*(i+1));
		}
		//This is where you put more graphics stuff.
	}
	
	/**
	 * This is the logic for moving gems
	 */
	public void updateGame(){
		//4 Stages: Player makes a move, move board, enemy makes a move, move board
		if (stage == 0){ //Player Turn
			if (!board.checkMoves()){
				System.out.println("No more moves");
				System.out.println(board.toString());
				System.exit(0);
			}
			if (swap[0] != -1){
				stage++;
			}
		} else if (stage == 1 || stage == 3){ //Move board
			//Makes things move
			distmoved+=SPEED;
			//If things have moved enough, stop them for the end of a cycle
			if (distmoved >= GEMSIZE){
				if (swap[0] != -1){
					switchGems(swap[0], swap[1], swap[2], swap[3]);
				}
				board.moveGems();
				distmoved = 0;
				//If you aren't reversing, and there's no empty spaces
				if (swap[0] == -1 && !board.runGravity()){ //If you take swap[0] == -1 out, runGrav breaks y-movement
					if (stage == 1){
						board.checkBoard(player,true,true);
					} else {
						board.checkBoard(enemy,true,true);
					}
				}
			}
			
			if (swap[0] == -1 && board.runGravity() == false){
				stage++;
			}
		} else if (stage == 2){ //Enemy turn
			/**try {
				Thread.sleep(300); //Make the enemy wait a little bit so that the player doesn't get overwhelmed
			} catch (InterruptedException e){
				Thread.interrupted();
			}*/

			if (!board.checkMoves()){
				System.out.println("No more moves");
				System.out.println(board.toString());
				System.exit(0);
			}
			Gem[][] grid = board.getBoard();
			int[] move = enemy.makeMove(board);
			
			setSwap(move[0], move[1], move[2], move[3]);
			board.setColumns(move[0],move[2]);
			grid[move[1]][move[0]].setxMove(move[2]-move[0]);
			grid[move[3]][move[2]].setxMove(move[0]-move[2]);
			grid[move[1]][move[0]].setyMove(move[3]-move[1]);
			grid[move[3]][move[2]].setyMove(move[1]-move[3]);
			stage++;
		} else { //Reset to player's turn
			stage = 0;
		}
	}
	
	public Board getBoard(){
		return board;
	}
	
	private void runBoard(){
		board.setColumns(0,7);
		while (! board.checkBoard(ghost,true,false)){
			while (board.runGravity()){
				board.moveGems();
			}
		}
		board.setColumns(-1,-1);
	}
	
	private void setSwap(int x1, int y1, int x2, int y2){
		swap[0] = x1;
		swap[1] = y1;
		swap[2] = x2;
		swap[3] = y2;
	}
	
	/**
	 * Switches gems on the board
	 * @param x1 x-coord of the first gem
	 * @param y1 y-coord of the first gem
	 * @param x2 x-coord of the second gem
	 * @param y2 y-coord of the second gem
	 * @return Whether there is three-in-a-row after the switch
	 */
	private boolean switchGems(int x1, int y1, int x2, int y2){
		int temp = board.getBoard()[y1][x1].getValue();
		board.getBoard()[y1][x1].setValue(board.getBoard()[y2][x2].getValue());
		board.getBoard()[y2][x2].setValue(temp);
		
		//Reverse the gems, there was no match
		if (board.checkBoard(ghost,false,false) && allowReverse){
			if (x1 < x2){
				board.getBoard()[y1][x1].setxMove(1);
				board.getBoard()[y1][x1].setyMove(0);
				board.getBoard()[y2][x2].setxMove(-1);
				board.getBoard()[y2][x2].setyMove(0);
			} else if (x1 > x2){
				board.getBoard()[y1][x1].setxMove(-1);
				board.getBoard()[y1][x1].setyMove(0);
				board.getBoard()[y2][x2].setxMove(1);
				board.getBoard()[y2][x2].setyMove(0);
			} else if (y1 < y2){
				board.getBoard()[y1][x1].setxMove(0);
				board.getBoard()[y1][x1].setyMove(1);
				board.getBoard()[y2][x2].setxMove(0);
				board.getBoard()[y2][x2].setyMove(-1);
			} else if (y1 > y2){
				board.getBoard()[y1][x1].setxMove(0);
				board.getBoard()[y1][x1].setyMove(-1);
				board.getBoard()[y2][x2].setxMove(0);
				board.getBoard()[y2][x2].setyMove(1);
			}
			allowReverse = false;	
			setSwap(x2,y2,x1,y1);
			return false;
		//Just swapped the gems to original positions. Stop movement.	
		} else if (board.checkBoard(ghost,false,false)){
			board.getBoard()[y1][x1].setxMove(0);
			board.getBoard()[y1][x1].setyMove(0);
			board.getBoard()[y2][x2].setxMove(0);
			board.getBoard()[y2][x2].setyMove(0);
			allowReverse = true;
			setSwap(-1,-1,-1,-1);
			return false;
		//Found a match, let gravity run?
		} else {
			board.getBoard()[y1][x1].setxMove(0);
			board.getBoard()[y1][x1].setyMove(0);
			board.getBoard()[y2][x2].setxMove(0);
			board.getBoard()[y2][x2].setyMove(0);
			allowReverse = true;
			setSwap(-1,-1,-1,-1);
			return true;
		}
	}
		
	public void mouseClicked(MouseEvent evt) {
		if (stage != 0){
			return;
		}
		int x = evt.getX();
		int y = evt.getY();
		boolean sel = false; //whether to select the gem
		Gem[][] grid = board.getBoard();
		
		if (getLocation(x,y) == 1){
			//When you click on the side bar that contains player info
		} else if (getLocation(x,y) == 2){
			x = (x-100)/50;
			y = y/50;
			if (y > 0 && grid[y-1][x].isSelected()){
				setSwap(x, y, x, y-1);
				board.setColumns(x,x);
				grid[y][x].setyMove(-1);
				grid[y-1][x].setyMove(1);
			} else if (y < grid.length-1 && grid[y+1][x].isSelected()){
				setSwap(x, y, x, y+1);
				board.setColumns(x,x);
				grid[y][x].setyMove(1);
				grid[y+1][x].setyMove(-1);
			} else if (x > 0 && grid[y][x-1].isSelected()){
				setSwap(x, y, x-1, y);
				board.setColumns(x-1,x);
				grid[y][x].setxMove(-1);
				grid[y][x-1].setxMove(1);
			} else if (x < grid[y].length-1 && grid[y][x+1].isSelected()){
				setSwap(x, y, x+1, y);
				board.setColumns(x,x+1);
				grid[y][x].setxMove(1);
				grid[y][x+1].setxMove(-1);
			} else if (! grid[y][x].isSelected()) {
				sel = true;
			//} else {	//Debug setting: double click makes turns a gem into air, or prints the board
			//	grid[y][x].setValue(1);
			//	System.out.println(board);
			}
		} else if (getLocation(x,y) == 3){
			//When you click on the side bar that contains enemy info
		}

		board.deselectAll();
		if (sel){
			grid[y][x].select();
		}
	}
	//Returns 1 for player side, 2 for game board, and 3 for enemy side.
	//TODO: Add 1 and 3 to this def.
	private int getLocation(int x, int y){
		if (x >= 100 && x <= 499 && y >= 0 && y <= 399){
			return 2;
		}
		return 0;
	}
	
	/**
	 * These four methods not used, but needed for MouseListener
	 */
	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}
	public void mousePressed(MouseEvent evt) {}
	public void mouseReleased(MouseEvent evt) {}
}
