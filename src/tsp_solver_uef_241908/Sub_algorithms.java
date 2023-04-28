package tsp_solver_uef_241908;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 9 of 11 
 * 
 * The sub algorithm class with lots of tools like the Euclidean distance, 
 * the minimum spanning tree and the convex hull. 
 * 
 * - Euclidean distance #1 real 
 * - Euclidean distance #2 squared, a bit less computing effort, though Java is always a slowpoke compared to C++ for example 
 * - Convex hull for limiting the space with a "rubber band"
 * - Counter-clockwise turn checking
 * (- Smaller or bigger angle checking)
 * - Prim variation of the Minimal Spanning Tree
 * - Euler tour for edges/lines and embedding the TSP tour
 * - Christofides heuristic #2 for possible logic fragment usages
 * - Christofides matching #1, mimics ant colony optimization
 * - Christofides matching #2, use when a good matching cannot be found
 * - Checking the matching edge coordinates (a little help method)
 * - Nearest neighbour sprout
 * - A simple 2-opt move
 * (- 3-opt not implemented, has been left out of this version)
 * - Lin-Kernighan heuristic, pushes nodes to a stack and pops them while trying out new connections
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
public class Sub_algorithms {
    
    /**
     * Calculate real Euclidean distance. 
     * 
     * @param x1 double
     * @param y1 double
     * @param x2 double
     * @param y2 double
     * @return d double
     */
    public static double Euclidean_distance(
            double x1, double y1, double x2, double y2) {
        double d = Math.sqrt(((x2 - x1)*(x2 - x1))+((y2 - y1)*(y2 - y1)));
        return d;
    }
    
    /**
     * Calculate Euclidean distance without square root. 
     * 
     * @param x1 double
     * @param y1 double
     * @param x2 double
     * @param y2 double
     * @return d double
     */
    public static double Euclidean_distance_squared(
            double x1, double y1, double x2, double y2) {
        double d = ((x2 - x1)*(x2 - x1))+((y2 - y1)*(y2 - y1));
        //System.out.println(d);
        return d;
    }
    
    /**
     * Generate convex hull with Graham scan.
     * 
     * Has a very small bug but the TSP tour still computes with no problem 
     
        NAME : 
        COMMENT : 
        TYPE : TSP
        EDGE_WEIGHT_TYPE : EUC_2D
        NODE_COORD_SECTION 
        1 3 4 
        2 1 5 
        3 1 1 
        4 5 1 
        5 5.0 5.0 
        6 4.9 4.9
        7 4.8 4.8
        8 4.7 4.7
        9 4.6 4.6
        EOF
     
     * 
     * @param min integer
     * @param max integer
     * @param x_coordinates ArrayList
     * @param y_coordinates ArrayList
     * @return res String
     */
    public static String ConvexHull(int min, int max, 
            ArrayList x_coordinates, ArrayList y_coordinates) {
        double tempX;
        double tempY;
        for(int i = 0; i < y_coordinates.size()-1; i++) {
            for(int j = i+1; j < y_coordinates.size(); j++) {
                if((double)y_coordinates.get(i) > (double)y_coordinates.get(j)) {
                    tempX = (double)x_coordinates.get(j);
                    tempY = (double)y_coordinates.get(j);
                    x_coordinates.set(j, (double)x_coordinates.get(i));
                    y_coordinates.set(j, (double)y_coordinates.get(i));
                    x_coordinates.set(i, tempX);
                    y_coordinates.set(i, tempY);
                }
                if((double)x_coordinates.get(i) < (double)x_coordinates.get(j)) {
                    tempX = (double)x_coordinates.get(j);
                    tempY = (double)y_coordinates.get(j);
                    x_coordinates.set(j, (double)x_coordinates.get(i));
                    y_coordinates.set(j, (double)y_coordinates.get(i));
                    x_coordinates.set(i, tempX);
                    y_coordinates.set(i, tempY);
                }
            }
        }
        
        double[][][] result = new double[max][2][2];  // pointer, isY?, isEnd? 
        String[][][] result2;
        double[][] handleVerticesInThisOrder = new double[max][2];
        double temp_max_x = Double.MIN_VALUE;
        for(int i = 0; i < max; i++) {  // search for the rightmost vertice
            if((double)x_coordinates.get(i) > temp_max_x){
                temp_max_x = (double)x_coordinates.get(i);
            }
        }
        double temp_min_y = Double.MAX_VALUE;
        int index_of_downmost_of_rightmosts = -1;
        for(int i = 0; i < max; i++) {
            if((double)x_coordinates.get(i) == temp_max_x) {
                if((double)y_coordinates.get(i) < temp_min_y) {
                    temp_min_y = (double)y_coordinates.get(i);
                    index_of_downmost_of_rightmosts = i;
                }
            }
        }
        
        // calculate the slopes by comparing every other point 
        // with the first ("rightmostdownmost") point 
        // (y2 - y1) / (x2 - x1) from smallest to biggest 
        //                       but first the ones with same x coordinates 
        //                       and from min y to max y 
        
        for(int j = 0; j < max-1; j++) {
            int tempminindex = j;
            for(int k = j+1; k < max; k++) {
                if((double)y_coordinates.get(k) < (double)y_coordinates.get(tempminindex)) {
                    if((double)x_coordinates.get(k) == temp_max_x) {
                        tempminindex = k;
                    }
                }
            }
            if(tempminindex != j && (double)x_coordinates.get(j) == temp_max_x) {   
            // if a change to tempminindex was made, swap 
                if((double)y_coordinates.get(tempminindex) < 
                   (double)y_coordinates.get(index_of_downmost_of_rightmosts)) {
                    double pivot;

                    pivot = (double)y_coordinates.get(tempminindex);
                    y_coordinates.set(tempminindex, y_coordinates.get(j));
                    y_coordinates.set(j, pivot);

                    pivot = (double)x_coordinates.get(tempminindex);
                    x_coordinates.set(tempminindex, x_coordinates.get(j));
                    x_coordinates.set(j, pivot);
                    
                    index_of_downmost_of_rightmosts = tempminindex;
                }
            }
        } 
        //System.out.println("\n\n\nDownmost of rightmosts is " + 
        //   (double) x_coordinates.get(index_of_downmost_of_rightmosts) + 
        //   ", " + (double) y_coordinates.get(index_of_downmost_of_rightmosts));
        
        handleVerticesInThisOrder[0][0] = 
                (double) x_coordinates.get(index_of_downmost_of_rightmosts);
        handleVerticesInThisOrder[0][1] = 
                (double) y_coordinates.get(index_of_downmost_of_rightmosts);
        
        // take the max Xs coordinates and after that,
        // calculate slopes and take them also
        int index_with_handleVerticesInThisOrder = 0;
        for(int i = 0; i < max; i++) {
            if((double)x_coordinates.get(i) == temp_max_x){
                handleVerticesInThisOrder[index_with_handleVerticesInThisOrder]
                        [0] = (double)x_coordinates.get(i);
                handleVerticesInThisOrder[index_with_handleVerticesInThisOrder]
                        [1] = (double)y_coordinates.get(i);
                index_with_handleVerticesInThisOrder++;
            }
        }
        boolean no_slope = false;
        for(int j = 0; j < max-1; j++) {
            if((double)x_coordinates.get(j) != temp_max_x) {
                
                double slope_min = ((double)y_coordinates.get(j) - 
                        (double)y_coordinates.get(index_of_downmost_of_rightmosts)) / 
                                   ((double)x_coordinates.get(j) - 
                        (double)x_coordinates.get(index_of_downmost_of_rightmosts));
                int temp_min_index = j;
                for(int k = j+1; k < max; k++) {
                    double slope = ((double)y_coordinates.get(k) - 
                            (double)y_coordinates.get(index_of_downmost_of_rightmosts)) / 
                                   ((double)x_coordinates.get(k) - 
                            (double)x_coordinates.get(index_of_downmost_of_rightmosts));
                    if(slope < slope_min && no_slope == false) {
                        if((double)x_coordinates.get(k) != temp_max_x) {
                            slope_min = slope;
                            temp_min_index = k;
                            //System.out.println("slope_min = " + 
                            //slope_min + " between (" + 
                            //        (double)x_coordinates.get(temp_min_index) 
                            //+ ", " + 
                            //        (double)y_coordinates.get(temp_min_index) 
                            //+ ") and Downmost of rightmost");
                        }
                    }
                    no_slope = false;
                }
                if(temp_min_index != j && 
                        (double)x_coordinates.get(j) != temp_max_x ) {
                    double temp;
                    temp = (double)y_coordinates.get(temp_min_index);
                    y_coordinates.set(temp_min_index, y_coordinates.get(j));
                    y_coordinates.set(j, temp);
                    temp = (double)x_coordinates.get(temp_min_index);
                    x_coordinates.set(temp_min_index, x_coordinates.get(j));
                    x_coordinates.set(j, temp);
                    //System.out.println("swapped " + x_coordinates.get(temp_min_index) + 
                    //", " + y_coordinates.get(temp_min_index) + 
                    //        " AND " + x_coordinates.get(j) + ", " + y_coordinates.get(j));
                    //System.out.println("\n\n\ndownmost of rightmosts is " + 
                    //(double) x_coordinates.get(index_of_downmost_of_rightmosts) + 
                    //", " + (double) y_coordinates.get(index_of_downmost_of_rightmosts));
                }
            }
        }
        for(int i = 0; i < max; i++) {
            if((double)x_coordinates.get(i) != temp_max_x ) {
                handleVerticesInThisOrder[index_with_handleVerticesInThisOrder][0] = 
                        (double)x_coordinates.get(i);
                handleVerticesInThisOrder[index_with_handleVerticesInThisOrder][1] = 
                        (double)y_coordinates.get(i);
                index_with_handleVerticesInThisOrder++;
            }
        }
        for(int i = 0; i < handleVerticesInThisOrder.length -1; i++) {
            for(int j = i; j < handleVerticesInThisOrder.length; j++) {
                // if some slopes are equal (or there was no slope), sort by distance 
                // (y2 - y1) / (x2 - x1) 
                double slope1 = (handleVerticesInThisOrder[i][1] - 
                        (double)y_coordinates.get(index_of_downmost_of_rightmosts)) / 
                                (handleVerticesInThisOrder[i][0] - 
                        (double)x_coordinates.get(index_of_downmost_of_rightmosts)); 
                double slope2 = (handleVerticesInThisOrder[j][1] - 
                        (double)y_coordinates.get(index_of_downmost_of_rightmosts)) / 
                                (handleVerticesInThisOrder[j][0] - 
                        (double)x_coordinates.get(index_of_downmost_of_rightmosts));
                if(slope1 == slope2) {
                    double distance1 = Euclidean_distance_squared( 
                        // x1, y1, x2, y2 
                            (double)x_coordinates.get(index_of_downmost_of_rightmosts), 
                            (double)y_coordinates.get(index_of_downmost_of_rightmosts), 
                            handleVerticesInThisOrder[i][0], 
                            handleVerticesInThisOrder[i][1]);
                    double distance2 = Euclidean_distance_squared( 
                            (double)x_coordinates.get(index_of_downmost_of_rightmosts), 
                            (double)y_coordinates.get(index_of_downmost_of_rightmosts), 
                            handleVerticesInThisOrder[j][0], 
                            handleVerticesInThisOrder[j][1]);
                    //System.out.println("slope1: " + slope1 + ", slope2: " + slope2);
                    if((distance1 < distance2) &&   // vai > ? 
                        (handleVerticesInThisOrder[i][0] != temp_max_x) && 
                        (handleVerticesInThisOrder[j][0] != temp_max_x)) {
                        // swap "handleVerticesInThisOrder[i][0], 
                        //       handleVerticesInThisOrder[i][1]" 
                        //      and "handleVerticesInThisOrder[j][0], 
                        //           handleVerticesInThisOrder[j][1]" 
                        double temp;
                        temp = handleVerticesInThisOrder[i][0];
                        handleVerticesInThisOrder[i][0] = handleVerticesInThisOrder[j][0];
                        handleVerticesInThisOrder[j][0] = temp;
                        temp = handleVerticesInThisOrder[i][1];
                        handleVerticesInThisOrder[i][1] = handleVerticesInThisOrder[j][1];
                        handleVerticesInThisOrder[j][1] = temp;
                    }
                }
            }
        }
        /*for (double[] handleVerticesInThisOrder1 : handleVerticesInThisOrder){
            System.out.println("handleVerticesInThisOrder: " 
                    + handleVerticesInThisOrder1[0] + ", " + 
                    handleVerticesInThisOrder1[1]);
        }*/
        // the sorting part ends here
    
        // handle vertices and if not a counter-clockwise turn or 
        // 0 (straight line), repair connections 
        DoubleStack hulls_x_coordinates = new DoubleStack();
        DoubleStack hulls_y_coordinates = new DoubleStack();
        hulls_x_coordinates.push(handleVerticesInThisOrder[0][0]);
        hulls_y_coordinates.push(handleVerticesInThisOrder[0][1]);
        hulls_x_coordinates.push(handleVerticesInThisOrder[1][0]);
        hulls_y_coordinates.push(handleVerticesInThisOrder[1][1]);
        
        for(int i = 2; i < max; i++) {
            double top_x = hulls_x_coordinates.top();
            hulls_x_coordinates.pop();
            double top_y = hulls_y_coordinates.top();
            hulls_y_coordinates.pop();
            
            while (counterClockwiseTurn(hulls_x_coordinates.top(), 
                    hulls_y_coordinates.top(), 
                    top_x, top_y, handleVerticesInThisOrder[i][0], 
                    // discard if cw 
                    handleVerticesInThisOrder[i][1]) <= 0 && 
                    hulls_y_coordinates.isEmpty() == false) {
                
                        top_x = hulls_x_coordinates.top();
                        hulls_x_coordinates.pop();
                        top_y = hulls_y_coordinates.top();
                        hulls_y_coordinates.pop();
            }
            hulls_x_coordinates.push(top_x);
            hulls_y_coordinates.push(top_y);
            hulls_x_coordinates.push(handleVerticesInThisOrder[i][0]);
            hulls_y_coordinates.push(handleVerticesInThisOrder[i][1]);
        }
        for(int i = 0; i < max; i++) {
            result[i][0][0] = hulls_x_coordinates.top();
            hulls_x_coordinates.pop();
            result[i][1][0] = hulls_y_coordinates.top(); 
            hulls_y_coordinates.pop();
        }
        // generate a cycle of the vertices 
        int lastIndex = 0;
        for(int i = 0; i < max -1; i++) {
            if(result[i+1][0][0] < Double.MAX_VALUE && 
                    result[i+1][1][0] < Double.MAX_VALUE){
                lastIndex = i+1;
                result[i][0][1] = result[i+1][0][0]; // index, isY, isEnd 
                result[i][1][1] = result[i+1][1][0];
            }
        }
        // complete the cycle:
        result[lastIndex][0][1] = result[0][0][0];
        result[lastIndex][1][1] = result[0][1][0];
        
        // if 3 on the same line at the end, delete the center:
        for(int i = 0; i < lastIndex -1; i++) {
            for(int j = 0; j < lastIndex; j++){
                for(int k = 0; k < lastIndex +1; k++){
                    if(     result[i][0][1] < Double.MAX_VALUE && 
                            result[j][0][1] < Double.MAX_VALUE && 
                            result[k][0][1] < Double.MAX_VALUE &&
                            i < j && j < k) {
                        if (Sub_algorithms.counterClockwiseTurn(
                                    result[i][0][1], result[i][1][1], 
                                    result[j][0][1], result[j][1][1], 
                                    result[k][0][1], result[k][1][1]) == 0){
                            result[k][0][0] = Double.MAX_VALUE;
                            result[k][1][0] = Double.MAX_VALUE;
                            result[i][0][0] = result[j][0][1];
                            result[i][1][0] = result[j][1][1];
                            result[k][0][1] = Double.MAX_VALUE;
                            result[k][1][1] = Double.MAX_VALUE;
                            // must remember to decrease the last index:
                            lastIndex--;
                        }
                    }
                }
            }
        }
        result2 = new String[lastIndex+1][2][2];
        
        for(int i = 0; i < max; i++) {
            if(result[i][0][1] < Double.MAX_VALUE && 
                    result[i][1][1] < Double.MAX_VALUE && 
                    result[i][0][0] < Double.MAX_VALUE && 
                    result[i][1][0] < Double.MAX_VALUE){
                result2[i][0][0] = String.valueOf(result[i][0][0]);
                result2[i][0][1] = String.valueOf(result[i][1][0]);
                result2[i][1][0] = String.valueOf(result[i][0][1]);
                result2[i][1][1] = String.valueOf(result[i][1][1]);
            }
        }
        String res = Arrays.deepToString(result2);
        return res;
    }
    
    /**
     * This method checks if a turn with 3 points is 
     * counterclockwise ( more than 0, return +1 ), 
     * clockwise ( less than 0, return -1 ) or collinear ( =0, return 0 ),
     * called by Graham Convex Hull.
     * 
     * @param x1 double
     * @param y1 double
     * @param x2 double
     * @param y2 double
     * @param x3 double
     * @param y3 double
     * @return ret_value integer
     */
    public static int counterClockwiseTurn(double x1, double y1, 
                                           double x2, double y2, 
                                           double x3, double y3) {
        int ret_value = -2;
        try {
            int area2 = (int) (((x2 - x1)*(y3 - y1)) - ((y2 - y1)*(x3 - x1)));
            if(area2 < 0) {
                ret_value =  -1;    //cw
            }
            else {
                if(area2 > 0) {
                    ret_value =  1; //ccw
                }
                else {
                    ret_value = 0;
                }
            }
        }
        catch(Exception e) {
            System.err.println(e);
        }
        return ret_value;
    }
    
    /**
     * Checks if the first point has a smaller "angle" than the second point, 
     * from Skiena & Revilla (2003) page 318.
     * Programming Challenges, The Programming Contest Training Manual 
     * 
     * Unused.
     * 
     * @param d1 double
     * @param d2 double
     * @param d3 double
     * @param d4 double
     * @param d5 double
     * @param d6 double
     */
    private static boolean smaller_angle(double d1, double d2, 
            double d3, double d4, double d5, double d6) {
        if(counterClockwiseTurn(d1, d2, d3, d4, d5, d6) == 0) {
            return Euclidean_distance_squared(d1, d2, d3, d4) > 
                   Euclidean_distance_squared(d1, d2, d5, d6);
        }
        return counterClockwiseTurn(d1, d2, d3, d4, d5, d6) != 1;
    }
    
    /**
     * Create a minimal spanning tree with Prim's algorithm.
     * Prim adds edges to the existing tree, Kruskal would just add anywhere until the result is MST.
     * 
     * @param min integer
     * @param max integer
     * @param x_coordinates ArrayList
     * @param y_coordinates ArrayList
     * @return result double[][][]
     */
    public static double[][][] MST_Prim(int min, int max, 
            ArrayList x_coordinates, ArrayList y_coordinates) {
        double[][][] result = new double[20000][2][2];  // pointer, isY?, isEnd? 
        Random rand = new Random();                     // though this is not the best way to store edges 
        int random = rand.nextInt((max - min) + 1) + min;
        boolean[] booltable = new boolean[max];
        result[0][0][0] = (double) x_coordinates.get(random -1);
        result[0][1][0] = (double) y_coordinates.get(random -1);
        int pointer = 0; 
        //int pointer_start = random -1;
        double X1 = 0;
        double Y1 = 0;
        double X2 = 0;
        double Y2 = 0;
        for(int i = 1; i < max; i++) {
            // check every connected node and find a min distance to 
            // the next unconnected node 
            booltable[random - 1] = true;
            double min_distance = Double.MAX_VALUE;
            double temp_distance;
            for(int h = 0; h < booltable.length; h++) {
                for(int j = 0; j < booltable.length; j++) {
                    if(booltable[j] == false && booltable[h] == true) {
                        double x1 = (double) x_coordinates.get(h);
                        double y1 = (double) y_coordinates.get(h);
                        double x2 = (double) x_coordinates.get(j);
                        double y2 = (double) y_coordinates.get(j);
                        temp_distance = Sub_algorithms.
                                Euclidean_distance_squared(x1, y1, x2, y2);
                        if(temp_distance < min_distance) {
                            min_distance = temp_distance;
                            pointer = j;
                            X1 = x1; Y1 = y1; X2 = x2; Y2 = y2; 
                        }
                    }
                }
            }
            booltable[pointer] = true; 
            result[i-1][0][0] = X1; 
            result[i-1][1][0] = Y1; 
            result[i-1][0][1] = X2; 
            result[i-1][1][1] = Y2; 
        }
        return result; 
    }
    
    /**
     * Do an Eulerian walk and embed the tour.
     * 
     * @param edges double[][][]
     * @param edges_in_tsp_solution integer
     * @param max integer
     * @return result String[]
     */
    public static String[] Euler_and_embedded_tour(double edges[][][], 
            int edges_in_tsp_solution, int max) { // the last is max +1 or edges +1
        
        //System.out.println("Euler_and_embedded_tour, edges in TSP solution is " + 
        //        edges_in_tsp_solution + " and max is " + max);
        
        String[] result = new String[edges_in_tsp_solution +1];
        String[] sub_result = new String[max];
        boolean[] isTaken = new boolean[max -1];
        boolean isTaken_has_all_true = false;
        int pointer = 0;
        int previousPointer;// = 0;

        isTaken[pointer] = true;
        sub_result[pointer] = String.valueOf(edges[pointer][0][1]) + " " + 
                String.valueOf(edges[pointer][1][1]); 
        String last_value = sub_result[pointer];
        String start_value = "";
        double last_x = edges[pointer][0][1]; 
        double last_y = edges[pointer][1][1]; 

        
        // the first edge is ok, now connect the others: 
        while (isTaken_has_all_true == false) {
            previousPointer = pointer;
            
            for(int i = 1; i < max -1; i++) {
                if((((edges[i][0][0] == last_x) && 
                      (edges[i][1][0] == last_y))) && (isTaken[i] == false)) {
                    //check that a connection is made so that the line doesn't cut 
                    isTaken[i] = true;
                    sub_result[pointer] = String.valueOf(edges[i][0][1]) + 
                            " " + String.valueOf(edges[i][1][1]); 
                    last_x = edges[i][0][1]; 
                    last_y = edges[i][1][1]; 
                    
                    pointer++;
                    
                }
                
                if(i == 1) {
                        start_value = String.valueOf(edges[i][0][0]) + 
                                " " + String.valueOf(edges[i][1][0]); 
                }
            }
            isTaken_has_all_true = true;
            for(int i = 0; i < max -1; i++) {
                if(isTaken[i] == false) {
                    isTaken_has_all_true = false;
                }
            }
            
            if(pointer == previousPointer) {
                for(int i = 1; i < isTaken.length; i++) {
                    if(isTaken[i] == false && 
                            ((edges[i][0][0] == last_x) &&
                             (edges[i][1][0] == last_y))) {
                        isTaken[i] = true;
                        sub_result[pointer] = String.valueOf(edges[i][0][1]) + 
                                " " + String.valueOf(edges[i][1][1]); 
                        last_x = edges[i][0][1]; 
                        last_y = edges[i][1][1]; 

                        pointer++;
                        //System.out.println("Got out of stuck in Christofides");
                        i = isTaken.length;
                    }
                    if(isTaken[i] == false && 
                            ((edges[i][0][1] == last_x) &&
                             (edges[i][1][1] == last_y))) {
                        // swap
                        double helpvarX = edges[i][0][1];
                        double helpvarY = edges[i][1][1];
                        edges[i][0][1] = edges[i][0][0];
                        edges[i][1][1] = edges[i][1][0];
                        edges[i][0][0] = helpvarX;
                        edges[i][1][0] = helpvarY;
                        
                        
                        isTaken[i] = true;
                        sub_result[pointer] = String.valueOf(edges[i][0][1]) + 
                                " " + String.valueOf(edges[i][1][1]); 
                        last_x = edges[i][0][1]; 
                        last_y = edges[i][1][1]; 

                        pointer++;
                        //System.out.println("Got out of stuck in Christofides by swapping");
                        i = isTaken.length;
                    }
                }
                if(pointer == previousPointer) { // if still stuck, just choose the next false in isTaken
                    for(int i = 0; i < isTaken.length; i++) {
                        if(isTaken[i] == false) {
                            isTaken[i] = true;
                            sub_result[pointer] = String.valueOf(edges[i][0][1]) + 
                                    " " + String.valueOf(edges[i][1][1]); 
                            last_x = edges[i][0][1]; 
                            last_y = edges[i][1][1]; 

                            pointer++;
                            //System.out.println("Got out of stuck in Christofides by selecting the next available false");
                            i = isTaken.length;
                        }
                    }
                }
            }
            
            //System.out.println(Arrays.toString(isTaken));
            //System.out.println("Euler_and_embedded_tour's while loop end");
        }
        
        // adding the last value and the first value + swapping the table 
        sub_result[max - 2] = last_value; 
        sub_result[max - 1] = start_value;
        //System.out.println(Arrays.toString(sub_result)  + " ignore last");
        String help_var;
        String first_val = "";
        for(int i = sub_result.length -2; i > -1; i--) { 
            if(i == sub_result.length -2) {
                first_val = sub_result[i];
            }
            help_var = sub_result[i+1];
            sub_result[i+1] = sub_result[i];
            sub_result[i] = help_var;
        }
        sub_result[0] = first_val;
        
        // embedding the tour:
        for(int i = 0; i < sub_result.length; i++) {
            for(int j = i+1; j < sub_result.length; j++) {
                if(sub_result[i].equals(sub_result[j]) && 
                   !sub_result[i].equals("del")) {
                    if(sub_result[j].equals(
                            sub_result[0]) && j == sub_result.length -1) {
                        // do nothing
                    }
                    else {
                        sub_result[j] = "del";
                        //System.out.println("Deleted " + j);
                    }
                }
            }
        }
        int k = 0;
        for (String sub_result1 : sub_result) {
            if (sub_result1.equals("del") == false) {
                // the string arrays' lengths need checking
                try {
                    result[k] = sub_result1;
                    k++;
                }
                catch(Exception e) {
                    //System.out.println(e);
                }
            }
        }
        return result;
    }
    
    /**
     * For sprout usages: 
     * The Christofides Heuristic (CHRI). Tested for small inputs.
     * An improvement of 2-MST, still uses Prim MST, ant colony matching part added.
     * Mathematical proof of "at most 1.5 times the optimal tour" exists.
     * Matching part's optimization matters. Bad matching = never mind the proof.
     * 
     * @param startingNodeIndex_NO_ZERO int
     * @param coordinates double[][]
     * @return String[]
     */
    public static String[] Christofides(int startingNodeIndex_NO_ZERO, double[][] coordinates) {
        
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        
        if(coordinates[1].length < 1) {
            System.out.println("No coordinates in Christofides method");
            return null;
        }
        for(int i = 0; i < coordinates[1].length; i++) {
            if(coordinates[i].length != 2) {
                System.out.println("Problem with 2d array of the coordinates in Christofides method");
                return null;
            }
        }
        
        for(int i = 0; i < coordinates[1].length; i++) {
            for(int j = 0; j < 2; j++) {
                if(j==0) {
                    coordinates_x.add(coordinates[i][0]);
                }
                else {
                    coordinates_y.add(coordinates[i][1]);
                }
            }
        }
        
        double edges[][][] = Sub_algorithms.MST_Prim(1, coordinates_y.size(), 
                coordinates_x, coordinates_y); 
            // point, isY?, isEnd? 

        // finding out what are the odd degree edges (their amount should be even):
        // (the 3D table is not the best way to store edges)
        ArrayList coordinates_x_match = new ArrayList();
        ArrayList coordinates_y_match = new ArrayList();
        
        
        for(int j = 0 ; j < (coordinates_y.size())-1 ; j++){
            for(int k = 0 ; k < 2 ; k++){
                for(int l = 0 ; l < 2 ; l++){
                    //System.out.println("[" + j + "][" + k + "][" + l + "] " + edges[j][k][l]);
                    // xx yy xx yy xx yy ...
                    if(k == 0) {
                        coordinates_x_match.add(edges[j][k][l]);
                    }
                    else {
                        coordinates_y_match.add(edges[j][k][l]);
                    }
                }
            }
        }

        boolean[] isHandled = new boolean[coordinates_x_match.size()];
        boolean[] isEven = new boolean[coordinates_x_match.size()];
        for(int j = 0; j < coordinates_x_match.size(); j++) {
            isEven[j] = false;
            isHandled[j] = false;
        }

        for(int j = 0; j < coordinates_x_match.size(); j++) {
            for(int k = j+1; k < coordinates_x_match.size(); k++) {
                if(coordinates_x_match.get(j).equals(coordinates_x_match.get(k)) && 
                   coordinates_y_match.get(j).equals(coordinates_y_match.get(k)) &&
                        (isHandled[j] == false) && (isHandled[k] == false)) {

                        isHandled[j] = true;
                        isHandled[k] = true;
                        isEven[j] = true;
                        isEven[k] = true;
                        k = coordinates_x_match.size(); // end the inner for loop

                }
            }
        }

        //System.out.println(coordinates_x_match.toString());
        //System.out.println(coordinates_y_match.toString());
        //System.out.println("isOdd " + Arrays.toString(isEven));
        //System.out.println("isHandled " + Arrays.toString(isHandled));

        // if "false" exists somewhere, that should be an even degree node
        // not all of the same are yet marked as false so:
        for(int j = 0; j < coordinates_x_match.size(); j++) {
            if(isEven[j] == false) {
                for(int k = j; k < coordinates_x_match.size(); k++) {
                    if(coordinates_x_match.get(j).equals(coordinates_x_match.get(k)) && 
                       coordinates_y_match.get(j).equals(coordinates_y_match.get(k))) {
                            isEven[k] = false;
                    }
                }
            }
        }

        for(int j = 0; j < coordinates_x_match.size(); j++) {
            if(isEven[j] == true) {
                coordinates_x_match.set(j, null);
                coordinates_y_match.set(j, null);
            }
        }
        for(int j = 0; j < coordinates_x_match.size(); j++) {
            if(coordinates_x_match.get(j) == null// && coordinates_y_match.get(j) == null
                    ) {
                coordinates_x_match.remove(j);
                coordinates_y_match.remove(j);
                j--; // removing will change the array size so the loop index must be updated as well
            }
        }

        //System.out.println(coordinates_x_match.toString());
        //System.out.println(coordinates_y_match.toString());
        //System.out.println(Arrays.toString(isEven));

        // add the matching edges: 
        ArrayList matchedConnections = Sub_algorithms.Matching(coordinates_x_match, coordinates_y_match); 

        //System.out.println("matchedConnections.size() is " + matchedConnections.size());
        System.out.println("Matching done");
        // start end start end start end
        // x,y,x,y,  x,y,x,y,  x,y,x,y...

        double[][][] mst_with_odd_matched_edges = new double[(coordinates_y.size() + matchedConnections.size())-1][2][2];
        for(int j = 0; j < coordinates_y.size(); j++) {
            if((edges[j][0][0] != edges[j][0][1]) && 
               (edges[j][1][0] != edges[j][1][1])) {
                    mst_with_odd_matched_edges[j][0][0] = edges[j][0][0];
                    mst_with_odd_matched_edges[j][1][0] = edges[j][1][0];
                    mst_with_odd_matched_edges[j][0][1] = edges[j][0][1];
                    mst_with_odd_matched_edges[j][1][1] = edges[j][1][1];
            }
            else {
                System.out.println("Same: " + 
                        edges[j][0][0] + ", " + 
                        edges[j][0][1] + " and " + 
                        edges[j][1][0] + ", " + 
                        edges[j][1][1]);
            }
        }

        int insertIndex = coordinates_y.size()-1;
        for(int j = 0; j < matchedConnections.size(); j+=4) {
            if((matchedConnections.get(j) != matchedConnections.get(j+2)) && 
               (matchedConnections.get(j+1) != matchedConnections.get(j+3))) {
                    mst_with_odd_matched_edges[insertIndex][0][0] = (double)matchedConnections.get(j);
                    mst_with_odd_matched_edges[insertIndex][1][0] = (double)matchedConnections.get(j+1);
                    mst_with_odd_matched_edges[insertIndex][0][1] = (double)matchedConnections.get(j+2);
                    mst_with_odd_matched_edges[insertIndex][1][1] = (double)matchedConnections.get(j+3);
                    insertIndex++;
            }
            else {
                System.out.println("Same: " + 
                        matchedConnections.get(j) + ", " + 
                        matchedConnections.get(j+2) + " and " + 
                        matchedConnections.get(j+1) + ", " + 
                        matchedConnections.get(j+3));
            }
        }

        // ^improvement to do: check if some edges should be turned around?

        String connections_string = "";
        int connections = 0;
        // the next for loop helps to recognize 
        // what exactly are the current connections: 
        for(int j = 0; j < (coordinates_y.size() + (matchedConnections.size()/4))-1; j++) {
                    String X1 = String.valueOf(mst_with_odd_matched_edges[j][0][0]); 
                    String Y1 = String.valueOf(mst_with_odd_matched_edges[j][1][0]); 
                    String X2 = String.valueOf(mst_with_odd_matched_edges[j][0][1]); 
                    String Y2 = String.valueOf(mst_with_odd_matched_edges[j][1][1]); 
                    connections_string = connections_string.concat( 
                            "\tFrom (" + X1 + ", " + Y1 + 
                            ")    to    (" + X2 + ", " + Y2 + ")\n");
                    connections++;
        }

        //System.out.println(connections_string);
        
        // finally, make the Euler tour and shortcuts: 
        String[] result = Sub_algorithms.Euler_and_embedded_tour(mst_with_odd_matched_edges, 
                coordinates_y.size(), connections+1);
        System.out.println("Christofides RESULT: " + Arrays.toString(result));
        return result;
    }
    
    /**
     * Matching, used by the Christofides heuristic.
     * 
     * This matching should be used for small inputs (small Christofides sprouts)
     * For bigger inputs (like 500 nodes), a simpler method is recommended.
     * 
     * When there are even number of vertices, pair them 1 to 1 like dancers.
     * Ant colony optimization style, making NNH sprouts that add feromon.
     * Select the top feromoned edges (reminds of the knapsack problem).
     * Must be careful since .-.-. is never wanted.
     * 
     * @param x_coordinates ArrayList
     * @param y_coordinates ArrayList
     * @return ArrayList
     */
    public static ArrayList Matching(ArrayList x_coordinates, ArrayList y_coordinates) {
        int sprout_length = (x_coordinates.size()*2); //(((int)Math.ceil(x_coordinates.size() / 2))+1);
        
        if(x_coordinates.size() != y_coordinates.size()) {
            System.out.println("X and Y sizes are not equal, in matching");
            return null;
        }
        for(int i = 0; i < x_coordinates.size()-1; i++) {
            for(int j = i+1; j < x_coordinates.size(); j++) {
                if((x_coordinates.get(i) == x_coordinates.get(j)) && 
                   (y_coordinates.get(i) == y_coordinates.get(j))) {
                    System.out.println("ERROR in MATCHING: equal coordinate inputs found.");
                        return null;
                }
            }
        }
        
        if(x_coordinates.size() > 150) { 
                // if there are more than 150 match nodes, do not try too complex computations 
            // just taking one "good" solution!
            ArrayList selected_ret_values = computeRelativelyGoodMatchingNotPerfectMin(x_coordinates, y_coordinates);
            System.out.println("computeRelativelyGoodMatchingNotPerfectMin called, " + selected_ret_values.toString() + "\n");
            return selected_ret_values;
        }
        double[][] coordinates;// = new double[x_coordinates.size()][y_coordinates.size()]; // x and y listing in decimal array 
        int[][] edges_feromoned = new int[x_coordinates.size()][y_coordinates.size()]; // zero is used so remember always to add 1 -> node number 
        // ^ 2d matrix that presents the connections 
        boolean[][] isTaken = new boolean[x_coordinates.size()][y_coordinates.size()]; 
        for(int i = 0; i < x_coordinates.size(); i++) {
            for(int j = 0; j < x_coordinates.size(); j++) {
                isTaken[i][j] = false;
            }
        }

        for(int h = 0; h < 2; h++) { // make NNH sprouts 2 times from each node 
            for(int i = 0; i < x_coordinates.size(); i++) { 
                // do a NNH sprout as much as int sprout_length is, 
                // always add pheromone to the matrix
                    // possible improvement: use Math.random() if the sprout would have 2 equally good ways to proceed

                coordinates = Sub_algorithms.NNHsprout(sprout_length, i+1, x_coordinates, y_coordinates);
                for(int j = 0; j < sprout_length; j++) {
                        //System.out.println("Increasing connection from " + 
                        //    coordinates[j][0] + ", " + coordinates[j][1] + " to " + 
                        //    coordinates[j+1][0] + ", " + coordinates[j+1][1]);

                    for(int k = 0; k < x_coordinates.size(); k++) {
                        for(int l = 0; l < x_coordinates.size(); l++) {
                            if(coordinates[j][0] == (double)x_coordinates.get(k) && 
                               coordinates[j][1] == (double)y_coordinates.get(k) && 
                               coordinates[j+1][0] == (double)x_coordinates.get(l) && 
                               coordinates[j+1][1] == (double)y_coordinates.get(l)) {
                                    edges_feromoned[k][l]++;
                                    edges_feromoned[l][k]++;
                                    //System.out.println(k + " and " + l + 
                                    //        " indexes increased (plus 1 = vertex number)\n");
                            }
                        }
                    }
                }
            }
        }

        /**
        // increase everything by 1 just for sure:
        for(int k = 0; k < x_coordinates.size(); k++) {
            for(int l = 0; l < x_coordinates.size(); l++) {
                    edges_feromoned[k][l]++;
                    //edges_feromoned[l][k]++;
            }
        }
        **/

        // must count both sides, add feromones "a to b" AND "b to a"

        // Choose those that have the highest feromone values, 
        // 2 pairs cannot connect each other (that would mean .-.-.)
        // ONLY select edges that are connected for AT LEAST 1 time
        // Do not stop until an acceptable solution has been found,
        // in other words change the connections when needed until all are paired.

        ArrayList ret_values = new ArrayList();
        double[] ret_values_from_x = new double[x_coordinates.size()];
        double[] ret_values_from_y = new double[x_coordinates.size()];
        double[] ret_values_to_x = new double[x_coordinates.size()];
        double[] ret_values_to_y = new double[x_coordinates.size()];

        boolean solutionOK = false;
        while(!solutionOK) {
            int insertingIndex = 0;
            int temp_max_fer = 0;
            int max_fer_index_i = 0;
            int max_fer_index_j = 0;

            for(int i = 0; i < x_coordinates.size(); i++) {
                for(int j = i+1; j < x_coordinates.size(); j++) { // because of symmetry in the matrix, j does not begin at 0 
                    if(((edges_feromoned[i][j] + edges_feromoned[j][i]) > temp_max_fer) && 
                            ((edges_feromoned[i][j] + edges_feromoned[j][i]) > 0) &&
                            (isTaken[i][j] == false)) {
                        temp_max_fer = edges_feromoned[i][j] + edges_feromoned[j][i];
                        max_fer_index_i = i;
                        max_fer_index_j = j;

                    }
                }
            }

            //System.out.println("Max fer " + temp_max_fer + " when added " + 
            //        edges_feromoned[max_fer_index_i][max_fer_index_j] + " and " + 
            //        edges_feromoned[max_fer_index_j][max_fer_index_i] + " with indexes " + 
            //        max_fer_index_i + ", " + max_fer_index_j);

            isTaken[max_fer_index_i][max_fer_index_j] = true;
            isTaken[max_fer_index_j][max_fer_index_i] = true;

            ret_values_from_x[insertingIndex] = (double)x_coordinates.get(max_fer_index_i);
            ret_values_from_y[insertingIndex] = (double)y_coordinates.get(max_fer_index_i);
            ret_values_to_x[insertingIndex] = (double)x_coordinates.get(max_fer_index_j);
            ret_values_to_y[insertingIndex] = (double)y_coordinates.get(max_fer_index_j);
            insertingIndex++;

            solutionOK = true;
            for(int i = 0; i < x_coordinates.size(); i++) {
                for(int j = 0; j < x_coordinates.size(); j++) {
                    if((i != j) && (isTaken[i][j] == false) && (edges_feromoned[i][j] > 0)){
                        solutionOK = false;
                        //System.out.println("False found in checking " + i + ", " + j);
                    }
                }
            }

            for(int i = 0; i < insertingIndex; i++) {
                //System.out.println(ret_values_from_x[i] + ", " + ret_values_from_y[i] + " (from)");
                //System.out.println(ret_values_to_x[i] + ", " + ret_values_to_y[i] + " (to)");

                    ret_values.add(ret_values_from_x[i]);
                    ret_values.add(ret_values_from_y[i]);
                    ret_values.add(ret_values_to_x[i]);
                    ret_values.add(ret_values_to_y[i]);
                    // System.gc(); // avoided some memory issues with Java heap space during a bug that is now fixed
            }
            //System.out.println("while loop's end, in matching method");
        }

        /**
        System.out.println("The feromone table: ");
        for (int[] row : edges_feromoned) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println("\nret_values " + ret_values.toString() + "\n");
        **/

        int selected_connections;// = 0;
        int[] connectionsToTake = new int[(x_coordinates.size()/2)*4];
        //boolean taken[];
        int insert_index;// = 0;
        boolean solutionOk = false;

        ArrayList indexes = new ArrayList();
        //ArrayList helpindexes = new ArrayList();

        for(int h = 0; h < x_coordinates.size()+4; h+=4) {
            for(int i = (h+4); i < connectionsToTake.length+4; i+=4) {       
                indexes.add(h);                                              
                indexes.add(i);
                //indexes.add(h);
            }
        }
        /**
        for(int h = 0; h < x_coordinates.size()+4; h+=4) {
            for(int i = (h+4); i < connectionsToTake.length+4; i+=4) {        
                indexes.add(i);                                               
                indexes.add(h);
                //indexes.add(i);
            }
        }
        for(int h = 0; h < connectionsToTake.length+4; h+=4) {
            indexes.add(h);
            for(int g = connectionsToTake.length; g > -4; g-=4) {
                indexes.add(g);
                indexes.add(h);
                indexes.add(g);
            }
        }
        **/

        //System.out.println("Indexes are: " + indexes.toString());
        //indexes = permute((ArrayList)java.util.Arrays.asList(indexes), 0);

        /** 
        // https://stackoverflow.com/questions/2920315/permutation-of-array (17.11.2022) an idea, sketch 
        for(int tail = indexes.size() - 1; tail > 0; tail--){
            if ((int)indexes.get(tail - 1) < (int)indexes.get(tail)){ 

                int s = indexes.size() - 1;
                while((int)indexes.get(tail-1) >= (int)indexes.get(s))
                    s--;

                swap(indexes, tail-1, s);

                for(int i = tail, j = indexes.size() - 1; i < j; i++, j--){
                    swap(indexes, i, j);
                }
                break;
            }
        }
        **/

        // next: begin selecting the feromoned edges 
            int j;// = 0;
        int begin_index = 0;
        connectionsToTake = new int[(x_coordinates.size()/2)*4];
        for(int k = 0; k < connectionsToTake.length; k++) {
            connectionsToTake[k] = -1;
        }
        while(solutionOk == false) {

            selected_connections = 0;
            boolean[] taken = new boolean[indexes.size()];
            insert_index = 0;

            //for(int a = 0; a < indexes.size(); a++) {              
                //for(i = begin_index; i < indexes.size(); i++) {
                    //System.out.println(indexes.get(i));
                    for(j = begin_index; j < indexes.size(); j++) {
                        if((selected_connections < (x_coordinates.size()/2)) && 
                        (taken[j] == false)) {

                            //boolean proceed = true;
                            //for(int l = 0; l < connectionsToTake.length; l+=4) {
                            //    if(connectionsToTake[l] != -1) {

                            //else {
                                boolean proceed = true;
                                /**
                                if((int)indexes.get(i) == (int)indexes.get(j)) {
                                    proceed = false;
                                }
                                **/
                                    for(int l = 0; l < connectionsToTake.length; l+=4) {

                                        if(connectionsToTake[l] != -1) {

                                            if(checkMatchingEdgeCoordinates((double)ret_values.get(connectionsToTake[l]), 
                                                                            (double)ret_values.get(connectionsToTake[l+1]), 
                                                                            (double)ret_values.get(connectionsToTake[l+2]), 
                                                                            (double)ret_values.get(connectionsToTake[l+3]), 
                                                                            (double)ret_values.get((int)indexes.get(j)),    
                                                                            (double)ret_values.get((int)indexes.get(j) +1), 
                                                                            (double)ret_values.get((int)indexes.get(j) +2), 
                                                                            (double)ret_values.get((int)indexes.get(j) +3)) == false) {
                                                // not proceeding because we would take a ".-.-." 
                                                proceed = false;
                                                /**
                                                System.out.println("Found same: " + 
                                                        (double)ret_values.get((int)indexes.get(l)) + ", " +
                                                        (double)ret_values.get((int)indexes.get(l)+1) + " - " +
                                                        (double)ret_values.get((int)indexes.get(l)+2) + ", " +
                                                        (double)ret_values.get((int)indexes.get(l)+3) + "; " +
                                                        (double)ret_values.get((int)indexes.get(j)) + ", " +
                                                        (double)ret_values.get((int)indexes.get(j)+1) + " - " +
                                                        (double)ret_values.get((int)indexes.get(j)+2) + ", " +
                                                        (double)ret_values.get((int)indexes.get(j)+3));
                                                        **/
                                            }
                                        }

                                    }

                                    /**
                                if(checkMatchingEdgeCoordinates((double)ret_values.get((int)indexes.get(i)), 
                                                                (double)ret_values.get((int)indexes.get(i) +1), 
                                                                (double)ret_values.get((int)indexes.get(i) +2), 
                                                                (double)ret_values.get((int)indexes.get(i) +3), 
                                                                (double)ret_values.get((int)indexes.get(j)),    
                                                                (double)ret_values.get((int)indexes.get(j) +1), 
                                                                (double)ret_values.get((int)indexes.get(j) +2), 
                                                                (double)ret_values.get((int)indexes.get(j) +3)) == false) {
                                    // not proceeding because we would take a ".-.-." 
                                    proceed = false;
                                }
                                **/
                                if(proceed) {
                                    //if((int)indexes.get(i) != (int)indexes.get(j)) {

                                        //j = (int)indexes.get(j);

                                        //System.out.println("Selected edge from " + 
                                        //        (int)indexes.get(j) + ", " + ((int)indexes.get(j)+1) + 
                                        // " to " + ((int)indexes.get(j)+2) + ", " + ((int)indexes.get(j)+3));

                                        // j should not match with previous ones that are already taken 
                                        connectionsToTake[insert_index] = (int)indexes.get(j);
                                        connectionsToTake[insert_index+1] = (int)indexes.get(j)+1;
                                        connectionsToTake[insert_index+2] = (int)indexes.get(j)+2;
                                        connectionsToTake[insert_index+3] = (int)indexes.get(j)+3;

                                        /**
                                        for(int k = 0; k < taken.length; k++){
                                            if((int)indexes.get(k) == (int)indexes.get(j)) {
                                                taken[k] = true;
                                            }
                                        }
                                        **/

                                        taken[j] = true;

                                        insert_index+=4;
                                        selected_connections++;

                                        //j = 1;
                                        //j = indexes.size(); // ending the current loop 
                                    //}
                                }
                            //}
                        }
                    }
                //}
            //}
            //System.out.println(Arrays.toString(connectionsToTake) + "\n");

            boolean stuck = true;
            for(int k = 0; k < connectionsToTake.length; k++) {
                if(connectionsToTake[k] != -1) {
                    stuck = false;
                }
            }
            if(stuck) { 
                    // if stuck, no longer try the previous complex computations 
                // just take one "good" solution!
                //System.out.println("STUCK in matching");
                
                ArrayList selected_ret_values;// = new ArrayList();
                // try MST and select from it, uncomment to try if works: 
                /**
                double[][][] mst;
                boolean taken2[] = new boolean[x_coordinates.size()];
                mst = MST_Prim(1, x_coordinates.size(), x_coordinates, y_coordinates);
                for(int k = 0; k < x_coordinates.size()-1; k++) {
                    for(int l = k+1; l < x_coordinates.size(); l++) {
                        double bex1 = mst[k][0][0]; 
                        double bey1 = mst[k][1][0]; 
                        double bex2 = mst[l][0][0]; 
                        double bey2 = mst[l][1][0];
                        double nex1 = mst[k][0][1]; 
                        double ney1 = mst[k][1][1]; 
                        double nex2 = mst[l][0][1]; 
                        double ney2 = mst[l][1][1];
                        if((taken2[k] == false) && (taken2[l] == false) && 
                          (checkMatchingEdgeCoordinates(bex1, bey1, bex2, bey2,
                                                        nex1, ney1, nex2, ney2))) {
                            selected_ret_values.add((double)mst[k][0][0]);
                            selected_ret_values.add((double)mst[k][1][0]);
                            selected_ret_values.add((double)mst[l][0][0]);
                            selected_ret_values.add((double)mst[l][1][0]);
                            selected_ret_values.add((double)mst[k][0][1]);
                            selected_ret_values.add((double)mst[k][1][1]);
                            selected_ret_values.add((double)mst[l][0][1]);
                            selected_ret_values.add((double)mst[l][1][1]);
                            taken2[k] = true;
                            taken2[l] = true;
                        }
                    }
                }
                System.out.println("MST done for matched edges");
                return selected_ret_values;
                **/
                // the 3rd try: 
                selected_ret_values = computeRelativelyGoodMatchingNotPerfectMin(x_coordinates, y_coordinates);
                //System.out.println("After stuck: " + selected_ret_values.toString() + "\n");
                return selected_ret_values;
            }

            solutionOk = true;

            if(selected_connections < (x_coordinates.size()/2)) {

                solutionOk = false;

                //selected_connections = 0;
                //insert_index = 0;
                //i = 0;
                begin_index++;

                //System.out.println("Trying again, begin_index is \n" + begin_index);

                for(int k = 0; k < taken.length; k++) {
                    taken[k] = false;
                }
                for(int k = 0; k < connectionsToTake.length; k++) {
                    connectionsToTake[k] = -1;
                }
            }
        }

        ArrayList selected_ret_values = new ArrayList();
        // finally, choosing the actual values: 
        for(int k = 0; k < connectionsToTake.length; k++) {
                selected_ret_values.add(ret_values.get(connectionsToTake[k]));
        }

        return selected_ret_values;
    }
    
    /**
     * When computing the perfect minimum matching is too difficult to program, 
     * this will compute a relatively good matching.
     * 
     * May purposely return less edges than needed but that should not spoil the 
     * Christofides heuristic. Uncomment a phase to return less edges.
     * 
     * @param x_coordinates ArrayList
     * @param y_coordinates ArrayList
     * @return ArrayList
     */
    public static ArrayList computeRelativelyGoodMatchingNotPerfectMin(ArrayList x_coordinates, ArrayList y_coordinates) {
        ArrayList selected_ret_values = new ArrayList();
        boolean[] taken = new boolean[x_coordinates.size()];
        boolean randomtakenHasFalse = true;
        
        // order only some 
        for(int j = (taken.length/3)+(taken.length/2); j < taken.length-(taken.length/7); j++) {
            for(int k = (taken.length/3)+(taken.length/2); k < taken.length-(taken.length/7); k++) {
                if(((double)(x_coordinates.get(j)) > (double)(x_coordinates.get(k))) && (k != j)) {
                    //swap 
                    double helpVar1 = (double)x_coordinates.get(j);
                    double helpVar2 = (double)y_coordinates.get(j);

                    x_coordinates.set(j, (double)x_coordinates.get(k));
                    y_coordinates.set(j, (double)y_coordinates.get(k));

                    x_coordinates.set(k, helpVar1);
                    y_coordinates.set(k, helpVar2);
                }
            }
        }
        
        while(randomtakenHasFalse) {
            int k = -1;
            int l = -1;
            double bestMinDist = Double.MAX_VALUE;
            for(int i = 0; i < taken.length; i++) {
                for(int j = i; j < taken.length; j++) {
                    double distance = Euclidean_distance_squared(
                            (double)x_coordinates.get(i), (double)y_coordinates.get(i), 
                            (double)x_coordinates.get(j), (double)y_coordinates.get(j));
                    if((i < j) && (taken[j] == false) && (taken[i] == false) && 
                            (distance < (bestMinDist * 1.5))) {
                        bestMinDist = distance;
                        k = i;
                        l = j;
                    }
                }
            }
            selected_ret_values.add(x_coordinates.get(k));
            selected_ret_values.add(y_coordinates.get(k));
            selected_ret_values.add(x_coordinates.get(l));
            selected_ret_values.add(y_coordinates.get(l));
            taken[k] = true;
            taken[l] = true;
            
            /** (just an idea)
            double r = ((Math.random() * (0.008 - 0.0000002)) + 0.0000002);
            //System.out.println("Random is " + r);
            double wantedMinDistance = Double.MAX_VALUE;
            for(int i = 0; i < randomtaken.length; i++) {
                for(int j = i; j < randomtaken.length; j++) {
                    double distance = Euclidean_distance_squared(
                            (double)x_coordinates.get(i), (double)y_coordinates.get(i), 
                            (double)x_coordinates.get(j), (double)y_coordinates.get(j));
                    if((i != j) && (randomtaken[j] == false) && (randomtaken[i] == false) && (distance < wantedMinDistance)) {
                        wantedMinDistance = (distance * 0.935) +r; // the next distance should be max 93,5% +random (can be changed) 
                        selected_ret_values.add(x_coordinates.get(j));
                        selected_ret_values.add(y_coordinates.get(j));
                        selected_ret_values.add(x_coordinates.get(i));
                        selected_ret_values.add(y_coordinates.get(i));
                        randomtaken[j] = true;
                        randomtaken[i] = true;
                    }
                    else {
                        wantedMinDistance = (wantedMinDistance * 1.001) +0.1 +r; // increasing the requirement (can be changed) 
                    }
                }
            }
            **/
            
            /** (another idea that turned out to be very bad, computing angles from the origo to 2 nodes) 
            boolean angle = smaller_angle(0.0, 0.0, previousX, previousY, 
                    (double)x_coordinates.get(r), (double)y_coordinates.get(r));

                if((randomtaken[r] == false) && (angle)) {
                    selected_ret_values.add(x_coordinates.get(r));
                    selected_ret_values.add(y_coordinates.get(r));
                    randomtaken[r] = true;
                    previousX = (double)x_coordinates.get(r);
                    previousY = (double)y_coordinates.get(r);
                }
                if(!angle) {
                    previousX-=6;
                    previousY-=1;
                }
            **/
            randomtakenHasFalse = false;
            for(int i = 0; i < taken.length; i++) {
                if(taken[i] == false) {
                    randomtakenHasFalse = true;
                }
            }
        }
        /** 
        try {
            for(int i = selected_ret_values.size()-4; i > selected_ret_values.size()-5; i--) {
                System.out.println("Removed with index " + i);
                double dis = Euclidean_distance((double)selected_ret_values.get(i), 
                                                (double)selected_ret_values.get(i+1), 
                                                (double)selected_ret_values.get(i+2), 
                                                (double)selected_ret_values.get(i+3));
                if(dis > 100) {
                    // deleting so that there are less matched edges than normally should, 
                    // should not affect negatively in calculating the TSP tour though 
                    System.out.println("Purposely removed distance: " + dis);
                    selected_ret_values.remove(i+3);
                    selected_ret_values.remove(i+2);
                    selected_ret_values.remove(i+1);
                    selected_ret_values.remove(i);
                }
                i = -1; // end the loop since length will change when something is removed 
            }
        }
        catch(Exception e) {
            //System.out.println(e);
        }
        **/
        return selected_ret_values;
    }
    
    /**
     * Checks if there are equal coordinates so that the 
     * matching would become ".-.-." in the Christofides heuristic,
     * "be" is "begin edge", "ne" is "next edge". (".-.-." is never wanted!)
     * Return "true" if both Xs and Ys are always different as pairs 
     * in the 2 input edges (both starts and ends).
     * 
     * @param bex1 double
     * @param bey1 double
     * @param bex2 double
     * @param bey2 double
     * @param nex1 double
     * @param ney1 double
     * @param nex2 double
     * @param ney2 double
     * @return boolean
     */
    public static boolean checkMatchingEdgeCoordinates(double bex1, double bey1, double bex2, double bey2,
                                                       double nex1, double ney1, double nex2, double ney2) {
        return (bex1 == nex1 && bey1 == ney1) == false && 
               (bex1 == nex2 && bey1 == ney2) == false && 
               (bex2 == nex1 && bey2 == ney1) == false && 
               (bex2 == nex2 && bey2 == ney2) == false;
    }
    
    /**
     * Makes a nearest neighbour sprout from "start_index_NO_ZERO".
     * The "sprout length" should be at max "coordinate count times 2".
     * 
     * @param sprout_length int
     * @param start_index_NO_ZERO int
     * @param coordinates_x ArrayList
     * @param coordinates_y ArrayList
     * @return double[][]
     */
    public static double[][] NNHsprout(int sprout_length, int start_index_NO_ZERO, 
        ArrayList coordinates_x, ArrayList coordinates_y) {
            
        if(sprout_length > (coordinates_x.size() *2)) {
            System.out.println("Sprout length too long!");
            return null;
        }
        
        double[][] ret_values = new double[sprout_length +1][2];
        double[] ret_values_x = new double[sprout_length];
        double[] ret_values_y = new double[sprout_length];

        String solution = "";
        //double tour_length = 0.0;

        boolean[] booltable = new boolean[coordinates_x.size()*2];
        boolean booltable_has_false = true;
        solution = new StringBuilder(solution).append(
                String.valueOf(start_index_NO_ZERO)).toString();
        int pointer = 0; 
        int pointer_start = start_index_NO_ZERO -1;
        //double X1;
        //double Y1;

        int ret_pointer = 0;
        int amount_of_edges = 0;

        while(booltable_has_false && (amount_of_edges < sprout_length)) { 
            //System.out.println("-------------start_index_NO_ZERO is " + start_index_NO_ZERO);
            //System.out.println("-------------pointer_start is " + pointer_start);
            //System.out.println("-------------booltable.length is " + booltable.length);
            if(start_index_NO_ZERO > 0) {
                booltable[start_index_NO_ZERO - 1] = true;
            }
            double min_distance = Double.MAX_VALUE;
            double temp_distance;
            double x1;
            double y1;
            if(pointer_start >= coordinates_x.size()) {
                x1 = (double) coordinates_x.get(pointer_start-coordinates_x.size());
                y1 = (double) coordinates_y.get(pointer_start-coordinates_x.size());
            }
            else {
                x1 = (double) coordinates_x.get(pointer_start);
                y1 = (double) coordinates_y.get(pointer_start);
            }

            //X1 = (double) coordinates_x.get(start_index_NO_ZERO - 1); 
            //Y1 = (double) coordinates_y.get(start_index_NO_ZERO - 1); 
            for(int j = 0; j < booltable.length; j++) {
                if(booltable[j] == false) {
                    double x2;
                    double y2;
                    if(j >= coordinates_x.size()) {
                        x2 = (double) coordinates_x.get(j-coordinates_x.size());
                        y2 = (double) coordinates_y.get(j-coordinates_x.size());
                    }
                    else {
                        x2 = (double) coordinates_x.get(j);
                        y2 = (double) coordinates_y.get(j);
                    }
                    temp_distance = Sub_algorithms.
                            Euclidean_distance_squared(x1, y1, x2, y2);
                    if((temp_distance < min_distance) && (temp_distance > 0)) {
                        min_distance = temp_distance;
                        pointer = j;
                    }
                }
            }
            // new vertice to the result:
            booltable[pointer] = true;
            solution = new StringBuilder(solution).append("-").toString();
            if(pointer >= coordinates_x.size()) {
                solution = new StringBuilder(solution).append(pointer-coordinates_x.size() + 1).toString(); 
                ret_values_x[ret_pointer] = (double)coordinates_x.get(pointer-coordinates_x.size());
                ret_values_y[ret_pointer] = (double)coordinates_y.get(pointer-coordinates_x.size());
            }
            else {
                solution = new StringBuilder(solution).append(pointer + 1).toString(); 
                ret_values_x[ret_pointer] = (double)coordinates_x.get(pointer);
                ret_values_y[ret_pointer] = (double)coordinates_y.get(pointer);
            }

            ret_pointer++;

            //tour_length += Math.sqrt(min_distance);

            booltable_has_false = false;
            for(int j = 0; j < booltable.length; j++) {
                if(booltable[j] == false) {
                    booltable_has_false = true;
                }
            }
            pointer_start = pointer;
            amount_of_edges++;
        }

        //System.out.println(solution);

        ret_values[0][0] = (double)coordinates_x.get(start_index_NO_ZERO -1);
        ret_values[0][1] = (double)coordinates_y.get(start_index_NO_ZERO -1);

        for(int i = 1; i < ret_values_x.length +1; i++) {
            ret_values[i][0] = ret_values_x[i-1];
            ret_values[i][1] = ret_values_y[i-1];
        }
        
        return ret_values;
    }

    /**
     * Try to perform a 2-opt move that tries to remove some crossings for instance.
     * 
     * Should be tested if real improvement is surely made always (0 profit not wanted) 
     * -> this method call will be put in a loop that requires a profit check.
     * 
     * ArrayList coordinates should have edges stored this way:
     * (beginX, beginY, endX, endY) (beginX, beginY, endX, endY) (beginX, beginY, endX, endY) ... 
     * 
     * Uses the Squared Euclidean distance call to avoid worthless square root computing.
     * 
     * @param coordinates ArrayList
     * @param eucDistOld double
     * @return ArrayList
     */
    public static ArrayList twoOpt(ArrayList coordinates, double eucDistOld) {
        ArrayList originals = (ArrayList)coordinates.clone();
        
        //System.out.println("XXXXXXXXXXXXXXX" + coordinates.toString());
        
        for(int i = 4; i < coordinates.size()-8; i+=4) {
            for(int j = i+4; j < coordinates.size()-4; j+=4) {
                
                double ifConnect1 = Euclidean_distance_squared(
                        (double)coordinates.get(i), (double)coordinates.get(i+1), 
                        (double)coordinates.get(j), (double)coordinates.get(j+1));
                double ifConnect2 = Euclidean_distance_squared(
                        (double)coordinates.get(i+2), (double)coordinates.get(i+3), 
                        (double)coordinates.get(j+2), (double)coordinates.get(j+3));
                double iNormal = Euclidean_distance_squared(
                        (double)coordinates.get(i), (double)coordinates.get(i+1), 
                        (double)coordinates.get(i+2), (double)coordinates.get(i+3));
                double jNormal = Euclidean_distance_squared(
                        (double)coordinates.get(j), (double)coordinates.get(j+1), 
                        (double)coordinates.get(j+2), (double)coordinates.get(j+3));
                
                double profitNeg = (ifConnect1 + ifConnect2) - (iNormal + jNormal);
                
                //System.out.println("Tour profit (if negative, good) would be " + profitNeg);
                if(profitNeg < -0.000001) {
                    /**
                    System.out.println("Possible profit (squared) " + profitNeg + " when moved from " + 
                        (double)coordinates.get(i) + ", " + (double)coordinates.get(i+1) + 
                        " to " + (double)coordinates.get(j) + ", " + (double)coordinates.get(j+1) + 
                        " and " + (double)coordinates.get(i+2) + ", " + (double)coordinates.get(i+3) + 
                        " to " + (double)coordinates.get(j+2) + ", " + (double)coordinates.get(j+3) + 
                        " instead of " + (double)coordinates.get(i) + ", " + (double)coordinates.get(i+1) + 
                        " to " + (double)coordinates.get(i+2) + ", " + (double)coordinates.get(i+3) + 
                        " and " + (double)coordinates.get(j) + ", " + (double)coordinates.get(j+1) + 
                        " to " + (double)coordinates.get(j+2) + ", " + (double)coordinates.get(j+3));
                    **/
                    // swapping 
                    
                    // could be improved:
                    double helpVar1 = (double)coordinates.get(j+2);
                    double helpVar2 = (double)coordinates.get(j+3);
                    double helpVar3 = (double)coordinates.get(i);
                    double helpVar4 = (double)coordinates.get(i+1);
                    coordinates.set(i, coordinates.get(j));
                    coordinates.set(i+1, coordinates.get(j+1));
                    coordinates.set(j+2, coordinates.get(i+2));
                    coordinates.set(j+3, coordinates.get(i+3));
                    coordinates.set(i+2, helpVar1);
                    coordinates.set(i+3, helpVar2);
                    coordinates.set(j, helpVar3);
                    coordinates.set(j+1, helpVar4);
                    
                    //System.out.println("SWAP!! 2opt swap with indexes " + i + ", " + j);
                    //System.out.println("XXXXXXXXXXXXXXX" +coordinates.toString() + "\n");
                    
                    if(i > coordinates.size()/2) {
                        for(int k = 2; k < coordinates.size()-2; k+=2) {
                            for(int l = 2; l < coordinates.size()-2; l+=2) {
                                if(((double)coordinates.get(k) == (double)coordinates.get(l)) && 
                                   ((double)coordinates.get(k+1) == (double)coordinates.get(l+1))) {
                                        coordinates.remove(l+1);
                                        coordinates.remove(l);
                                        break;
                                }
                            }
                        }
                        //System.out.println("AFTER xXxXxXxXx" + coordinates.toString());
                    }
                    else {
                        for(int k = coordinates.size()-4; k > 2; k-=2) {
                            for(int l = coordinates.size()-4; l > 2; l-=2) {
                                if(((double)coordinates.get(k) == (double)coordinates.get(l)) && 
                                   ((double)coordinates.get(k+1) == (double)coordinates.get(l+1))) {
                                        coordinates.remove(l+1);
                                        coordinates.remove(l);
                                        k-=2;
                                        break;
                                }
                            }
                        }
                        //System.out.println("AFTER XXXXXXXXX" + coordinates.toString());
                    }
                    
                    int random1 = 2+(int)(Math.random()*((((coordinates.size()/2)-6) -2) +2));
                    for(int k = random1 *2; k < (random1 *2)+1; k+=2) {
                        for(int l = k+2; l < k+3; l+=2) {
                            helpVar1 = (double)coordinates.get(k);
                            helpVar2 = (double)coordinates.get(k+1);
                            coordinates.set(k, coordinates.get(l));
                            coordinates.set(k+1, coordinates.get(l+1));
                            coordinates.set(l, helpVar1);
                            coordinates.set(l+1, helpVar2);
                        }
                    }
                    
                    i = coordinates.size();
                    //j = coordinates.size(); // ending the i and j for loops 
                    break;
                    //System.out.println("After          " + coordinates.toString());
                }
            }
        }

        double eucNew = 0.0;
        for(int i = 0; i < coordinates.size()-2; i+=2) {
            eucNew += Euclidean_distance((double)coordinates.get(i), 
                                         (double)coordinates.get(i+1), 
                                         (double)coordinates.get(i+2), 
                                         (double)coordinates.get(i+3));
        }
        for(int k = 2; k < coordinates.size()-2; k+=2) {
            coordinates.add(k+1, coordinates.get(k+1));
            coordinates.add(k, coordinates.get(k));
                double helpVar1 = (double)coordinates.get(k+1);
                coordinates.set(k+1, coordinates.get(k+2));
                coordinates.set(k+2, helpVar1);
            k+=2;
        }
        
        //System.out.println("NEW dist: " + eucNew + ", OLD dist: " + eucDistOld);
        if(eucNew < eucDistOld) {
            System.out.println("IMPROVED RETURNED: " + coordinates);
            return coordinates;
        }
        else {
            //System.out.println("ORIGINALS RETURNED: " + originals);
            return originals;
        }
    }
    
    /**
     * An idea: removing 3 edges, try reconnecting in every possible way.
     * Reconnecting possibly 7 different ways.
     * If improvement is found, then return the better/best route.
     * 
     * @param coordinates ArrayList
     * @return ArrayList
     */
    public static ArrayList threeOpt(ArrayList coordinates) {
        System.out.println("Unimplemented method threeOpt called.");
        return null;
    }
    
    /**
     * Basic idea: take an already existing Hamiltonian cycle (TSP tour, chromosome).
     * Make opt moves and try to find out improvements (randomly in this implementation).
     * If stuck at a local minimum, possibly try to steer the computing to another direction.
     * Uses the double stack like the convex hull.
     * 
     * This implementation is not exactly like in official books. 
     * Uses 2-opts only, could be improved a lot, does not use a tree, does not 
     * delete bad gains.
     * 
     * @param max int
     * @param eucDistOld double
     * @param coordinates ArrayList
     * @return double[][]
     */
    public static double[][] linKernighan(int max, double eucDistOld, ArrayList coordinates) {
        
        // max = amount of vertices, then the best known distance, then the coordinates xy xy xy...
        System.out.println("LK at the start: " + coordinates.toString() + "\nOLD Euc. distance is " + eucDistOld);
        ArrayList coordinatesTry1 = new ArrayList<>();
        double wanted_distance_limit = 0; // or kind of gain control, turned out unnecessary so 0 is set 
        DoubleStack x_coordinatesSt = new DoubleStack();
        DoubleStack y_coordinatesSt = new DoubleStack();
        
        for(int i = 0; i < coordinates.size(); i+=2) {
            x_coordinatesSt.push((double)coordinates.get(i));
            y_coordinatesSt.push((double)coordinates.get(i+1));
            //System.out.println("pushed " + (double)coordinates.get(i) + 
            //        " and " + (double)coordinates.get(i+1));
        }
        boolean improvement = false;

        //System.out.println("Previously: " + coordinates);
        //System.gc(); // garbage collector
        
        for(int i = 0; i < 20000000; i++) { // for loop can adjust how many tries 
            double dxStartEnd = x_coordinatesSt.top(); 
                                x_coordinatesSt.pop();
            double dyStartEnd = y_coordinatesSt.top(); 
                                y_coordinatesSt.pop();
            coordinatesTry1.add(dxStartEnd);
            coordinatesTry1.add(dyStartEnd);
            
            boolean switc;// = false; // switch is an illegal variable name in Java 
            double d1; 
            double d2; 
            double d3; 
            double d4; 
            double d5; 
            double d6;
            while(!x_coordinatesSt.isEmpty() && !y_coordinatesSt.isEmpty()) {
                // System.gc(); // if Java memory issues occur 
                boolean done = false;
                double eucdist = 0.0;
                
                d1 = x_coordinatesSt.top();     // 3 nodes/vertices
                     x_coordinatesSt.pop();

                d2 = y_coordinatesSt.top();
                     y_coordinatesSt.pop();
                
                //if((d1 != dxStartEnd) && (d2 != dyStartEnd) && 
                //    d1 != Double.MAX_VALUE && d2 != Double.MAX_VALUE) {
                        d3 = x_coordinatesSt.top();
                             x_coordinatesSt.pop();

                        d4 = y_coordinatesSt.top();
                             y_coordinatesSt.pop();
                                
                //    if((d3 != dxStartEnd) && (d4 != dyStartEnd) && 
                //       (d3 != Double.MAX_VALUE && d4 != Double.MAX_VALUE)) {
                            d5 = x_coordinatesSt.top();
                                 x_coordinatesSt.pop();

                            d6 = y_coordinatesSt.top();
                                 y_coordinatesSt.pop();
                //    }
                //}
                
                if( //((d1 >= Double.MAX_VALUE && d2 >= Double.MAX_VALUE) || 
                    // (d3 >= Double.MAX_VALUE && d4 >= Double.MAX_VALUE) || 
                    // (d5 >= Double.MAX_VALUE && d6 >= Double.MAX_VALUE)) || 
                   ((d1 == dxStartEnd && d2 == dyStartEnd) || 
                    (d3 == dxStartEnd && d4 == dyStartEnd) || 
                    (d5 == dxStartEnd && d6 == dyStartEnd))) {
                        done = true;
                        x_coordinatesSt.empty();
                        y_coordinatesSt.empty();
                        
                        //System.out.println(d1 + ", " + d2 + ", " +  d3 + 
                        //           ", " +  d4 + ", " + d5 + ", " +  d6);
                    
                        if(d1 == dxStartEnd && d2 == dyStartEnd) {
                            coordinatesTry1.add(d1);
                            coordinatesTry1.add(d2);
                        }
                        if(d3 == dxStartEnd && d4 == dyStartEnd) {
                            coordinatesTry1.add(d1);
                            coordinatesTry1.add(d2);
                            coordinatesTry1.add(d3);
                            coordinatesTry1.add(d4);
                        }
                        if(d5 == dxStartEnd && d6 == dyStartEnd) {
                            coordinatesTry1.add(d1);
                            coordinatesTry1.add(d2);
                            coordinatesTry1.add(d3);
                            coordinatesTry1.add(d4);
                            coordinatesTry1.add(d5);
                            coordinatesTry1.add(d6);
                        }
                }
                
                if(!done) { // switch cannot be a variable name in Java 
                    switc = Math.random() < 0.8; // random boolean, can be "unfairly" balanced on purpose, 
                                                 // replaces the "odd - even" detail 
                    double testingEucdist1 = Euclidean_distance_squared(d1, d2, d3, d4);
                    double testingEucdist2 = Euclidean_distance_squared(d5, d6, d3, d4);
                    
                    //System.out.println("Trying with " + d1 + ", " + d2 + ", " +  
                    //        d3 + ", " +  d4 + ", " + d5 + ", " +  d6);
                    
                    if(wanted_distance_limit < (testingEucdist1 - testingEucdist2)) {
                        if(switc) {
                            if(testingEucdist1 < testingEucdist2) {
                                x_coordinatesSt.push(d5);
                                y_coordinatesSt.push(d6);
                                coordinatesTry1.add(d1);
                                coordinatesTry1.add(d2);
                                coordinatesTry1.add(d3);
                                coordinatesTry1.add(d4);
                            }
                            else {
                                x_coordinatesSt.push(d1);
                                y_coordinatesSt.push(d2);
                                coordinatesTry1.add(d3);
                                coordinatesTry1.add(d4);
                                coordinatesTry1.add(d5);
                                coordinatesTry1.add(d6);
                            }
                        }
                        else {
                            if(testingEucdist1 < testingEucdist2) {
                                x_coordinatesSt.push(d5);
                                y_coordinatesSt.push(d6);
                                coordinatesTry1.add(d3);
                                coordinatesTry1.add(d4);
                                coordinatesTry1.add(d1);
                                coordinatesTry1.add(d2);
                            }
                            else {
                                x_coordinatesSt.push(d1);
                                y_coordinatesSt.push(d2);
                                coordinatesTry1.add(d5);
                                coordinatesTry1.add(d6);
                                coordinatesTry1.add(d3);
                                coordinatesTry1.add(d4);
                            }
                        }
                        if((wanted_distance_limit < (testingEucdist1 - testingEucdist2))) {
                            wanted_distance_limit *= 1.2;
                        }
                        else {
                            if(wanted_distance_limit > testingEucdist1 *1.5) {
                                wanted_distance_limit -= testingEucdist1;
                            }   // adjusting with programmer's own will with this if-else part 
                            if(wanted_distance_limit > testingEucdist2 *1.5) {
                                wanted_distance_limit -= testingEucdist2;
                            }
                        }
                        /**
                         if((wanted_distance_limit < (testingEucdist1 - testingEucdist2)*2)) {
                            wanted_distance_limit *= 1.02;
                            a += 0.01;          // a can be the adjuster of switc (0.5 thing) 
                        }
                        else {
                            if(wanted_distance_limit > testingEucdist1 *1.1) {
                                wanted_distance_limit /= testingEucdist1;
                            }       // adjusting with programmer's own will with this if-else part 
                            a -= 0.08;
                        }
                        if(a > 0.7) {
                            a= 0.55;
                        }
                        if(a < 0.3) {
                            a= 0.4;
                        }
                         */
                    }
                    else {
                        coordinatesTry1.add(d1);
                        coordinatesTry1.add(d2);
                        coordinatesTry1.add(d3);
                        coordinatesTry1.add(d4);
                        coordinatesTry1.add(d5);
                        coordinatesTry1.add(d6);
                        wanted_distance_limit /=1.5;
                    }
                }
                
                if(done) {
                    for(int j = 0; j < coordinatesTry1.size()-2; j+=2) {
                        eucdist += Euclidean_distance((double)coordinatesTry1.get(j), 
                                                      (double)coordinatesTry1.get(j+1), 
                                                      (double)coordinatesTry1.get(j+2), 
                                                      (double)coordinatesTry1.get(j+3));
                        // be sure to add also the end node which is the same as start node
                    }
                    
                    x_coordinatesSt.empty();
                    y_coordinatesSt.empty();
                    if((eucdist < (eucDistOld)) && x_coordinatesSt.isEmpty() && y_coordinatesSt.isEmpty()) {
                        System.out.println("\tImprovement found in LK phase before more 2opts! "
                                + eucdist + " --- " + eucDistOld + "\n");
                        improvement =true;
                        eucDistOld = eucdist;
                        coordinates = (ArrayList)coordinatesTry1.clone();
                        // coordinates (ArrayList) should have an improved solution, if found 
                    }
                    else {
                        System.out.println("\tNo improvements this time. " + 
                                eucdist + ", old distance: " + eucDistOld + "\n");
                        // quit on purpose, leave the stacks empty if they 
                        // are empty - so the while loop can end 
                    }
                }
            }
        }
        System.out.println("And now: " + coordinates + "\nIMPROVEMENTS YET?: " + improvement);
        
        double[][] edges = new double[max+1][2]; // value, isY? 
        for(int i = 0; i < max+1; i++) {
            edges[i][0] = Double.MAX_VALUE;
            edges[i][1] = Double.MAX_VALUE;
        }
        
        for(int i = 2; i < coordinates.size()-2; i+=2) {
            coordinates.add(i+2, coordinates.get(i));
            coordinates.add(i+3, coordinates.get(i+1));
            i+=2;
        }
        
        // some random 2-opt trying:
        ArrayList coordinatesBest;// = new ArrayList();
        coordinatesBest = new ArrayList<>(coordinates);
        ArrayList coordinatesTry = coordinates;
        double eucdBestNew = eucDistOld;
        
        for(int i = 0; i < 3000; i++) {    // can adjust how many times 
            coordinatesTry = twoOpt(coordinatesTry, eucdBestNew);
            double eucd = 0.0;
            for(int j = 0; j < coordinatesTry.size(); j+=4) {
                eucd += Euclidean_distance((double)coordinatesTry.get(j), 
                                           (double)coordinatesTry.get(j+1), 
                                           (double)coordinatesTry.get(j+2), 
                                           (double)coordinatesTry.get(j+3));
            }
            if((eucd <= eucdBestNew) && (eucdBestNew <= eucDistOld)) {
                //System.out.println("---NEW BEST: " + eucd);
                eucdBestNew = eucd;
                coordinatesBest = (ArrayList)coordinatesTry.clone();
            }
        }
        
        for(int i = 2; i < coordinatesBest.size()-1; i+=2) {
            for(int j = 2; j < coordinatesBest.size()-1; j+=2) {
                if(coordinatesBest.get(i) == coordinatesBest.get(j) && 
                   coordinatesBest.get(i+1) == coordinatesBest.get(j+1)) {
                        coordinatesBest.remove(i);
                        coordinatesBest.remove(i);
                }
            }
        }
        coordinatesBest.add(coordinatesBest.get(0));
        coordinatesBest.add(coordinatesBest.get(1));
        
        int insertIndex = 0;
        for(int i = 0; i < coordinatesBest.size()-2; i+=2) {
            edges[insertIndex][0] = (double)coordinatesBest.get(i);
            edges[insertIndex][1] = (double)coordinatesBest.get(i+1);
            insertIndex++;
        }
        return edges;
    }
} 