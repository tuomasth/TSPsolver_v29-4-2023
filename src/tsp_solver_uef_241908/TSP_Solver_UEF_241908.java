package tsp_solver_uef_241908;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 10 of 11 
 * The UI class has the version variable "final String VERSION = "v-29-4-2023";" 
 * 
 * 
 * The main class with the main algorithms: 
 * 
 * F2 NNH               Nearest neighbour as the simplest heuristic there exists 
 * 
 * F3 2MST              Using minimum spanning tree's doubled edges and Euler tour 
 * 
 * F4 CHH               Convex hull around everything and connect the inner nodes one by one 
 * 
 * F5 CHRI              Christofides heuristic, using minimum spanning tree and its odd degree node matching and Euler tour 
 * 
 * F6 LK-NNH-CHH-CHRI   F2 for a couple of times, F4 once, F5 once, choose the best and try to improve with a quick 2opt-Lin-Kernighan 
 * 
 * F7 SOM-CH-NN         Calculate the convex hull so its nodes (or edge centroids) can be the input nodes and clusters, then 
 *                      the inner nodes are movable neurons that perform the Kohonen Self-Organizing Map algorithm, finally 
 *                      each cluster performs the NNH which chains everything and creates the Hamiltonian circuit 
 * 
 * F8 SOM-CH-NN-EVO     Same as F7 but with a bit more clusters and evolution is used, the population consists of 
 *                      chromosomes (multiple F7 results), the movable neurons also have logic stacks that tell what to do 
 *                      after moving towards the SOM goal, the programmer can make his/her own logic fragments to the stacks 
 * 
 * F9 LK-SOM-CH-NN-EVO  Same as F8 but the F6's quick Lin-Kernighan is used in the end once 
 * 
 * 
 * Great TSP test data available at the Waterloo University website: 
 * https://www.math.uwaterloo.ca/tsp/data/index.html 
 * (28/4/2023) 
 * 
 * A previous version is "TSPsolver_v14-3-2017" on github.com/tuomasth 
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
public class TSP_Solver_UEF_241908 {
    // The error message for the user when the graph has syntax errors or other issues 
    private final static String ERRORMSG = " The tsplib graph input has errors. "
        + "Check at least that:\n"
        + " 1) There are at least 4 vertices (also known as nodes, points).\n"
        + " 2) The edge weight type is EUC_2D.\n"
        + " 3) There are not 2 (or more) vertices with equal coordinates.\n"
        + " 4) All x & y coordinates are less than 5 000 000"
        + " but not less than 0 and that the E notation will not be needed.\n"
        + " 5) The coordinate separator mark is space (' ').\n"
        + " 6) Coordinate lines always start with an integer ID number."
        + " Keep the IDs in order 1 2 3 4 5... so node misconceptions will be avoided.\n"
        + " 7) Instead of commas (',') prefer points ('.') in decimal numbers."
        + " After the decimal point, please include a reasonable length of numbers.\n"
        + " 8) Do not write unnecessary content to the coordinate part. "
        + "Extra space marks might cause errors and the maximum row count is 500 000."
        + "\n\n The algorithm run did not start.";
    
    /**
     * The main method that begins with setting up the User Interface.
     * 
     * @param args String, the command line arguments 
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new User_interface().setVisible(true);
            } 
        );
    }
    
    /**
     * This method checks that the input does have some correct numbers.
     * 
     * @param coordinates ArrayList
     * @return String
     */
    public static String checkCoordinateInput(ArrayList coordinates) {
        boolean inputOk = true;
        for (Object coordinate : coordinates) {
            try {
                if ((double) coordinate >= 5000000) {
                    inputOk = false; 
                    System.out.println("5 000 000 or more found");
                }
                if ((double) coordinate < 0) {
                    inputOk = false; 
                    System.out.println("less than 0 found");
                }
                if (coordinate.toString().contains("E") ||
                    coordinate.toString().contains("e")) {
                    inputOk = false;
                    System.out.println("ridiculous coordinate value found, probably E notation (exponents)");
                }
                //if ((double) coordinate % 1 != 0) {
                //    inputOk = false; 
                //    System.out.println("not an integer");
                //}         // uncomment if only integers wanted 
            }
            catch(Exception e) {
                System.out.println(e);
                return ERRORMSG;
            }
        }
        if(inputOk) {
            return "ok";
        }
        else {
            return ERRORMSG;
        }
    }
    
    /**
     * Checks if a tour is a Hamiltonian circuit where every node is visited 
     * exactly once. Input should be something like: "1-2-3-4-5-6-1".
     * Does not touch any actual coordinates, pure String ID number checking.
     * 
     * @param solution String
     * @param wantedVerticecount int
     * @return boolean
     */
    public static boolean checkHamiltonian(String solution, int wantedVerticecount) {
        boolean hamiltonian = true;
        boolean[] booltable = new boolean[wantedVerticecount];
        String[] solutiontable = solution.split("-", solution.length());
        if(solutiontable.length-1 != wantedVerticecount) {
            System.out.println("Wrong length");
            System.out.println(Arrays.toString(solutiontable));
            hamiltonian = false;
            System.out.println("IS HAMILTONIAN CYCLE? = " + hamiltonian);
            return hamiltonian;
        }
        for(int i = 0; i < solutiontable.length; i++) {
            if(i >= solutiontable.length-1) {
                if(!solutiontable[0].equals(solutiontable[solutiontable.length-1])) {
                    System.out.println("Start and end are NOT the same");
                    System.out.println(Arrays.toString(solutiontable));
                    hamiltonian = false;
                    System.out.println("IS HAMILTONIAN CYCLE? = " + hamiltonian);
                    return hamiltonian;
                }
                //else {
                    //System.out.println("Start and end are the same: " + solutiontable[0]);
                //}
            }
            else {
                for(int j = 1; j < solutiontable.length; j++) {
                    hamiltonian = false;
                    if(Integer.valueOf(solutiontable[i]).equals(j)) {
                        hamiltonian = true;
                        booltable[j-1] = true;
                        //System.out.println("Found " + solutiontable[i]);
                        break; // end the loop
                    }
                }
            }
        }
        for(int i = 0; i < booltable.length; i++) {
            if(booltable[i] == false) {
                hamiltonian = false;
                System.out.println("It seems like at least 1 node (vertex, point) is missing," 
                        + " not a Hamiltonian circuit");
            }
        }
        //System.out.println(Arrays.toString(booltable));
        //System.out.println(Arrays.toString(solutiontable));
        //System.out.println("IS HAMILTONIAN CYCLE? = " + hamiltonian);
        return hamiltonian;
    }
    
    /**
     * The Nearest Neighbo(u)r Heuristic - the simplest algorithm.
     * 
     * @param input String
     * @return String
     */
    public static String NearestNeighbour_Algorithm(String input) {
        double tour_length = 0.0;
        String solution = "";
        int min = 1;
        int max = 0;
        String rows[] = new String[500000];    
        ArrayList coordinates = new ArrayList();
        int i = 0;
        String str;
        BufferedReader reader = new BufferedReader(new StringReader(input));
        boolean EUC_2D = false;
        boolean hasSameCoordinates = false;
        boolean rightAmountOfNumbers = false;
        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    rows[i] = str; 
                    if(rows[i].charAt(0) == '0' || 
                            rows[i].charAt(0) == '1' ||
                            rows[i].charAt(0) == '2' || 
                            rows[i].charAt(0) == '3' ||
                            rows[i].charAt(0) == '4' || 
                            rows[i].charAt(0) == '5' ||
                            rows[i].charAt(0) == '6' || 
                            rows[i].charAt(0) == '7' ||
                            rows[i].charAt(0) == '8' || 
                            rows[i].charAt(0) == '9') {
                        rows[i] = rows[i].replaceAll(",", ".");
                        max++;
                        try {
                            Double numberInput;
                            int endIndex;
                            for (int beginIndex = 0; beginIndex < 
                                    rows[i].length(); 
                                    beginIndex = endIndex + 1) {
                                endIndex = rows[i].indexOf(" ", beginIndex);
                                if (endIndex == -1) {
                                    endIndex = rows[i].length();
                                }
                                String numberString = rows[i].substring(
                                        beginIndex, endIndex);
                                try {
                                    numberInput = Double.valueOf(
                                            numberString);
                                    coordinates.add(numberInput);   
                                    // second x, third y 
                                } 
                                catch (java.lang.NumberFormatException nfe) {
                                    System.err.println(nfe);
                                }
                            }
                        }
                        catch(Exception e) {
                            System.err.println(e);
                        }
                    }
                    if(rows[i].contains("EUC_2D")) {
                        EUC_2D = true;
                        //System.out.println("Graph is EUC_2D");
                    }
                }
            }
        } 
        catch(IOException e) {
            System.err.println(e);
        }
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        for(int j = 1; j < coordinates.size(); j++) {
            if(j%3 == 0) {
                rightAmountOfNumbers = false;
            }
            if((j - 1)%3 == 0) {
                coordinates_x.add(coordinates.get(j));
                rightAmountOfNumbers = false;
            }
            if((j - 2)%3 == 0) {
                coordinates_y.add(coordinates.get(j));
                rightAmountOfNumbers = true;
            }
        }
        for(int j = 0; j < coordinates_x.size()-1; j++) {
            for(int k = j + 1; k < coordinates_x.size(); k++) {
                if(coordinates_x.get(j).equals(coordinates_x.get(k)) &&
                   coordinates_y.get(j).equals(coordinates_y.get(k))) {
                    hasSameCoordinates = true;
                    System.out.println("equal coordinates" + 
                            coordinates_x.get(j) + ", " + 
                            coordinates_y.get(j) + " and " + 
                            coordinates_x.get(k) + ", " + 
                            coordinates_y.get(k));
                }
            }
        }
        // The NNH input is read, now the algorithm starts if the input is ok.
        
        String inputresult = checkCoordinateInput(coordinates);
        if(inputresult.equals("ok") && max >=4 && EUC_2D && 
                !hasSameCoordinates && rightAmountOfNumbers) {
            System.gc(); // run garbage collector before starting 
            //String[] args = new String[1]; 
            //args[0] = "$ java -javaagent:tracker.jar TSP_Solver_UEF_241908 500 > /dev/null";
            //ResourceTracker.premain(args[0]);
            // ^ Comment when PhD Pekka Kilpeläinen ResourceTracker.java is not used 
            long startTime = System.nanoTime();
            
            Random rand = new Random();
            int random = rand.nextInt((max - min) + 1) + min;
            boolean[] booltable = new boolean[max];
            boolean booltable_has_false = true;
            solution = new StringBuilder(solution).append(
                    String.valueOf(random)).toString();
            int pointer = 0; 
            int pointer_start = random -1;
            double X1 = 0.0;
            double Y1 = 0.0;
            while(booltable_has_false) { 
                booltable[random - 1] = true;
                double min_distance = Double.MAX_VALUE;
                double temp_distance;
                double x1 = (double) coordinates_x.get(pointer_start);
                double y1 = (double) coordinates_y.get(pointer_start);
                X1 = (double) coordinates_x.get(random - 1); 
                Y1 = (double) coordinates_y.get(random - 1); 
                for(int j = 0; j < booltable.length; j++) {
                    if(booltable[j] == false) {
                        double x2 = (double) coordinates_x.get(j);
                        double y2 = (double) coordinates_y.get(j);
                        temp_distance = Sub_algorithms.
                                Euclidean_distance_squared(x1, y1, x2, y2);
                        if(temp_distance < min_distance) {
                            min_distance = temp_distance;
                            pointer = j;
                        }
                    }
                }
                // new vertice to the result:
                booltable[pointer] = true;
                solution = new StringBuilder(solution).append("-").toString();
                solution = new StringBuilder(solution).append(pointer + 1).toString();  
                tour_length += Math.sqrt(min_distance);

                booltable_has_false = false;
                for(int j = 0; j < booltable.length; j++) {
                    if(booltable[j] == false) {
                        booltable_has_false = true;
                    }
                }
                pointer_start = pointer;
            }
            
            // link back to the start node:
            solution = new StringBuilder(solution).append("-").toString();
            solution = new StringBuilder(solution).append(random).toString();
            tour_length += Sub_algorithms.Euclidean_distance(
                    (double) coordinates_x.get(pointer), 
                    (double) coordinates_y.get(pointer), X1, Y1);
            String justSolutionOnly = solution;
            solution = new StringBuilder(solution).append("\nTour length: ").
                    toString();
            solution = new StringBuilder(solution).append(tour_length).
                    toString();
            
            boolean hamiltonian = checkHamiltonian(justSolutionOnly, max);
            if(hamiltonian) {
                long endTime = System.nanoTime();
                System.out.println("Time (millisec): " + (endTime - startTime)/1000000);
                System.out.println("Time (sec, rounded down): " + (endTime - startTime)/1000000000 + "\n");
                return "Solution: \n" + solution;
            }
            else {
                return "Bug! The calculated tour is not a Hamiltonian circuit!\n" + solution;
            }
        }
        else {
            return ERRORMSG;
        }
    } // This NNH code is almost exactly the same as in "TSPsolver_v14-3-2017" 
    
    /**
     * The 2-MST algorithm. Double the MST's edges, then make the Euler tour. 
     * There does exist a proof that says the accuracy is at most 2 times the optimum. 
     * However, when this runs in practice, much lower than that 100% deviation can usually be expected. 
     * The accuracy can depend on how the vertices are stored and read in the memory. 
     * 
     * @param input String
     * @return String
     */
    public static String DoubleMST_Algorithm_Prim(String input) {
        double tour_length = 0.0;
        String solution;
        int min = 1;
        int max = 0;
        String rows[] = new String[500000];    
        ArrayList coordinates = new ArrayList();
        int i = 0;
        String str;
        BufferedReader reader = new BufferedReader(new StringReader(input));
        boolean EUC_2D = false;
        boolean hasSameCoordinates = false;
        boolean rightAmountOfNumbers = false;
        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    rows[i] = str;
                    if(rows[i].charAt(0) == '0' || 
                            rows[i].charAt(0) == '1' ||
                            rows[i].charAt(0) == '2' || 
                            rows[i].charAt(0) == '3' ||
                            rows[i].charAt(0) == '4' || 
                            rows[i].charAt(0) == '5' ||
                            rows[i].charAt(0) == '6' || 
                            rows[i].charAt(0) == '7' ||
                            rows[i].charAt(0) == '8' || 
                            rows[i].charAt(0) == '9') {
                        rows[i] = rows[i].replaceAll(",", ".");
                        max++;
                        try {
                            Double numberInput;
                            int endIndex;
                            for (int beginIndex = 0; beginIndex < 
                                    rows[i].length(); 
                                    beginIndex = endIndex + 1) {
                                endIndex = rows[i].indexOf(" ", beginIndex);
                                if (endIndex == -1) {
                                    endIndex = rows[i].length();
                                }
                                String numberString = rows[i].substring(
                                        beginIndex, endIndex);
                                try {
                                    numberInput = Double.valueOf(numberString);
                                    coordinates.add(numberInput);   
                                    // second x, third y 
                                } 
                                catch (java.lang.NumberFormatException nfe) {
                                    System.err.println(nfe);
                                }
                            }
                        }
                        catch(Exception e) {
                            System.err.println(e);
                        }
                    }
                    if(rows[i].contains("EUC_2D")) {
                        EUC_2D = true;
                    }
                }
            }
        } 
        catch(IOException e) {
            System.err.println(e);
        }
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        for(int j = 1; j < coordinates.size(); j++) {
            if(j%3 == 0) {
                rightAmountOfNumbers = false;
            }
            if((j - 1)%3 == 0) {
                coordinates_x.add(coordinates.get(j));
                rightAmountOfNumbers = false;
            }
            if((j - 2)%3 == 0) {
                coordinates_y.add(coordinates.get(j));
                rightAmountOfNumbers = true;
            }
        }
        for(int j = 0; j < coordinates_x.size()-1; j++) {
            for(int k = j + 1; k < coordinates_x.size(); k++) {
                if(coordinates_x.get(j).equals(coordinates_x.get(k)) &&
                   coordinates_y.get(j).equals(coordinates_y.get(k))) {
                    hasSameCoordinates = true;
                    System.out.println("equal coordinates" + 
                            coordinates_x.get(j) + ", " + 
                            coordinates_y.get(j) + " and " + 
                            coordinates_x.get(k) + ", " + 
                            coordinates_y.get(k));
                }
            }
        }
        // The 2MST input is read, now the algorithm starts if the input is ok.
        
        String inputresult = checkCoordinateInput(coordinates);
        if(inputresult.equals("ok") && max >=4 && EUC_2D && 
                !hasSameCoordinates && rightAmountOfNumbers) {
            System.gc(); // run garbage collector before starting 
            //String[] args = new String[1]; 
            //args[0] = "$ java -javaagent:tracker.jar TSP_Solver_UEF_241908 500 > /dev/null";
            //ResourceTracker.premain(args[0]);
            // ^ Comment if ResourceTracker.java is not used 
            long startTime = System.nanoTime();
            
            // call the MST Prim method: 
            double edges[][][] = Sub_algorithms.MST_Prim(min, max, coordinates_x, 
                    coordinates_y);
            // double the edges, in other words make a copy of each edge: 
            double doubled_edges[][][] = new double[(max*2)-1][2][2];
            for(int j = 1; j < max; j++) {
                doubled_edges[j-1][0][0] = edges[j-1][0][0];
                doubled_edges[j-1][1][0] = edges[j-1][1][0];
                doubled_edges[j-1][0][1] = edges[j-1][0][1];
                doubled_edges[j-1][1][1] = edges[j-1][1][1];
                doubled_edges[j+max-2][0][0] = edges[j-1][0][1];
                doubled_edges[j+max-2][1][0] = edges[j-1][1][1];
                doubled_edges[j+max-2][0][1] = edges[j-1][0][0];
                doubled_edges[j+max-2][1][1] = edges[j-1][1][0];
            }
            String connections_string = "";
            int prim_mst_connections = 0;
            // the next for loop helps to recognize 
            // what exactly are the current connections: 
            for(int j = 1; j < (max*2)-1; j++) {
                        String X1 = String.valueOf(doubled_edges[j-1][0][0]); 
                        String Y1 = String.valueOf(doubled_edges[j-1][1][0]); 
                        String X2 = String.valueOf(doubled_edges[j-1][0][1]); 
                        String Y2 = String.valueOf(doubled_edges[j-1][1][1]); 
                        connections_string = connections_string.concat( 
                                "\tFrom (" + X1 + ", " + Y1 + 
                                ")    to    (" + X2 + ", " + Y2 + ")\n");
                        prim_mst_connections++;
            }
            // finally, make the Euler tour and shortcuts: 
            String [] result = Sub_algorithms.Euler_and_embedded_tour(doubled_edges, 
                    max, (max*2)-1);
            // Hamiltonian tour and tour_length: 
            ArrayList numbers = new ArrayList();
            BufferedReader reader2 = new BufferedReader(new StringReader(input));
            try {
                while ((str = reader2.readLine()) != null) {
                    if (str.length() > 0) {
                        rows[i] = str;
                        if(rows[i].charAt(0) == '0' || 
                                rows[i].charAt(0) == '1' ||
                                rows[i].charAt(0) == '2' ||
                                rows[i].charAt(0) == '3' ||
                                rows[i].charAt(0) == '4' ||
                                rows[i].charAt(0) == '5' ||
                                rows[i].charAt(0) == '6' ||
                                rows[i].charAt(0) == '7' ||
                                rows[i].charAt(0) == '8' ||
                                rows[i].charAt(0) == '9') {
                            try {
                                double numberInput;
                                int endIndex;
                                for (int beginIndex = 0; beginIndex <
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", 
                                            beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        numberInput = Double.
                                                parseDouble(numberString);
                                        numbers.add(numberInput);
                                    } 
                                    catch (java.lang.
                                            NumberFormatException nfe) {
                                        System.err.println(nfe);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }   
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            // The next code may be weird but this works for cleaning up the result: 
            String string_to_compare_with_numbers = Arrays.toString(result);
            String replaced1 = string_to_compare_with_numbers.replaceAll(",", "");
            String replaced2 = replaced1.replace("[", "");
            String replaced3 = replaced2.replace("]", "");
            String replaced4 = replaced3.replaceAll("\\^[0-9]+(\\.[0-9]{1,4})?$","");
            Matcher m = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(replaced4);
            String replaced5 = numbers.toString().replaceAll(",", "");
            String replaced6 = replaced5.replace("[", "");
            String replaced7 = replaced6.replace("]", "");
            String replaced8 = replaced7.replaceAll("\\^[0-9]+(\\.[0-9]{1,4})?$","");
            Matcher m2 = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(replaced8);
            
            double[] TSPsolutionCoord_x = new double[(numbers.size() / 2)-1];
            double[] TSPsolutionCoord_y = new double[(numbers.size() / 2)-1];
            double[] comp_x = new double[numbers.size() / 2];
            double[] comp_y = new double[numbers.size() / 2];
            int index = 1;
            for(int j = 0; m.find(); j++) {
                double value = Double.parseDouble(m.group());
                if(j % 2 == 0 || j == 0) {
                    index--;
                    TSPsolutionCoord_x[index] = value;
                }
                if(j % 2 == 1) {
                    TSPsolutionCoord_y[index-1] = value;
                }
                index++;
            }
            double start_node_x = TSPsolutionCoord_x[0];
            double start_node_y = TSPsolutionCoord_y[0];
            StringBuilder sb = new StringBuilder();
            index = 0;
            for(int j = 0; m2.find(); j++) {
                double value = Double.parseDouble(m2.group());
                if((j - 1)%3 == 0) {    // x 
                    index--;
                    comp_x[index] = value;
                }
                if((j - 2)%3 == 0) {    // y 
                    index--;
                    comp_y[index] = value;
                }
                index++;
            }
            
            boolean first = true;
            int first_index = 0;
            double last_node_x = Double.MAX_VALUE;
            double last_node_y = Double.MAX_VALUE;
            for(int j = 0; j < max; j++) {
                for(int k = 0; k < max; k++) {
                    if(TSPsolutionCoord_x[j] == comp_x[k] && 
                       TSPsolutionCoord_y[j] == comp_y[k]) {
                        //System.out.println("MATCHED " + TSPsolutionCoord_x[j] + 
                        //        ", " + TSPsolutionCoord_y[j] + 
                        //        "\t" + comp_x[k] + ", " + comp_y[k]);
                        if(first) {
                            sb.append(k+1);
                            first_index = k+1;
                            first = false;
                            tour_length += Sub_algorithms.Euclidean_distance(
                                  start_node_x, start_node_y, 
                                  TSPsolutionCoord_x[j], TSPsolutionCoord_y[j]);
                        }
                        else {
                            sb.append("-").append(k+1);
                            tour_length += Sub_algorithms.Euclidean_distance(
                                  last_node_x, last_node_y, 
                                  TSPsolutionCoord_x[j], TSPsolutionCoord_y[j]);
                        }
                        last_node_x = TSPsolutionCoord_x[j];
                        last_node_y = TSPsolutionCoord_y[j];
                    }
                }
            }
            sb.append("-").append(first_index); // back to the first vertex 
            tour_length += Sub_algorithms.Euclidean_distance(
                    last_node_x, last_node_y, 
                    start_node_x, start_node_y);
            
            solution = sb.toString();
            
            boolean hamiltonian = checkHamiltonian(solution, max);
            if(hamiltonian) {
                long endTime = System.nanoTime();
                System.out.println("Time (millisec): " + (endTime - startTime)/1000000);
                System.out.println("Time (sec, rounded down): " + (endTime - startTime)/1000000000 + "\n");

                return "Doubled Prim MST connections:\n" + connections_string + 
                        "Total " + prim_mst_connections + " connections.\n\nSolution: \n" + 
                        solution + "\nTour length: " + tour_length;
            }
            else {
                return "Bug! The calculated tour is not a Hamiltonian circuit!\n" + solution;
            }
        }
        else {
            return ERRORMSG;
        }
    }
    
    /**
     * The convex hull heuristic. 
     * Forming a "rubber band" around the point set, circumference vertices are chosen. 
     * Only inner vertices remain and need to be connected for the TSP tour. 
     * Then connect those one by one. Tries to connect always the one that is the 
     * closest to the current hull. Even when the convex hull is not set 100% 
     * correctly, this should connect the outer vertices just like the inner ones. 
     * There should not really be any outer vertices, only inner ones. 
     * 
     * @param input String
     * @return String
     */
    public static String ConvexHull_Algorithm(String input) {
        double tour_length = 0.0; 
        String solution = "";
        int min = 1;
        int max = 0;
        String rows[] = new String[500000];    
        ArrayList coordinates = new ArrayList();
        int i = 0;
        String str;
        BufferedReader reader = new BufferedReader(new StringReader(input));
        boolean EUC_2D = false;
        boolean hasSameCoordinates = false;
        boolean rightAmountOfNumbers = false;
        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    rows[i] = str;
                    if(rows[i].charAt(0) == '0' || 
                            rows[i].charAt(0) == '1' ||
                            rows[i].charAt(0) == '2' || 
                            rows[i].charAt(0) == '3' ||
                            rows[i].charAt(0) == '4' || 
                            rows[i].charAt(0) == '5' ||
                            rows[i].charAt(0) == '6' || 
                            rows[i].charAt(0) == '7' ||
                            rows[i].charAt(0) == '8' || 
                            rows[i].charAt(0) == '9') {
                        rows[i] = rows[i].replaceAll(",", ".");
                        max++;
                        try {
                            Double numberInput;
                            int endIndex;
                            for (int beginIndex = 0; beginIndex < 
                                    rows[i].length(); 
                                    beginIndex = endIndex + 1) {
                                endIndex = rows[i].indexOf(" ", beginIndex);
                                if (endIndex == -1) {
                                    endIndex = rows[i].length();
                                }
                                String numberString = rows[i].substring(
                                        beginIndex, endIndex);
                                try {
                                    numberInput = Double.valueOf(numberString);
                                    coordinates.add(numberInput);   
                                    // second x, third y 
                                } 
                                catch (java.lang.
                                        NumberFormatException nfe) {
                                    System.err.println(nfe);
                                }
                            }
                        }
                        catch(Exception e) {
                            System.err.println(e);
                        }
                    }
                    if(rows[i].contains("EUC_2D")) {
                        EUC_2D = true;
                    }
                }
            }
        } 
        catch(IOException e) {
            System.err.println(e);
        }
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        ArrayList coordinates_x2 = new ArrayList(); 
            // 2 = list where the inner vertices remain 
        ArrayList coordinates_y2 = new ArrayList();
        for(int j = 1; j < coordinates.size(); j++) {
            if(j%3 == 0) {
                rightAmountOfNumbers = false;
            }
            if((j - 1)%3 == 0) {
                coordinates_x.add(coordinates.get(j));
                coordinates_x2.add(coordinates.get(j));
                rightAmountOfNumbers = false;
            }
            if((j - 2)%3 == 0) {
                coordinates_y.add(coordinates.get(j));
                coordinates_y2.add(coordinates.get(j));
                rightAmountOfNumbers = true;
            }
        }
        for(int j = 0; j < coordinates_x.size()-1; j++) {
            for(int k = j + 1; k < coordinates_x.size(); k++) {
                if(coordinates_x.get(j).equals(coordinates_x.get(k)) &&
                   coordinates_y.get(j).equals(coordinates_y.get(k))) {
                    hasSameCoordinates = true;
                    System.out.println("equal coordinates" + 
                            coordinates_x.get(j) + ", " + 
                            coordinates_y.get(j) + " and " + 
                            coordinates_x.get(k) + ", " + 
                            coordinates_y.get(k));
                }
            }
        }
        // The CHH input is read, now the algorithm starts if the input is ok.
        
        String inputresult = checkCoordinateInput(coordinates);
        if(inputresult.equals("ok") && max >=4 && EUC_2D && 
                !hasSameCoordinates && rightAmountOfNumbers) {
            System.gc(); // run garbage collector before starting 
            //String[] args = new String[1]; 
            //args[0] = "$ java -javaagent:tracker.jar TSP_Solver_UEF_241908 500 > /dev/null";
            //ResourceTracker.premain(args[0]);
            // ^ Comment if ResourceTracker.java is not used 
            long startTime = System.nanoTime();
            
            String hull = Sub_algorithms.ConvexHull(min, max, 
                    coordinates_x, coordinates_y);
            String replace1 = hull.replace("]], [[", "]],\n[[");
            String[] lines = replace1.split("\r\n|\r|\n");
            String replace2 = replace1.replace("[[[", "\tFrom (");
            String replace3 = replace2.replace("[[", "\tFrom (");
            String replace4 = replace3.replace("], [", ")    to    (");
            String replace5 = replace4.replace("]]]", ")");
            String replace6 = replace5.replace("]]", ")");
            String replace7 = replace6.replace("),", ")");
            String hullResult = "Convex hull connections: \n" + replace7 + 
                    "\nTotal " + lines.length + " connections.\n"
                    + "Connecting the closest vertices to the hull one by one.\n";
            String replace8 = replace7.replace(",", ".");
            String replace9 = replace8.replace("(", "");
            String replace10 = replace9.replace(")", "");
            String replace11 = replace10.replace("\tFrom ", "");
            String replace12 = replace11.replace("    to    ", " ");
            String replace13 = replace12.replace(". ", " ");

            double[][][] edges = new double[max][2][2];
            for(int j = 0; j < max; j++) {
                edges[j][0][0] = Double.MAX_VALUE;
                edges[j][0][1] = Double.MAX_VALUE;
                edges[j][1][0] = Double.MAX_VALUE;
                edges[j][1][1] = Double.MAX_VALUE;
            }
            String rows2[] = new String[500000];    
            ArrayList values = new ArrayList();
            int a = 0;
            String str2;
            BufferedReader reader2 = new BufferedReader(new StringReader(replace13));
            try {
                while ((str2 = reader2.readLine()) != null) {
                    if (str2.length() > 0) {
                        rows2[a] = str2; 
                        if(rows2[a].charAt(0) == '0' || 
                                rows2[a].charAt(0) == '1' ||
                                rows2[a].charAt(0) == '2' || 
                                rows2[a].charAt(0) == '3' ||
                                rows2[a].charAt(0) == '4' || 
                                rows2[a].charAt(0) == '5' ||
                                rows2[a].charAt(0) == '6' || 
                                rows2[a].charAt(0) == '7' ||
                                rows2[a].charAt(0) == '8' || 
                                rows2[a].charAt(0) == '9' ||
                                rows2[a].charAt(0) == '-') {
                            // max will not be increased 
                            try {
                                Double numberInput;
                                int endIndex;
                                for (int beginIndex = 0; beginIndex < 
                                        rows2[a].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows2[a].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows2[a].length();
                                    }
                                    String numberString = rows2[a].substring(
                                            beginIndex, endIndex);
                                    try {
                                        numberInput = Double.valueOf(
                                                numberString);
                                        values.add(numberInput);
                                    } 
                                    catch (java.lang.NumberFormatException nfe) {
                                        System.err.println(nfe);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        } 
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            
            int temp_index = 0;
            for(int j = 0; j < values.size(); j++) {
                if(j == 0 || j%4 == 0) {
                    edges[temp_index][0][0] = (double)values.get(j);
                }
                if((j + 1)%4 == 0) {
                    edges[temp_index][1][1] = (double)values.get(j);
                    temp_index++;
                }
                if((j + 2)%4 == 0) {
                    edges[temp_index][0][1] = (double)values.get(j);
                }
                if((j + 3)%4 == 0) {
                    edges[temp_index][1][0] = (double)values.get(j);
                }
            }
            boolean[] circumferenceVertices = new boolean[max]; 

            int match_count = 0;
            for(int j = 0; j < max; j++) {
                for(int k = 0; k < max; k++) {
                    if(coordinates_x2.get(j).equals(edges[k][0][0]) && 
                       coordinates_y2.get(j).equals(edges[k][1][0]) && 
                       edges[k][0][0] < Double.MAX_VALUE && 
                       edges[k][1][0] < Double.MAX_VALUE) {
                        match_count++;
                        circumferenceVertices[j] = true;
                        coordinates_x2.set(j, Double.MAX_VALUE);
                        coordinates_y2.set(j, Double.MAX_VALUE);
                    }
                }
            }   
            int inner_v_rounds = 0;
            if(match_count < max) {
                inner_v_rounds = max - match_count;
            }
            //boolean more_than_1_inner = false;
            //if(inner_v_rounds > 1) {
                //more_than_1_inner = true;
            //}
            //boolean circumferenceVertices[] currently knows what are in hull
            
            //boolean only_1_selected_when_several_equal_min_dist = false;
            long amount_of_inner_vert_total = 0;
            long amount_of_cir_calcs_in_inn_calcs = 0;
            long amount_of_triangle_calc_total = 0;
            
            for(int z = 0; z < inner_v_rounds; z++) {
                /* choosing the next vertice: for each edge in edges[][][], 
                 define a line, and from each line, compare distances with each 
                 inner vertex (circumferenceVertices[] has false in the index) 
                 choose the minimum having the distance so that it has to have 
                 both a counter-clockwise AND a clockwise turn with the 
                 edge's vertices AND to the top of that, the distance with the 
                 edge has to be less than the distance between any circumference 
                 vertice. (The vertex to be chosen by line comparation
                 is wanted in between the two vertices of the edge.
                 In other words, compare with "x-closest-1" and "x-closest-2".)
                 If not possible, choose the closest inner vertice - 
                 circumference vertice pair.*/

                double next_vertice_x;   // the next node to process 
                double next_vertice_y;   // the next node to process 
                double closestCircEdgesVert1_x;
                double closestCircEdgesVert1_y;
                double closestCircEdgesVert2_x;
                double closestCircEdgesVert2_y;
                double closestVertOfLine1_2_x;   // the orthogonal point
                double closestVertOfLine1_2_y;   // the orthogonal point
                double lineGeneral_Ax_term; //  y1 - y2 
                double lineGeneral_By_term; //  x2 - x1 
                double lineGeneral_C_term;  // (x1-x2)*y1+(y2-y1)*x1
                double min_distance = Double.MAX_VALUE; 
                int next_v_index_k = -1;
                
                for(int j = 0; j < max; j++) {  // finds min distance between 
                                                // any inner vertex - circumference vertex pair 
                    if(edges[j][0][0] < Double.MAX_VALUE && 
                       edges[j][1][0] < Double.MAX_VALUE &&
                       edges[j][0][1] < Double.MAX_VALUE && 
                       edges[j][1][1] < Double.MAX_VALUE) {
                        for(int k = 0; k < max; k++) {
                            if((double)coordinates_x2.get(k) < Double.MAX_VALUE &&
                                    (double)coordinates_y2.get(k) < Double.MAX_VALUE) {
                                next_vertice_x = (double)coordinates_x2.get(k);
                                next_vertice_y = (double)coordinates_y2.get(k);
                                if(Sub_algorithms.Euclidean_distance(
                                        edges[j][0][0], edges[j][1][0], 
                                        next_vertice_x, next_vertice_y) < min_distance) {
                                    min_distance = Sub_algorithms.
                                        Euclidean_distance(
                                               edges[j][0][0], edges[j][1][0], 
                                               next_vertice_x, next_vertice_y);
                                    next_v_index_k = k;
                                }
                            }
                        }
                    }
                }
                
                int amount_of_inner_vert = 0;
                for(int j = 0; j < circumferenceVertices.length; j++) {
                    if(!circumferenceVertices[j]) {
                        amount_of_inner_vert++;
                        amount_of_inner_vert_total++;
                    }
                }
                double innerVertices[][] = new double[amount_of_inner_vert][2];
                int index = 0;
                for(int j = 0; j < circumferenceVertices.length; j++) {
                    if(!circumferenceVertices[j]) {
                        innerVertices[index][0] = (double)coordinates_x2.get(j);
                        innerVertices[index][1] = (double)coordinates_y2.get(j);
                        index++;
                    }
                }
                double temp_X = 0.0;
                double temp_Y = 0.0;
                boolean orth_found = false;
                int amount_of_circ_vert = circumferenceVertices.length - 
                            amount_of_inner_vert;
                    //System.out.println("All: " + circumferenceVertices2.length + 
                    //        ", Cir: " + amount_of_circ_vert + 
                    //        ", Inn: " + amount_of_inner_vert);
                for(int h = 0; h < amount_of_inner_vert; h++) {                 
                    double next_vertice_x2 = innerVertices[h][0]; // x0
                    double next_vertice_y2 = innerVertices[h][1]; // y0
                    //System.out.println("Inner vertice candidate that is handled now " + temp + ": " + 
                    //        next_vertice_x + ", " + next_vertice_y);
                    for(int j = 0; j < amount_of_circ_vert; j++) {
                        amount_of_cir_calcs_in_inn_calcs++;
                        closestCircEdgesVert1_x = edges[j][0][0];
                        closestCircEdgesVert1_y = edges[j][1][0];
                        closestCircEdgesVert2_x = edges[j][0][1];
                        closestCircEdgesVert2_y = edges[j][1][1];
                        
                        //System.out.println("Now the edge from (" + closestCircEdgesVert1_x + 
                        //        ", " + closestCircEdgesVert1_y + ") to (" + closestCircEdgesVert2_x + 
                        //        ", " + closestCircEdgesVert2_y + ")");
                        lineGeneral_Ax_term = closestCircEdgesVert1_y - 
                                                     closestCircEdgesVert2_y; // y1 - y2 
                        lineGeneral_By_term = closestCircEdgesVert2_x - 
                                                     closestCircEdgesVert1_x; // x2 - x1 
                        lineGeneral_C_term = ((closestCircEdgesVert1_x - 
                                closestCircEdgesVert2_x) * closestCircEdgesVert1_y) + 
                                ((closestCircEdgesVert2_y - 
                                closestCircEdgesVert1_y) * 
                                closestCircEdgesVert1_x); //(x1-x2)*y1+(y2-y1)*x1
                        double temp_distance = Math.abs(lineGeneral_Ax_term * 
                                next_vertice_x2 + lineGeneral_By_term * next_vertice_y2 + 
                                lineGeneral_C_term) / Math.sqrt(lineGeneral_Ax_term * 
                                lineGeneral_Ax_term + lineGeneral_By_term * 
                                lineGeneral_By_term);
                        // Math.abs(|a*x0 + b*y0 + c |) / Math.sqrt(a*a + b*b);
                        
                        // calculating the intersection point of "1to2" and "orthogonal"-next_v:
                        // wanted: closestVertOfLine1_2_x and closestVertOfLine1_2_y 

                        // http://stackoverflow.com/questions/1811549/perpendicular-on-a-line-from-a-given-point 
                        //(8.12.2016) 
                        /* double k = ((y2-y1) * (x3-x1) - (x2-x1) * (y3-y1)) / 
                                ((y2-y1)*(y2-y1) + (x2-x1)*(x2-x1));
                        x4 = x3 - k * (y2-y1)
                        y4 = y3 + k * (x2-x1) */
                        double sl = ((closestCircEdgesVert2_y-closestCircEdgesVert1_y) * 
                                (next_vertice_x2-closestCircEdgesVert1_x) - 
                                (closestCircEdgesVert2_x-closestCircEdgesVert1_x) * 
                                (next_vertice_y2-closestCircEdgesVert1_y)) / 
                                ((closestCircEdgesVert2_y-closestCircEdgesVert1_y)* 
                                (closestCircEdgesVert2_y-closestCircEdgesVert1_y) + 
                                (closestCircEdgesVert2_x-closestCircEdgesVert1_x)* 
                                (closestCircEdgesVert2_x-closestCircEdgesVert1_x));
                        closestVertOfLine1_2_x = next_vertice_x2 - sl * 
                                (closestCircEdgesVert2_y-closestCircEdgesVert1_y);
                        closestVertOfLine1_2_y = next_vertice_y2 + sl * 
                                (closestCircEdgesVert2_x-closestCircEdgesVert1_x);
                        //System.out.println("ORTH: (" + closestVertOfLine1_2_x + 
                        //", " + closestVertOfLine1_2_y + ")");
                        if(temp_distance < min_distance) {
                            if((Sub_algorithms.counterClockwiseTurn(
                                    next_vertice_x2, next_vertice_y2, 
                                    closestVertOfLine1_2_x, closestVertOfLine1_2_y, 
                                    closestCircEdgesVert1_x, closestCircEdgesVert1_y) >= 0) ^ (//xor 
                                    Sub_algorithms.counterClockwiseTurn(
                                    next_vertice_x2, next_vertice_y2, 
                                    closestVertOfLine1_2_x, closestVertOfLine1_2_y, 
                                    closestCircEdgesVert2_x, closestCircEdgesVert2_y) > 0)){
                                    // must be both cw and ccw turn, replace 0,0's 
                                if(temp_distance > 0.0) {
                                // vertices on the hull line will be ignored
                                    min_distance = temp_distance;
                                    temp_X = next_vertice_x2;
                                    temp_Y = next_vertice_y2;
                                    orth_found = true;
                                    /*System.out.println("Min distance (orthogonal): " + min_distance + 
                                        " between (" + next_vertice_x2 + ", " + next_vertice_y2 + ") and line from (" + 
                                        closestCircEdgesVert1_x + ", " + closestCircEdgesVert1_y + 
                                        ") to (" + closestCircEdgesVert2_x + ", " + closestCircEdgesVert2_y + ")");*/
                                }
                                if(temp_distance == 0.0) {
                                    //+ has to be in between the 2 points:
                                    if (Sub_algorithms.Euclidean_distance_squared(
                                            closestCircEdgesVert2_x, closestCircEdgesVert2_y, 
                                            next_vertice_x2, next_vertice_y2) + 
                                        Sub_algorithms.Euclidean_distance_squared(
                                                closestCircEdgesVert1_x, closestCircEdgesVert1_y, 
                                                next_vertice_x2, next_vertice_y2) == 
                                        Sub_algorithms.Euclidean_distance_squared(
                                                closestCircEdgesVert2_x, closestCircEdgesVert2_y, 
                                                closestCircEdgesVert1_x, closestCircEdgesVert1_y)){
                                        min_distance = temp_distance;
                                        temp_X = next_vertice_x2;
                                        temp_Y = next_vertice_y2;
                                        orth_found = true;
                                        /*System.out.println("Min distance (orthogonal): " + min_distance + 
                                            " between (" + next_vertice_x2 + ", " + next_vertice_y2 + ") and line from (" + 
                                            closestCircEdgesVert1_x + ", " + closestCircEdgesVert1_y + 
                                            ") to (" + closestCircEdgesVert2_x + ", " + closestCircEdgesVert2_y + ")");*/
                                    }
                                    //else {
                                    //    System.out.println("Distance was 0 but the vertice was not in between the line");
                                    //}
                                }
                            }
                            //else {
                                //System.out.println("Orthogonal point outside the circ.");
                            //}
                        }
                        //if(temp_distance == min_distance) {
                        //    only_1_selected_when_several_equal_min_dist = true;
                        //}
                    }
                }
                
                if(orth_found) {
                    next_vertice_x = temp_X;
                    next_vertice_y = temp_Y;
                }
                else {
                    next_vertice_x = (double)coordinates_x2.get(next_v_index_k);
                    next_vertice_y = (double)coordinates_y2.get(next_v_index_k);
                }
                //System.out.println("The next vertice is now decided: (" + 
                //        next_vertice_x + ", " + next_vertice_y + ")");
                //if(next_vertice_x == Double.MAX_VALUE || 
                //   next_vertice_y == Double.MAX_VALUE) {
                //    System.err.println("Next vertice not chosen");
                //} 
                for(int j = 0; j < max; j++) {
                    if(coordinates_x2.get(j).equals(next_vertice_x) && 
                       coordinates_y2.get(j).equals(next_vertice_y) ) {
                        circumferenceVertices[j] = true;
                        coordinates_x2.set(j, Double.MAX_VALUE);
                        coordinates_y2.set(j, Double.MAX_VALUE);
                        //System.out.println("MATCH: " + coordinates_x2.get(j) + 
                        //        ", " + coordinates_y2.get(j) + 
                        //        "\t " + j + " was marked true in the list");
                    }
                }
                
                // slope (y2 - y1) / (x2 - x1) where x2 - x1 != 0 
                // point's (x0, y0) distance from a line (a*x+b*y+c) 
                // is abs(|a*x0 + b*y0 + c |) / sqrt(a*a + b*b) 

                // The next vertice is now decided.
                // Next calculate (unrealistic squared) distances to the vertice
                // from 2 vertices and the edge "a" of "c+b-a" is in edges[][][], 
                // between these, discard the "a" of the the minimum "c+b-a" set. 
                // "c+b-a" is the cost calculation for a triangle 
                double min_weight_triangle = Double.MAX_VALUE; 
                double c1_x;
                double c1_y;    // circumference vertice 1
                double c2_x;
                double c2_y;    // circumference vertice 2
                double c1_x_to_take = Double.MAX_VALUE;
                double c1_y_to_take = Double.MAX_VALUE;
                double c2_x_to_take = Double.MAX_VALUE;
                double c2_y_to_take = Double.MAX_VALUE;
                double C;
                double B;       // edge lengths
                double A;
                
                for(int j = 0; j < amount_of_circ_vert; j++) {
                    amount_of_triangle_calc_total++;
                    c1_x = edges[j][0][0]; //index, boolean isY, boolean isEnd
                    c1_y = edges[j][1][0];
                    c2_x = edges[j][0][1];
                    c2_y = edges[j][1][1];
                    C = Sub_algorithms.Euclidean_distance_squared(
                        next_vertice_x, next_vertice_y, 
                        c1_x, c1_y);
                    B = Sub_algorithms.Euclidean_distance_squared(
                        next_vertice_x, next_vertice_y, 
                        c2_x, c2_y);
                    A = Sub_algorithms.Euclidean_distance_squared(
                        c1_x, c1_y, c2_x, c2_y);
                    double triangle_weight = C + B - A;
                    if(triangle_weight < min_weight_triangle) {
                        min_weight_triangle = triangle_weight;
                        c1_x_to_take = c1_x;
                        c1_y_to_take = c1_y;
                        c2_x_to_take = c2_x;
                        c2_y_to_take = c2_y;
                    }
                }
                // new vertice (next_v_index) will be marked as a 
                // new circumference vertice,
                // two edges will be added to edges[][][] as one is left out
                
                /*find a line in edges that has both c1 and c2, delete it,
                move the rest of the list forward by 1 step each, 
                if c1 was first, add    c1_to_take -> next_vertice  to index
                                        next_vertice -> c2_to_take  to index+1
                if c2 was first, add    c2_to_take -> next_vertice  to index
                                        next_vertice -> c1_to_take  to index+1 
                mark circumferenceVertices[j] = true */
                
                boolean c1_was_first = false;
                for(int j = 0; j < max -1; j++) {
                    if(edges[j][0][0] == c1_x_to_take && 
                       edges[j][1][0] == c1_y_to_take && 
                       edges[j][0][1] == c2_x_to_take && 
                       edges[j][1][1] == c2_y_to_take) {    // c1 is the first 
                        c1_was_first = true;
                        edges[j][0][0] = Double.MAX_VALUE;
                        edges[j][0][1] = Double.MAX_VALUE;
                        edges[j][1][0] = Double.MAX_VALUE;
                        edges[j][1][1] = Double.MAX_VALUE;  // edge (line) A deleted 
                        for(int k = max -2; k >= j; k--) {
                            double temp_1 = edges[k+1][0][0];
                            double temp_2 = edges[k+1][1][0];
                            double temp_3 = edges[k+1][0][1];
                            double temp_4 = edges[k+1][1][1];
                            edges[k+1][0][0] = edges[k][0][0];
                            edges[k+1][1][0] = edges[k][1][0];
                            edges[k+1][0][1] = edges[k][0][1];
                            edges[k+1][1][1] = edges[k][1][1];
                            edges[k][0][0] = temp_1;
                            edges[k][1][0] = temp_2;
                            edges[k][0][1] = temp_3;
                            edges[k][1][1] = temp_4;
                        }
                    }
                    else {
                        if(edges[j][0][0] == c2_x_to_take && 
                           edges[j][1][0] == c2_y_to_take && 
                           edges[j][0][1] == c1_x_to_take && 
                           edges[j][1][1] == c1_y_to_take) {    // c2 is the first 
                            edges[j][0][0] = Double.MAX_VALUE;
                            edges[j][0][1] = Double.MAX_VALUE;
                            edges[j][1][0] = Double.MAX_VALUE;
                            edges[j][1][1] = Double.MAX_VALUE;  // edge A deleted 
                            for(int k = max -2; k >= j; k--) {
                                double temp_1 = edges[k+1][0][0];
                                double temp_2 = edges[k+1][1][0];
                                double temp_3 = edges[k+1][0][1];
                                double temp_4 = edges[k+1][1][1];
                                edges[k+1][0][0] = edges[k][0][0];
                                edges[k+1][1][0] = edges[k][1][0];
                                edges[k+1][0][1] = edges[k][0][1];
                                edges[k+1][1][1] = edges[k][1][1];
                                edges[k][0][0] = temp_1;
                                edges[k][1][0] = temp_2;
                                edges[k][0][1] = temp_3;
                                edges[k][1][1] = temp_4;
                            }
                            
                        }
                    }
                }
                for(int j = 0; j < max -1; j++) {
                    if(c1_was_first) {
                        if(edges[j][0][0] >= Double.MAX_VALUE) {
                            edges[j][0][0] = c1_x_to_take;
                            edges[j][1][0] = c1_y_to_take;
                            edges[j][0][1] = next_vertice_x;
                            edges[j][1][1] = next_vertice_y;
                            edges[j+1][0][0] = next_vertice_x;
                            edges[j+1][1][0] = next_vertice_y;
                            edges[j+1][0][1] = c2_x_to_take;
                            edges[j+1][1][1] = c2_y_to_take;
                            j = max;
                        }
                    }
                    else {
                        if(edges[j][0][0] >= Double.MAX_VALUE) {
                            edges[j][0][0] = c2_x_to_take;
                            edges[j][1][0] = c2_y_to_take;
                            edges[j][0][1] = next_vertice_x;
                            edges[j][1][1] = next_vertice_y;
                            edges[j+1][0][0] = next_vertice_x;
                            edges[j+1][1][0] = next_vertice_y;
                            edges[j+1][0][1] = c1_x_to_take;
                            edges[j+1][1][1] = c1_y_to_take;
                            j = max;
                        }
                    }
                }
            }
            
            // finally, make the solution string and calculate the tour length:
            String rows3[] = new String[500000];    
            ArrayList coordinates3 = new ArrayList();
            i = 0;
            String str3;
            BufferedReader reader3 = new BufferedReader(new StringReader(input));
            try {
                while ((str3 = reader3.readLine()) != null) {
                    if (str3.length() > 0) {
                        rows3[i] = str3;
                        if(rows3[i].charAt(0) == '0' || 
                                rows3[i].charAt(0) == '1' ||
                                rows3[i].charAt(0) == '2' ||
                                rows3[i].charAt(0) == '3' ||
                                rows3[i].charAt(0) == '4' ||
                                rows3[i].charAt(0) == '5' ||
                                rows3[i].charAt(0) == '6' ||
                                rows3[i].charAt(0) == '7' ||
                                rows3[i].charAt(0) == '8' ||
                                rows3[i].charAt(0) == '9') {
                            try {
                                double numberInput;
                                int endIndex;
                                for (int beginIndex = 0; beginIndex <
                                        rows3[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows3[i].indexOf(" ", 
                                            beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows3[i].length();
                                    }
                                    String numberString = rows3[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        numberInput = Double.
                                                parseDouble(numberString);
                                        coordinates3.add(numberInput);
                                    } 
                                    catch (java.lang.
                                            NumberFormatException nfe) {
                                        System.err.println(nfe);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }   
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            // coordinates3 has now the original coordinates,
            // when they match with the tsp result in edges, add to solution,
            // at the same time += tour length
            
            ArrayList coordinates_x3 = new ArrayList();
            ArrayList coordinates_y3 = new ArrayList();
            for(int j = 0; j < coordinates3.size(); j++) {
                if(j%3 == 0 || j == 0) {    // do nothing, intentionally 
                }
                if((j - 1)%3 == 0) {
                    coordinates_x3.add(coordinates3.get(j));
                }
                if((j - 2)%3 == 0) {
                    coordinates_y3.add(coordinates3.get(j));
                }
            }
            match_count = 0;
            int first_index = 0;
            double startX = Double.MAX_VALUE;
            double startY = Double.MAX_VALUE;
            double lastX = Double.MAX_VALUE;
            double lastY = Double.MAX_VALUE;
            for(int j = 0; j < max; j++) {
                for(int k = 0; k < max; k++) {
                    if(coordinates_x3.get(k).equals(edges[j][0][0]) && 
                       coordinates_y3.get(k).equals(edges[j][1][0]) && 
                       edges[j][0][0] < Double.MAX_VALUE && 
                       edges[j][1][0] < Double.MAX_VALUE) {
                        if(match_count > 0) {
                            solution = new StringBuilder(solution).append("-").toString();
                            tour_length += Sub_algorithms.Euclidean_distance(
                                    edges[j][0][0], edges[j][1][0], 
                                    lastX, lastY);
                        }
                        else {
                            first_index = k+1;
                            startX = edges[j][0][0];
                            startY = edges[j][1][0];
                        }
                        lastX = edges[j][0][0];
                        lastY = edges[j][1][0];
                        match_count++;
                        solution = new StringBuilder(solution).append(k+1).toString();
                    }
                }
            }
            // connection back to the last vertex 
            // and the last addition to the tour length 
            solution = new StringBuilder(solution).append("-").toString();
            solution = new StringBuilder(solution).append(first_index).toString();
            tour_length += Sub_algorithms.Euclidean_distance(
                    lastX, lastY, startX, startY);
            
            boolean hamiltonian = checkHamiltonian(solution, max);
            if(hamiltonian) {
                long endTime = System.nanoTime();
                System.out.println("Time (millisec): " + (endTime - startTime)/1000000);
                System.out.println("Time (sec, rounded down): " + (endTime - startTime)/1000000000);
                long vspt = max * max * inner_v_rounds;
                System.out.println("Inner vertice rounds: " + inner_v_rounds);
                System.out.println("Vertices squared part total: " + vspt);
                System.out.println("Inner vertice handles total: " + amount_of_inner_vert_total);
                System.out.println("Handles in the for loop where cir in inn: " + 
                        amount_of_cir_calcs_in_inn_calcs);
                System.out.println("Triangle calculations total: " + 
                        amount_of_triangle_calc_total + "\n");

                    return hullResult + "\nSolution: \n" + solution + 
                        "\nTour length: " + tour_length;
            }
            else {
                return "Bug! The calculated tour is not a Hamiltonian circuit!\n" + solution;
            }
        }
        else {
            return ERRORMSG;
        }
    }
    
    /**
     * The Christofides Heuristic (CHRI).
     * An improvement of 2-MST, still uses Prim MST. Ant colony matching part included 
     * using NNH sprouts but it might not find a matching. If stuck, just choose some matching. 
     * Mathematical proof of "at most 1.5 times the optimal tour" exists. 
     * If the matching is 100%:tly optimized, only then 1.5 is guaranteed. 
     * This implementation right here does not guarantee that 1.5 times the 
     * optimum solution for very large inputs, sorry. 
     * 
     * Christofides is also in "Sub_algorithms" class for sprout usages, 
     * one of these might be deleted later.
     * 
     * @param input String
     * @return String
     */
    public static String Christofides_Algorithm(String input) {
        double tour_length = 0.0;
        String solution;
        int min = 1;
        int max = 0;
        String rows[] = new String[500000];    
        ArrayList coordinates = new ArrayList();
        int i = 0;
        String str;
        BufferedReader reader = new BufferedReader(new StringReader(input));
        boolean EUC_2D = false;
        boolean hasSameCoordinates = false;
        boolean rightAmountOfNumbers = false;
        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    rows[i] = str;
                    if(rows[i].charAt(0) == '0' || 
                            rows[i].charAt(0) == '1' ||
                            rows[i].charAt(0) == '2' || 
                            rows[i].charAt(0) == '3' ||
                            rows[i].charAt(0) == '4' || 
                            rows[i].charAt(0) == '5' ||
                            rows[i].charAt(0) == '6' || 
                            rows[i].charAt(0) == '7' ||
                            rows[i].charAt(0) == '8' || 
                            rows[i].charAt(0) == '9') {
                        rows[i] = rows[i].replaceAll(",", ".");
                        max++;
                        try {
                            Double numberInput;
                            int endIndex;
                            for (int beginIndex = 0; beginIndex < 
                                    rows[i].length(); 
                                    beginIndex = endIndex + 1) {
                                endIndex = rows[i].indexOf(" ", beginIndex);
                                if (endIndex == -1) {
                                    endIndex = rows[i].length();
                                }
                                String numberString = rows[i].substring(
                                        beginIndex, endIndex);
                                try {
                                    numberInput = Double.valueOf(numberString);
                                    coordinates.add(numberInput);   
                                    // second x, third y 
                                } 
                                catch (java.lang.NumberFormatException nfe) {
                                    System.err.println(nfe);
                                }
                            }
                        }
                        catch(Exception e) {
                            System.err.println(e);
                        }
                    }
                    if(rows[i].contains("EUC_2D")) {
                        EUC_2D = true;
                    }
                }
            }
        } 
        catch(IOException e) {
            System.err.println(e);
        }
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        for(int j = 1; j < coordinates.size(); j++) {
            if(j%3 == 0) {
                rightAmountOfNumbers = false;
            }
            if((j - 1)%3 == 0) {
                coordinates_x.add(coordinates.get(j));
                rightAmountOfNumbers = false;
            }
            if((j - 2)%3 == 0) {
                coordinates_y.add(coordinates.get(j));
                rightAmountOfNumbers = true;
            }
        }
        for(int j = 0; j < coordinates_x.size()-1; j++) {
            for(int k = j + 1; k < coordinates_x.size(); k++) {
                if(coordinates_x.get(j).equals(coordinates_x.get(k)) &&
                   coordinates_y.get(j).equals(coordinates_y.get(k))) {
                    hasSameCoordinates = true;
                    System.out.println("equal coordinates" + 
                            coordinates_x.get(j) + ", " + 
                            coordinates_y.get(j) + " and " + 
                            coordinates_x.get(k) + ", " + 
                            coordinates_y.get(k));
                }
            }
        }
        // The Christofides input is read, now the algorithm starts if the input is ok.
        
        String inputresult = checkCoordinateInput(coordinates);
        if(inputresult.equals("ok") && max >=4 && EUC_2D && 
                !hasSameCoordinates && rightAmountOfNumbers) {
            System.gc(); // run garbage collector before starting 
            
            //if(max > 30) {
            //    solution = " The Christofides heuristic is mainly used for neuron logic sprouts"
            //            + " and in \n  this application, it only works with small inputs. Please have max 30 vertices!"
            //            + "\n The algorithm run did not start.";
            //    return solution;
            //}
            //else {
                //String[] args = new String[1]; 
                //args[0] = "$ java -javaagent:tracker.jar TSP_Solver_UEF_241908 500 > /dev/null";
                //ResourceTracker.premain(args[0]);
                // ^ Comment if ResourceTracker.java is not used 
                long startTime = System.nanoTime();

                // next: call the MST Prim 

                double edges[][][] = Sub_algorithms.MST_Prim(min, max, coordinates_x, 
                        coordinates_y); // point, isY?, isEnd? 

                // finding out what are the odd degree edges (their amount should be even):
                // (the 3D table is not the best way to store edges)
                ArrayList coordinates_x_match = new ArrayList();
                ArrayList coordinates_y_match = new ArrayList();

                for(int j = 0 ; j < max-1 ; j++){
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
                
                // now, if "false" exists somewhere, that should be an even degree node 2468... 
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

                //System.out.println("\nTHIS MANY MST ODD DEGREE NODES: " + coordinates_x_match.size() + 
                //        "\nAND TOTAL AMOUNT OF NODES IS " + max + "\n");

                // add the matching edges: 
                ArrayList matchedConnections = Sub_algorithms.Matching(coordinates_x_match, coordinates_y_match); 

                //System.out.println("matchedConnections.size() is " + matchedConnections.size());
                System.out.println("Matching done");
                // start end start end start end
                // x,y,x,y,  x,y,x,y,  x,y,x,y...

                double[][][] mst_with_odd_matched_edges = new double[(max + matchedConnections.size())-1][2][2];
                for(int j = 0; j < max; j++) {
                    mst_with_odd_matched_edges[j][0][0] = edges[j][0][0];
                    mst_with_odd_matched_edges[j][1][0] = edges[j][1][0];
                    mst_with_odd_matched_edges[j][0][1] = edges[j][0][1];
                    mst_with_odd_matched_edges[j][1][1] = edges[j][1][1];
                }

                int insertIndex = max-1;
                for(int j = 0; j < matchedConnections.size(); j+=4) {
                    mst_with_odd_matched_edges[insertIndex][0][0] = (double)matchedConnections.get(j);
                    mst_with_odd_matched_edges[insertIndex][1][0] = (double)matchedConnections.get(j+1);
                    mst_with_odd_matched_edges[insertIndex][0][1] = (double)matchedConnections.get(j+2);
                    mst_with_odd_matched_edges[insertIndex][1][1] = (double)matchedConnections.get(j+3);
                    insertIndex++;
                }

                // ^improvement idea: check if some edges should be turned around?
                
                /** // ordering this way only seems to make the Christofides algorithm more inaccurate:
                //order the edges a bit so that there exist no huge distances when read from begin to end:
                for(int j = 0; j < (max + matchedConnections.size())-1; j++) {
                    for(int k = j; k < (max + matchedConnections.size())-1; k++) {
                        if((mst_with_odd_matched_edges[k][0][0] > mst_with_odd_matched_edges[j][0][0]) && 
                           (mst_with_odd_matched_edges[k][1][0] > mst_with_odd_matched_edges[j][1][0])) {
                            //swap 
                            double helpVar1 = mst_with_odd_matched_edges[j][0][0];
                            double helpVar2 = mst_with_odd_matched_edges[j][1][0];
                            double helpVar3 = mst_with_odd_matched_edges[j][0][1];
                            double helpVar4 = mst_with_odd_matched_edges[j][1][1];

                            mst_with_odd_matched_edges[j][0][0] = mst_with_odd_matched_edges[k][0][0];
                            mst_with_odd_matched_edges[j][1][0] = mst_with_odd_matched_edges[k][1][0];
                            mst_with_odd_matched_edges[j][0][1] = mst_with_odd_matched_edges[k][0][1];
                            mst_with_odd_matched_edges[j][1][1] = mst_with_odd_matched_edges[k][1][1];

                            mst_with_odd_matched_edges[k][0][0] = helpVar1;
                            mst_with_odd_matched_edges[k][1][0] = helpVar2;
                            mst_with_odd_matched_edges[k][0][1] = helpVar3;
                            mst_with_odd_matched_edges[k][1][1] = helpVar4;
                        }
                    }
                }
                **/
                
                String connections_string = "";
                int connections = 0;
                // the next for loop helps to recognize 
                // what exactly are the current connections: 
                for(int j = 0; j < (max + (matchedConnections.size()/4))-1; j++) {
                            String X1 = String.valueOf(mst_with_odd_matched_edges[j][0][0]); 
                            String Y1 = String.valueOf(mst_with_odd_matched_edges[j][1][0]); 
                            String X2 = String.valueOf(mst_with_odd_matched_edges[j][0][1]); 
                            String Y2 = String.valueOf(mst_with_odd_matched_edges[j][1][1]); 
                            connections_string = connections_string.concat( 
                                    "\tFrom (" + X1 + ", " + Y1 + 
                                    ")    to    (" + X2 + ", " + Y2 + ")\n");
                            connections++;
                }

                System.out.println(connections_string);

                // finally, make the Euler tour and the shortcuts: 
                String [] result = Sub_algorithms.Euler_and_embedded_tour(mst_with_odd_matched_edges, 
                        max, connections+1);
                // Hamiltonian tour and tour_length: 
                ArrayList numbers = new ArrayList();
                BufferedReader reader2 = new BufferedReader(new StringReader(input));
                try {
                    while ((str = reader2.readLine()) != null) {
                        if (str.length() > 0) {
                            rows[i] = str;
                            if(rows[i].charAt(0) == '0' || 
                                    rows[i].charAt(0) == '1' ||
                                    rows[i].charAt(0) == '2' ||
                                    rows[i].charAt(0) == '3' ||
                                    rows[i].charAt(0) == '4' ||
                                    rows[i].charAt(0) == '5' ||
                                    rows[i].charAt(0) == '6' ||
                                    rows[i].charAt(0) == '7' ||
                                    rows[i].charAt(0) == '8' ||
                                    rows[i].charAt(0) == '9') {
                                try {
                                    double numberInput;
                                    int endIndex;
                                    for (int beginIndex = 0; beginIndex <
                                            rows[i].length(); 
                                            beginIndex = endIndex + 1) {
                                        endIndex = rows[i].indexOf(" ", 
                                                beginIndex);
                                        if (endIndex == -1) {
                                            endIndex = rows[i].length();
                                        }
                                        String numberString = rows[i].substring(
                                                beginIndex, endIndex);
                                        try {
                                            numberInput = Double.
                                                    parseDouble(numberString);
                                            numbers.add(numberInput);
                                        } 
                                        catch (java.lang.
                                                NumberFormatException nfe) {
                                            System.err.println(nfe);
                                        }
                                    }
                                }
                                catch(Exception e) {
                                    System.err.println(e);
                                }
                            }   
                        }
                    }
                } 
                catch(IOException e) {
                    System.err.println(e);
                }

                String string_to_compare_with_numbers = Arrays.toString(result);
                String replaced1 = string_to_compare_with_numbers.replaceAll(",", "");
                String replaced2 = replaced1.replace("[", "");
                String replaced3 = replaced2.replace("]", "");
                String replaced4 = replaced3.replaceAll("\\^[0-9]+(\\.[0-9]{1,4})?$","");
                Matcher m = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(replaced4);
                String replaced5 = numbers.toString().replaceAll(",", "");
                String replaced6 = replaced5.replace("[", "");
                String replaced7 = replaced6.replace("]", "");
                String replaced8 = replaced7.replaceAll("\\^[0-9]+(\\.[0-9]{1,4})?$","");
                Matcher m2 = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(replaced8);

                double[] TSPsolutionCoord_x = new double[(numbers.size() / 2)-1];
                double[] TSPsolutionCoord_y = new double[(numbers.size() / 2)-1];
                double[] comp_x = new double[numbers.size() / 2];
                double[] comp_y = new double[numbers.size() / 2];
                int index = 1;
                for(int j = 0; m.find(); j++) {
                    double value = Double.parseDouble(m.group());
                    if(j % 2 == 0 || j == 0) {
                        index--;
                        TSPsolutionCoord_x[index] = value;
                    }
                    if(j % 2 == 1) {
                        TSPsolutionCoord_y[index-1] = value;
                    }
                    index++;
                }
                double start_node_x = TSPsolutionCoord_x[0];
                double start_node_y = TSPsolutionCoord_y[0];
                StringBuilder sb = new StringBuilder();
                index = 0;
                for(int j = 0; m2.find(); j++) {
                    double value = Double.parseDouble(m2.group());
                    if((j - 1)%3 == 0) {    // x 
                        index--;
                        comp_x[index] = value;
                    }
                    if((j - 2)%3 == 0) {    // y 
                        index--;
                        comp_y[index] = value;
                    }
                    index++;
                }

                boolean first = true;
                int first_index = 0;
                double last_node_x = Double.MAX_VALUE;
                double last_node_y = Double.MAX_VALUE;
                for(int j = 0; j < max; j++) {
                    for(int k = 0; k < max; k++) {
                        if(TSPsolutionCoord_x[j] == comp_x[k] && 
                           TSPsolutionCoord_y[j] == comp_y[k]) {
                            //System.out.println("MATCHED " + TSPsolutionCoord_x[j] + 
                            //        ", " + TSPsolutionCoord_y[j] + 
                            //        "\t" + comp_x[k] + ", " + comp_y[k]);
                            if(first) {
                                sb.append(k+1);
                                first_index = k+1;
                                first = false;
                                tour_length += Sub_algorithms.Euclidean_distance(
                                      start_node_x, start_node_y, 
                                      TSPsolutionCoord_x[j], TSPsolutionCoord_y[j]);
                            }
                            else {
                                sb.append("-").append(k+1);
                                tour_length += Sub_algorithms.Euclidean_distance(
                                      last_node_x, last_node_y, 
                                      TSPsolutionCoord_x[j], TSPsolutionCoord_y[j]);
                            }
                            last_node_x = TSPsolutionCoord_x[j];
                            last_node_y = TSPsolutionCoord_y[j];
                        }
                    }
                }
                sb.append("-").append(first_index); // back to the first vertex 
                tour_length += Sub_algorithms.Euclidean_distance(
                        last_node_x, last_node_y, 
                        start_node_x, start_node_y);

                solution = sb.toString();

                boolean hamiltonian = checkHamiltonian(solution, max);
                if(hamiltonian) {
                    long endTime = System.nanoTime();
                    System.out.println("Time (millisec): " + (endTime - startTime)/1000000);
                    System.out.println("Time (sec, rounded down): " + (endTime - startTime)/1000000000 + "\n");

                    return "Prim MST connections + odd degree node matchings:\n" + connections_string + 
                            "Total " + connections + " connections.\n"
                            + "\nNOTE: MATCHING MIGHT NOT BE THE MINIMUM PERFECT MATCHING.\n"
                            + "FOR LARGE INPUTS, MAX 1.5 TIMES THE OPTIMAL TOUR IS NOT GUARANTEED.\nSolution: \n" + 
                            solution + "\nTour length: " + tour_length;
                }
                else {
                    return "Bug! The calculated tour is not a Hamiltonian circuit!\n" + solution;
                }
            //}
        }
        else {
            return ERRORMSG;
        }   
    }

    /**
     * The Self-Organizing Map solving the Travelling Salesman Problem with a 
     * Convex Hull input (or hull edge centrals). Inner vertices are neurons that 
     * will be clustered. Finally, connect all the vertices with the 
     * Nearest Neighbour algorithm. 
     * 
     * The SOM used here is not for real machine learning purposes, the algorithm 
     * logic itself for clustering is the focus.
     * 
     * SOM neurons usually do not fully go on the inputs, only a little towards them.
     * 
     * @param input String
     * @return String
     */
    public static String SOM_CH_NN_Algorithm(String input) {
        double tour_length = 0.0; 
        String solution = "";
        int min = 1;
        int max = 0; // amount of vertices, will be increased later below 
        String rows[] = new String[500000];    
        ArrayList coordinates = new ArrayList();
        int i = 0;
        String str;
        BufferedReader reader = new BufferedReader(new StringReader(input));
        boolean EUC_2D = false;
        boolean hasSameCoordinates = false;
        boolean rightAmountOfNumbers = false;
        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    rows[i] = str;
                    if(rows[i].charAt(0) == '0' || 
                            rows[i].charAt(0) == '1' ||
                            rows[i].charAt(0) == '2' || 
                            rows[i].charAt(0) == '3' ||
                            rows[i].charAt(0) == '4' || 
                            rows[i].charAt(0) == '5' ||
                            rows[i].charAt(0) == '6' || 
                            rows[i].charAt(0) == '7' ||
                            rows[i].charAt(0) == '8' || 
                            rows[i].charAt(0) == '9') {
                        rows[i] = rows[i].replaceAll(",", ".");
                        max++;
                        try {
                            Double numberInput;
                            int endIndex;
                            for (int beginIndex = 0; beginIndex < 
                                    rows[i].length(); 
                                    beginIndex = endIndex + 1) {
                                endIndex = rows[i].indexOf(" ", beginIndex);
                                if (endIndex == -1) {
                                    endIndex = rows[i].length();
                                }
                                String numberString = rows[i].substring(
                                        beginIndex, endIndex);
                                try {
                                    numberInput = Double.valueOf(numberString);
                                    coordinates.add(numberInput);   
                                    // second x, third y, again 
                                } 
                                catch (java.lang.
                                        NumberFormatException nfe) {
                                    System.err.println(nfe);
                                }
                            }
                        }
                        catch(Exception e) {
                            System.err.println(e);
                        }
                    }
                    if(rows[i].contains("EUC_2D")) {
                        EUC_2D = true;
                    }
                }
            }
        } 
        catch(IOException e) {
            System.err.println(e);
        }
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        ArrayList coordinates_x2 = new ArrayList(); 
            // 2 = list where the inner vertices remain 
        ArrayList coordinates_y2 = new ArrayList();
        for(int j = 1; j < coordinates.size(); j++) {
            if(j%3 == 0) {
                rightAmountOfNumbers = false;
            }
            if((j - 1)%3 == 0) {
                coordinates_x.add(coordinates.get(j));
                coordinates_x2.add(coordinates.get(j));
                rightAmountOfNumbers = false;
            }
            if((j - 2)%3 == 0) {
                coordinates_y.add(coordinates.get(j));
                coordinates_y2.add(coordinates.get(j));
                rightAmountOfNumbers = true;
            }
        }
        for(int j = 0; j < coordinates_x.size()-1; j++) {
            for(int k = j + 1; k < coordinates_x.size(); k++) {
                if(coordinates_x.get(j).equals(coordinates_x.get(k)) &&
                   coordinates_y.get(j).equals(coordinates_y.get(k))) {
                    hasSameCoordinates = true;
                    System.out.println("equal coordinates" + 
                            coordinates_x.get(j) + ", " + 
                            coordinates_y.get(j) + " and " + 
                            coordinates_x.get(k) + ", " + 
                            coordinates_y.get(k));
                }
            }
        }
        // The SOM-CH-NN input is read, now the algorithm starts if the input is ok.
        
        String inputresult = checkCoordinateInput(coordinates);
        if(inputresult.equals("ok") && max >=4 && EUC_2D && 
                !hasSameCoordinates && rightAmountOfNumbers) {
            System.gc(); // run garbage collector before starting 
            //String[] args = new String[1]; 
            //args[0] = "$ java -javaagent:tracker.jar TSP_Solver_UEF_241908 500 > /dev/null";
            //ResourceTracker.premain(args[0]);
            // ^ Comment if ResourceTracker.java is not used 
            long startTime = System.nanoTime();
            
            String hull = Sub_algorithms.ConvexHull(min, max, 
                    coordinates_x, coordinates_y);
            String replace1 = hull.replace("]], [[", "]],\n[[");
            String[] lines = replace1.split("\r\n|\r|\n");
            String replace2 = replace1.replace("[[[", "\tFrom (");
            String replace3 = replace2.replace("[[", "\tFrom (");
            String replace4 = replace3.replace("], [", ")    to    (");
            String replace5 = replace4.replace("]]]", ")");
            String replace6 = replace5.replace("]]", ")");
            String replace7 = replace6.replace("),", ")");
            String hullResult = "Convex hull connections: \n" + replace7 + 
                    "\nTotal " + lines.length + " connections, the average"
                    + " coordinates (edge centers) are SOM inputs.\n";
            String replace8 = replace7.replace(",", ".");
            String replace9 = replace8.replace("(", "");
            String replace10 = replace9.replace(")", "");
            String replace11 = replace10.replace("\tFrom ", "");
            String replace12 = replace11.replace("    to    ", " ");
            String replace13 = replace12.replace(". ", " ");

            double[][][] edges = new double[max][2][2];
            for(int j = 0; j < max; j++) {
                edges[j][0][0] = Double.MAX_VALUE;
                edges[j][0][1] = Double.MAX_VALUE;
                edges[j][1][0] = Double.MAX_VALUE;
                edges[j][1][1] = Double.MAX_VALUE;
            }
            String rows2[] = new String[500000];    
            ArrayList values = new ArrayList();
            int a = 0;
            String str2;
            BufferedReader reader2 = new BufferedReader(new StringReader(replace13));
            try {
                while ((str2 = reader2.readLine()) != null) {
                    if (str2.length() > 0) {
                        rows2[a] = str2; 
                        if(rows2[a].charAt(0) == '0' || 
                                rows2[a].charAt(0) == '1' ||
                                rows2[a].charAt(0) == '2' || 
                                rows2[a].charAt(0) == '3' ||
                                rows2[a].charAt(0) == '4' || 
                                rows2[a].charAt(0) == '5' ||
                                rows2[a].charAt(0) == '6' || 
                                rows2[a].charAt(0) == '7' ||
                                rows2[a].charAt(0) == '8' || 
                                rows2[a].charAt(0) == '9' ||
                                rows2[a].charAt(0) == '-') {
                            // max will not be increased 
                            try {
                                Double numberInput;
                                int endIndex;
                                for (int beginIndex = 0; beginIndex < 
                                        rows2[a].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows2[a].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows2[a].length();
                                    }
                                    String numberString = rows2[a].substring(
                                            beginIndex, endIndex);
                                    try {
                                        numberInput = Double.valueOf(
                                                numberString);
                                        values.add(numberInput);
                                    } 
                                    catch (java.lang.NumberFormatException nfe) {
                                        System.err.println(nfe);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        } 
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            
            int temp_index = 0;
            for(int j = 0; j < values.size(); j++) {
                if(j == 0 || j%4 == 0) {
                    edges[temp_index][0][0] = (double)values.get(j);
                }
                if((j + 1)%4 == 0) {
                    edges[temp_index][1][1] = (double)values.get(j);
                    temp_index++;
                }
                if((j + 2)%4 == 0) {
                    edges[temp_index][0][1] = (double)values.get(j);
                }
                if((j + 3)%4 == 0) {
                    edges[temp_index][1][0] = (double)values.get(j);
                }
            }
            boolean[] circumferenceVertices = new boolean[max]; 

            int match_count = 0;
            for(int j = 0; j < max; j++) {
                for(int k = 0; k < max; k++) {
                    if(coordinates_x2.get(j).equals(edges[k][0][0]) && 
                       coordinates_y2.get(j).equals(edges[k][1][0]) && 
                       edges[k][0][0] < Double.MAX_VALUE && 
                       edges[k][1][0] < Double.MAX_VALUE) {
                        match_count++;
                        circumferenceVertices[j] = true;
                        coordinates_x2.set(j, Double.MAX_VALUE);
                        coordinates_y2.set(j, Double.MAX_VALUE);
                    }
                }
            } 
            //boolean circumferenceVertices[] currently knows what are in hull 
            int amountOfHullVertices = 0;
            for(int j = 0; j < max; j++) {
                //System.out.println(edges[j][0][0] + ", " + edges[j][1][0] + 
                //            "; " + edges[j][0][1] + ", " + edges[j][1][1] + 
                //            ";\t " + 
                //        coordinates_x2.get(j) + ", " + coordinates_y2.get(j) + 
                //        "; \tIN HULL? " + circumferenceVertices[j]);
                if((edges[j][0][0] < Double.MAX_VALUE) && 
                        (edges[j][1][0] < Double.MAX_VALUE) && 
                        (edges[j][0][1] < Double.MAX_VALUE) && 
                        (edges[j][1][1] < Double.MAX_VALUE)) {
                    amountOfHullVertices++;
                    //System.out.println("amountOfHullVertices is " + amountOfHullVertices);
                }
            }
            
            // calculate the hull average points, in other words the hull edge middle or central points
            // calculate each point pair distance with 2 fors and find out the maxPossibleDistanceInHull;
            ArrayList hullPointsToApproach = new ArrayList();
            for(int j = 0; j < amountOfHullVertices; j++) {
                double averageX = (edges[j][0][0] + edges[j][0][1])/2;
                double averageY = (edges[j][1][0] + edges[j][1][1])/2;
                hullPointsToApproach.add(averageX);
                hullPointsToApproach.add(averageY); // xy xy xy xy...
            }
            System.out.println("\nhullPointsToApproach: " + hullPointsToApproach + "\n");
            
            double maxPossibleDistanceInHullAvgs = 0;
            for(int j = 0; j < hullPointsToApproach.size(); j+=2) {
                for(int k = 0; k < hullPointsToApproach.size(); k+=2) {
                    double distance = Sub_algorithms.Euclidean_distance(
                                (double)hullPointsToApproach.get(j), (double)hullPointsToApproach.get(j+1), 
                                (double)hullPointsToApproach.get(k), (double)hullPointsToApproach.get(k+1));
                    if(distance > maxPossibleDistanceInHullAvgs) {
                        maxPossibleDistanceInHullAvgs = distance;
                    }
                }
            }
            System.out.println("maxPossibleDistanceInHull: " + maxPossibleDistanceInHullAvgs);
            // initialize the variables before SOM: 
            ArrayList somResultArray;// = new ArrayList();
            ArrayList inputCoordinateXs = new ArrayList();
            ArrayList inputCoordinateYs = new ArrayList();
            ArrayList neuronCoordinateXs = new ArrayList();
            ArrayList neuronCoordinateYs = new ArrayList();
            ArrayList neuronCoordinateXsCopy = new ArrayList();
            ArrayList neuronCoordinateYsCopy = new ArrayList();
            
            /*
            Important! If more clusters wanted, just set some extra circumference vertices like this: 
            circumferenceVertices[j] = true;
            The for loop below will then set the inner ones as movable neurons.
            */
            for(int j = 0; j < circumferenceVertices.length; j++) {
                if(circumferenceVertices[j] == false) {
                    neuronCoordinateXs.add(coordinates_x2.get(j));
                    neuronCoordinateYs.add(coordinates_y2.get(j));
                    neuronCoordinateXsCopy.add(coordinates_x2.get(j));
                    neuronCoordinateYsCopy.add(coordinates_y2.get(j));
                }
            }
            
            for(int j = 0; j < coordinates_x2.size(); j++) {
                if(circumferenceVertices[j] == true) {
                    coordinates_x2.remove(j);
                    coordinates_y2.remove(j);
                    j--; // because removing decreases the array length, j-- 
                }
            }
            
            for(int j = 0; j < hullPointsToApproach.size()-1; j+=2) {
                if(j < hullPointsToApproach.size()-1) {
                    inputCoordinateXs.add(hullPointsToApproach.get(j));
                    inputCoordinateYs.add(hullPointsToApproach.get(j+1));
                }
            }
            
            int maxIterations = ((inputCoordinateXs.size()*2)  // can be changed however wanted 
                    + (int)(neuronCoordinateXs.size()/(0.08 * neuronCoordinateXs.size()))) +2;
            
            somResultArray = KohonenSOM.performSOM(maxIterations, maxPossibleDistanceInHullAvgs, 
                        inputCoordinateXs, inputCoordinateYs, 
                        neuronCoordinateXs, neuronCoordinateYs, false, null); // no logic stacks 
            String somSolution = "\nClusters after the Self-Organizing Map phase:" 
                    + "\n\tNew position of X,   New position of Y,   CLUSTER ID \t"
                    + "           Original X,   Original Y";
            
            int helpindex = 0;
            for(int j = 0; j < somResultArray.size()-2; j+=3) {
                somSolution = new StringBuilder(somSolution).append("\n\t")
                        .append(somResultArray.get(j)).append(", ").append(somResultArray.get(j+1))
                        .append(", in CLUSTER ").append(somResultArray.get(j+2)).toString();
                if(helpindex < neuronCoordinateXsCopy.size()) {
                    somSolution = new StringBuilder(somSolution).append("\t\t")
                            .append(neuronCoordinateXsCopy.get(helpindex))
                            .append(", ").append(neuronCoordinateYsCopy.get(helpindex)).toString();
                    helpindex++;
                }
            }
            somSolution = new StringBuilder(somSolution).append("\nTotal ")
                    .append(neuronCoordinateXs.size()).append(
                            " neuron points. Chaining the clusters with Nearest Neighbor.").toString();
            
            // somResultArray has "x, y, cluster number"; "x, y, cluster number"; "x, y, cluster number" ... 
            // make NNH for each cluster (remember also the original inputs, in other words the hull vertices (not centrals)!)
            // then concatenate the sprouts (remember to proceed in the correct order, around each of the hull's "edge cluster"!)
            
            // Improvement idea: 1st do the nnh for the MOVED neuron vertices, 
            // THEN find out what they were originally? 
            // (currently the new locations are ignored and the clustering just starts for originals)
            
            ArrayList nnh_purpose_coordinates = new ArrayList();
            ArrayList nnh_purpose_coordinates_x = new ArrayList();
            ArrayList nnh_purpose_coordinates_y = new ArrayList();
            ArrayList nnh_purpose_coordinates_clust = new ArrayList();
            
            for(int j = 0; j < amountOfHullVertices; j++) {
                nnh_purpose_coordinates.add((double)edges[j][0][0]);
                nnh_purpose_coordinates.add((double)edges[j][1][0]);
                nnh_purpose_coordinates.add(j);
                //System.out.println("Hull: " + (double)edges[j][0][0] + ", " + (double)edges[j][1][0] + ", " + j);
            }
            helpindex = 2;
            for(int j = 0; j < neuronCoordinateXsCopy.size(); j++) {
                nnh_purpose_coordinates.add(neuronCoordinateXsCopy.get(j));
                nnh_purpose_coordinates.add(neuronCoordinateYsCopy.get(j));
                nnh_purpose_coordinates.add(somResultArray.get(helpindex));
                //System.out.println("Neurons and clusters: " + neuronCoordinateXsCopy.get(j) + ", " + 
                //        neuronCoordinateYsCopy.get(j) + ", " + somResultArray.get(helpindex));
                helpindex +=3;
            }
            for(int j = 0; j < nnh_purpose_coordinates.size()-2; j+=3) {
                    nnh_purpose_coordinates_x.add(nnh_purpose_coordinates.get(j));
                    nnh_purpose_coordinates_y.add(nnh_purpose_coordinates.get(j+1));
                    nnh_purpose_coordinates_clust.add(nnh_purpose_coordinates.get(j+2));
            }
            //System.out.println("All are " + nnh_purpose_coordinates.toString());
            //System.out.println("Xs are " + nnh_purpose_coordinates_x.toString());
            //System.out.println("Ys are " + nnh_purpose_coordinates_y.toString());
            //System.out.println("Clusters are " + nnh_purpose_coordinates_clust.toString());
            // amountOfHullVertices and cluster count are equal 
            
            Random rand = new Random();
            int start = rand.nextInt((max - min) + 1) + min; // min 1  
            boolean[] booltable = new boolean[max];
            boolean booltable_has_false = true;
            int pointer = 1; 
            int pointer_start = start -1;
            int clusterInTurnNow = (int)nnh_purpose_coordinates_clust.get(pointer_start);
            
            //solution = new StringBuilder(solution).append(String.valueOf(start)).toString();
            double[] solutionArray = new double[(max +1)*2];
            int solutionArrayInsertIndex = 0;
            for(int j = 0; j < max +1; j++) {
                        solutionArray[j] = Double.MAX_VALUE;
            }
            solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(pointer_start);
            solutionArrayInsertIndex++;
            solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(pointer_start);
            solutionArrayInsertIndex++;
            boolean verticeadded;// = true;
            booltable[pointer_start] = true;
            while(booltable_has_false) {
                //System.out.println("Pointer start is " + pointer_start + 
                //        ", pointer is (begin of while loop): " + pointer + 
                //        ", the wanted cluster is " + clusterInTurnNow);
                double min_distance = Double.MAX_VALUE;
                double temp_distance;
                double x1;
                double y1;
                x1 = (double) nnh_purpose_coordinates_x.get(pointer_start);
                y1 = (double) nnh_purpose_coordinates_y.get(pointer_start);
                double x2;
                double y2;
                
                for(int j = 0; j < booltable.length; j++) {
                    if((booltable[j] == false) 
                             && (clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(j))
                            ) {
                        x2 = (double) nnh_purpose_coordinates_x.get(j);
                        y2 = (double) nnh_purpose_coordinates_y.get(j);
                        temp_distance = Sub_algorithms.
                                Euclidean_distance_squared(x1, y1, x2, y2);
                        if((temp_distance < min_distance)) {
                            min_distance = temp_distance;
                            pointer = j;
                            //System.out.println("min distance is " + min_distance);
                        }
                    }
                }
                
                // new vertice to the result: 
                if((booltable[pointer] == false) 
                        // && (clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(pointer)) 
                ) {
                    booltable[pointer] = true;
                    //solution = new StringBuilder(solution).append("-").toString();
                    //solution = new StringBuilder(solution).append(pointer + 1).toString();
                    solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(pointer);
                    solutionArrayInsertIndex++;
                    solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(pointer);
                    solutionArrayInsertIndex++;
                    //System.out.println("ADDED with pointer " + pointer + ", cluster wanted: " + clusterInTurnNow);
                    verticeadded = true;
                }
                else {
                    //System.out.println("DID NOT ADD ANYTHING, pointer is " + pointer);
                    verticeadded = false;
                }

                boolean all_in_this_cluster_taken = true;
                for(int j = 0; j < nnh_purpose_coordinates_clust.size(); j++) {
                    if((booltable[j] == false) && (
                            clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(j))) {
                                all_in_this_cluster_taken = false;
                                //System.out.println("NOT TAKEN YET: " + j + " in cluster " + clusterInTurnNow);
                                j = nnh_purpose_coordinates_clust.size(); // end the loop 
                    }
                }
                //System.out.println("Booltable: " + Arrays.toString(booltable));
                if(all_in_this_cluster_taken) {
                    //if(verticeadded) {
                        clusterInTurnNow++;
                        if(clusterInTurnNow >= amountOfHullVertices) {
                            clusterInTurnNow = 0;
                        }
                        //System.out.println("CLUSTER HANDLED, clusterInTurnNow changed to " + clusterInTurnNow);
                    //}
                }
                
                booltable_has_false = false;
                for(int j = 0; j < booltable.length; j++) {
                    if(booltable[j] == false) {
                        booltable_has_false = true;
                    }
                }
                if(verticeadded) {
                    pointer_start = pointer;
                    //verticeadded = false;
                }
            }
            // link back to the start node:
            solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(start-1);
            solutionArrayInsertIndex++;
            solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(start-1);
            solutionArrayInsertIndex++;
            
            //System.out.println("ADDED index (final) is " + (start-1));

            // Finally calculating the tour length and the solution: 
            for(int j = 0; j < solutionArray.length -2; j+=2) {
                double distance = Sub_algorithms.Euclidean_distance(
                        solutionArray[j], 
                        solutionArray[j+1], 
                        solutionArray[j+2], 
                        solutionArray[j+3]);
                //System.out.println("Added " + distance + " on j as " + j);
                tour_length += distance;
            }
            //int firstNode = -1;
            System.out.println("\tSOLUTION: " + Arrays.toString(solutionArray));
            
            System.out.println("\tCoord: " + coordinates);
            for(int k = 0; k < solutionArray.length; k+=2) {
                for(int j = coordinates.size()-3; j > -1; j-=3) { 
                    if(coordinates.get(j+1).equals(solutionArray[k]) && 
                       coordinates.get(j+2).equals(solutionArray[k+1])) {
                        //System.out.println("Match: " + coordinates.get(j+1) + ", " + 
                        //        coordinates.get(j+2) + " & " + solutionArray[k] + ", " + solutionArray[k+1]);
                        if(solution.length() < 1) {
                            solution = new StringBuilder(solution).append(((int)(double)coordinates.get(j))).toString();
                            //firstNode = ((int)(double)coordinates.get(j));
                        }
                        else {
                            solution = new StringBuilder(solution).append("-").toString();
                            solution = new StringBuilder(solution).append(((int)(double)coordinates.get(j))).toString();
                        }
                    }
                }
            }
            
            boolean hamiltonian = checkHamiltonian(solution, max);
            
            if(hamiltonian) {
                long endTime = System.nanoTime();
                System.out.println("Time (millisec): " + (endTime - startTime)/1000000);
                System.out.println("Time (sec, rounded down): " + (endTime - startTime)/1000000000 + "\n");
                    return hullResult + somSolution + "\nSolution: \n" + solution + 
                        "\nTour length: " + tour_length;
            }
            else {
                return "The calculated tour is not a Hamiltonian circuit!\n" + 
                        hullResult + somSolution + "\nSolution: \n" + solution + 
                        "\nTour length: " + tour_length;
            }
        }
        else {
            return ERRORMSG;
        }
    }

    /**
     * Lin-Kernighan-edition-3-with-2-opts.
     * Try 3 of some already existing heuristics, finally improve the best one 
     * with Lin-Kernighan. It uses opt moves with stack in order to improve an already 
     * existing Hamiltonian TSP tour. The search can be adjusted. Even the 
     * 2-opts alone are good tools for improvements. 
     * Everything depends on what vertices are put back to the stack and when + how 
     * the code's variables and loops are set.
     * 
     * The selected 3 heuristics are NNH, CHH and CHRI.
     * NNH is the least time-consuming, can be called for multiple times.
     * Lin-Kernighan computations are in the class Sub_algorithms.
     * 
     * @param text
     * @return 
     */
    static String LK3_Algorithm(String text) {
        double tour_length = 0.0;
        String solution = "";
        int max = 0; // int min = 1;
        String rows[] = new String[500000];    
        ArrayList coordinates = new ArrayList();
        int i = 0;
        String str;
        BufferedReader reader = new BufferedReader(new StringReader(text));
        boolean EUC_2D = false;
        boolean hasSameCoordinates = false;
        boolean rightAmountOfNumbers = false;
        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    rows[i] = str; 
                    if(rows[i].charAt(0) == '0' || 
                            rows[i].charAt(0) == '1' ||
                            rows[i].charAt(0) == '2' || 
                            rows[i].charAt(0) == '3' ||
                            rows[i].charAt(0) == '4' || 
                            rows[i].charAt(0) == '5' ||
                            rows[i].charAt(0) == '6' || 
                            rows[i].charAt(0) == '7' ||
                            rows[i].charAt(0) == '8' || 
                            rows[i].charAt(0) == '9') {
                        rows[i] = rows[i].replaceAll(",", ".");
                        max++;
                        try {
                            Double numberInput;
                            int endIndex;
                            for (int beginIndex = 0; beginIndex < 
                                    rows[i].length(); 
                                    beginIndex = endIndex + 1) {
                                endIndex = rows[i].indexOf(" ", beginIndex);
                                if (endIndex == -1) {
                                    endIndex = rows[i].length();
                                }
                                String numberString = rows[i].substring(
                                        beginIndex, endIndex);
                                try {
                                    numberInput = Double.valueOf(
                                            numberString);
                                    coordinates.add(numberInput);   
                                    // second x, third y 
                                } 
                                catch (java.lang.NumberFormatException nfe) {
                                    System.err.println(nfe);
                                }
                            }
                        }
                        catch(Exception e) {
                            System.err.println(e);
                        }
                    }
                    if(rows[i].contains("EUC_2D")) {
                        EUC_2D = true;
                        //System.out.println("Graph is EUC_2D");
                    }
                }
            }
        } 
        catch(IOException e) {
            System.err.println(e);
        }
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        ArrayList coordinate_ids = new ArrayList();
        for(int j = 0; j < coordinates.size(); j++) {
            if(j%3 == 0) {
                rightAmountOfNumbers = false;
                coordinate_ids.add(coordinates.get(j));
            }
            if((j - 1)%3 == 0) {
                coordinates_x.add(coordinates.get(j));
                rightAmountOfNumbers = false;
            }
            if((j - 2)%3 == 0) {
                coordinates_y.add(coordinates.get(j));
                rightAmountOfNumbers = true;
            }
        }
        for(int j = 0; j < coordinates_x.size()-1; j++) {
            for(int k = j + 1; k < coordinates_x.size(); k++) {
                if(coordinates_x.get(j).equals(coordinates_x.get(k)) &&
                   coordinates_y.get(j).equals(coordinates_y.get(k))) {
                    hasSameCoordinates = true;
                    System.out.println("equal coordinates" + 
                            coordinates_x.get(j) + ", " + 
                            coordinates_y.get(j) + " and " + 
                            coordinates_x.get(k) + ", " + 
                            coordinates_y.get(k));
                }
            }
        }
        // coordinates have been read in Lin-Kernighan-3 
        
        String inputresult = checkCoordinateInput(coordinates);
        if(inputresult.equals("ok") && max >=4 && EUC_2D && 
                !hasSameCoordinates && rightAmountOfNumbers) {
            System.gc(); // run garbage collector before starting 
            //String[] args = new String[1]; 
            //args[0] = "$ java -javaagent:tracker.jar TSP_Solver_UEF_241908 500 > /dev/null";
            //ResourceTracker.premain(args[0]);
            // ^ Comment if ResourceTracker.java is not used 
            long startTime = System.nanoTime();
            
            String result1; // Strings after just reading all there is 
            String result2;
            String result3;
            String resultE;
            String tour1 = ""; // Strings after reading the tour, 1-2-3-4-1 for example 
            String tour2 = "";
            String tour3 = "";
            String tourE = "";
            ArrayList nnh_coordinates = new ArrayList(); // Strings containing the TSP coordinates, xy xy xy...
            ArrayList chh_coordinates = new ArrayList();
            ArrayList chri_coordinates = new ArrayList();
            String length1 = ""; // variables after reading a length of a TSP tour, 10.12345...
            String length2 = "";
            String length3 = "";
            String lengthE = "";
            double length1d;// = -1.0;
            double length2d;// = -1.0;
            double length3d;// = -1.0;
            double lengthEd;// = -1.0;
            int bestOneOfTheTours;// = -1; // will be 1, 2 or 3 
            //double bestTourLength = Double.MAX_VALUE;
            //String bestTour = "";
            
            result1 = NearestNeighbour_Algorithm(text); //    call NNH   (1) 
            result2 = ConvexHull_Algorithm(text);       //    call CHH   (2) 
            result3 = Christofides_Algorithm(text);     //    call CHRI  (3) 
            resultE = NearestNeighbour_Algorithm(text); //    call NNH again just for a nice extra, could be edited to call 2MST 
            // the results must never contain words like "terror" etc. -> misunderstands the string 
            if(result1.contains("error") || result1.contains("Error") || result1.contains("ERROR")) { 
                return ERRORMSG;
            }
            if(result2.contains("error") || result2.contains("Error") || result2.contains("ERROR")) { 
                return ERRORMSG;
            }
            if(result3.contains("error") || result3.contains("Error") || result3.contains("ERROR")) { 
                return ERRORMSG;
            }
            if(resultE.contains("error") || resultE.contains("Error") || resultE.contains("ERROR")) { 
                return ERRORMSG;
            }
            // read all 3(4) results, compare with the original text input, take the lengths and tours 
            // so the Lin-Kernighan can be called (I. Length, II. Tour and III. Then find coordinates) 
            i = 0;
            String str1;
            BufferedReader reader1 = new BufferedReader(new StringReader(result1));
            try {
                while ((str1 = reader1.readLine()) != null) {
                    if (str1.length() > 0) {
                        rows[i] = str1; 
                        if(rows[i].contains("Tour length:")) {
                            try {
                                int endIndex;
                                for (int beginIndex = 2; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        length1 = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                        if(Character.isDigit(rows[i].charAt(0)) && (
                           rows[i].charAt(1) == '-' || 
                           rows[i].charAt(2) == '-' || 
                           rows[i].charAt(3) == '-' || 
                           rows[i].charAt(4) == '-' || 
                           rows[i].charAt(5) == '-' || 
                           rows[i].charAt(6) == '-')) {
                            try {
                                int endIndex;
                                for (int beginIndex = 0; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        tour1 = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            i = 0;
            String strE;
            BufferedReader readerE = new BufferedReader(new StringReader(resultE));
            try {
                while ((strE = readerE.readLine()) != null) {
                    if (strE.length() > 0) {
                        rows[i] = strE; 
                        if(rows[i].contains("Tour length:")) {
                            try {
                                int endIndex;
                                for (int beginIndex = 2; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        lengthE = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                        if(Character.isDigit(rows[i].charAt(0)) && (
                           rows[i].charAt(1) == '-' || 
                           rows[i].charAt(2) == '-' || 
                           rows[i].charAt(3) == '-' || 
                           rows[i].charAt(4) == '-' || 
                           rows[i].charAt(5) == '-' || 
                           rows[i].charAt(6) == '-')) {
                            try {
                                int endIndex;
                                for (int beginIndex = 0; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        tourE = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            length1d = Double.parseDouble(length1);
            //System.out.println(length1d);
            //System.out.println(tour1);
            lengthEd = Double.parseDouble(lengthE);
            //System.out.println(lengthEd);
            //System.out.println(tourE);
            if(lengthEd < length1d) { // choose the best of the 2 NNHs 
                length1d = lengthEd;
                tour1 = tourE;
            }
            
            // same for the others...
            i = 0;
            String str2;
            BufferedReader reader2 = new BufferedReader(new StringReader(result2));
            try {
                while ((str2 = reader2.readLine()) != null) {
                    if (str2.length() > 0) {
                        rows[i] = str2; 
                        if(rows[i].contains("Tour length:")) {
                            try {
                                int endIndex;
                                for (int beginIndex = 2; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        length2 = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                        if(Character.isDigit(rows[i].charAt(0)) && (
                           rows[i].charAt(1) == '-' || 
                           rows[i].charAt(2) == '-' || 
                           rows[i].charAt(3) == '-' || 
                           rows[i].charAt(4) == '-' || 
                           rows[i].charAt(5) == '-' || 
                           rows[i].charAt(6) == '-')) {
                            try {
                                int endIndex;
                                for (int beginIndex = 0; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        tour2 = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            length2d = Double.parseDouble(length2);
            //System.out.println(length2d);
            //System.out.println(tour2);

            // one more time, the last is CHRI:
            i = 0;
            String str3;
            BufferedReader reader3 = new BufferedReader(new StringReader(result3));
            try {
                while ((str3 = reader3.readLine()) != null) {
                    if (str3.length() > 0) {
                        rows[i] = str3; 
                        if(rows[i].contains("Tour length:")) {
                            try {
                                int endIndex;
                                for (int beginIndex = 2; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        length3 = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                        if(Character.isDigit(rows[i].charAt(0)) && (
                           rows[i].charAt(1) == '-' || 
                           rows[i].charAt(2) == '-' || 
                           rows[i].charAt(3) == '-' || 
                           rows[i].charAt(4) == '-' || 
                           rows[i].charAt(5) == '-' || 
                           rows[i].charAt(6) == '-')) {
                            try {
                                int endIndex;
                                for (int beginIndex = 0; beginIndex < 
                                        rows[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows[i].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows[i].length();
                                    }
                                    String numberString = rows[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        tour3 = numberString;
                                    } 
                                    catch (Exception e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            length3d = Double.parseDouble(length3);
            //System.out.println(length3d);
            //System.out.println(tour3);

            String preResult = "\tNNH for a couple of times:  " + tour1 + "\n\t     " + length1d + "\n" + 
                               "\tCHH for 1 time:  " + tour2 + "\n\t     " + length2d + "\n" + 
                               "\tCHRI for 1 time:  " + tour3 + "\n\t     " + length3d + "\n";
            
            if(length1d < length2d) {
                if(length1d < length3d) {
                    // length1d is the minimum,      2nd and 3rd both remain unknown
                    // choose NNH
                    bestOneOfTheTours = 1;
                        if((length1d == length2d) && (length1d == length3d)) {// even if unfair, NNH
                            bestOneOfTheTours = 1;
                        }
                        if(length1d == length2d) {// even if unfair, choose NNH
                            bestOneOfTheTours = 1;
                        }
                        if(length1d == length3d) {// even if unfair, choose NNH
                            bestOneOfTheTours = 1;
                        }
                }
                else {
                    // length3d is the minimum,      3rd < 1st < 2nd
                    // choose CHRI
                    bestOneOfTheTours = 3;
                        if((length1d == length2d) && (length1d == length3d)) {// even if unfair, NNH
                            bestOneOfTheTours = 1;
                        }
                        if(length2d == length3d) {// even if unfair, choose CHH
                            bestOneOfTheTours = 2;
                        }
                        if(length1d == length3d) {// even if unfair, choose NNH
                            bestOneOfTheTours = 1;
                        }
                }
            }
            else {
                if(length2d < length3d) {
                    // length2d is the minimum,      1st and 3rd both remain unknown
                    // choose CHH
                    bestOneOfTheTours = 2;
                        if((length1d == length2d) && (length1d == length3d)) {// even if unfair, NNH
                            bestOneOfTheTours = 1;
                        }
                        if(length1d == length2d) {// even if unfair, choose NNH
                            bestOneOfTheTours = 1;
                        }
                        if(length2d == length3d) {// even if unfair, choose CHH
                            bestOneOfTheTours = 2;
                        }
                }
                else {
                    // length3d is the minimum,      3rd < 2nd < 1st
                    // choose CHRI
                    bestOneOfTheTours = 3;
                        if((length1d == length2d) && (length1d == length3d)) {// even if unfair, NNH
                            bestOneOfTheTours = 1;
                        }
                        if(length2d == length3d) {// even if unfair, choose CHH
                            bestOneOfTheTours = 2;
                        }
                        if(length1d == length3d) {// even if unfair, choose NNH
                            bestOneOfTheTours = 1;
                        }
                }
            }
            
            String[] tourArr1 = tour1.split("-", max+1);
            String[] tourArr2 = tour2.split("-", max+1);
            String[] tourArr3 = tour3.split("-", max+1);
            
            for (String tourArr11 : tourArr1) { // for tourArr1.length 
                for (int j = 0; j < coordinate_ids.size(); j++) {
                    Double d = (double)coordinate_ids.get(j);
                    int value = d.intValue();
                    if (value == Integer.parseInt(tourArr11)) {
                        nnh_coordinates.add(coordinates_x.get(j));
                        nnh_coordinates.add(coordinates_y.get(j));
                    }
                }
            }
            for (String tourArr21 : tourArr2) {
                for (int j = 0; j < coordinate_ids.size(); j++) {
                    Double d = (double)coordinate_ids.get(j);
                    int value = d.intValue();
                    if (value == Integer.parseInt(tourArr21)) {
                        chh_coordinates.add(coordinates_x.get(j));
                        chh_coordinates.add(coordinates_y.get(j));
                    }
                }
            }
            for (String tourArr31 : tourArr3) {
                for (int j = 0; j < coordinate_ids.size(); j++) {
                    Double d = (double)coordinate_ids.get(j);
                    int value = d.intValue();
                    if (value == Integer.parseInt(tourArr31)) {
                        chri_coordinates.add(coordinates_x.get(j));
                        chri_coordinates.add(coordinates_y.get(j));
                    }
                }
            }
            //System.out.println(nnh_coordinates);
            
            double[][] edges = new double[max][2]; // the LK3 improved edges 
            
            switch(bestOneOfTheTours) {
                case(1): {
                    preResult += "NNH was chosen for improving purposes."
                            + " Please note that not always the solution will be improved!";
                    edges = Sub_algorithms.linKernighan(max, length1d, nnh_coordinates);
                    break;
                }
                case(2): {
                    preResult += "CHH was chosen for improving purposes."
                            + " Please note that not always the solution will be improved!";
                    edges = Sub_algorithms.linKernighan(max, length2d, chh_coordinates);
                    break;
                }
                case(3): {
                    preResult += "CHRI was chosen for improving purposes."
                            + " Please note that not always the solution will be improved!";
                    edges = Sub_algorithms.linKernighan(max, length3d, chri_coordinates);
                    break;
                }
            }
            
            // finally, just form the solution string and calculate the tour length:
            String[] rowsf = new String[500000];    
            ArrayList coordinatesf = new ArrayList();
            i = 0;
            String strf;
            BufferedReader readerf = new BufferedReader(new StringReader(text));
            try {
                while ((strf = readerf.readLine()) != null) {
                    if (strf.length() > 0) {
                        rowsf[i] = strf;
                        if(rowsf[i].charAt(0) == '0' || 
                                rowsf[i].charAt(0) == '1' ||
                                rowsf[i].charAt(0) == '2' ||
                                rowsf[i].charAt(0) == '3' ||
                                rowsf[i].charAt(0) == '4' ||
                                rowsf[i].charAt(0) == '5' ||
                                rowsf[i].charAt(0) == '6' ||
                                rowsf[i].charAt(0) == '7' ||
                                rowsf[i].charAt(0) == '8' ||
                                rowsf[i].charAt(0) == '9') {
                            try {
                                double numberInput;
                                int endIndex;
                                for (int beginIndex = 0; beginIndex <
                                        rowsf[i].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rowsf[i].indexOf(" ", 
                                            beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rowsf[i].length();
                                    }
                                    String numberString = rowsf[i].substring(
                                            beginIndex, endIndex);
                                    try {
                                        numberInput = Double.
                                                parseDouble(numberString);
                                        coordinatesf.add(numberInput);
                                    } 
                                    catch (java.lang.
                                            NumberFormatException nfe) {
                                        System.err.println(nfe);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        }   
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            // coordinatesf has now the original coordinates,
            // when they match with the tsp result in edges, add to solution,
            // at the same time += tour length
            
            ArrayList coordinates_xf = new ArrayList(); // f means final 
            ArrayList coordinates_yf = new ArrayList();
            for(int j = 0; j < coordinatesf.size(); j++) {
                if(j%3 == 0 || j == 0) {    // do nothing, intentional 
                }
                if((j - 1)%3 == 0) {
                    coordinates_xf.add(coordinatesf.get(j));
                }
                if((j - 2)%3 == 0) {
                    coordinates_yf.add(coordinatesf.get(j));
                }
            }
            int match_count = 0;
            int first_index = 0;
            double startX = edges[0][0];
            double startY = edges[0][1];
            double lastX = edges[0][0];
            double lastY = edges[0][1];
            
            for(int j = 0; j < max; j++) {
                for(int k = 0; k < max; k++) {
                    if(coordinates_xf.get(k).equals(edges[j][0]) && 
                       coordinates_yf.get(k).equals(edges[j][1]) && 
                       edges[j][0] < Double.MAX_VALUE && 
                       edges[j][1] < Double.MAX_VALUE) {
                        if(match_count > 0) {
                            solution = new StringBuilder(solution).append("-").toString();
                            tour_length += Sub_algorithms.Euclidean_distance(
                                    edges[j][0], edges[j][1], 
                                    lastX, lastY);
                        }
                        else {
                            first_index = k+1;
                            startX = edges[j][0];
                            startY = edges[j][1];
                        }
                        lastX = edges[j][0];
                        lastY = edges[j][1];
                        match_count++;
                        solution = new StringBuilder(solution).append(k+1).toString();
                    }
                }
            }
            // connection back to the last vertex 
            // and the last addition to tour length 
            solution = new StringBuilder(solution).append("-").toString();
            solution = new StringBuilder(solution).append(first_index).toString();
            tour_length += Sub_algorithms.Euclidean_distance(lastX, lastY, startX, startY);
            
            boolean hamiltonian = checkHamiltonian(solution, max);
            if(hamiltonian) {
                long endTime = System.nanoTime();
                System.out.println("After LK3 - Time (millisec): " + (endTime - startTime)/1000000);
                System.out.println("After LK3 - Time (sec, rounded down): " + (endTime - startTime)/1000000000 + "\n");

                return "Note: if some solutions are equally good, only one will still be selected for a quick 2-opt-Lin-Kernighan."
                       // + " NNH dominates CHH and CHH dominates CHRI."
                        + "\n" + preResult + "\n" + 
                        "\nSolution: \n" + solution + "\nTour length: " + tour_length;
            }
            else {
                return "The calculated tour is not a Hamiltonian circuit!\n" + solution;
            }
        }
        return ERRORMSG;
    }

    /**
     * SOM-CH-NN-EVO call. 
     * Same as "SOM_CH_NN_Algorithm" but much more complex. There are also twice as 
     * much clusters as hull vertices (unless edited to be something else). Evolutionary computing used. 
     * Since there is also the "LK_SOM_CH_NN_EVO_Algorithm", this is just a call to that 
     * with the "boolean callLinKernighanFinally" difference. The boolean's purpose is to 
     * prevent copy-pasting the exact same codes twice.
     * 
     * @param inputText String
     * @return String
     */
    public static String SOM_CH_NN_EVO_Algorithm(String inputText) {
        return LK_SOM_CH_NN_EVO_Algorithm(inputText, false);
    }

    /**
     * Same as "SOM_CH_NN_EVO_Algorithm" but try to improve the solution after the 
     * calculations. Same as "SOM_CH_NN_Algorithm" but much more complex. There are also twice as 
     * much clusters as hull vertices. Evolutionary computing is used. 
     * 
     * @param inputText String
     * @param callLinKernighanFinally boolean
     * @return String
     */
    public static String LK_SOM_CH_NN_EVO_Algorithm(String inputText, boolean callLinKernighanFinally) {
        //double tour_length; 
        //tour_length = 0.0;
        String solution = "";
        int min = 1;
        int max = 0; // amount of vertices, will be increased later below 
        String rows[] = new String[500000];    
        ArrayList coordinates = new ArrayList();
        int i = 0;
        String str;
        BufferedReader reader = new BufferedReader(new StringReader(inputText));
        boolean EUC_2D = false;
        boolean hasSameCoordinates = false;
        boolean rightAmountOfNumbers = false;
        try {
            while ((str = reader.readLine()) != null) {
                if (str.length() > 0) {
                    rows[i] = str;
                    if(rows[i].charAt(0) == '0' || 
                            rows[i].charAt(0) == '1' ||
                            rows[i].charAt(0) == '2' || 
                            rows[i].charAt(0) == '3' ||
                            rows[i].charAt(0) == '4' || 
                            rows[i].charAt(0) == '5' ||
                            rows[i].charAt(0) == '6' || 
                            rows[i].charAt(0) == '7' ||
                            rows[i].charAt(0) == '8' || 
                            rows[i].charAt(0) == '9') {
                        rows[i] = rows[i].replaceAll(",", ".");
                        max++;
                        try {
                            Double numberInput;
                            int endIndex;
                            for (int beginIndex = 0; beginIndex < 
                                    rows[i].length(); 
                                    beginIndex = endIndex + 1) {
                                endIndex = rows[i].indexOf(" ", beginIndex);
                                if (endIndex == -1) {
                                    endIndex = rows[i].length();
                                }
                                String numberString = rows[i].substring(
                                        beginIndex, endIndex);
                                try {
                                    numberInput = Double.valueOf(numberString);
                                    coordinates.add(numberInput);   
                                    // second x, third y 
                                } 
                                catch (java.lang.
                                        NumberFormatException nfe) {
                                    System.err.println(nfe);
                                }
                            }
                        }
                        catch(Exception e) {
                            System.err.println(e);
                        }
                    }
                    if(rows[i].contains("EUC_2D")) {
                        EUC_2D = true;
                        //System.out.println("Graph is EUC_2D");
                    }
                }
            }
        } 
        catch(IOException e) {
            System.err.println(e);
        }
        ArrayList coordinates_x = new ArrayList();
        ArrayList coordinates_y = new ArrayList();
        ArrayList coordinates_x2 = new ArrayList(); 
            // 2 = list where the inner vertices remain 
        ArrayList coordinates_y2 = new ArrayList();
        for(int j = 1; j < coordinates.size(); j++) {
            if(j%3 == 0) {
                rightAmountOfNumbers = false;
            }
            if((j - 1)%3 == 0) {
                coordinates_x.add(coordinates.get(j));
                coordinates_x2.add(coordinates.get(j));
                rightAmountOfNumbers = false;
            }
            if((j - 2)%3 == 0) {
                coordinates_y.add(coordinates.get(j));
                coordinates_y2.add(coordinates.get(j));
                rightAmountOfNumbers = true;
            }
        }
        for(int j = 0; j < coordinates_x.size()-1; j++) {
            for(int k = j + 1; k < coordinates_x.size(); k++) {
                if(coordinates_x.get(j).equals(coordinates_x.get(k)) &&
                   coordinates_y.get(j).equals(coordinates_y.get(k))) {
                    hasSameCoordinates = true;
                    System.out.println("equal coordinates" + 
                            coordinates_x.get(j) + ", " + 
                            coordinates_y.get(j) + " and " + 
                            coordinates_x.get(k) + ", " + 
                            coordinates_y.get(k));
                }
            }
        }
        // (LK-)SOM-CH-NN-EVO is read, the algorithm starts if the input is ok.
        
        String inputresult = checkCoordinateInput(coordinates);
        if(inputresult.equals("ok") && max >=4 && EUC_2D && 
                !hasSameCoordinates && rightAmountOfNumbers) {
            System.gc(); // run garbage collector before starting 
            //String[] args = new String[1]; 
            //args[0] = "$ java -javaagent:tracker.jar TSP_Solver_UEF_241908 500 > /dev/null";
            //ResourceTracker.premain(args[0]);
            // ^ Comment if ResourceTracker.java is not used 
            long startTime = System.nanoTime();
            
            String hull = Sub_algorithms.ConvexHull(min, max, 
                    coordinates_x, coordinates_y);
            String replace1 = hull.replace("]], [[", "]],\n[[");
            String[] lines = replace1.split("\r\n|\r|\n");
            String replace2 = replace1.replace("[[[", "\tFrom (");
            String replace3 = replace2.replace("[[", "\tFrom (");
            String replace4 = replace3.replace("], [", ")    to    (");
            String replace5 = replace4.replace("]]]", ")");
            String replace6 = replace5.replace("]]", ")");
            String replace7 = replace6.replace("),", ")");
            String hullResult = "Convex hull connections: \n" + replace7 + 
                    "\nTotal " + lines.length + " connections. ";
            String replace8 = replace7.replace(",", ".");
            String replace9 = replace8.replace("(", "");
            String replace10 = replace9.replace(")", "");
            String replace11 = replace10.replace("\tFrom ", "");
            String replace12 = replace11.replace("    to    ", " ");
            String replace13 = replace12.replace(". ", " ");

            double[][][] edges = new double[max][2][2];
            for(int j = 0; j < max; j++) {
                edges[j][0][0] = Double.MAX_VALUE;
                edges[j][0][1] = Double.MAX_VALUE;
                edges[j][1][0] = Double.MAX_VALUE;
                edges[j][1][1] = Double.MAX_VALUE;
            }
            String rows2[] = new String[500000];    
            ArrayList values = new ArrayList();
            int a = 0;
            String str2;
            BufferedReader reader2 = new BufferedReader(new StringReader(replace13));
            try {
                while ((str2 = reader2.readLine()) != null) {
                    if (str2.length() > 0) {
                        rows2[a] = str2; 
                        if(rows2[a].charAt(0) == '0' || 
                                rows2[a].charAt(0) == '1' ||
                                rows2[a].charAt(0) == '2' || 
                                rows2[a].charAt(0) == '3' ||
                                rows2[a].charAt(0) == '4' || 
                                rows2[a].charAt(0) == '5' ||
                                rows2[a].charAt(0) == '6' || 
                                rows2[a].charAt(0) == '7' ||
                                rows2[a].charAt(0) == '8' || 
                                rows2[a].charAt(0) == '9' ||
                                rows2[a].charAt(0) == '-') {
                            // max will not be increased 
                            try {
                                Double numberInput;
                                int endIndex;
                                for (int beginIndex = 0; beginIndex < 
                                        rows2[a].length(); 
                                        beginIndex = endIndex + 1) {
                                    endIndex = rows2[a].indexOf(" ", beginIndex);
                                    if (endIndex == -1) {
                                        endIndex = rows2[a].length();
                                    }
                                    String numberString = rows2[a].substring(
                                            beginIndex, endIndex);
                                    try {
                                        numberInput = Double.valueOf(
                                                numberString);
                                        values.add(numberInput);   
                                    } 
                                    catch (java.lang.NumberFormatException nfe) {
                                        System.err.println(nfe);
                                    }
                                }
                            }
                            catch(Exception e) {
                                System.err.println(e);
                            }
                        } 
                    }
                }
            } 
            catch(IOException e) {
                System.err.println(e);
            }
            
            int temp_index = 0;
            for(int j = 0; j < values.size(); j++) {
                if(j == 0 || j%4 == 0) {
                    edges[temp_index][0][0] = (double)values.get(j);
                }
                if((j + 1)%4 == 0) {
                    edges[temp_index][1][1] = (double)values.get(j);
                    temp_index++;
                }
                if((j + 2)%4 == 0) {
                    edges[temp_index][0][1] = (double)values.get(j);
                }
                if((j + 3)%4 == 0) {
                    edges[temp_index][1][0] = (double)values.get(j);
                }
            }
            boolean[] circumferenceVertices = new boolean[max]; 

            int match_count = 0;
            for(int j = 0; j < max; j++) {
                for(int k = 0; k < max; k++) {
                    if(coordinates_x2.get(j).equals(edges[k][0][0]) && 
                       coordinates_y2.get(j).equals(edges[k][1][0]) && 
                       edges[k][0][0] < Double.MAX_VALUE && 
                       edges[k][1][0] < Double.MAX_VALUE) {
                        match_count++;
                        circumferenceVertices[j] = true;
                        coordinates_x2.set(j, Double.MAX_VALUE);
                        coordinates_y2.set(j, Double.MAX_VALUE);
                    }
                }
            } 
            //boolean circumferenceVertices[] currently knows what are in hull 
            int amountOfHullVertices = 0;
            for(int j = 0; j < max; j++) {
                //System.out.println(edges[j][0][0] + ", " + edges[j][1][0] + 
                //            "; " + edges[j][0][1] + ", " + edges[j][1][1] + 
                //            ";\t " + 
                //        coordinates_x2.get(j) + ", " + coordinates_y2.get(j) + 
                //        "; \tIN HULL? " + circumferenceVertices[j]);
                if((edges[j][0][0] < Double.MAX_VALUE) && 
                        (edges[j][1][0] < Double.MAX_VALUE) && 
                        (edges[j][0][1] < Double.MAX_VALUE) && 
                        (edges[j][1][1] < Double.MAX_VALUE)) {
                    amountOfHullVertices++;
                    System.out.println("amountOfHullVertices is " + amountOfHullVertices);
                }
            }
            
            // calculate the hull average points, in other words the hull edge middle or central points
            // calculate each point pair distance with 2 fors and find out the maxPossibleDistanceInHull;
            ArrayList hullPointsToApproach = new ArrayList();
            for(int j = 0; j < amountOfHullVertices; j++) {
                double averageX = (edges[j][0][0] + edges[j][0][1])/2;
                double averageY = (edges[j][1][0] + edges[j][1][1])/2;
                hullPointsToApproach.add(averageX);
                hullPointsToApproach.add(averageY); // xy xy xy xy...
            }
            // also add another set of points so there will be about twice as many clusters:
            
            // (comment the next 2 for loops if less clusters wanted) !! 
            
            for(int j = 0; j < (int)amountOfHullVertices/2; j++) {
                int index = j + ((int)amountOfHullVertices/2);
                double random = ((Math.random() * (7 - 3)) + 3);
                double randomX = (edges[j][0][0] + edges[index][0][1])/random;
                double randomY = (edges[j][1][0] + edges[index][1][1])/random;
                hullPointsToApproach.add(randomX+1);
                hullPointsToApproach.add(randomY+1); // still xy xy xy xy...
            }
            int index = 0;
            for(int j = (int)amountOfHullVertices/2; j < amountOfHullVertices; j++) {
                double random = ((Math.random() * (7 - 3)) + 3);
                double randomX = (edges[j][0][0] + edges[index][0][1])/random;
                double randomY = (edges[j][1][0] + edges[index][1][1])/random;
                hullPointsToApproach.add(randomX+1);
                hullPointsToApproach.add(randomY+1); // still xy xy xy xy...
                index++;
            }
            hullResult += "The SOM inputs are: \n";
            for(int j = 0; j < hullPointsToApproach.size()-1; j+=2) {
                hullResult += "\t" + hullPointsToApproach.get(j) + ", " + hullPointsToApproach.get(j+1) + "\n";
            }
            
            double totalApproachPoints = hullPointsToApproach.size()/2;
            amountOfHullVertices = (int) totalApproachPoints; // update "hull point" count 
                // because this time there are more input points! 
            System.out.println("Points to approach are " + hullPointsToApproach + 
                    ", total amount of points: " + totalApproachPoints);
            
            double maxPossibleDistanceInHullAvgs = 0;
            for(int j = 0; j < hullPointsToApproach.size(); j+=2) {
                for(int k = 0; k < hullPointsToApproach.size(); k+=2) {
                    double distance = Sub_algorithms.Euclidean_distance(
                                (double)hullPointsToApproach.get(j), (double)hullPointsToApproach.get(j+1), 
                                (double)hullPointsToApproach.get(k), (double)hullPointsToApproach.get(k+1));
                    if(distance > maxPossibleDistanceInHullAvgs) {
                        maxPossibleDistanceInHullAvgs = distance;
                    }
                }
            }
            System.out.println("maxPossibleDistanceInHull: " + maxPossibleDistanceInHullAvgs);
            // initialize the variables before SOM:
            ArrayList somResultArray;// = new ArrayList();
            ArrayList inputCoordinateXs = new ArrayList();
            ArrayList inputCoordinateYs = new ArrayList();
            ArrayList neuronCoordinateXs = new ArrayList();
            ArrayList neuronCoordinateYs = new ArrayList();
            ArrayList neuronCoordinateXsCopy = new ArrayList();
            ArrayList neuronCoordinateYsCopy = new ArrayList();
            
            for(int j = 0; j < circumferenceVertices.length; j++) {
                if(circumferenceVertices[j] == false) {
                    neuronCoordinateXs.add(coordinates_x2.get(j));
                    neuronCoordinateYs.add(coordinates_y2.get(j));
                    neuronCoordinateXsCopy.add(coordinates_x2.get(j));
                    neuronCoordinateYsCopy.add(coordinates_y2.get(j));
                }
            }
            
            for(int j = 0; j < coordinates_x2.size(); j++) {
                if(circumferenceVertices[j] == true) {
                    coordinates_x2.remove(j);
                    coordinates_y2.remove(j);
                    j--; // because removing decreases the array length 
                }
            }
            
            for(int j = 0; j < hullPointsToApproach.size()-1; j+=2) {
                if(j < hullPointsToApproach.size()-1) {
                    inputCoordinateXs.add(hullPointsToApproach.get(j));
                    inputCoordinateYs.add(hullPointsToApproach.get(j+1));
                }
            }
            
            int maxSomIterations = ((inputCoordinateXs.size()*2) +  // can be changed however wanted 
                    (int)(neuronCoordinateXs.size()/(0.08 * neuronCoordinateXs.size()))) +2;
            
            
            // The important variables for the (genetic algorithm) evolution phase! 
            ///// Improvement idea: ask the user these variables for evolution? 
            
            double percentRequirement = 1.12;   // when terminating, how many % from the best known optimum is 
                                                // allowed to survive the terminating? 1 = 0%, 1.05 = 5% etc. 
                                                
                                                
                                                // the Elastic Band Theorem should affect this percentRequirement variable 
                                                // higher than that means: "you cut the rubber band, terminating" 
                                                
            int wantedPopulationSize = 12; // at the start, can change later
            int wantedPopulationSizeOriginal = wantedPopulationSize;
            int wantedLogicStackSize = 10; // at the start, can change later
            int wantedEvolutionIterations = 3; // only "decreases" (for loop proceeds), always by 1
                // ^ generation count
            if(max >= 300) {
                wantedEvolutionIterations = 1;
                System.out.println("The graph is very large, iterating only for " + 
                        wantedEvolutionIterations + " generation(s). Remove the if statement ---if(max >= 300)---" 
                                + "from source code if real evolution is wanted.");
            }
            System.out.println(" Variables for evolution: \n"
                    + " Limit % of terminating: " + percentRequirement
                    + ", Population size: " + wantedPopulationSize
                    + ", Neuron logic stack size: " + wantedLogicStackSize
                    + ", Generations to iterate: " + wantedEvolutionIterations + "\n");
            
            double[] solutionArray = new double[(max +1)*2];
            String somSolution = "";
            List instances = new ArrayList(); // the population, very important List 
            List logicStackCopiesOfOriginals = new ArrayList();
            
            for(int p = 0; p < wantedPopulationSize; p++) {
                
                somResultArray = KohonenSOM.performSOM(maxSomIterations, maxPossibleDistanceInHullAvgs, 
                            inputCoordinateXs, inputCoordinateYs, 
                            neuronCoordinateXs, neuronCoordinateYs, false, null); // not yet using logic stacks and their fragments 
                //somSolution = "\nClusters after the Self-Organizing Map and evolution phases:" 
                //        + "\n\tNew position of X,   New position of Y,   CLUSTER ID \t"
                //        + "           Original X,   Original Y";
                // but only add when the last evo round is done 
                
                /**
                int helpindex = 0;
                for(int j = 0; j < somResultArray.size()-2; j+=3) {
                    somSolution = new StringBuilder(somSolution).append("\n\t" + somResultArray.get(j) + ", " + 
                            somResultArray.get(j+1) + ", in CLUSTER " + somResultArray.get(j+2)).toString();
                    if(helpindex < neuronCoordinateXsCopy.size()) {
                        somSolution = new StringBuilder(somSolution).append("\t\t" + neuronCoordinateXsCopy.get(helpindex) + 
                                ", " + neuronCoordinateYsCopy.get(helpindex)).toString();
                        helpindex++;
                    }
                }
                somSolution = new StringBuilder(somSolution).append("\nTotal " + 
                        neuronCoordinateXs.size() + " neuron points."
                                + " Chaining the clusters with Nearest Neighbor.").toString();
                **/
                
                // somResultArray has "x, y, cluster number"; "x, y, cluster number"; "x, y, cluster number" ... 
                // make NNH for each cluster (remember also the original inputs, in other words the hull vertices (not centrals)!)
                // then concatenate the sprouts (remember to proceed in the correct order, around each of the hull's "edge cluster"!)

                // Improvement idea: 1st do the nnh for the MOVED neuron vertices, 
                // THEN find out what they were originally 
                // (currently the new locations are ignored and the clustering just starts for originals)

                ArrayList nnh_purpose_coordinates = new ArrayList();
                ArrayList nnh_purpose_coordinates_x = new ArrayList();
                ArrayList nnh_purpose_coordinates_y = new ArrayList();
                ArrayList nnh_purpose_coordinates_clust = new ArrayList();

                for(int j = 0; j < amountOfHullVertices/2; j++) {           // changed to "/2"
                    nnh_purpose_coordinates.add((double)edges[j][0][0]);
                    nnh_purpose_coordinates.add((double)edges[j][1][0]);
                    nnh_purpose_coordinates.add(j);
                    //System.out.println("Hull: " + (double)edges[j][0][0] + ", " + (double)edges[j][1][0] + ", " + j);
                }
                int helpindex = 2;
                for(int j = 0; j < neuronCoordinateXsCopy.size(); j++) {
                    nnh_purpose_coordinates.add(neuronCoordinateXsCopy.get(j));
                    nnh_purpose_coordinates.add(neuronCoordinateYsCopy.get(j));
                    nnh_purpose_coordinates.add(somResultArray.get(helpindex));
                    //System.out.println("Neurons and clusters: " + neuronCoordinateXsCopy.get(j) + ", " + 
                    //        neuronCoordinateYsCopy.get(j) + ", " + somResultArray.get(helpindex));
                    helpindex +=3;
                }
                for(int j = 0; j < nnh_purpose_coordinates.size()-2; j+=3) {
                        nnh_purpose_coordinates_x.add(nnh_purpose_coordinates.get(j));
                        nnh_purpose_coordinates_y.add(nnh_purpose_coordinates.get(j+1));
                        nnh_purpose_coordinates_clust.add(nnh_purpose_coordinates.get(j+2));
                }
                //System.out.println("All are " + nnh_purpose_coordinates.toString());
                //System.out.println("Xs are " + nnh_purpose_coordinates_x.toString());
                //System.out.println("Ys are " + nnh_purpose_coordinates_y.toString());
                //System.out.println("Clusters are " + nnh_purpose_coordinates_clust.toString());
                // amountOfHullVertices and cluster count are equal 
                
                Random rand = new Random();
                int start = rand.nextInt((max - min) + 1) + min; // min 1 
                boolean[] booltable = new boolean[max];
                boolean booltable_has_false = true;
                int pointer = 1; 
                int pointer_start = start -1;
                int clusterInTurnNow = (int)nnh_purpose_coordinates_clust.get(pointer_start);

                //solution = new StringBuilder(solution).append(String.valueOf(start)).toString();
                solutionArray = new double[(max +1)*2];
                int solutionArrayInsertIndex = 0;
                for(int j = 0; j < max +1; j++) {
                            solutionArray[j] = Double.MAX_VALUE;
                }
                solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(pointer_start);
                solutionArrayInsertIndex++;
                solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(pointer_start);
                solutionArrayInsertIndex++;
                boolean verticeadded;// = true;
                booltable[pointer_start] = true;
                while(booltable_has_false) {
                    //System.out.println("Pointer start is " + pointer_start + 
                    //        ", pointer is (begin of while loop): " + pointer + 
                    //        ", the wanted cluster is " + clusterInTurnNow);
                    double min_distance = Double.MAX_VALUE;
                    double temp_distance;
                    double x1;
                    double y1;
                    x1 = (double) nnh_purpose_coordinates_x.get(pointer_start);
                    y1 = (double) nnh_purpose_coordinates_y.get(pointer_start);
                    double x2;
                    double y2;

                    for(int j = 0; j < booltable.length; j++) {
                        if((booltable[j] == false) 
                                 && (clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(j))
                                ) {
                            x2 = (double) nnh_purpose_coordinates_x.get(j);
                            y2 = (double) nnh_purpose_coordinates_y.get(j);
                            temp_distance = Sub_algorithms.
                                    Euclidean_distance_squared(x1, y1, x2, y2);
                            if((temp_distance < min_distance)) {
                                min_distance = temp_distance;
                                pointer = j;
                                //System.out.println("min distance is " + min_distance);
                            }
                        }
                    }

                    // new vertice to the result: 
                    if((booltable[pointer] == false) 
                            // && (clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(pointer)) 
                    ) {
                        booltable[pointer] = true;
                        //solution = new StringBuilder(solution).append("-").toString();
                        //solution = new StringBuilder(solution).append(pointer + 1).toString();
                        solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(pointer);
                        solutionArrayInsertIndex++;
                        solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(pointer);
                        solutionArrayInsertIndex++;
                        //System.out.println("ADDED with pointer " + pointer + ", cluster wanted: " + clusterInTurnNow);
                        verticeadded = true;
                    }
                    else {
                        //System.out.println("DID NOT ADD ANYTHING, pointer is " + pointer);
                        verticeadded = false;
                    }

                    boolean all_in_this_cluster_taken = true;
                    for(int j = 0; j < nnh_purpose_coordinates_clust.size(); j++) {
                        if((booltable[j] == false) && (
                                clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(j))) {
                                    all_in_this_cluster_taken = false;
                                    //System.out.println("NOT TAKEN YET: " + j + " in cluster " + clusterInTurnNow);
                                    j = nnh_purpose_coordinates_clust.size(); // end the loop 
                        }
                    }
                    //System.out.println("Booltable: " + Arrays.toString(booltable));
                    if(all_in_this_cluster_taken) {
                        //if(verticeadded) {
                            clusterInTurnNow++;
                            if(clusterInTurnNow >= amountOfHullVertices) {
                                clusterInTurnNow = 0;
                            }
                            //System.out.println("CLUSTER HANDLED, clusterInTurnNow changed to " + clusterInTurnNow);
                        //}
                    }

                    booltable_has_false = false;
                    for(int j = 0; j < booltable.length; j++) {
                        if(booltable[j] == false) {
                            booltable_has_false = true;
                        }
                    }
                    if(verticeadded) {
                        pointer_start = pointer;
                        //verticeadded = false;
                    }
                }
                // link back to the start node:
                solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(start-1);
                solutionArrayInsertIndex++;
                solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(start-1);
                solutionArrayInsertIndex++;
                //System.out.println("ADDED index (final) is " + (start-1));
                
                /*
                double tour_length = 0;
                for(int j = 0; j < solutionArray.length -2; j+=2) {
                    double distance = Sub_algorithms.Euclidean_distance(
                            solutionArray[j], 
                            solutionArray[j+1], 
                            solutionArray[j+2], 
                            solutionArray[j+3]);
                    //System.out.println("Added " + distance + " on j as " + j);
                    tour_length += distance;
                }
                */
                
                ArrayList arr = new ArrayList();
                for(int j = 0; j < solutionArray.length; j++) {
                    arr.add(solutionArray[j]);
                }
                System.out.println("\t\tNEW chromosome created: " + p + ", " + arr);
                Chromosome c = new Chromosome(String.valueOf(p), arr); // now a new chromosome has been created, 
                // its name is p (int but String), population = all instances of the class Chromosome 
                Logic_Stack ls = new Logic_Stack();
                for(int j = 0; j < wantedLogicStackSize; j++) {
                    int randomINT = (int) ((Math.random() * (Logic_Fragments.get_amount_of_existing_logic_fragments())) + 1);  
                    ls.push(randomINT);
                    System.out.println("\t\tLOGIC STACK PUSH before the evolution: " + randomINT);
                }
                c.setLogicStacks(ls);
                
                instances.add(c);
            }
            
            
            // continue the same computing but with "neuronLogicStacks true" and termination loop
            // -> improvements are made 
            for(int e = 0; e < wantedEvolutionIterations; e++) {
                for(int chro = 0; chro < wantedPopulationSize; chro++) {
                    
                    Chromosome tempC = (Chromosome) instances.get(chro);
                    logicStackCopiesOfOriginals.add(tempC.getStack());
                    
                    
                    somResultArray = KohonenSOM.performSOM(maxSomIterations, maxPossibleDistanceInHullAvgs, 
                                inputCoordinateXs, inputCoordinateYs, 
                                neuronCoordinateXs, neuronCoordinateYs, true, (Chromosome) instances.get(chro));
                    
                    //somSolution = "\nClusters after the Self-Organizing Map and evolution phases:" 
                    //        + "\n\tNew position of X,   New position of Y,   CLUSTER ID \t"
                    //        + "           Original X,   Original Y";
                    // but only add when the last evo round is done 

                    /**
                    int helpindex = 0;
                    for(int j = 0; j < somResultArray.size()-2; j+=3) {
                        somSolution = new StringBuilder(somSolution).append("\n\t" + somResultArray.get(j) + ", " + 
                                somResultArray.get(j+1) + ", in CLUSTER " + somResultArray.get(j+2)).toString();
                        if(helpindex < neuronCoordinateXsCopy.size()) {
                            somSolution = new StringBuilder(somSolution).append("\t\t" + neuronCoordinateXsCopy.get(helpindex) + 
                                    ", " + neuronCoordinateYsCopy.get(helpindex)).toString();
                            helpindex++;
                        }
                    }
                    somSolution = new StringBuilder(somSolution).append("\nTotal " + 
                            neuronCoordinateXs.size() + " neuron points."
                                    + " Chaining the clusters with Nearest Neighbor.").toString();
                    **/
                    // somResultArray has "x, y, cluster number"; "x, y, cluster number"; "x, y, cluster number" ... 
                    // make NNH for each cluster (remember also the original inputs, in other words the hull vertices (not centrals)!)
                    // then concatenate the sprouts (remember to proceed in the correct order, around each of the hull's "edge cluster"!)

                    // Improvement idea: 1st do the nnh for the MOVED neuron vertices, 
                    // THEN find out what they were originally 
                    // (currently the new locations are ignored and the clustering just starts for originals)

                    ArrayList nnh_purpose_coordinates = new ArrayList();
                    ArrayList nnh_purpose_coordinates_x = new ArrayList();
                    ArrayList nnh_purpose_coordinates_y = new ArrayList();
                    ArrayList nnh_purpose_coordinates_clust = new ArrayList();

                    for(int j = 0; j < amountOfHullVertices/2; j++) {           // changed to "/2" //CLUSTERS EDIT 
                        nnh_purpose_coordinates.add((double)edges[j][0][0]);
                        nnh_purpose_coordinates.add((double)edges[j][1][0]);
                        nnh_purpose_coordinates.add(j);
                        //System.out.println("Hull: " + (double)edges[j][0][0] + ", " + (double)edges[j][1][0] + ", " + j);
                    }
                    int helpindex = 2;
                    for(int j = 0; j < neuronCoordinateXsCopy.size(); j++) {
                        nnh_purpose_coordinates.add(neuronCoordinateXsCopy.get(j));
                        nnh_purpose_coordinates.add(neuronCoordinateYsCopy.get(j));
                        nnh_purpose_coordinates.add(somResultArray.get(helpindex));
                        //System.out.println("Neurons and clusters: " + neuronCoordinateXsCopy.get(j) + ", " + 
                        //        neuronCoordinateYsCopy.get(j) + ", " + somResultArray.get(helpindex));
                        helpindex +=3;
                    }
                    for(int j = 0; j < nnh_purpose_coordinates.size()-2; j+=3) {
                            nnh_purpose_coordinates_x.add(nnh_purpose_coordinates.get(j));
                            nnh_purpose_coordinates_y.add(nnh_purpose_coordinates.get(j+1));
                            nnh_purpose_coordinates_clust.add(nnh_purpose_coordinates.get(j+2));
                    }
                    //System.out.println("All are " + nnh_purpose_coordinates.toString());
                    //System.out.println("Xs are " + nnh_purpose_coordinates_x.toString());
                    //System.out.println("Ys are " + nnh_purpose_coordinates_y.toString());
                    //System.out.println("Clusters are " + nnh_purpose_coordinates_clust.toString());
                    // amountOfHullVertices and cluster count are equal 
                    
                    Random rand = new Random();
                    int start = rand.nextInt((max - min) + 1) + min; // min 1 
                    boolean[] booltable = new boolean[max];
                    boolean booltable_has_false = true;
                    int pointer = 1; 
                    int pointer_start = start -1;
                    int clusterInTurnNow = (int)nnh_purpose_coordinates_clust.get(pointer_start);

                    //solution = new StringBuilder(solution).append(String.valueOf(start)).toString();
                    solutionArray = new double[(max +1)*2];
                    int solutionArrayInsertIndex = 0;
                    for(int j = 0; j < max +1; j++) {
                                solutionArray[j] = Double.MAX_VALUE;
                    }
                    solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(pointer_start);
                    solutionArrayInsertIndex++;
                    solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(pointer_start);
                    solutionArrayInsertIndex++;
                    boolean verticeadded;// = true;
                    booltable[pointer_start] = true;
                    while(booltable_has_false) {
                        //System.out.println("Pointer start is " + pointer_start + 
                        //        ", pointer is (begin of while loop): " + pointer + 
                        //        ", the wanted cluster is " + clusterInTurnNow);
                        double min_distance = Double.MAX_VALUE;
                        double temp_distance;
                        double x1;
                        double y1;
                        x1 = (double) nnh_purpose_coordinates_x.get(pointer_start);
                        y1 = (double) nnh_purpose_coordinates_y.get(pointer_start);
                        double x2;
                        double y2;

                        for(int j = 0; j < booltable.length; j++) {
                            if((booltable[j] == false) 
                                     && (clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(j))
                                    ) {
                                x2 = (double) nnh_purpose_coordinates_x.get(j);
                                y2 = (double) nnh_purpose_coordinates_y.get(j);
                                temp_distance = Sub_algorithms.
                                        Euclidean_distance_squared(x1, y1, x2, y2);
                                if((temp_distance < min_distance)) {
                                    min_distance = temp_distance;
                                    pointer = j;
                                    //System.out.println("min distance is " + min_distance);
                                }
                            }
                        }

                        // new vertice to the result: 
                        if((booltable[pointer] == false) 
                                // && (clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(pointer)) 
                        ) {
                            booltable[pointer] = true;
                            //solution = new StringBuilder(solution).append("-").toString();
                            //solution = new StringBuilder(solution).append(pointer + 1).toString();
                            solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(pointer);
                            solutionArrayInsertIndex++;
                            solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(pointer);
                            solutionArrayInsertIndex++;
                            //System.out.println("ADDED with pointer " + pointer + ", cluster wanted: " + clusterInTurnNow);
                            verticeadded = true;
                        }
                        else {
                            //System.out.println("DID NOT ADD ANYTHING, pointer is " + pointer);
                            verticeadded = false;
                        }

                        boolean all_in_this_cluster_taken = true;
                        for(int j = 0; j < nnh_purpose_coordinates_clust.size(); j++) {
                            if((booltable[j] == false) && (
                                    clusterInTurnNow == (int)nnh_purpose_coordinates_clust.get(j))) {
                                        all_in_this_cluster_taken = false;
                                        //System.out.println("NOT TAKEN YET: " + j + " in cluster " + clusterInTurnNow);
                                        j = nnh_purpose_coordinates_clust.size(); // end the loop 
                            }
                        }
                        //System.out.println("Booltable: " + Arrays.toString(booltable));
                        if(all_in_this_cluster_taken) {
                            //if(verticeadded) {
                                clusterInTurnNow++;
                                if(clusterInTurnNow >= amountOfHullVertices) {
                                    clusterInTurnNow = 0;
                                }
                                //System.out.println("CLUSTER HANDLED, clusterInTurnNow changed to " + clusterInTurnNow);
                            //}
                        }

                        booltable_has_false = false;
                        for(int j = 0; j < booltable.length; j++) {
                            if(booltable[j] == false) {
                                booltable_has_false = true;
                            }
                        }
                        if(verticeadded) {
                            pointer_start = pointer;
                            //verticeadded = false;
                        }
                    }
                    // link back to the start node:
                    solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_x.get(start-1);
                    solutionArrayInsertIndex++;
                    solutionArray[solutionArrayInsertIndex] = (double) nnh_purpose_coordinates_y.get(start-1);
                    solutionArrayInsertIndex++;
                    //System.out.println("ADDED index (final) is " + (start-1));
                    
                    /*
                    double tour_length = 0;
                    for(int j = 0; j < solutionArray.length -2; j+=2) {
                        double distance = Sub_algorithms.Euclidean_distance(
                                solutionArray[j], 
                                solutionArray[j+1], 
                                solutionArray[j+2], 
                                solutionArray[j+3]);
                        //System.out.println("Added " + distance + " on j as " + j);
                        tour_length += distance;
                    }
                    */
                }
                
                
                // grown, now the fitness calculations:
                double wantedTourLength = EvolutionaryParts.calculateFitness(instances, percentRequirement); 
                // the percentRequirement can be edited, 1.2 means 20% from the known best one, 
                // 1.5 means 50% from the known best one and so on... 
                if(instances.size() > wantedPopulationSizeOriginal*1.3) {
                    // population is getting too large, stricter requirements 
                    wantedTourLength = wantedTourLength * 0.98;
                    System.out.println("\nA BIT STRICTER REQUIREMENTS because of overpopulating");
                }
                if(instances.size() > wantedPopulationSizeOriginal*1.6) {
                    // population is getting too large, stricter requirements 
                    wantedTourLength = wantedTourLength * 0.95;
                    System.out.println("\nVERY STRICT REQUIREMENTS because of overpopulating");
                }
                // terminating:
                for(int chro = 0; chro < instances.size(); chro++) {
                    Chromosome c = (Chromosome) instances.get(chro);
                    ArrayList c_coordinates = c.getChromosomePoints();
                    double eucDistOfChrom = 0;
                    for(int j = 0; j < c.getChromosomePoints().size()-2; j+=2) {
                        eucDistOfChrom += Sub_algorithms.Euclidean_distance((double)c_coordinates.get(j), 
                                                     (double)c_coordinates.get(j+1), 
                                                     (double)c_coordinates.get(j+2), 
                                                     (double)c_coordinates.get(j+3));
                    }
                    if((eucDistOfChrom > wantedTourLength) || 
                            ((Math.random() < 0.02) && (eucDistOfChrom*1.15 > wantedTourLength))) { 
                            // a chromosome can also be cruelly deleted because of this Math.random line 
                        if(instances.size() > 4) { // do not delete anything in the population if less than 5 
                            System.out.println("\nTERMINATING " + chro + " with length " + eucDistOfChrom);
                            instances.remove(chro);
                            logicStackCopiesOfOriginals.remove(chro);
                            chro--;
                            Chromosome.terminateChromosome(c);
                            System.out.println("Instances " + instances.toString() + "\nChromosome count is " + instances.size());
                        }
                    }
                }
                
                // select / pair / find a partner:
                // in other words, reorder the instance list a bit and add some new children:
                instances = EvolutionaryParts.selectWhatWillBePairedThenCrossingOverAndMutation(instances, logicStackCopiesOfOriginals);
                System.out.println("\nNEW GENERATION! Instances " + instances.toString() + "\nChromosome count is " + instances.size());
                wantedPopulationSize = instances.size();
                System.gc();
                
            }
            /* NOTES, MAIN-IDEA 
             * for each: grow
             * fitness calculations
             * terminating
             * for survivors: select (pair)
             * trade stack data (took a copy before popping all out)
             * mutate
             */
            
            // make sure that the best of the evolution computing is in "solutionArray":
            // also making the solution Strings:
            
            double tourLength;// = 0;
            double currentBest = Double.MAX_VALUE;
            for(int j = 0; j < instances.size(); j++) {
                ArrayList solutionArrayListTEMP = ((Chromosome)(instances.get(j))).getChromosomePoints();
                //LogicStack ls = ((Chromosome)(instances.get(j))).getStack();
                
                tourLength = 0;
                for(int k = 0; k < solutionArrayListTEMP.size()-2; k+=2) {
                    double distance = Sub_algorithms.Euclidean_distance(
                    (double)solutionArrayListTEMP.get(k), 
                    (double)solutionArrayListTEMP.get(k+1), 
                    (double)solutionArrayListTEMP.get(k+2), 
                    (double)solutionArrayListTEMP.get(k+3));
                    tourLength += distance;
                }
                if(tourLength < currentBest) {
                    currentBest = tourLength;
                    for(int k = 0; k < solutionArrayListTEMP.size(); k++) {
                        solutionArray[k] = (double)solutionArrayListTEMP.get(k);
                        /*
                        System.out.println("Choosing the best of the remaining population... new best has coord: " + 
                                solutionArray[k] + ", \t length: " + currentBest 
                                + " \tlogic stack instance copy ref: " + ls 
                            ); // an empty stack though in ls 
                            */
                    }
                }
            }
            // ^choosing the best of the remaining population
            
            
            if(callLinKernighanFinally) {
                System.out.println("Entering the Lin-Kernighan phase");
                ArrayList solutionArrayList = new ArrayList();
                for(int j = 0; j < solutionArray.length; j++) {
                    solutionArrayList.add(solutionArray[j]);
                }
                somSolution += "Trying to quick improve with Lin-Kernighan using 2opts.";
                double[][] answer = Sub_algorithms.linKernighan(max, currentBest, solutionArrayList);
                
                //System.out.println("LENGTHS " + solutionArray.length + ", " + answer.length);
                int insert_index = 0;
                for(int j = 0; j < solutionArray.length-2; j+=2) {
                    solutionArray[j] = answer[insert_index][0];
                    solutionArray[j+1] = answer[insert_index][1];
                    insert_index++;
                }
            }
            
            // Finally calculating the tour length and the solution: 
            double tour_length2 = 0;
            for(int j = 0; j < solutionArray.length -2; j+=2) {
                double distance = Sub_algorithms.Euclidean_distance(
                        solutionArray[j], 
                        solutionArray[j+1], 
                        solutionArray[j+2], 
                        solutionArray[j+3]);
                //System.out.println("Added " + distance + " on j as " + j);
                tour_length2 += distance;
            }
            if(tour_length2 < currentBest) {
                somSolution += " Improvements were found!";
            }
            
            //int firstNode = -1;
            //System.out.println("\tSOLUTION: " + Arrays.toString(solutionArray));
            //System.out.println("\tX: " + coordinates_x);
            //System.out.println("\tY: " + coordinates_y);
            //System.out.println("\tCoord: " + coordinates);
            for(int k = 0; k < solutionArray.length; k+=2) {
                for(int j = coordinates.size()-3; j > -1; j-=3) { 
                    if(coordinates.get(j+1).equals(solutionArray[k]) && 
                       coordinates.get(j+2).equals(solutionArray[k+1])) {
                        //System.out.println("Match: " + coordinates.get(j+1) + ", " + 
                        //        coordinates.get(j+2) + " & " + solutionArray[k] + ", " + solutionArray[k+1]);
                        if(solution.length() < 1) {
                            solution = new StringBuilder(solution).append(((int)(double)coordinates.get(j))).toString();
                            //firstNode = ((int)(double)coordinates.get(j));
                        }
                        else {
                            solution = new StringBuilder(solution).append("-").toString();
                            solution = new StringBuilder(solution).append(((int)(double)coordinates.get(j))).toString();
                        }
                    }
                }
            }
            
            boolean hamiltonian = checkHamiltonian(solution, max);
            
            if(hamiltonian) {
                long endTime = System.nanoTime();
                System.out.println("Time (millisec): " + (endTime - startTime)/1000000);
                System.out.println("Time (sec, rounded down): " + (endTime - startTime)/1000000000 + "\n");
                    return hullResult + somSolution + "\nSolution: \n" + solution + 
                        "\nTour length: " + tour_length2;
                
            }
            else {
                return "The calculated tour is not a Hamiltonian circuit!\n" + 
                        hullResult + somSolution + "\nSolution: \n" + solution + 
                        "\nTour length: " + tour_length2;
            }
        }
        else {
            return ERRORMSG;
        }
    }
} 