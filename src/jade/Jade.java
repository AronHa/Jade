package jade;

/**
 * Runs the game Jade
 * @author Aron Harder
 * @since 2014/07/25
 */

public class Jade {

	public static void main(String[] args) {
		Display display = new Display();
		Board board = display.getBoard();
		System.out.println(board);
		
		display.run();
	}

}