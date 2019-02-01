/* 
 * Samuel Zrna | sez160230
 * 4365.002
 * Programming Assignment 1
 * 19 October 2018
 */

package RBFS;

import java.io.File;

public class AIProjectOne {

	public static void main(String[] args) {
		
		// Create RecursiveBestFirstSearch object
		RecursiveBestFirstSearch rbfs = new RecursiveBestFirstSearch();
		
		// Initial puzzle to solve
		char[][] puzzle = { {'2','5','3'}, {'1','6','0'}, {'7','8','4'} };
		
		// Allows args[0] to be used if it contains a test file
		if (args.length == 1) 
			puzzle = rbfs.createPuzzle(new File(args[0]));
		
		// Call search method
		rbfs.search(puzzle);
	}
}
