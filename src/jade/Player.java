package jade;

/**
 * Records player (user and opponent) data
 * @author aronharder
 * @since 2015/04/08
 */
public class Player {
	private final static double EPSILON = 0.00001;
	protected float[] preferred = new float[7];
	protected int[] gems = new int[7];
	
	public Player(){
		for (int i = 0; i < preferred.length; i++){
			preferred[i] = (float) (1.0/7);
		}
	}
	public Player(float[] initPreferred){
		try {
			float sum = 0;
			for (float num : initPreferred){
				sum+=num;
			}
			if (sum <= 1-EPSILON || sum >= 1+EPSILON){
				throw new IllegalArgumentException();
			}
			preferred = initPreferred;
		} catch (IllegalArgumentException e) {
			System.out.println("Preference array must sum to zero.");
			for (int i = 0; i < preferred.length; i++){	//Set to default values
				preferred[i] = (1/7);
			}
		}
		
	}
	
	public void addGem(int color){
		gems[color-1]++;
	}
	public int[] getGems(){
		return gems;
	}
	
	/**
	 * Finds the max value of an array. Honestly, this should probably be in a java library somewhere.
	 * @param array
	 * @return the max value of the array
	 */
	protected int max(int[] array){
		if (array.length == 0){
			return 0;
		}
		int best = array[0];
		for (int i : array){
			if (i > best)
				best = i;
		}
		return best;
	}
	protected float totalPoints(int[] reward){ //Should only pass in length-7 arrays
		float sum = 0;
		for (int i = 0; i < reward.length; i++){
			sum+=(reward[i]*preferred[i]);
		}
		return sum;
	}
	/**
	 * .clone() doesn't work for Gem[][] type, so use this method instead
	 * @param g
	 * @return clone
	 */
	protected Gem[][] cloneGrid(Gem[][] g){
		Gem[][] newGrid = new Gem[8][8];
		for (int x = 0; x < g.length; x++){
			for (int y = 0; y < g[x].length; y++){
				newGrid[x][y] = new Gem(x,y,g[x][y].getValue());
			}
		}
		return newGrid;
	}
}
