package jade;

import java.util.Random;

/**
 * The game Board for Jade
 * @author Aron Harder
 * @since 2014/07/25
 */

public class Board {
	private final int HEIGHT = 8;
	private final int WIDTH = 8;
	private Gem[][] grid;
	private boolean moving = false;
	private int combo = 1;
	private int[] columns = {-1,-1}; //These are the bookends of the columns that should be falling/checked
	
	/**
	 * Constructs a new randomized board
	 */
	public Board(){
		Random r = new Random();
		grid = new Gem[HEIGHT][WIDTH];
		for (int i = 0; i < grid.length; i++){
			for(int j = 0; j < grid[i].length; j++){
				grid[i][j] = new Gem(i,j,r.nextInt(7)+1);
			}
		}		
	}
	
	public Board(Gem[][] initBoard){
		grid = initBoard;
	}
	
	public Board(int[][] initBoard){ //This is a debug method for testing
		grid = new Gem[HEIGHT][WIDTH];
		for (int i = 0; i < initBoard.length; i++){
			for (int j = 0; j < initBoard[i].length; j++){
				grid[i][j] = new Gem(i,j,initBoard[i][j]);
			}
		}
	}
	
	/**
	 * Checks for 3 in a row
	 * @param destroy - whether to destroy the gems
	 * @return
	 */
	public boolean checkBoard(Player p, boolean destroy, boolean incCombo){
		if (moving){
			return false;
		}
		boolean noMatches = true;
		int leftPos = columns[0]-2;
		if (leftPos < 0) leftPos = 0;
		for (int i = 0; i < HEIGHT; i++){
			for (int j = leftPos; j <= columns[1]; j++){
				boolean across = checkAcross(j, i, destroy);
				boolean down = checkDown(j, i, destroy);
				if  (across || down){
					noMatches = false;
				}
			}
		}
		if (! noMatches){
			if (incCombo){
				combo++;
			}
			removeDestroyed(p);
		} else {
			combo = 1;
		}
		return noMatches;
	}
	
	private void removeDestroyed(Player p){
		for (int i = 0; i < HEIGHT; i++){
			for (int j = 0; j < WIDTH; j++){
				if (grid[i][j].getValue() < 0){
					p.addGem(-grid[i][j].getValue());
					grid[i][j].setValue(0);
				}
			}
		}
	}
	
	/**
	 * Checks for three in a row going across
	 * @param x - the x coordinate of the leftmost gem
	 * @param y - the y coordinate of the leftmost gem
	 * @param destroy - whether to destroy the gems
	 * @return whether there are three or more in a row
	 */
	private boolean checkAcross(int x, int y, boolean destroy){
		if (x > 5){
			return false;
		} else if (y > 7){
			return false;
		} else {
			int value = Math.abs(grid[y][x].getValue());
			if (Math.abs(grid[y][x+1].getValue()) == value &&
				Math.abs(grid[y][x+2].getValue()) == value){
				if (destroy){
					for (int i = x; i < WIDTH && Math.abs(grid[y][i].getValue()) == value; i++){
						grid[y][i].setValue(-value);
					}
				}
				if (x < columns[0]){
					columns[0] = x;
				} else if (x+2 > columns[1]){
					columns[1] = x+2;
				}
				return true;
			} else {
				return false;
			}
		}
	}
	/**
	 * Checks for three in a row going down
	 * @param x - the x coordinate of the topmost gem
	 * @param y - the y coordinate of the topmost gem
	 * @param destroy - whether to destroy the gems
	 * @return whether there are three or more in a row
	 */
	private boolean checkDown(int x, int y, boolean destroy){
		if (y > 5){
			return false;
		} else if (x > 7){
			return false;
		} else {
			int value = Math.abs(grid[y][x].getValue());
			if (Math.abs(grid[y+1][x].getValue()) == value &&
				Math.abs(grid[y+2][x].getValue()) == value){
				if (destroy){
					for (int i = y; i < HEIGHT && Math.abs(grid[i][x].getValue()) == value; i++){
						grid[i][x].setValue(-value);
					}
				}
				return true;
			} else {
				return false;
			}
		}
	}
	/**
	 * Runs one turn on the game board
	 * Also checks if there are empty spaces on the board 
	 */
	public boolean runGravity(){
		if (columns[0] < 0) return false;
		boolean anyFall = false;
		for (int i = columns[0]; i <= columns[1]; i++){
			boolean toFall = false;
			for (int j = HEIGHT-1; j >= 0; j--){
				if (!toFall && grid[j][i].getValue() == 0){
					toFall = true;
					anyFall = true;
				} else if (!toFall && grid[j][i].getValue() != 0){
					grid[j][i].setyMove(0);
				} else if (toFall && grid[j][i].getValue() != 0){
					grid[j][i].setyMove(1);
				}
			}
		}
		return anyFall;
	}
	public boolean isThree(){
		if (columns[0] < 0) return false;
		for (int i = 0; i < grid.length; i++){
			for (int j = columns[0]; j <= columns[1]; j++){
				int checkValue = grid[i][j].getValue();
				if (i > 1 && grid[i-1][j].getValue() == checkValue && grid[i-2][j].getValue() == checkValue){
					return true;
				} else if (j > 1 && grid[i][j-1].getValue() == checkValue && grid[i][j-1].getValue() == checkValue){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Moves gems down 1 space if there's nothing directly below them
	 */
	public void moveGems(){
		for (int i = HEIGHT-2; i >= 0; i--){
			for (int j = columns[0]; j <= columns[1]; j++){
				if (grid[i+1][j].getValue() == 0){
					grid[i+1][j].setValue(grid[i][j].getValue());
					grid[i][j].setValue(0);
				}
			}
		}
		for (int j = 0; j < WIDTH; j++){
			Random r = new Random();
			if (grid[0][j].getValue() == 0){
				grid[0][j].setValue(r.nextInt(7)+1);
			}
		}
	}
	
	/**
	 * Checks for possible moves
	 * @return whether there is a possible match
	 */
	//NOTE: You cannot set this to only check relevant columns for efficiency because then it misses possible moves a few columns over.
	public boolean checkMoves(){
		for (int i = 0; i < grid.length; i++){ //i is the y direction
			for (int j = 0; j < grid[i].length; j++){ //j is the x direction
				int value = grid[i][j].getValue();
				//check NW gem
				if (i > 0 && j > 0 && value == grid[i-1][j-1].getValue()){
					if (i-1 > 0 && value == grid[i-2][j].getValue()){
						return true;
					} else if (i-1 > 0 && value == grid[i-2][j-1].getValue()){
						return true;
					} else if (j-1 > 0 && value == grid[i-1][j-2].getValue()){
						return true;
					} else if (j-1 > 0 && value == grid[i][j-2].getValue()){
						return true;
					}
				}
				//check NE gem
				if (i > 0 && j < grid[i].length-1 && value == grid[i-1][j+1].getValue()){
					if (i-1 > 0 && value == grid[i-2][j].getValue()){
						return true;
					} else if (i-1 > 0 && value == grid[i-2][j+1].getValue()){
						return true;
					} else if (j+1 < grid[i].length-1 && value == grid[i-1][j+2].getValue()){
						return true;
					} else if (j+1 < grid[i].length-1 && value == grid[i][j+2].getValue()){
						return true;
					}
				}
				//check SE gem
				if (i < grid.length-1 && j < grid[i].length-1 && value == grid[i+1][j+1].getValue()){
					if (i+1 < grid.length-1 && value == grid[i+2][j].getValue()){
						return true;
					} else if (i+1 < grid.length-1 && value == grid[i+2][j+1].getValue()){
						return true;
					} else if (j+1 < grid[i].length-1 && value == grid[i+1][j+2].getValue()){
						return true;
					} else if (j+1 < grid[i].length-1 && value == grid[i][j+2].getValue()){
						return true;
					}
				}
				//check SW gem
				if (i < grid.length-1 && j > 0 && value == grid[i+1][j-1].getValue()){
					if (i+1 < grid.length-1 && value == grid[i+2][j].getValue()){
						return true;
					} else if (i+1 < grid.length-1 && value == grid[i+2][j-1].getValue()){
						return true;
					} else if (j-1 > 0 && value == grid[i+1][j-2].getValue()){
						return true;
					} else if (j-1 > 0 && value == grid[i][j-2].getValue()){
						return true;
					}
				}
				//check 1 2 1 1 horizontally
				if (j >= 3 && value == grid[i][j-3].getValue()){ //Need at least 4 gems horizontally to check for this one
					if (value == grid[i][j-1].getValue() || value == grid[i][j-2].getValue()) return true;
				}
				//check 1 2 1 1 vertically
				if (i >= 3 && value == grid[i-3][j].getValue()){ //Need at least 4 gems vertically to check for this one
					if (value == grid[i-1][j].getValue() || value == grid[i-2][j].getValue()) return true;
				}
			}
		}
		
		return false;
	}
	
	public void setMoving(boolean value){
		moving = value;
	}
	
	public boolean isMoving(){
		return moving;
	}
	
	public int getCombo(){
		return combo;
	}
	
	public void deselectAll(){
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[i].length; j++){
				grid[i][j].deselect();
			}
		}
	}
	
	/**
	 * Sets the columns
	 * @param newColumns
	 */
	public void setColumns(int newLeft, int newRight){
		columns[0] = newLeft;
		columns[1] = newRight;
	}
	
	/**
	 * Returns the columns
	 * @return columns
	 */
	public int[] getColumns(){
		return columns;
	}
	
	/**
	 * Sets the grid to something new
	 * @param newGrid the new grid
	 */
	public void setBoard(Gem[][] newGrid){
		grid = newGrid;
	}
	
	/**
	 * Returns the board for testing purposes
	 * @return the game board
	 */
	public Gem[][] getBoard(){
		return grid;
	}
	public void setGem(int x, int y, int newValue){
		grid[y][x].setValue(newValue);
	}
	/**
	 * Displays the board in a textual format
	 */
	public String toString(){
		String output = "";
		for (Gem[] row : grid){
			for (Gem gem : row){
				output+= String.valueOf(gem.getValue());
			}
			output+="\n";
		}
		return output;
	}
}

//TODO: Add player information, make this more game-like
//TODO: No moves left isn't 100% accurate, somehow. Need more research to find exact conditions.
//TODO: Sometimes, there's just an empty space that nothing falls into for a turn. Why? Why does it do this?
//NOTE: Could it have something to do with the columns optimization?