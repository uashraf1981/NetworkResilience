/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphpackageinterdiction;

import ilog.concert.IloException;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
public class Driver_Interdiction {
    public static Boolean useVariableLinkCapacity = Boolean.TRUE;
    public enum SolutionType {
                          CARD,
                          CARD_Greedy,
                          UpperLimit,
                          CARD_Fair,
                          CARD_Fair_Greedy,
                          MCMF,
                          DOMINATINGSET};
    public static float demands[];
    public static int numbOfNodesToBeInterdicted;
    public static int numbOfNodesToBeProtected;

    public static void main(String args[]) throws FileNotFoundException, IOException, IloException, InterruptedException, Exception{
        /*******    Variables    ****************/


        int k=30;
        String constraintFile = Globals_Interdiction.path+"constraintFile.lp";
        SolutionType option = SolutionType.DOMINATINGSET;  // MRMCA-FAIR-EXACT
        useVariableLinkCapacity = Boolean.TRUE;

         numbOfNodesToBeProtected=5; // specify your budget for installation

         numbOfNodesToBeInterdicted=5; // specify worst case scenario with no. of nodes damaged by adversary

         Graph_Interdiction myGraph = new Graph_Interdiction(Globals_Interdiction.path+"node100-1000by1000",k);
        GephiGraph_Interdiction gGraph = new GephiGraph_Interdiction(Globals_Interdiction.path+"simple.pdf");
        gGraph.printGraphToPDF(myGraph);
        ConstraintGenerator_Interdiction constraintGenerator =new ConstraintGenerator_Interdiction(constraintFile,
                myGraph,option,numbOfNodesToBeProtected,numbOfNodesToBeInterdicted, gGraph);
        gGraph.printGraphToPDFAndKillDeadNodes(myGraph);
        gGraph.printGraphToPDFAndKillDeadNodes(myGraph);

     }

}
