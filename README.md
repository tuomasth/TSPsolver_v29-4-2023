# TSPsolver_v29-4-2023 
TSP Solver with 8 algorithms (combinations do exist), 
Traveling Salesman Problem Solver

F2, NNH,               Nearest neighbour as the simplest heuristic there exists 
 
F3, 2MST,              Using minimum spanning tree's doubled edges and Euler tour 
 
F4, CHH,               Convex hull around everything and connect the inner nodes one by one 
 
F5, CHRI,              Christofides heuristic, using minimum spanning tree and its odd degree node matching and Euler tour 
 
F6, LK-NNH-CHH-CHRI,   F2 for a couple of times, F4 once, F5 once, choose the best and try to improve with a quick 2opt-Lin-Kernighan 
 
F7, SOM-CH-NN,         Calculate the convex hull so its nodes (or edge centroids) can be the input nodes and clusters, then 
                      the inner nodes are movable neurons that perform the Kohonen Self-Organizing Map algorithm, finally 
                      each cluster performs the NNH which chains everything and creates the Hamiltonian circuit 
 
F8, SOM-CH-NN-EVO,     Same as F7 but with a bit more clusters and evolution is used, the population consists of 
                      chromosomes (multiple F7 results), the movable neurons also have logic stacks that tell what to do 
                      after moving towards the SOM goal, the programmer can make his/her own logic fragments to the stacks 
 
F9, LK-SOM-CH-NN-EVO,  Same as F8 but the F6's quick Lin-Kernighan is used in the end once 

Javadoc is not available on GitHub for this version. This version is only an experiment and should not be taken seriously yet like the next upcoming version should.
