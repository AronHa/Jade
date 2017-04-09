package jade;

/**
 * The Gems for the game Board
 * @author Aron Harder
 * @since 2014/07/25
 */
public class Gem {
	private boolean selected = false; //Whether the gem has been selected
	private int xMove = 0; //Which x direction to move the gem
	private int yMove = 0; //Which y direction to move the gem
	private int row; //The row of the gem
	private int col; //The column of the gem
	private int value; //The value of the gem
	
	/**
	 * Constructs a gem with all values set to 0
	 */
	public Gem(){
		row = 0;
		col = 0;
		value = 0;
	}
	/**
	 * Constructs a gem with given values
	 * @param initRow - The row of the gem
	 * @param initCol - The column of the gem
	 * @param initVal - The value of the gem
	 */
	public Gem(int initRow, int initCol, int initVal){
		row = initRow;
		col = initCol;
		value = initVal;
	}
	
	/**
	 * Select of deselects the gem
	 */
	public void select(){
		selected = true;
	}
	public void deselect(){
		selected = false;
	}

	/**
	 * Tells the gem to move in a certain direction
	 */
	public void setxMove(int newX){
		xMove = newX;
	}
	public void setyMove(int newY){
		yMove = newY;
	}
	
	/**
	 * Sets the position of the gem
	 * @param newRow - The row to move the gem to
	 * @param newCol - The column to move the gem to
	 */
	public void setPos(int newRow, int newCol){
		row = newRow;
		col = newCol;
	}
	/**
	 * Sets the value of the gem
	 * @param newVal - The value to set the gem to
	 */
	public void setValue(int newValue){
		value = newValue;
	}

	/**
	 * Returns whether the gem is selected
	 * @return selected
	 */
	public boolean isSelected(){
		return selected;
	}
	/**
	 * Returns what direction(s) the gem is moving
	 * @return xMove/yMove
	 */
	public int getxMove(){
		return xMove;
	}
	public int getyMove(){
		return yMove;
	}
	/**
	 * Returns the row of the gem
	 * @return row
	 */
	public int getRow(){
		return row;
	}
	/**
	 * Returns the column of the gem
	 * @return col
	 */
	public int getCol(){
		return col;
	}
	/**
	 * Returns the value of gem
	 * @return value
	 */
	public int getValue(){
		return value;
	}
	
	/**
	 * Displays the value and position of the gem
	 * @return A string with the value, row, and column
	 */
	public String toString(){
		return "Gem with value "+value+" at ("+col+","+row+")";
	}
}
