package Program;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NQueensSolver {
    private int[] queens; // Array to store positions of queens on the chessboard.
    private List<Set<Integer>> domains; // List of domain sets for each queen.

    // Constructor: Initializes the queens array and domain sets for each queen.
    public NQueensSolver(int n) {
        queens = new int[n]; // Initialize the queens array with size n.
        Arrays.fill(queens, -1); // Set all positions to -1, indicating no queens placed.
        domains = new ArrayList<>(); // Initialize the list of domain sets.
        for (int i = 0; i < n; i++) {
            Set<Integer> domain = new HashSet<>(); // Create a new domain set for each queen.
            for (int j = 0; j < n; j++) {
                domain.add(j); // Add all possible positions to the domain set.
            }
            domains.add(domain); // Add the domain set to the list.
        }
    }

    // Solves the N-Queens problem.
    public boolean solveNQueens() {
        if (!ac3()) { // Apply AC-3 algorithm. If it returns false, there's no solution.
            return false;
        }
        return backtrack(); // Use backtracking to find a solution.
    }

    // AC-3 algorithm implementation to achieve arc consistency.
    private boolean ac3() {
        Queue<Arc> queue = new LinkedList<>(initializeArcs(domains.size())); // Initialize arcs.

        while (!queue.isEmpty()) { // Process all arcs.
            Arc arc = queue.poll(); // Get an arc from the queue.
            if (removeInconsistentValues(arc)) { // Remove inconsistent values.
                if (domains.get(arc.x).isEmpty()) { // If domain is empty, no solution.
                    return false;
                }

                // Add all neighboring arcs back to the queue.
                for (int i = 0; i < domains.size(); i++) {
                    if (i != arc.x) {
                        queue.add(new Arc(i, arc.x));
                    }
                }
            }
        }
        return true; // Arc consistency achieved.
    }

    // Removes inconsistent values from the domain of a variable.
    private boolean removeInconsistentValues(Arc arc) {
        boolean removed = false; // Flag to check if any value was removed.
        Set<Integer> domainX = new HashSet<>(domains.get(arc.x)); // Domain of the first variable.
        Set<Integer> domainY = domains.get(arc.y); // Domain of the second variable.

        // Check and remove inconsistent values.
        for (Integer xVal : domainX) {
            if (domainY.stream().noneMatch(yVal -> isConsistent(xVal, arc.x, yVal, arc.y))) {
                domains.get(arc.x).remove(xVal); // Remove the inconsistent value.
                removed = true; // Set the flag to true.
            }
        }
        return removed; // Return true if any value was removed.
    }

    // Checks if two values are consistent with each other.
    private boolean isConsistent(int valX, int queenX, int valY, int queenY) {
        // Check if queens do not attack each other.
        return valX != valY && Math.abs(queenX - queenY) != Math.abs(valX - valY);
    }

    // Overloaded consistency check used in forward checking.
    private boolean isConsistent(int x, int y) {
        for (int i = 0; i < y; i++) {
            // Check if any queen placed before y attacks the queen at (x, y).
            if (queens[i] == x || Math.abs(x - queens[i]) == Math.abs(y - i)) {
                return false; // Queens attack each other.
            }
        }
        return true; // Queens do not attack each other.
    }

    // Initializes the list of arcs for the AC-3 algorithm.
    private List<Arc> initializeArcs(int n) {
        List<Arc> arcs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    arcs.add(new Arc(i, j)); // Add an arc for each pair of variables.
                }
            }
        }
        return arcs;
    }

    // Backtracking algorithm to find a solution for the N-Queens problem.
    private boolean backtrack() {
        int var = selectUnassignedVariable(); // Select an unassigned variable.
        if (var == -1) {
            return isComplete(); // If all variables are assigned, check if solution is complete.
        }

        for (Integer value : orderDomainValues(var)) { // Iterate over ordered domain values.
            queens[var] = value; // Assign a value to the variable.
            List<Set<Integer>> domainSnapshots = forwardCheck(var, value); // Forward check.

            if (domainSnapshots != null && backtrack()) { // Recursive backtrack call.
                return true; // Solution found.
            }

            if (domainSnapshots != null) {
                restoreDomains(domainSnapshots); // Restore domains if forward check fails.
            }
            queens[var] = -1; // Unassign the variable.
        }

        return false; // No solution found.
    }

    // Selects the next variable to assign using the Minimum Remaining Values heuristic.
    private int selectUnassignedVariable() {
        int minDomainSize = Integer.MAX_VALUE; // Initialize to maximum value.
        int minDomainVariable = -1; // Variable with minimum domain size.

        // Iterate over all variables to find the one with the smallest domain size.
        for (int i = 0; i < queens.length; i++) {
            if (queens[i] == -1 && domains.get(i).size() < minDomainSize) {
                minDomainSize = domains.get(i).size();
                minDomainVariable = i;
            }
        }

        return minDomainVariable; // Return the variable with the smallest domain.
    }

    // Returns the ordered list of domain values for a variable.
    private List<Integer> orderDomainValues(int var) {
        return new ArrayList<>(domains.get(var)); // Return a list of domain values.
    }

    // Checks if all variables (queens) are assigned a value.
    private boolean isComplete() {
        for (int value : queens) {
            if (value == -1) {
                return false; // If any queen is unassigned, return false.
            }
        }
        return true; // All queens are assigned.
    }

    // Forward checking algorithm to reduce domains of unassigned variables.
    private List<Set<Integer>> forwardCheck(int var, int value) {
        List<Set<Integer>> snapshots = new ArrayList<>(); // Store snapshots of domains.
        for (int i = 0; i < queens.length; i++) {
            snapshots.add(new HashSet<>()); // Initialize with empty sets.
        }

        for (int i = 0; i < queens.length; i++) {
            final int index = i;
            if (index != var && queens[index] == -1) {
                Set<Integer> domainBefore = new HashSet<>(domains.get(index)); // Save current domain.
                domains.get(index).removeIf(val -> !isConsistent(val, index, value, var)); // Remove inconsistent values.
                Set<Integer> domainAfter = domains.get(index);

                domainBefore.removeAll(domainAfter); // Capture changes made to the domain.
                snapshots.set(index, domainBefore);

                if (domains.get(index).isEmpty()) {
                    restoreDomains(snapshots); // Restore domains if no values left.
                    return null;
                }
            }
        }
        return snapshots; // Return the snapshots for restoration.
    }
    
    // Restores the domains of variables to their state before forward checking.
    private void restoreDomains(List<Set<Integer>> snapshots) {
        for (int i = 0; i < queens.length; i++) {
            if (!snapshots.get(i).isEmpty()) {
                domains.get(i).addAll(snapshots.get(i)); // Add back the removed values.
            }
        }
    }

    // Inner class representing an arc (constraint) between two variables.
    class Arc {
        int x, y; // Variables involved in the arc.

        public Arc(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // Reads a file and counts the number of lines that do not start with '#'.
    public static int fileReader(String fileName) throws IOException {
        int count = 0; // Initialize line count.
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("#")) {
                    count++; // Increment count if line does not start with '#'.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count; // Return the line count.
    }

    // Main method to run the N-Queens solver.
    public static void main(String[] args) throws IOException {
    	runTests();
    	
        String fileName = "nqueens.txt"; // Name of the file containing the board size.
        int N = fileReader(fileName); // Read board size from the file.
       // int N =300;
        NQueensSolver solver = new NQueensSolver(N); // Create a solver instance.
        if (solver.solveNQueens()) {
            System.out.println("Solution found:");
            solver.printBoard(); // Print the board if a solution is found.
        } else {
            System.out.println("No solution exists."); // Indicate if no solution exists.
        }
    }

    // Prints the current state of the chessboard with queens.
    public void printBoard() {
        for (int i = 0; i < queens.length; i++) {
            for (int j = 0; j < queens.length; j++) {
                if (queens[i] == j) {
                    System.out.print("Q "); // Print 'Q' for a queen.
                } else {
                    System.out.print(". "); // Print '.' for an empty square.
                }
            }
            System.out.println(); // New line after each row.
        }
    }
    
    // writing tests to check the correctness of the algorithm 
    private static void runTests() {
        testAC3();
        testBacktracking();
        testMRV();
        System.out.println("All tests passed.");
    }
    
 // Test for the AC3 algorithm.
    private static void testAC3() {
        NQueensSolver solver = new NQueensSolver(4);
        assert solver.ac3() : "AC3 failed on 4 queens";
    }

    // Test for the backtracking algorithm.
    private static void testBacktracking() {
        NQueensSolver solver = new NQueensSolver(4);
        assert solver.backtrack() : "Backtracking failed on 4 queens";
    }

    // Test for the Minimum Remaining Values (MRV) heuristic.
    private static void testMRV() {
        NQueensSolver solver = new NQueensSolver(4);
        solver.queens[0] = 1; // Assign a queen to create an unassigned scenario.
        int var = solver.selectUnassignedVariable();
        assert var != -1 : "MRV failed to select an unassigned variable";
    }

}
