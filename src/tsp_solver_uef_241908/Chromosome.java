package tsp_solver_uef_241908;
import java.util.ArrayList;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 1 of 11, alphabetical order 
 * 
 * The Chromosome class. 
 * A chromosome is in other words a Hamiltonian circuit instance, also known as 
 * a TSP tour, each chromosome has (a) logic stack(s) for the inner vertices (neurons).
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
public class Chromosome {
    // private final String nameID;
    private Logic_Stack logicstack;
    private final ArrayList points; // odds = Xs, evens = Ys  // class "Point.java" might turn out to be unnecessary 
    // note: "ArrayList points" can and must change if needed, copying the chromosome instances all the time is 
    // another story, delete the "final" if necessary 
    
    // This was new knowledge to me: 
    // https://stackoverflow.com/questions/10750791/what-is-the-sense-of-final-arraylist
    // visited on 28/Apr/2023 
    
    //      AN IDEA: if permanent IDs or names wanted for the chromosomes, not implemented though 
    //      https://stackoverflow.com/questions/19961844/creating-multiple-objects-with-different-names-in-a-loop-to-store-in-an-array-li
    //      visited on 7/Dec/2022 
    
    /**
     * The constructor
     * 
     * @param nameID String 
     * @param points ArrayList 
     */
    public Chromosome(String nameID, ArrayList points) {
        //this.nameID = nameID;
        this.points = points;
    }
    
    /**
     * Get chromosome (TSP tour, Hamiltonian circuit)
     * 
     * @return points 
     */
    public ArrayList getChromosomePoints() {
        return this.points;
    }
    
    /**
     * Get the logic stack of a chromosome.
     * 
     * @return logicstack
     */
    public Logic_Stack getStack() {
        return this.logicstack;
    }
    
    /**
     * Sets logic stacks for the chromosome so each of its neurons know how to move.
     * 
     * @param logicstack Logic_Stack
     */
    public void setLogicStacks(Logic_Stack logicstack) {
        this.logicstack = logicstack;
        // improvement: set for each point individually?
    }
    
    /**
     * A getter method for a chromosome.
     * 
     * @param nameID String
     * @return this 
     */
    public Chromosome getChromosome(String nameID) {
        return this;
    }
    
    /**
     * Sets a Chromosome instance to null. Can call the garbage collector.
     * 
     * @param c Chromosome
     */
    public static void terminateChromosome(Chromosome c) {
        //this.nameID = null;
        //this.points = null;
        //this.logicstack = null;
        c = null;
        System.gc();
    }
}