package tsp_solver_uef_241908;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static tsp_solver_uef_241908.Sub_algorithms.Euclidean_distance;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 3 of 11 
 * 
 * A class of some evolutionary computing parts, the genetic algorithm tools. 
 * The original idea was to include all of the evolution codes here but it 
 * turned out that when a List has the chromosome instances, most functions can be 
 * done to the object List directly, for example removing a chromosome (individual, TSP tour) etc. 
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
public class EvolutionaryParts {

    /**
     * Selection (after selection, there are still crossing-over and mutation parts later): 
     * the input List will be reordered so that when pairs are later combined, the couple can easily be 
     * selected when reading the List just 2 by 2. If the input is odd instead of even, then the 
     * last one just does not reproduce if decided so.
     * For evolution, it is not wise to reproduce chromosomes (their stacks) that are exactly equal.
     * 
     * Also includes crossing-overs (reproducing) and mutations.
     * 
     * @param instances List
     * @param logicStackCopiesOfOriginals List
     * @return List
     */
    public static List selectWhatWillBePairedThenCrossingOverAndMutation(List instances, List logicStackCopiesOfOriginals) {
        /**
        List logicStackCopiesOfOriginals = new ArrayList();
        for(int chro = 0; chro < instances.size(); chro++) {
                    Chromosome tempC = (Chromosome) instances.get(chro);
                    logicStackCopiesOfOriginals.add(tempC.getStack());
        }
        **/
        for(int i = 0; i < instances.size()-1; i++) {   // partner searching: 
            for(int j = 1; j < instances.size(); j++) {
                if(i != j) {
                    Logic_Stack ls1 = (Logic_Stack) logicStackCopiesOfOriginals.get(i);
                    Logic_Stack ls2 = (Logic_Stack) logicStackCopiesOfOriginals.get(j);
                    if(ls1.top() != ls2.top()) { // if different tops, move closer 
                        if(ls1.getTopIndex() == ls2.getTopIndex()) { // same stack sizes 
                            Chromosome helpVar = (Chromosome) instances.get(i+1);   // i+1 to helpv 
                            instances.set(i+1, (Chromosome) instances.get(j));      // j to i+1 
                            instances.set(j, helpVar);                              // helpv to j 
                        }
                    }
                    else if(j < i) {
                        if(ls1.getTopIndex() > 2 && ls2.getTopIndex() > 2) {
                            ls1.pop();
                            ls2.pop();
                        }
                    }
                    if(ls1.isEmpty() && ls2.isEmpty()) {
                        i = instances.size()+1;
                        j = instances.size()+1;
                        // just end the computations, hopefully at least some 
                        // instances that are near each other are different and not "identical twins" 
                    }
                }
            }
        }
        
        int originalInstanceSize = instances.size();
        // next, adding the children to the population "instances": 
        // the child of i and i+1 is added to the end of the list, then i+=2 and the next children 
        for(int i = 0; i < originalInstanceSize -1; i+=2) {
            // even 0, 2, 4, 6 ... are "females", odds are "males" and their purpose is to affect the stack of the child, 
            // trade childrens' stack data: (took a copy before popping all out into logicStackCopiesOfOriginals) 
            Chromosome newC = (Chromosome) instances.get(i);
            newC = possiblySwapTwoRandomNodes(newC);
            
            Logic_Stack newL = new Logic_Stack();
            Logic_Stack motherL = (Logic_Stack)logicStackCopiesOfOriginals.get(i);
            Logic_Stack fatherL = (Logic_Stack)logicStackCopiesOfOriginals.get(i+1);
            while(!motherL.isEmpty() && !fatherL.isEmpty()) {
                newL.push(motherL.top());
                motherL.pop();
                newL.push(fatherL.top());
                fatherL.pop();
            } // the stack will have changes 
            // improvement idea: always choose the best point set (father or mother) 
            
            // mutations (mostly deletion types), allows the child to be something 
            // way more than just a combination of its parents 
            // some randoms are deleted so the new stack is not so long: 
            
            System.gc();
            Logic_Stack newerL = new Logic_Stack();
            while(!newL.isEmpty()) {
                boolean switc = Math.random() < 0.49; // switch variable, can set the probability of adding 
                if(switc) {                          // when the stack is reversed again 
                    newerL.push(newL.top());
                }
                switc = Math.random() < 0.05;
                if(switc && (newL.top() > 2)) {
                    newerL.push(newL.top()-2);  // this kind of code allows completely new genes not present in parents 
                }                               // assuming there are at least 3 logic fragments present 
                newL.pop();
            }
            
            newC.setLogicStacks(newerL);
            instances.add(newC);
        }
        return instances;
    }
    
    /**
     * Calculating the fitness value.
     * Those who respect the elastic band principle, should survive in nature.
     * 
     * 1.0 requirement would mean "we want the best that is known currently and that's it".
     * 3.0 requirement means "at maximum, we allow a tour that is 300% the known optimum tour".
     * 
     * @param instances List
     * @param wantedPercentsAddedTo100_Min1_Max3 double
     * @return double
     */
    public static double calculateFitness(List instances, double wantedPercentsAddedTo100_Min1_Max3) {
        if(wantedPercentsAddedTo100_Min1_Max3 < 1.0000001 || 
           wantedPercentsAddedTo100_Min1_Max3 > 3.0) {
                System.out.println("The percent requirement is ridiculous in calculateFitness. Should be from 1.0000001 to 3.0");
                // 3.0 means 300% 
                return Double.MAX_VALUE;
        }
        double bestFitness = Double.MAX_VALUE;
        for(int chro = 0; chro < instances.size(); chro++) {
            Chromosome c = (Chromosome) instances.get(chro);
            ArrayList coordinates = c.getChromosomePoints();
            double eucNew = 0;
            for(int i = 0; i < c.getChromosomePoints().size()-2; i+=2) {
                eucNew += Euclidean_distance((double)coordinates.get(i), 
                                             (double)coordinates.get(i+1), 
                                             (double)coordinates.get(i+2), 
                                             (double)coordinates.get(i+3));
            }
            if(eucNew < bestFitness) {
                bestFitness = eucNew;
            }
        }
        double requiredFitness = bestFitness * wantedPercentsAddedTo100_Min1_Max3; // can be changed 
        return requiredFitness;
    }
    
    /**
     * Possibly swap 2 random vertices/points/nodes when the points are stored 
     * like xy xy xy xy... 
     * Sort of mutation that affects the TSP tour of the child when this is 
     * called for the child. 
     * 
     * @param c Chromosome
     * @return Chromosome
     */
    public static Chromosome possiblySwapTwoRandomNodes(Chromosome c) {
        int i = (c.getChromosomePoints().size()/2)-4; // maximum 
        int random1x = (int) ((Math.random() * (i - 1)) + 1);
        int random1y = random1x +1;
        int random2x = (int) ((Math.random() * (i - 1)) + 1);
        int random2y = random2x +1;
        
        if((Math.random() < 0.5) && (random1x%2 == 0) && (random2x%2 == 0) && (i > 0) && 
                (random1x != random2x) && (random1x > 1) && (random2x > random1x)) {
            //System.out.println("i is " + i);
            //System.out.println("random1x " + random1x + ", random2x " + random2x);
            //System.out.println("OLD before swap " + c.getChromosomePoints().toString());
            Collections.swap(c.getChromosomePoints(), (int)(random1x), (int)(random2x));
            Collections.swap(c.getChromosomePoints(), (int)(random1y), (int)(random2y));
            //System.out.println("NEW after swap  " + c.getChromosomePoints().toString());
        }
        return c;
    }
}
/**
 * OLD NOTES, DESIGN THOUGHTS WHEN PROGRAMMING THIS: 
 * 
 * set population (size x), add to the population one by one (for loop), push fragments to stack
 * 
 * growth, som neuron movements, tsp tours are made in a loop that has terminating
 * 
 * select the best ones that have good fitness, terminate others
 * 
 * reproduce, crossing-overs to the stacks
 * 
 * new chromosomes are created
 * 
 * also do random mutations for children (shuffle the stacks for some parts, a bit like in LK)
 * 
 * until only 1 remains, if multiple then just select the best one
 */