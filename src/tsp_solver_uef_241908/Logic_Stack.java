package tsp_solver_uef_241908;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 7 of 11 
 * 
 * A logic stack, used for inner vertices' (neurons') movements in order to decide the traverse logic 
 * when not moving towards the SOM goal. In practice, this is a simple integer ID stack, nothing too complex. 
 * 
 * Open source Java code, feel free to edit and try your own improvements. 
 * Tested with Windows 11 
 * Apache NetBeans 17 
 * Java JRE 8u371 64bit 
 * Java JDK 18.0.2 64bit 
 * 
 * @author Tuomas Hyvönen 
 * @version 2.0 
 */
public class Logic_Stack {
    private int topIndex;
    private static final int MAX_SIZE = 5000000;
    private final int[] DATA; // DATA has integers, which are actually IDs that 
                              // tell what logic fragment method to call.
                              // For example, 1 would be "use NN sprout and move towards 3rd" or
                              // some number would be "use Christofides and move towards 5th CW" and so on.
                              // The neurons move by calling a method and then traversing.
    // Interesting series of movements are set in something like [3,14,15,92,6,5] and 
    // when it reproduces with e.g. [6,5,4,3,2,1], then the child might first be for example [3,5,15,3,6,1] 
    // and after mutations that might be [3,5,16,4,6,1] (two random "+1 additions" at the center for 15 and 3). 
    // This of course, is only an example. 
    
    // It might be a good idea to set similar fragments near each other when numbering them, 
    // for example all of a same sprout types could be at range 1-4 and not at 1,3,5,7. 
    // This way, one type of a mutation does not cause too weird behaviour all of a sudden with high probability 
    // when the mutation is like "add 1" or "subtract 1". 
    
    
    /**
     * The constructor
     */
    public Logic_Stack() {
        System.gc();
        DATA = new int[MAX_SIZE];
        topIndex = -1;
    }
    
    /**
     * Return the top index.
     * @return topIndex integer
     */
    public int getTopIndex() {
        return topIndex;
    }

    /**
     * Checks if the stack is empty (is the top index -1?)
     * @return boolean
     */
    public boolean isEmpty() {
        return topIndex <= -1;
    }

    /**
     * Checks if the stack is full (is the top index MAX_SIZE - 1?)
     * @return boolean
     */
    public boolean isFull() {
        return topIndex >= MAX_SIZE - 1;
    }

    /**
     * Mark the stack empty (set the top index -1), the values may still exist in memory.
     */
    public void empty() {
        topIndex = -1;
    }
    
    /**
     * Push the number onto the top of the stack. 
     * @param x int
     */
    public void push(int x) {
        if(isFull() == false) {
            topIndex++;
            DATA[topIndex] = x;
            //System.out.println("push: Pushes " + data[topIndex] + 
            //        " to the stack. ");
        }
        else{
            //System.out.println("\t\t\t push(): Tried to push an "
            //        + "element but the stack is full");
        }
    }
    
    /**
     * Return the top element of the stack.
     * @return int
     */
    public int top() {
        if(isEmpty() == false) {
            //System.out.println("top(): Returns " + data[topIndex] + ". ");
            return DATA[topIndex];
        }
        else{
            //System.out.println("\t\t top(): Tried to return the top "
            //        + "element but the stack is empty");
            return Integer.MAX_VALUE; 
        }
    }
    
    /**
     * Remove the top element from the stack.
     */
    public void pop() {
        if(isEmpty() == false) {
            //System.out.println("pop(): Removes " + data[topIndex] + ". ");
            DATA[topIndex] = Integer.MAX_VALUE; 
            topIndex--;
        }
        else{
            //System.out.println("\t\t pop(): Tried to remove the top element "
            //        + "but the stack is empty");
        }
    }
}
