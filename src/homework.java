
import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.io.IOException; // Import the IOException class to handle errors
import java.io.FileWriter; // Import the FileWriter class
import java.util.*;

class Cell {
	int x, y, g;
	double cost;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return y + "," + x;
	}

	public Cell(int x, int y) {
		// TODO Auto-generated constructor stub
		this.x = x;
		this.y = y;
		this.cost = 0;
		this.g = 0;
	}

	public Cell(int x, int y, int cost) {
		this.x = x;
		this.y = y;
		this.cost = cost;
		this.g = 0;
	}
}

class Cost_Comparator implements Comparator<Cell> {
	public int compare(Cell x, Cell y) {
		if (x.cost == y.cost)
			return 0;
		else if (x.cost > y.cost)
			return 1;
		else
			return -1;
	}
}

public class homework {
	String searchtype = "";
	int max_elevation = 0;
	int[] dimension = new int[2];
	Cell start;
	LinkedHashSet<Cell> targets = new LinkedHashSet<>();
	int[][] grid;
	// Create a list of constants to compute adjacent vertices
	ArrayList<Cell> consts = new ArrayList<>();
	// Create a hash map to store Cell->Parent Cell
	Map<Cell, Cell> parent = new HashMap<>();

	public homework() {
		this.consts.add(new Cell(0, 1, 10));
		this.consts.add(new Cell(1, 0, 10));
		this.consts.add(new Cell(0, -1, 10));
		this.consts.add(new Cell(-1, 0, 10));
		this.consts.add(new Cell(1, -1, 14));
		this.consts.add(new Cell(-1, 1, 14));
		this.consts.add(new Cell(1, 1, 14));
		this.consts.add(new Cell(-1, -1, 14));
	}

	public static void main(String[] args) {
		homework hw = new homework();
		int targets_num = 0;
		try {
			File myObj = new File("Test cases\\input.txt");
			Scanner myReader = new Scanner(myObj);
			hw.searchtype = myReader.nextLine().trim();
			String[] dim = myReader.nextLine().trim().split("[ ]{1,}");
			hw.dimension[0] = Integer.parseInt(dim[1].trim());
			hw.dimension[1] = Integer.parseInt(dim[0].trim());
			hw.grid = new int[hw.dimension[0]][hw.dimension[1]];
			dim = myReader.nextLine().trim().split("[ ]{1,}");
			hw.start = new Cell(Integer.parseInt(dim[1].trim()), Integer.parseInt(dim[0].trim()));
			hw.max_elevation = Integer.parseInt(myReader.nextLine().trim());
			targets_num = Integer.parseInt(myReader.nextLine().trim());
			for (int i = 0; i < targets_num; i++) {
				dim = myReader.nextLine().trim().split("[ ]{1,}");
				hw.targets.add(new Cell(Integer.parseInt(dim[1].trim()), Integer.parseInt(dim[0].trim())));
			}
			String[] val;
			for (int row = 0; row < hw.dimension[0]; row++) {
				val = myReader.nextLine().trim().split("[ ]{1,}");
				for (int col = 0; col < hw.dimension[1]; col++) {
					hw.grid[row][col] = Integer.parseInt(val[col].trim());
				}
			}
			// System.out.println("Dimensions of grid:"+Arrays.toString(hw.dimension));
			// System.out.println("Grid:");
			/*
			 * for (int[] row : hw.grid) System.out.println(Arrays.toString(row));
			 */
//			System.out.println("Starting point:("+hw.start.y+","+hw.start.x+")");
//			System.out.println("Maximum elevation wagon can handle:"+hw.max_elevation);
//			System.out.println("Number of targets:"+targets_num);
//			System.out.println("Targets:"+hw.targets);
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		if (hw.searchtype.equals("BFS"))
			hw.run_BFS();
		else if (hw.searchtype.equals("UCS"))
			hw.run_UCS();
		else {
			int i = 0;
			for (Cell cell : hw.targets) {
				hw.run_Astar(cell);
				if (i == hw.targets.size() - 1)
					hw.print_astar_path(cell, true);
				else
					hw.print_astar_path(cell, false);
				i += 1;
			}

		}
	}

	public boolean isRock(Cell c) {
		if (grid[c.x][c.y] < 0)
			return true;
		return false;
	}

	public boolean isValid(Cell s, Cell t) {
		if (!((t.x >= 0 && t.x < dimension[0]) && (t.y >= 0 && t.y < dimension[1])))
			return false;
		if (isRock(s)) {
			if (isRock(t)) {
				if (Math.abs(grid[t.x][t.y] - grid[s.x][s.y]) > max_elevation)
					return false;
			} else {
				// target cell is muddy with rock-height = 0
				if (Math.abs(grid[s.x][s.y]) > max_elevation)
					return false;
			}
		} else {
			// source cell is muddy with rock-height = 0
			if (isRock(t)) {
				if (Math.abs(grid[t.x][t.y]) > max_elevation)
					return false;
			}
		}
		return true;
	}

	public int getDelta(Cell s, Cell t) {
		if (isRock(s) && isRock(t)) // both cells have rocks
			return Math.abs(grid[t.x][t.y] - grid[s.x][s.y]);
		else if (isRock(s)) // source cell has rocks and target cell is muddy
			return grid[t.x][t.y] + Math.abs(grid[s.x][s.y]);
		else if (isRock(t)) // source cell is muddy and target cell has rocks
			return Math.abs(grid[t.x][t.y]);
		else // both cells are muddy
			return grid[t.x][t.y];
	}

	public void printPath() {
		try {
			FileWriter myWriter = new FileWriter("output.txt");
			int i = 0;
			for (Cell cell : targets) {
				ArrayList<Cell> resultpath = new ArrayList<>();
				if (parent.containsKey(cell)) {
					resultpath.add(cell);
					while (parent.get(cell) != null) {
						resultpath.add(parent.get(cell));
						cell = parent.get(cell);
					}
					for (int j = resultpath.size() - 1; j >= 0; j--) {
						if (j == 0) {
							//System.out.print(resultpath.get(j));
							myWriter.write(resultpath.get(j).toString());
						} else {
							//System.out.print(resultpath.get(j) + " ");
							myWriter.write(resultpath.get(j).toString() + " ");
						}
					}
				} else {
					//System.out.println("FAIL");
					myWriter.write("FAIL");
				}
				if (i != targets.size() - 1) {
					//System.out.println();
					myWriter.write("\n");
				}
				i++;
			}
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public void print_astar_path(Cell cell, boolean isLast) {
		try {
			FileWriter myWriter = new FileWriter("output.txt", true);
			ArrayList<Cell> resultpath = new ArrayList<>();
			if (parent.containsKey(cell)) {
				resultpath.add(cell);
				while (parent.get(cell) != null) {
					resultpath.add(parent.get(cell));
					cell = parent.get(cell);
				}
				for (int j = resultpath.size() - 1; j >= 0; j--) {
					if (j == 0) {
						//System.out.print(resultpath.get(j));
						myWriter.write(resultpath.get(j).toString());
					} else {
						//System.out.print(resultpath.get(j) + " ");
						myWriter.write(resultpath.get(j).toString() + " ");
					}
				}
			} else {
				//System.out.println("FAIL");
				myWriter.write("FAIL");
			}
			if (!isLast) {
				//System.out.println();
				myWriter.write("\n");
			}
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public void run_BFS() {
		// System.out.println("Starting BFS");
		int cnt = targets.size();
		HashSet<Cell> visited = new HashSet<>();
		// Create a queue for BFS
		LinkedList<Cell> queue = new LinkedList<Cell>(); // use queue
		// Mark the current node as visited and enqueue it
		visited.add(start);
		queue.add(start);
		parent.put(start, null);
		/*
		 * System.out.println("Parent contains:"); for (Cell keys : parent.keySet())
		 * System.out.println(keys + ":"+ parent.get(keys));
		 */
		while (queue.size() != 0 && cnt != 0) {
			// Dequeue a vertex from queue and print it
			Cell s = queue.poll();
			// System.out.println("Dequeued "+s.x+","+s.y+" ");
			// System.out.println(dimension[0]+" by "+dimension[1]);
			for (int i = 0; i < consts.size(); i++) {
				Cell t = new Cell(s.x + consts.get(i).x, s.y + consts.get(i).y);
				if (isValid(s, t) && (!visited.contains(t))) {
					// System.out.println("Wagon move from "+s.x+","+s.y+" with value
					// "+grid[s.x][s.y]+" to "+t.x+","+t.y+" with value "+grid[t.x][t.y]+" is
					// Valid");
					t.cost = s.cost + 1;
					if (targets.contains(t)) {
						//System.out.println("Reached target " + t + " in cost: " + t.cost);
						cnt--;
					}
					visited.add(t);
					queue.add(t);
					parent.put(t, s);
					/*
					 * System.out.println("Now parent contains:"); for (Cell keys : parent.keySet())
					 * System.out.println(keys + ":"+ parent.get(keys));
					 */
					// System.out.println("Current path cost is "+t.cost);

				}
			}

		}
		printPath();
	}

	public void run_UCS() {
		// System.out.println("Starting UCS");
		int cnt = targets.size();
		Map<Cell, Cell> pqmap = new HashMap<>();
		HashSet<Cell> visited = new HashSet<>();
		// Create a priority queue for UCS
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>(new Cost_Comparator());
		// Mark the current node as visited and enqueue it
		pq.add(start);
		pqmap.put(start, start);
		parent.put(start, null);
		// System.out.println("The elements with the highest priority element at front
		// of queue order:");
//		for (Cell element : pq) 
//			System.out.print(element + ":" + element.cost + " ");
//		System.out.println();
		// System.out.println(pq);
		while (pq.size() != 0 && cnt != 0) {
			Cell s = pq.poll();
			// System.out.println("Dequeued "+s.x+","+s.y+" ");
			if (targets.contains(s)) {
				//System.out.println("Reached target " + s + " in cost: " + s.cost);
				cnt--;
			}
			visited.add(s);
			// System.out.println(dimension[0]+" by "+dimension[1]);
			for (int i = 0; i < consts.size(); i++) {
				Cell t = new Cell(s.x + consts.get(i).x, s.y + consts.get(i).y);
				if (isValid(s, t) && (!visited.contains(t))) {
					// System.out.println("Wagon move from "+s.x+","+s.y+" with value
					// "+grid[s.x][s.y]+" to "+t.x+","+t.y+" with value "+grid[t.x][t.y]+" is
					// Valid");
					t.cost = s.cost + consts.get(i).cost;
					if (pqmap.containsKey(t)) {
						if (t.cost < pqmap.get(t).cost) {
							pqmap.get(t).cost = t.cost;
							parent.put(t, s);
						}
					} else {
						pq.add(t);
						pqmap.put(t, t);
						parent.put(t, s);
					}

				}
				// else
				// System.out.println("Wagon move from "+s.x+","+s.y+" with value
				// "+grid[s.x][s.y]+" to "+t.x+","+t.y+" is Invalid");
			}
			// System.out.println(pq);
			// System.out.println("The elements with the highest priority element at front
			// of queue order:");
//			for (Cell element : pq) 
//				System.out.print(element + ":" + element.cost + " "); 
//			System.out.println();
		}
		printPath();
	}

	public void run_Astar(Cell target) {
		// System.out.println("Starting A*");
		Map<Cell, Cell> pqmap = new HashMap<>();
		HashSet<Cell> visited = new HashSet<>();
		parent = new HashMap<>();
		PriorityQueue<Cell> pq = new PriorityQueue<Cell>(new Cost_Comparator());
		// Mark the current node as visited and enqueue it
		pq.add(start);
		pqmap.put(start, start);
		parent.put(start, null);
		// System.out.println("The elements with the highest priority element at front
		// of queue order:");
//		for (Cell element : pq) 
//			System.out.print(element + ":" + element.cost + " ");
//		System.out.println();
		// System.out.println(pq);
		while (pq.size() != 0) {
			Cell s = pq.poll();
			// System.out.println("Dequeued "+s.x+","+s.y+" ");
			if (s.equals(target)) {
				//System.out.println("Reached target " + s + " in cost: " + s.g);
				break;
			}
			visited.add(s);
			// System.out.println(dimension[0]+" by "+dimension[1]);
			for (int i = 0; i < consts.size(); i++) {
				Cell t = new Cell(s.x + consts.get(i).x, s.y + consts.get(i).y);
				if (isValid(s, t) && (!visited.contains(t))) {
					// System.out.println("Wagon move from "+s.x+","+s.y+" with value
					// "+grid[s.x][s.y]+" to "+t.x+","+t.y+" with value "+grid[t.x][t.y]+" is
					// Valid");
					// Mark the current node as visited and enqueue it

					t.g = s.g + (int) consts.get(i).cost + getDelta(s, t);
					t.cost = (double) t.g + Math.sqrt(Math.pow(target.x - t.x, 2) + Math.pow(target.y - t.y, 2));
					if (pqmap.containsKey(t)) {
						if (t.cost < pqmap.get(t).cost) {
							pqmap.get(t).cost = t.cost;
							pqmap.get(t).g = t.g;
							parent.put(t, s);
						}
					} else {
						pq.add(t);
						pqmap.put(t, t);
						parent.put(t, s);
					}

				}
				// else
				// System.out.println("Wagon move from "+s.x+","+s.y+" with value
				// "+grid[s.x][s.y]+" to "+t.x+","+t.y+" is Invalid");
			}
			// System.out.println(pq);
			// System.out.println("The elements with the highest priority element at front
			// of queue order:");
//			for (Cell element : pq) 
//				System.out.print(element + ":" + element.cost + " "); 
//			System.out.println();
		}
		// printPath();
	}
}
