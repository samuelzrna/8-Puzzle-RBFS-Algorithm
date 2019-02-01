/* 
 * Samuel Zrna | sez160230
 * 4365.002
 * Programming Assignment 1
 * 19 October 2018
 */

package RBFS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecursiveBestFirstSearch {
	
	// Initialize iteration limit to prevent stack overflow
	int iterationLimit = 0;

	// Static final Declarations
	private static final int dimension = 3;
	private static final Double INFINITY = Double.POSITIVE_INFINITY;
	
	// Bottom, left, top, right
	// These arrays will help swap values for the child puzzles 
	int[] row = { 1, 0, -1, 0 };
	int[] col = { 0, -1, 0, 1 };
	
	// Goal state of the 8-puzzle
	char[][] goal = { {'0','1','2'}, {'3','4','5'}, {'6','7','8'} };

	// FileInputStream declaration
	private FileInputStream fis;

	/*** PUBLIC METHODS ***/
	
	/*
	 * search
	 * 
	 * Returns void.
	 * This method takes the initial puzzle as a parameter and finds the coordinates of the blank space.
	 * Creates the root node with the coordinates of the blank space, calls the rbfs, prints the path
	 * and prints out whether the algorithm succeeds or fails.
	 * 
	 */
	public void search(char[][] initPuzzle) {
		
		int[] coordinates = getBlankSpaceCoordinates(initPuzzle);
		
		Node p = new Node(initPuzzle, coordinates[0], coordinates[1], coordinates[0], coordinates[1], null);  
		Node n = p;
		
		SearchResult sr = rbfs(p, n, (double) p.cost, INFINITY);
		
		printPath(sr.getSolution());
		
		if (sr.getOutcome() == SearchResult.SearchOutcome.SOLUTION_FOUND)
			System.out.println("Solution Found");
		else
			System.out.println("RBFS Falure");
		
	}
	
	/*
	 * createPuzzle
	 * 
	 * Returns the initial puzzle (char[][]).
	 * This method is only called if args.length is not 0.
	 * If args[0] contains a test file, then this method will parse through it and return the initial puzzle.
	 */
	public char[][] createPuzzle(File file) {
		
		char[][] puzzle = new char[3][3];
		
		if (!file.exists())
		      System.out.println(file + " does not exist.");		    
	    try {
	      fis = new FileInputStream(file);
	      char current;
	      int i = 0, j = 0;
	      while (fis.available() > 0) {
	      
	    	  	current = (char) fis.read();

	    	  	if(current != ' ') {
	    	  		puzzle[i][j] = current;
		    	  	if (j == 2) {
		    	  		j = -1;
		    	  		i++;
		    	  	}
		    	  	j++;
	    	  	}
	      }
	    } catch (IOException e) { e.printStackTrace(); }

		return puzzle;
	}
	
	/*** PRIVATE METHODS ***/
	
	/*
	 * rbfs
	 * 
	 * Returns a SearchResult.
	 * This recursive method is the meat of the programming assignment.
	 * Most of it was from the code repository that the book has developed, but has been modified in order
	 * for the algorithm to work.
	 */
	private SearchResult rbfs(Node p, Node c, Double fNode, Double fLimit) {
		
		// This limit is incremented until met
		iterationLimit++;
		
		if (c.cost == 0) return new SearchResult(c, fLimit);
		
		List<Node> successors = expandNode(c);
		
		double[] f = new double[successors.size()];
		
		if (successors.size() == 0) return new SearchResult(null, INFINITY);
		
		for (int s = 0; s < successors.size(); s++) {
			// f[s] <- max(g(s) + h(s), f[node])
			if (successors.get(s).cost < fNode)
				f[s] = Math.min(successors.get(s).cost, fNode);
			else
				f[s] = Math.max(successors.get(s).cost, fNode);
		}	
		
		// RBFS must succeed before stack overflow. Limited to 1000 iterations
		while (iterationLimit < 1000) {
			
			// best <- the lowest f-value node in successors
			int bestIndex = getBestFValueIndex(f);
			// if f[best] > f_limit then return failure, f[best]
			if (f[bestIndex] > fLimit)
				return new SearchResult(null, f[bestIndex]);
			
			// alternative <- the second-lowest f-value among successors
			int altIndex = getNextBestFValueIndex(f, bestIndex);
			// result, f[best] <- RBFS(problem, best, min(f_limit, alternative))
			SearchResult sr = rbfs(p, successors.get(bestIndex), f[bestIndex], Math.min(fLimit, f[altIndex]));
			f[bestIndex] = sr.getFCostLimit();
			
			// if result <> failure then return result
			if (sr.getOutcome() == SearchResult.SearchOutcome.SOLUTION_FOUND) {
				return sr;
			}
		}
		
		return new SearchResult(null, INFINITY);
	}
	
	/*
	 * heuristic
	 * 
	 * Returns an integer value and takes the initial and goal puzzles as parameters.
	 * This method will call the manDis method for every tile on the initial puzzle 
	 * that doesn't match the goal.
	 * This will return our desired heuristic value of the summation of each tile's Manhattan distance
	 * from the goal.
	 */
	private int heuristic(char[][] initPuzzle, char[][] goal) {
		
		int heu = 0;
		int n = initPuzzle.length;
		
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (initPuzzle[i][j] != '0' && initPuzzle[i][j] != goal[i][j]) 
					heu = heu + manDis(initPuzzle, i, j);

		return heu;
	}
	
	/*
	 * manDis
	 * 
	 * Returns an integer value and takes the initial puzzle and the mismatching tile coordinates as parameters.
	 * Either returns 1 or the absolute value of the difference of each x and y coordinate of the initial puzzle
	 * minus the i and j coordinates of where the tile should be.
	 * Essentially this will return the Manhattan distance of a tile that is out of place. 
	 */
	private int manDis(char[][] puzzle, int x, int y) {
		
		int n = puzzle.length;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)			
				if (puzzle[i][j] != '0' && puzzle[x][y] == goal[i][j]) 	// Once it finds where the mismatched tile should be
					return Math.abs(x - i) + Math.abs(y - j);			// Return the absolute value of the difference in coordinates
		
		return 1;
	}
	
	/*
	 * getBlankSpaceCoordinates
	 * 
	 * Returns an array of integers (max size is 2).
	 * This method will locate and return the x and y coordinates of the blank tile of the initial puzzle.
	 */
	private int[] getBlankSpaceCoordinates(char[][] initPuzzle) {
		int[] coordinates = {0,0};
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				if (initPuzzle[i][j] == '0') {
					coordinates[0] = i;
					coordinates[1] = j;
				}
		
		return coordinates;
	}
	
	/*
	 * printPath
	 * 
	 * Returns void.
	 * This recursive method will print the puzzle of each child the root Node has to create in order to
	 * find the solution.
	 * It also calls printPuzzle which will simple print the puzzle of each Node in the path. 
	 */
	private void printPath(Node root) {
		if (root == null) { return; }
		printPath(root.parent);
		printPuzzle(root.puzzle);
		System.out.println();
	}
	
	/*
	 * printPuzzle
	 * 
	 * Returns void.
	 * This method simply prints out each character of the puzzle and then a line after each level.
	 */
	private void printPuzzle(char[][] Puzzle) {
		for (int i = 0; i < Puzzle.length; i++) {
			for (int j = 0; j < Puzzle.length; j++)
				System.out.print(Puzzle[i][j] + " ");
			System.out.println();
		}
	}
	
	/*
	 * isSafe
	 * 
	 * Returns a boolean.
	 * This method makes sure that indexes x & y never leave their bounds.
	 */
	private boolean isSafe(int x, int y) { return (x >= 0 && x < dimension && y >= 0 && y < dimension); }
	
	/*
	 * expandNode
	 * 
	 * Returns a List of Nodes.
	 * This method will expand the current Node n by creating it's children and adding it to a Node List nl.
	 * It also gets the heuristic value for the cost of each child.
	 */
	private List<Node> expandNode(Node n) {
		
		List<Node> nl = new ArrayList<Node>();
		
		// There is a maximum of 4 children
		for (int i = 0; i < 4; i++)
            if (isSafe(n.x + row[i], n.y + col[i])) {
	            switch (i) {
	            
	            // If n has a swappable tile below
	            case 0:
		            	n.down = new Node(n.puzzle, n.x, n.y, n.x + row[i], n.y + col[i], n);
		            	n.down.cost = heuristic(n.down.puzzle, goal);
		            	nl.add(n.down);
		    			break;
		    		// If n has a swappable tile to the left
	            
	            case 1:
		            	n.left = new Node(n.puzzle, n.x, n.y, n.x + row[i], n.y + col[i], n);
		            	n.left.cost = heuristic(n.left.puzzle, goal);
		            	nl.add(n.left);
		    			break;
		    			
		    		// If n has a swappable tile above
	            case 2:
		            	n.top = new Node(n.puzzle, n.x, n.y, n.x + row[i], n.y + col[i], n);
		            	n.top.cost = heuristic(n.top.puzzle, goal);
		            	nl.add(n.top);
		    			break;
	            
		    		// If n has a swappable tile to the right 
	            case 3:
		            	n.right = new Node(n.puzzle, n.x, n.y, n.x + row[i], n.y + col[i], n);
		            	n.right.cost = heuristic(n.right.puzzle, goal);
		            	nl.add(n.right);
		    			break;
	            }
            	}
		
		return nl;
	}
	
	/*
	 * getBestFValueIndex
	 * 
	 * Returns an integer.
	 * This method will return an index for the lowest value of the f array. 
	 */
	private int getBestFValueIndex(double[] f) {
		int lidx = 0;
		Double lowestSoFar = INFINITY;

		for (int i = 0; i < f.length; i++) {
			if (f[i] < lowestSoFar) {
				lowestSoFar = f[i];
				lidx = i;
			}
		}

		return lidx;
	}

	/*
	 * getNextBestFValueIndex
	 * 
	 * Returns an integer.
	 * This method will return an index for the second lowest value of the f array. 
	 */
	private int getNextBestFValueIndex(double[] f, int bestIndex) {
		// Array may only contain 1 item (i.e. no alternative),
		// therefore default to bestIndex initially
		int lidx = bestIndex;
		Double lowestSoFar = INFINITY;

		for (int i = 0; i < f.length; i++) {
			if (i != bestIndex && f[i] < lowestSoFar) {
				lowestSoFar = f[i];
				lidx = i;
			}
		}

		return lidx;
	}
}

/*
 * SearchResult Class
 * 
 * This is a class used from the code example online.
 * It makes it easier to use the rbfs algorithm because it gives it a unique return value that has more functionality. 
 */
class SearchResult {
	public enum SearchOutcome {
		FAILURE, SOLUTION_FOUND
	};

	private Node solution;

	private SearchOutcome outcome;

	private final Double fCostLimit;

	public SearchResult(Node solution, Double fCostLimit) {
		if (null == solution) {
			this.outcome = SearchOutcome.FAILURE;
		} else {
			this.outcome = SearchOutcome.SOLUTION_FOUND;
			this.solution = solution;
		}
		this.fCostLimit = fCostLimit;
	}

	public SearchOutcome getOutcome() {
		return outcome;
	}

	public Node getSolution() {
		return solution;
	}

	public Double getFCostLimit() {
		return fCostLimit;
	}
}
