package jade;
//TODO: Still doesn't work right
/**
 * 20516502
 * 16620563
 * 55116360
 * 50462403
 * 35112414
 * 05340502
 * 23150561
 * 15031360
 */

/**
 * An AI to work against the player
 * @author Aron Harder
 * @since 2015/04/08
 */
public class Opponent extends Player {
	private static final float DISCOUNT = (float) 0.9;
	/**
	 * Default prefers 0 = air most, 6 = water least
	 */
	public Opponent(){
		super();
	}
	
	public Opponent(float[] initPrefer){
		super(initPrefer);
	}
	
	/**
	 * Makes a move on the game board.
	 * @param board
	 * @return coords of the two gems to switch. Default (no move) is moving top-left gem down 
	 */
	public int[] makeMove(Board board){
		return findMax(board);
	}
	
	private int[] findMax(Board board){
		Gem[][] b = board.getBoard();
		int[] bestMove = {0,0,0,1}; //Save the best move found so far
		int[] bestReward = {-10,-10,-10,-10,-10,-10,-10}; //Save the best reward gotten so far
		int[] bestPlayer = {10,10,10,10,10,10,10};
		int[] noMovePlayer = findMin(board);
		for (int i = 0; i < b.length; i++){
			for (int j = 0; j < b[i].length; j++){
				int[] currentMove = {j,i,j,i}; //Check this move against best at the end
				int[] currentReward = new int[7]; //Check this reward against best at the end
				int[] currentPlayer = new int[7];
				if (i < b.length-1){ //don't move bottom gems down
					currentMove[3]+=1; //Show correct move direction
					int temp = b[i][j].getValue(); //Swap values
					b[i][j].setValue(b[i+1][j].getValue());
					b[i+1][j].setValue(temp);
					currentReward[b[i][j].getValue()-1]+=assessTotal(b, i, j); //Calculate reward
					currentReward[b[i+1][j].getValue()-1]+=assessTotal(b, i+1, j);
					boolean revert = true;
					for (int val : currentReward){
						if (val != 0){
							revert = false;
						}
					}
					if (revert){
						currentPlayer = noMovePlayer;
						if (totalPoints(currentReward)-totalPoints(currentPlayer)*DISCOUNT > totalPoints(bestReward)-totalPoints(bestPlayer)*DISCOUNT){
							bestReward = currentReward;
							bestPlayer = currentPlayer;
							bestMove = currentMove.clone();
						}
						b[i+1][j].setValue(b[i][j].getValue()); //Reset gem values
						b[i][j].setValue(temp);
						currentMove[3]-=1; //Reset currentMove
					} else {
						int iteration = 3; //Running 3 times should be sufficient for 99% of cases
						Gem[][] step = cloneGrid(b);
						while (iteration > 0){ //Calculate rewards gained from falling gems
							step = simulateGravity(step);
							int[] columns = {currentMove[2],currentMove[0]};
							columns[0] -= (Math.max(assessLeft(b,currentMove[1],currentMove[0])-1,assessLeft(b,currentMove[3],currentMove[2])-1));
							columns[1] += (Math.max(assessRight(b,currentMove[1],currentMove[0])-1,assessRight(b,currentMove[3],currentMove[2])-1));
							if (columns[0] > columns[1]){
								columns[0] = currentMove[0];
								columns[1] = currentMove[2];
							}
							for (int r = 0; r < step.length; r++){
								for (int c = columns[0]; c <= columns[1]; c++){
									if (step[r][c].getValue() > 0 && assessTotal(step,r,c) > 0){
										currentReward[step[r][c].getValue()-1]+=1;
									}
								}
							}
							iteration--;
						}
						currentPlayer = findMin(new Board(step)); //Find the best player move from here.
						//If reward is better in some way, record the new move and reward
						if (totalPoints(currentReward)-totalPoints(currentPlayer)*DISCOUNT > totalPoints(bestReward)-totalPoints(bestPlayer)*DISCOUNT){
							bestReward = currentReward;
							bestPlayer = currentPlayer;
							bestMove = currentMove.clone();
						}
						b[i+1][j].setValue(b[i][j].getValue()); //Reset gem values
						b[i][j].setValue(temp);
						currentMove[3]-=1; //Reset currentMove
					}
				}
				currentReward = new int[7]; //Does the whole thing over again moving gem right instead of down
				if (j < b[i].length-1){ //don't move right side gems right
					currentMove[2]+=1;
					int temp = b[i][j].getValue();
					b[i][j].setValue(b[i][j+1].getValue());
					b[i][j+1].setValue(temp);
					currentReward[b[i][j].getValue()-1]+=assessTotal(b, i, j);
					currentReward[b[i][j+1].getValue()-1]+=assessTotal(b, i, j+1);
					boolean revert = true;
					for (int val : currentReward){
						if (val != 0){
							revert = false;
						}
					}
					if (revert){
						currentPlayer = noMovePlayer;
						if (totalPoints(currentReward)-totalPoints(currentPlayer)*DISCOUNT > totalPoints(bestReward)-totalPoints(bestPlayer)*DISCOUNT){
							bestReward = currentReward;
							bestPlayer = currentPlayer;
							bestMove = currentMove.clone();
						}
						b[i][j+1].setValue(b[i][j].getValue()); //Reset gem values
						b[i][j].setValue(temp);
						currentMove[2]-=1; //Reset currentMove
					} else {
						int iteration = 3;
						Gem[][] step = cloneGrid(b);
						while (iteration > 0){
							step = simulateGravity(step);
							int[] columns = {currentMove[2],currentMove[0]};
							columns[0] -= (Math.max(assessLeft(b,currentMove[1],currentMove[0])-1,assessLeft(b,currentMove[3],currentMove[2])-1));
							columns[1] += (Math.max(assessRight(b,currentMove[1],currentMove[0])-1,assessRight(b,currentMove[3],currentMove[2])-1));
							if (columns[0] > columns[1]){
								columns[0] = currentMove[0];
								columns[1] = currentMove[2];
							}
							for (int r = 0; r < step.length; r++){
								for (int c = columns[0]; c <= columns[1]; c++){
									if (step[r][c].getValue() > 0 && assessTotal(step,r,c) > 0){
										currentReward[step[r][c].getValue()-1]+=1;
									}
								}
							}
							iteration--;
						}
						currentPlayer = findMin(new Board(step));
						if (totalPoints(currentReward)-totalPoints(currentPlayer)*DISCOUNT > totalPoints(bestReward)-totalPoints(bestPlayer)*DISCOUNT){
							bestReward = currentReward;
							bestPlayer = currentPlayer;
							bestMove = currentMove.clone();
						}
						b[i][j+1].setValue(b[i][j].getValue());
						b[i][j].setValue(temp);
						currentMove[2]-=1;
					}
				}
			}
		}
		//System.out.println("("+bestMove[0]+","+bestMove[1]+","+bestMove[2]+","+bestMove[3]+")");
		return bestMove;
	}
	
	private int[] findMin(Board board){
		int[] bestPlayer = new int[7];
		Gem[][] b = board.getBoard();
		
		for (int i = 0; i < b.length; i++){
			for (int j = 0; j < b[i].length; j++){
				int[] currentMove = {j,i,j,i}; //Check this move against best at the end
				int[] currentPlayer = new int[7];
				if (b[i][j].getValue() < 0){
					continue;
				}
				if (i < b.length-1){ //don't move bottom gems down
					currentMove[3]+=1; //Show correct move direction
					if (b[i+1][j].getValue() < 0){
						continue;
					}
					int temp = b[i][j].getValue(); //Swap values
					b[i][j].setValue(b[i+1][j].getValue());
					b[i+1][j].setValue(temp);
					currentPlayer[b[i][j].getValue()-1]+=assessTotal(b, i, j); //Calculate reward
					currentPlayer[b[i+1][j].getValue()-1]+=assessTotal(b, i+1, j);
					boolean revert = true;
					for (int val : currentPlayer){
						if (val != 0){
							revert = false;
						}
					}
					if (revert){
						if (totalPoints(currentPlayer) > totalPoints(bestPlayer)){
							bestPlayer = currentPlayer;
						}
						b[i+1][j].setValue(b[i][j].getValue()); //Reset gem values
						b[i][j].setValue(temp);
						currentMove[3]-=1; //Reset currentMove
					} else {
						int iteration = 3; //Running 3 times should be sufficient for 99% of cases
						Gem[][] step = cloneGrid(b);
						while (iteration > 0){ //Calculate rewards gained from falling gems
							step = simulateGravity(step);
							int[] columns = {currentMove[2],currentMove[0]};
							columns[0] -= (Math.max(assessLeft(b,currentMove[1],currentMove[0])-1,assessLeft(b,currentMove[3],currentMove[2])-1));
							columns[1] += (Math.max(assessRight(b,currentMove[1],currentMove[0])-1,assessRight(b,currentMove[3],currentMove[2])-1));
							if (columns[0] > columns[1]){
								columns[0] = currentMove[0];
								columns[1] = currentMove[2];
							}
							for (int r = 0; r < step.length; r++){
								for (int c = columns[0]; c <= columns[1]; c++){
									if (step[r][c].getValue() > 0 && assessTotal(step,r,c) > 0){
										currentPlayer[step[r][c].getValue()-1]+=1;
									}
								}
							}
							iteration--;
						}
						//If reward is better in some way, record the new move and reward
						if (totalPoints(currentPlayer) > totalPoints(bestPlayer)){
							bestPlayer = currentPlayer;
						}
						b[i+1][j].setValue(b[i][j].getValue()); //Reset gem values
						b[i][j].setValue(temp);
						currentMove[3]-=1; //Reset currentMove
					}
				}
				currentPlayer = new int[7]; //Does the whole thing over again moving gem right instead of down
				if (j < b[i].length-1){ //don't move right side gems right
					if (b[i][j+1].getValue() < 0){
						continue;
					}
					currentMove[2]+=1;
					int temp = b[i][j].getValue();
					b[i][j].setValue(b[i][j+1].getValue());
					b[i][j+1].setValue(temp);
					currentPlayer[b[i][j].getValue()-1]+=assessTotal(b, i, j);
					currentPlayer[b[i][j+1].getValue()-1]+=assessTotal(b, i, j+1);
					boolean revert = true;
					for (int val : currentPlayer){
						if (val != 0){
							revert = false;
						}
					}
					if (revert){
						if (totalPoints(currentPlayer) > totalPoints(bestPlayer)){
							bestPlayer = currentPlayer;
						}
						b[i][j+1].setValue(b[i][j].getValue()); //Reset gem values
						b[i][j].setValue(temp);
						currentMove[2]-=1; //Reset currentMove
					} else {
						int iteration = 3;
						Gem[][] step = cloneGrid(b);
						while (iteration > 0){
							step = simulateGravity(step);
							int[] columns = {currentMove[2],currentMove[0]};
							columns[0] -= (Math.max(assessLeft(b,currentMove[1],currentMove[0])-1,assessLeft(b,currentMove[3],currentMove[2])-1));
							columns[1] += (Math.max(assessRight(b,currentMove[1],currentMove[0])-1,assessRight(b,currentMove[3],currentMove[2])-1));
							if (columns[0] > columns[1]){
								columns[0] = currentMove[0];
								columns[1] = currentMove[2];
							}
							for (int r = 0; r < step.length; r++){
								for (int c = columns[0]; c <= columns[1]; c++){
									if (step[r][c].getValue() > 0 && assessTotal(step,r,c) > 0){
										currentPlayer[step[r][c].getValue()-1]+=1;
									}
								}
							}
							iteration--;
						}
						if (totalPoints(currentPlayer) > totalPoints(bestPlayer)){
							bestPlayer = currentPlayer;
						}
						b[i][j+1].setValue(b[i][j].getValue());
						b[i][j].setValue(temp);
						currentMove[2]-=1;
					}
				}
			}
		}
		return bestPlayer;
	}
	
	/**
	 * Assess the total value of a gem's position
	 * @param grid
	 * @param r - the row of the gem
	 * @param c - the column of the gem
	 * @return the total points of that position
	 */
	private int assessTotal(Gem[][] grid, int r, int c){
		int leftRight = assessRight(grid, r, c)+assessLeft(grid, r, c)-1; //-1 because it counts middle gem twice
		int upDown = assessUp(grid, r, c)+assessDown(grid, r, c)-1;

		if (leftRight < 3){ //row of 1, 2 = 0 points
			leftRight = 0;
		} else {
			leftRight-=2; //row of 3 = 1 point, row of 4 = 2, etc.
		}
		if (upDown < 3){
			upDown = 0;
		} else {
			upDown-=2;
		}
		return leftRight+upDown;
	}
	/**
	 * Counts same-colored gems in a certain direction (counts the starting gem as 1)
	 * @param grid
	 * @param r
	 * @param c
	 * @return total
	 */
	private int assessUp(Gem[][] grid, int r, int c){
		int total = 0;
		while (r-total >= 0 && grid[r-total][c].getValue() == grid[r][c].getValue()){
			total++;
		}
		return total;
	}
	private int assessDown(Gem[][] grid, int r, int c){
		int total = 0;
		while (r+total < grid.length && grid[r+total][c].getValue() == grid[r][c].getValue()){
			total++;
		}
		return total;
	}
	private int assessLeft(Gem[][] grid, int r, int c){
		int total = 0;
		while (c-total >= 0 && grid[r][c-total].getValue() == grid[r][c].getValue()){
			total++;
		}
		return total;
	}
	private int assessRight(Gem[][] grid, int r, int c){
		int total = 0;
		while (c+total < grid[r].length && grid[r][c+total].getValue() == grid[r][c].getValue()){
			total++;
		}
		return total;
	}
	
	/**
	 * Runs gravity so the AI can calculate gems gained via falling
	 * @param grid
	 * @param move
	 * @return the grid post-gravity
	 */
	private Gem[][] simulateGravity(Gem[][] grid){
		Gem[][] newGrid = cloneGrid(grid);
		
		newGrid = destroy(newGrid); //Destroy gems
		
		for (int i = 0; i < newGrid[0].length; i++){ //Make stuff fall
			int dist = 1;
			for (int j = newGrid.length-1; j-dist >= 0; j--){
				if (newGrid[j][i].getValue() < 0){
					while (j-dist > 0 && newGrid[j-dist][i].getValue() < 0){
						dist++;
					}
					newGrid[j][i].setValue(newGrid[j-dist][i].getValue());
					newGrid[j-dist][i].setValue(-newGrid[j-dist][i].getValue());
				}
			}
		}
		return newGrid;
	}
	/**
	 * Destroys 3-in-a-row gems
	 * @param grid
	 * @param r
	 * @param c
	 * @return new grid with gems destroyed
	 */
	private Gem[][] destroy(Gem[][] grid){
		Gem[][] newGrid = cloneGrid(grid);
		for (int i = 0; i < newGrid.length; i++){
			for (int j = 0; j < newGrid[i].length; j++){
				int value = Math.abs(newGrid[i][j].getValue());
				if (j <= 5){
					if (Math.abs(newGrid[i][j+1].getValue()) == value &&
						Math.abs(newGrid[i][j+2].getValue()) == value){
						for (int k = j; k < newGrid[i].length && Math.abs(newGrid[i][k].getValue()) == value; k++){
							newGrid[i][k].setValue(-value);
						}
					}
				}
				if (i <= 5) {
					if (Math.abs(newGrid[i+1][j].getValue()) == value &&
							Math.abs(newGrid[i+2][j].getValue()) == value){
						for (int k = i; k < newGrid.length && Math.abs(newGrid[k][j].getValue()) == value; k++){
							newGrid[k][j].setValue(-value);
						}
					}
				}
			}
		}
		return newGrid;
	}
}
//TODO: Make algorithm more efficient. Alpha-beta?