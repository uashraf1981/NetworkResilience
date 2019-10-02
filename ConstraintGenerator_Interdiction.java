/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphpackageinterdiction;

import ilog.cplex.IloCplex.UnknownObjectException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.nio.*;
import java.util.Iterator;
import java.util.LinkedList;
import ilog.concert.*;
import ilog.cplex.*;
import java.io.*;
import java.util.Iterator;
/**
 *
 * @author Administrator
 */
public class ConstraintGenerator_Interdiction {

    private IloNumVar [] variables;
    private Graph_Interdiction g;
    private String constraintFile;
    private File file;
    private Scanner input;
    private BufferedWriter writer;
    private int constraintNumb;
    private int [] covered;
    private int numbOfNodesToBeInterdicted;
    private int numbOfNodesToBeProtected;
    private float Phi;
    private float bestResult;
    private int lastIndex;
    private int currIndex;
    private String [] xVariables; // stores the list of X Variables in order to call //
    private int [] xVariablesConstraintNumb; // stores the list of X Variables in order to call //
    private String [] fVariables; // stores the list of F Variables in order to call //
    private int [] fVariablesConstraintNumb; // stores the list of F Variables in order to call //
    private double totalTime;
    public int [] previouslySelected;
    int constraintNumbOfXisExactlyOne;
    int [][] constraintNumbInfoHolder;
    CplexSolver_Interdiction myAlgoCplex;
    long globalStart;



    ConstraintGenerator_Interdiction(String constFile, Graph_Interdiction givenGraph, Driver_Interdiction.SolutionType sol, int P, int Q, GephiGraph_Interdiction gGraph) throws FileNotFoundException, IOException, Exception{


        numbOfNodesToBeProtected=P;
        numbOfNodesToBeInterdicted=Q;
        lastIndex=0;
        currIndex=0;
        totalTime=0;
        g = givenGraph;
        Globals_Interdiction.nodeIsAlive = new int [g.getNofNodes()];
        variables = new IloNumVar [g.getNofNodes()];
        constraintNumb=0;
        Phi=0;
        covered = new int[g.getNofNodes()];
        for(int i=0; i < g.getNofNodes();i++)
        {
           covered[i]=0;
           Globals_Interdiction.nodeIsAlive[i]=1;
        }
        constraintFile = constFile;
        file = new File(constFile);
        writer = new BufferedWriter(new FileWriter(file));
        input = new Scanner(file);
        writer.write("\\ Optimization Problem with Constraints\n");

        /* make calls to different methods */

        /************************-------------------------------****************************/
 /************************-------------------------------****************************/
 /*************************************************************************/
     // MAXIMUM FLOW FORTIFICATION GREEDY - I
      if(sol == Driver_Interdiction.SolutionType.DOMINATINGSET)
      {
          globalStart = System.currentTimeMillis();
          myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;


          // First do all the writing stuff //
          this.objFunctionUnknownDemandsSingleRadio();
          this.flowConservConstraintsSingleRadio();
          //this.constraintNoFlowOutOfDestination();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
          //this.cliqueConstraints();
          this.writeBinaryVariablesForFortification();
          writer.write("End");
          writer.close();





         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;


       long startTime = System.currentTimeMillis();
       myAlgoCplex.createModelOnce(constFile);
       myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());

       originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);

       int [] nodesFortified=myAlgoCplex.solveGreedy1forMaxFlow(g,numbOfNodesToBeProtected, this);
       System.out.println("#########  PASSED GREEDY MARK  ##########");

           myAlgoCplex.fixNodesForInterdiction(previouslySelected);

         System.out.println();

         double currThroughput = myAlgoCplex.solveProblem(Boolean.TRUE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - globalStart;
         System.out.println("****************    RESULTS  GREEDY  *******************");
         System.out.println("Original Throughput = "+(originalThroughput/1024)+" Mbps");
         System.out.println("Current Throughput = "+(currThroughput/1024)+" Mbps");
         System.out.println("Total time = "+duration*0.001+" seconds");

          solveOutermostOptimizationProblem(previouslySelected,originalThroughput,currThroughput, Driver_Interdiction.SolutionType.CARD_Greedy);
         myAlgoCplex.printToFile();
         myAlgoCplex.closeFile();
       }
      //////////////////////////////////////////////////////////////////////////////////////////

     /* MULTICOMMODITY_MAXIMUM_FLOW_FORTIFICATION_EXACT */
     if(sol == Driver_Interdiction.SolutionType.CARD)
      {
          globalStart = System.currentTimeMillis();
          myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;

         xVariables = new String [this.getNoOfCapacityConstraints()];
         xVariablesConstraintNumb = new int [this.getNoOfCapacityConstraints()];
          // First do all the writing stuff //
          this.objFunctionUnknownDemandsSingleRadio();
          this.flowConservConstraintsSingleRadio();
          //this.constraintNoFlowOutOfDestination();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
          this.cliqueConstraints();
          this.writeBinaryVariablesForFortification();

          //this.capacityConstraints();
          //this.nonNegativityConstraints();
          writer.write("End");
          writer.close();
         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;

       long startTime = System.currentTimeMillis();


       for(int nodeCount=0; nodeCount < numbOfNodesToBeInterdicted; nodeCount++)
       {

         bestResult=1000000;
         bestIndex=0;
         bestVariable="";
         lastIndex = -1;

         // Call once to get the basic solution //
         myAlgoCplex.createModelOnce(constFile);
         myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());
         if(nodeCount==0)
         {
           originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
           System.out.println("Original Throughput = "+originalThroughput);
         }
         // Repeast as many times as there are nodes in the network //
         for(int i=0; i < g.getNofNodes(); i++)
         {
            currNode = i;
            if(previouslySelected[i] == 0 && isSourceOrDestination(currNode)==Boolean.FALSE)
            {
                result = myAlgoCplex.fixInterdictionVariablesAndSolve(currNode);
                currIndex = i;
                if(result < bestResult)
                {
                    bestResult = result;
                    bestVariable = "x_"+currNode;
                    bestIndex = i;
                }

                lastIndex = currIndex;
                count++;
                System.out.println("+++++++++++++++++++++  SOlutions tried: "+count);
                totalTime+=myAlgoCplex.getTimeConsumed();
          }
         }

         if(nodeCount < numbOfNodesToBeProtected && isSourceOrDestination(bestIndex)==Boolean.FALSE)
           {
             previouslySelected[bestIndex]=1;
           }

          } // end of number of nodes to be interdicted
          myAlgoCplex.fixAllSelectedIndicesForInterdiction(previouslySelected);
         /* Put result stats */


         System.out.println();
         System.out.println("****************    RESULTS    *******************");
         double currThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - startTime;
         System.out.println("Original Throughput = "+originalThroughput);
         System.out.println("Current Throughput = "+currThroughput);
         System.out.println("Total time = "+totalTime*0.001+" seconds");

         /********************************************************************************/
         /********************************************************************************/
         /********************************************************************************/
         System.out.println("#########   FORTIFYING   #########");
         solveOutermostOptimizationProblem(previouslySelected,originalThroughput,currThroughput, Driver_Interdiction.SolutionType.CARD);

         myAlgoCplex.printToFile();
         myAlgoCplex.closeFile();
       }
     /*************************************************************************/

     /* Standard MAximimum Multiflow i.e. inner optimization problem, not multi-level and it just selects
      the priority nodes without calculating the upper optimization problems, resultingin sub-optimal
      results */
     if(sol == Driver_Interdiction.SolutionType.UpperLimit)
      {
          globalStart = System.currentTimeMillis();
          myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;

         xVariables = new String [this.getNoOfCapacityConstraints()];
         xVariablesConstraintNumb = new int [this.getNoOfCapacityConstraints()];
          // First do all the writing stuff //
          this.objFunctionUnknownDemandsSingleRadio();
          this.flowConservConstraintsSingleRadio();
          //this.constraintNoFlowOutOfDestination();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
          //this.cliqueConstraints();
          this.writeBinaryVariablesForFortification();

          //this.capacityConstraints();
          //this.nonNegativityConstraints();
          writer.write("End");
          writer.close();
         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;

       long startTime = System.currentTimeMillis();
       //myAlgoCplex.createModelOnce(constFile);
       //myAlgoCplex.solveProblem();
       //originalCapacity = myAlgoCplex.cplex.getObjValue();
       //System.out.println("Original Capacity: "+originalCapacity);

       for(int nodeCount=0; nodeCount < numbOfNodesToBeInterdicted; nodeCount++)
       {

         bestResult=100000;
         bestIndex=0;
         bestVariable="";
         lastIndex = -1;

         // Call once to get the basic solution //
         myAlgoCplex.createModelOnce(constFile);
         myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());
         if(nodeCount==0)
         {
           originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
           System.out.println("Original Throughput = "+originalThroughput);
         }
         // Repeast as many times as there are nodes in the network //
         for(int i=0; i < g.getNofNodes(); i++)
         {
            currNode = i;
            if(previouslySelected[i] == 0 && isSourceOrDestination(currNode)==Boolean.FALSE)
            {
                result = myAlgoCplex.fixInterdictionVariablesAndSolve(currNode);
                currIndex = i;
              //  if(result < bestResult)
              //  {
                    bestResult = result;
                    bestVariable = "x_"+currNode;
                    bestIndex = i;
              //  }

                lastIndex = currIndex;
                count++;
                System.out.println("+++++++++++++++++++++  SOlutions tried: "+count);
                totalTime+=myAlgoCplex.getTimeConsumed();
          }
         }

          if(nodeCount < numbOfNodesToBeProtected && isSourceOrDestination(bestIndex)==Boolean.FALSE)
           {
            previouslySelected[bestIndex]=1;

           }

          } // end of number of nodes to be interdicted

         /* Put result stats */
         myAlgoCplex.fixAllSelectedIndicesForInterdiction(previouslySelected);

         System.out.println();
         System.out.println("****************    RESULTS    *******************");
         double currThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - startTime;
         System.out.println("Original Throughput = "+originalThroughput);
         System.out.println("Current Throughput = "+currThroughput);
         System.out.println("Total time = "+totalTime*0.001+" seconds");

         /********************************************************************************/
         /********************************************************************************/
         /********************************************************************************/
         System.out.println("#########   FORTIFYING   #########");
         solveOutermostOptimizationProblem(previouslySelected,originalThroughput,currThroughput,Driver_Interdiction.SolutionType.UpperLimit);

         myAlgoCplex.printToFile();
         myAlgoCplex.closeFile();
       }
     /*************************************************************************/

     /*************************************************************************/
     // MAXIMUM FLOW FORTIFICATION GREEDY - I
      if(sol == Driver_Interdiction.SolutionType.CARD_Greedy)
      {
          globalStart = System.currentTimeMillis();
          myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;


          // First do all the writing stuff //
          this.objFunctionUnknownDemandsSingleRadio();
          this.flowConservConstraintsSingleRadio();
          //this.constraintNoFlowOutOfDestination();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
          //this.cliqueConstraints();
          this.writeBinaryVariablesForFortification();
          writer.write("End");
          writer.close();





         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;


       long startTime = System.currentTimeMillis();
       myAlgoCplex.createModelOnce(constFile);
       myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());

       originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);

       int [] nodesFortified=myAlgoCplex.solveGreedy1forMaxFlow(g,numbOfNodesToBeProtected, this);
       System.out.println("#########  PASSED GREEDY MARK  ##########");
      //  double currThroughput = myAlgoCplex.solveProblem();
       //myAlgoCplex.createModelOnce(constFile);
       //myAlgoCplex.solveProblem();
       //originalCapacity = myAlgoCplex.cplex.getObjValue();
       //System.out.println("Original Capacity: "+originalCapacity);

       for(int nodeCount=0; nodeCount < numbOfNodesToBeInterdicted; nodeCount++)
       {
         System.out.println("node count is: "+nodeCount);
         bestResult=100000;
         bestIndex=0;
         bestVariable="";
         lastIndex = -1;

         // Call once to get the basic solution //
         myAlgoCplex.createModelOnce(constFile);
         myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());
         if(nodeCount==0)
         {
           originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
           System.out.println("Original Throughput = "+originalThroughput);
         }
         // Repeast as many times as there are nodes in the network //


         for(int i=0; i < g.getNofNodes(); i++)
         {
            currNode = i;
            System.out.println("currNode: "+currNode);
            if(previouslySelected[i] == 0 && isSourceOrDestination(currNode)==Boolean.FALSE && nodesFortified[i]==0)
            {
                result = myAlgoCplex.fixInterdictionVariablesAndSolve(currNode);
                currIndex = i;
                if(result < bestResult)
                {
                    bestResult = result;
                    bestVariable = "x_"+currNode;
                    bestIndex = i;
                }

                lastIndex = currIndex;
                count++;
                System.out.println("+++++++++++++++++++++  SOlutions tried: "+count);
                totalTime+=myAlgoCplex.getTimeConsumed();
          }
         }

          if(nodeCount < numbOfNodesToBeProtected && isSourceOrDestination(bestIndex)==Boolean.FALSE)
           {
             previouslySelected[bestIndex]=1;
           }

          } // end of number of nodes to be interdicted

         // Put result stats //


         myAlgoCplex.fixAllSelectedIndicesForInterdiction(previouslySelected);

         System.out.println();

         double currThroughput = myAlgoCplex.solveProblem(Boolean.TRUE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - globalStart;
         System.out.println("****************    RESULTS  GREEDY  *******************");
         System.out.println("Original Throughput = "+(originalThroughput/1024)+" Mbps");
         System.out.println("Current Throughput = "+(currThroughput/1024)+" Mbps");
         System.out.println("Total time = "+duration*0.001+" seconds");

          solveOutermostOptimizationProblem(previouslySelected,originalThroughput,currThroughput, Driver_Interdiction.SolutionType.CARD_Greedy);
         myAlgoCplex.printToFile();
         myAlgoCplex.closeFile();
       }












      /*^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^*/
      /*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/

      /*************************************************************************/
     /*************************************************************************/
     // MAXIMUM FLOW FORTIFICATION GREEDY - II
      if(sol == Driver_Interdiction.SolutionType.CARD_Fair_Greedy)
      {
          globalStart = System.currentTimeMillis();
          myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;


          this.objFunctionFairness();
          this.fairnessConstraints();
          //this.flowConservConstraintsSingleRadio();
          this.flowConservConstraintsForFairness();
          //this.constraintNoFlowOutOfDestination();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
          //this.cliqueConstraints();
          this.writeBinaryVariablesForFortification();
          writer.write("End");
          writer.close();

         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;


       long startTime = System.currentTimeMillis();
       myAlgoCplex.createModelOnce(constFile);
       myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());

       originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);

       int [] nodesFortified=myAlgoCplex.solveGreedy_2_forMaxFlow(g,numbOfNodesToBeProtected);
       System.out.println("#########  PASSED GREEDY MARK  ##########");
      //  double currThroughput = myAlgoCplex.solveProblem();
       //myAlgoCplex.createModelOnce(constFile);
       //myAlgoCplex.solveProblem();
       //originalCapacity = myAlgoCplex.cplex.getObjValue();
       //System.out.println("Original Capacity: "+originalCapacity);

       for(int nodeCount=0; nodeCount < numbOfNodesToBeInterdicted; nodeCount++)
       {

         bestResult=100000;
         bestIndex=0;
         bestVariable="";
         lastIndex = -1;

         // Call once to get the basic solution //
         //myAlgoCplex.createModelOnce(constFile);
         //myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());
         //if(nodeCount==0)
         //{
         //  originalThroughput = myAlgoCplex.solveProblem();
         //  System.out.println("Original Throughput = "+originalThroughput);
         //}
         // Repeast as many times as there are nodes in the network //
         for(int i=0; i < g.getNofNodes(); i++)
         {
            currNode = i;
            if(previouslySelected[i] == 0 && isSourceOrDestination(currNode)==Boolean.FALSE && nodesFortified[i]==0)
            {
                result = myAlgoCplex.fixInterdictionVariablesAndSolve(currNode);
                currIndex = i;
                if(result < bestResult)
                {
                    bestResult = result;
                    bestVariable = "x_"+currNode;
                    bestIndex = i;
                }

                lastIndex = currIndex;
                count++;
                System.out.println("+++++++++++++++++++++  SOlutions tried: "+count);
                totalTime+=myAlgoCplex.getTimeConsumed();
          }
         }

           previouslySelected[bestIndex]=1;
           myAlgoCplex.fixAllSelectedIndicesForInterdiction(previouslySelected);

          } // end of number of nodes to be interdicted

         // Put result stats //


         System.out.println();
         System.out.println("****************   FINAL RESULTS for FAIR GREEDY    *******************");
         double currThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - globalStart;

         System.out.println("Original Throughput = "+originalThroughput);
         System.out.println("Current Throughput = "+currThroughput);
         System.out.println("Ratio of Flows Satisfied = "+(currThroughput/Driver_Interdiction.demands[0]));
         System.out.println("Total time = "+duration*0.001+" seconds");


         myAlgoCplex.printToFile();
         myAlgoCplex.closeFile();
       }




      /****************************************************************/
      /****************************************************************/


       /* MULTICOMMODITY_CONCURRENT_FLOW_FORTIFICATION_EXACT */
     if(sol == Driver_Interdiction.SolutionType.CARD_Fair)
      {
          globalStart = System.currentTimeMillis();
          myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;

         xVariables = new String [this.getNoOfCapacityConstraints()];
         xVariablesConstraintNumb = new int [this.getNoOfCapacityConstraints()];



          System.out.println("calling objective function fairness");
          this.objFunctionFairness();
          this.fairnessConstraints();
          //this.flowConservConstraintsSingleRadio();
          this.flowConservConstraintsForFairness();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
          //this.cliqueConstraints();
          this.writeBinaryVariablesForFortification();
          //this.capacityConstraints();
          //this.PhiBounds();

          //this.capacityConstraints();
          //this.nonNegativityConstraints();
          writer.write("End");
          writer.close();
         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;

       long startTime = System.currentTimeMillis();
       //myAlgoCplex.createModelOnce(constFile);
       //myAlgoCplex.solveProblem();
       //originalCapacity = myAlgoCplex.cplex.getObjValue();
       //System.out.println("Original Capacity: "+originalCapacity);

       for(int nodeCount=0; nodeCount < numbOfNodesToBeInterdicted; nodeCount++)
       {

         bestResult=100000;
         bestIndex=0;
         bestVariable="";
         lastIndex = -1;

         // Call once to get the basic solution //
         myAlgoCplex.createModelOnce(constFile);
         myAlgoCplex.setNumbOfLinkVariables((g.getNofEdges()/2)*g.getK(),g.getNofNodes());
         if(nodeCount==0)
         {
           originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);

         }
         // Repeast as many times as there are nodes in the network //
         for(int i=0; i < g.getNofNodes(); i++)
         {
            currNode = i;
            if(previouslySelected[i] == 0 && isSourceOrDestination(currNode)==Boolean.FALSE)
            {
                result = myAlgoCplex.fixInterdictionVariablesAndSolve(currNode);
                currIndex = i;
                if(result < bestResult)
                {
                    bestResult = result;
                    bestVariable = "x_"+currNode;
                    bestIndex = i;
                }

                lastIndex = currIndex;
                count++;
                System.out.println("+++++++++++++++++++++  SOlutions tried: "+count);
                totalTime+=myAlgoCplex.getTimeConsumed();
          }
         }

           previouslySelected[bestIndex]=1;
           myAlgoCplex.fixAllSelectedIndicesForInterdiction(previouslySelected);

          } // end of number of nodes to be interdicted

         // Put result stats //


         System.out.println();
         System.out.println("****************    RESULTS    *******************");
         double currThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - startTime;
         System.out.println("Original Throughput = "+originalThroughput);
         System.out.println("Current Throughput = "+currThroughput);
         System.out.println("Total time = "+totalTime*0.001+" seconds");
         //System.out.println("Ratio: "+calcMinimumRatioOfFlowsSatisfied(myAlgoCplex));
         /********************************************************************************/
         /********************************************************************************/
         /********************************************************************************/
         System.out.println("#########   FORTIFYING   #########");
         solveOutermostOptimizationProblemForFairness(previouslySelected,originalThroughput,currThroughput);


         myAlgoCplex.printToFile();
         myAlgoCplex.closeFile();
         writer.close();
       }





}

    /******************   JAIN FAIRNESS INDEX  *************************/
public double jainFairnessIndex(CplexSolver_Interdiction myAlgoCplex) throws UnknownObjectException, IloException
{
    double[] sources = new double [g.getNofDestinations()];
    double[] flows = new double [g.getNofDestinations()];

    for(int i=0; i < sources.length; i++)
    {
        sources[i] = g.getSourceId(i);
    }

    int destIndex=0;
    String currVariable,temp;
    String [] nodes = new String[2];
    String name;

    double[] x = myAlgoCplex.cplex.getValues(myAlgoCplex.lp);
    for (int i = 0; i < x.length; i++)
    {
          name = myAlgoCplex.lp.getNumVar(i).getName();
       if(x[i] > 0 && myAlgoCplex.lp.getNumVar(i).getType()!=IloNumVarType.Bool &&
              "P".equals(name)==Boolean.FALSE && "R".equals(name.charAt(0))==Boolean.FALSE)
         /* extract the destination part of the variable */
       {
           currVariable = myAlgoCplex.lp.getNumVar(i).getName();


         int leftbrace = currVariable.indexOf("(");
         int rightbrace = currVariable.indexOf(")");
         temp = currVariable.substring(leftbrace+1,rightbrace);
         nodes = temp.split("~");

        for(int j=0; j < sources.length; j++)
        {
            //System.out.println("nodes[1] = "+nodes[1]);
            //System.out.println("destinations[j] = "+destinations[j]);
            if(Integer.parseInt(nodes[0])==sources[j])
            {
               //System.out.println("CCCVariable: "+myAlgoCplex.lp.getNumVar(i).getName()+"; Value = " + x[i]);
               flows[j] += x[i];
            }
        }

       }
    }

    /* Now array flows contains all the flows */
    double sum = 0, sumSquare=0;
    for(int i=0; i < flows.length; i++)
    {
       sum += flows[i];
       sumSquare += (flows[i]*flows[i]);
    }

    double fairnessIndex = (sum*sum)/(flows.length*sumSquare);
    System.out.println("Jain Fairness Index = "+fairnessIndex);
    return fairnessIndex;
}

public double calcMinimumRatioOfFlowsSatisfied(CplexSolver_Interdiction myAlgoCplex) throws UnknownObjectException, IloException
{

    /* Create an array to store the values of flows. The size should be equal to numb. of source/dest. pairs */
    double[] sources = new double [g.getNofSources()];
    double[] flows = new double [g.getNofDestinations()];

    for(int i=0; i < sources.length; i++)
    {
        sources[i] = g.getSourceId(i);
    }

    int destIndex=0;
    String currVariable,temp;
    String [] nodes = new String[2];
    String name="";

    double[] x = myAlgoCplex.cplex.getValues(myAlgoCplex.lp);
    for (int i = 0; i < x.length; i++)
    {
        name = myAlgoCplex.lp.getNumVar(i).getName();
       if(x[i] > 0 && myAlgoCplex.lp.getNumVar(i).getType()!=IloNumVarType.Bool &&
                 "P".equals(name)==Boolean.FALSE && "R".equals(name.charAt(0))==Boolean.FALSE)
            //  "P".equals(name)==Boolean.FALSE && "R".equals(name.charAt(0))==Boolean.FALSE)
       {
         /* extract the destination part of the variable */
         currVariable = myAlgoCplex.lp.getNumVar(i).getName();

         int rightCurlyBracket = currVariable.indexOf("}");
         int leftbrace = currVariable.indexOf("(");
         int rightbrace = currVariable.indexOf(")");
         System.out.println("currVariable: in calc "+currVariable);
         String kk = currVariable.substring(rightCurlyBracket-1,rightCurlyBracket);
         int K = Integer.parseInt(kk);
         temp = currVariable.substring(leftbrace+1,rightbrace);
         nodes = temp.split("~");

        for(int j=0; j < sources.length; j++)
        {
            //System.out.println("nodes[1] = "+nodes[1]);
            //System.out.println("destinations[j] = "+destinations[j]);
            if(Integer.parseInt(nodes[0])==sources[j] && j==K)
            {
               //System.out.println("Adding to flow["+j+"]: "+x[i]+"current constraint is: "+myAlgoCplex.lp.getNumVar(i).getName());
               flows[j] += x[i];
            }
        }

       }
    }

    double largestRatio=20000;
    for(int i=0; i < sources.length; i++)
    {
        //System.out.println("Flow out of Source "+sources[i]+" = "+flows[i]);
        if(flows[i] < largestRatio)
        {
            largestRatio = flows[i];

        }

    }

    return largestRatio/Driver_Interdiction.demands[0];
}

/************************   Throughput for fair case */
public double getThroughputForFairCase(CplexSolver_Interdiction myAlgoCplex) throws UnknownObjectException, IloException
{
    double[] sources = new double [g.getNofDestinations()];
    double[] flows = new double [g.getNofDestinations()];

    for(int i=0; i < sources.length; i++)
    {
        sources[i] = g.getSourceId(i);
    }

    int destIndex=0;
    String currVariable,temp;
    String [] nodes = new String[2];
    String name;

    double[] x = myAlgoCplex.cplex.getValues(myAlgoCplex.lp);
    for (int i = 0; i < x.length; i++)
    {
          name = myAlgoCplex.lp.getNumVar(i).getName();
       if(x[i] > 0 && myAlgoCplex.lp.getNumVar(i).getType()!=IloNumVarType.Bool &&
              "P".equals(name)==Boolean.FALSE && "R".equals(name.charAt(0))==Boolean.FALSE &&
               "x".equals(String.valueOf(name.charAt(0)))==Boolean.FALSE && "z".equals(String.valueOf(name.charAt(0)))==Boolean.FALSE)
         /* extract the destination part of the variable */
       {
           currVariable = myAlgoCplex.lp.getNumVar(i).getName();

          int rightCurlyBracket = currVariable.indexOf("}");
          String kk = currVariable.substring(rightCurlyBracket-1,rightCurlyBracket);
         int K = Integer.parseInt(kk);
         int leftbrace = currVariable.indexOf("(");
         int rightbrace = currVariable.indexOf(")");
         temp = currVariable.substring(leftbrace+1,rightbrace);
         nodes = temp.split("~");

        for(int j=0; j < sources.length; j++)
        {
            //System.out.println("nodes[1] = "+nodes[1]);
            //System.out.println("destinations[j] = "+destinations[j]);
            if(Integer.parseInt(nodes[0])==sources[j] && j==K)
            {
               //System.out.println("CCCVariable: "+myAlgoCplex.lp.getNumVar(i).getName()+"; Value = " + x[i]);
               flows[j] += x[i];
            }
        }

       }
    }

    /* Now array flows contains all the flows */
    double sum = 0;
    for(int i=0; i < flows.length; i++)
    {
       sum += flows[i];
    }
    return sum;
}

/**************************************************************/
public double remainingCapacity(String variable)
{
   System.out.println("vairable ISS :  "+variable);
   String [] twonodes;
   /* First, get the variable */
   int leftbrace = variable.indexOf("(");
   int rightbrace = variable.indexOf(")");
   String temp = variable.substring(leftbrace+1,rightbrace);
   System.out.println("temp1 = "+temp);
   twonodes = temp.split("~");
   System.out.println("array length = "+twonodes.length);
   if(twonodes.length != 2)
       System.out.println("NOTE: ************************************* Length not 2");
   return g.getCliqueRemainingCapcity(Integer.parseInt(twonodes[0]),Integer.parseInt(twonodes[1]));
}

/**************************************************************/
/**************************************************************/
public int getConstraintNumbAgainstVariableName(String variable)
{
    for(int i=0; i < this.getNoOfCapacityConstraints(); i++)
    {
        //System.out.println("variable= "+variable+"   xVariables["+i+"] = "+xVariables[i]);
        if(variable.equals(xVariables[i])==Boolean.TRUE)
        {
           return  xVariablesConstraintNumb[i];
        }
    }

    System.out.println("*************************ERROR*****************************************************");
    return -1;
}


/*************************************************************/


    public void objFunctionUnknownDemandsSingleRadio() throws IOException
    {
      writer.newLine();
      writer.write("Maximize\n");
      writer.write("  obj: ");
      String completeObjFunction="";
      int currSource;
      int K;
      /* scan all sources in graph one-by-one */
      for(int i=0; i < g.getNofSources(); i++)
      {
          currSource = g.getSourceId(i);
          for(int nextNeigh=0; nextNeigh < g.forwardNeighbors(currSource).size(); nextNeigh++)
          {
              K = g.getK();
              for(int k=0; k < K; k++)
              {
                // IMP: consider only forward neighbors */
               if(currSource == g.getSourceId(k))
               {
                completeObjFunction+=(g.forwardNeighbors(currSource).get(nextNeigh)).getVariable(k);
                completeObjFunction+=" + ";
                }

              }

          }

      }
      completeObjFunction = completeObjFunction.substring(0, completeObjFunction.length() - 3);
      System.out.println("Maximize \n Obj: "+completeObjFunction);
      writer.write(completeObjFunction+"\n");
      System.out.println("Subject To");
      writer.write("Subject To\n");
    }
    /* No flow out of destination nodes for the respective K */
    public void constraintNoFlowOutOfDestination() throws Exception
    {
        String constraint="";
         int currSource,currDest;
         int K = g.getK();
         int nextNode,previousNode;
         for(int k=0; k < K; k++)
         {
            constraint=""; // initialize for every loop
            currDest = g.getDestId(k);
            for(int i=0; i < g.forwardNeighbors(currDest).size(); i++)
            {
                // parse all outgoing links of this destination one by one //
                constraint += g.forwardNeighbors(currDest).get(i).getVariable(k);
                constraint+=" + ";
            }
            constraint= constraint.substring(0, constraint.length() - 3);
            constraint+=" = 0";
            System.out.println(constraint);
            writer.write("C"+constraintNumb+": ");
            writer.write(constraint+"\n");
            constraintNumb++; // increment the global constraint numbers //
         }

    }

    public void flowConservConstraintsSingleRadio() throws IOException
    {

        /****** Flow Conservation Constraints containing Source and Destination *****/
         String constraint="";
         int currSource,currDest;
         int K = g.getK();
         int nextNode,previousNode;
         for(int k=0; k < K; k++)
         {
            constraint=""; // initialize for every loop
            // get the source node corresponding to this K //
            currSource = g.getSourceId(k);
            for(int i=0; i < g.forwardNeighbors(currSource).size(); i++)
            {
                // parse forward links of this source one by one //
                constraint += g.forwardNeighbors(currSource).get(i).getVariable(k);
                constraint+=" + ";
            }
            constraint = constraint.substring(0, constraint.length() - 3);
            currDest = g.getDestId(k);
            constraint+=" - ";
            for(int i=0; i < g.reverseNeighbors(currDest).size(); i++)
            {
                // parse reverse links of the dest for this source one by one //
                constraint += g.reverseNeighbors(currDest).get(i).getVariable(k);
                constraint+=" - ";
            }
            constraint= constraint.substring(0, constraint.length() - 3);
            constraint+=" = 0";
            System.out.println(constraint);
            writer.write("C"+constraintNumb+": ");
            writer.write(constraint+"\n");
            constraintNumb++; // increment the global constraint numbers //
         }
        /****** Flow Conservation Constraint that flow out of source equals demands *****/
         for(int k=0; k < K; k++)
         {
            // get the source node corresponding to this K //
            currSource = g.getSourceId(k);
            constraint = "";
            for(int i=0; i < g.forwardNeighbors(currSource).size(); i++)
            {
                // parse forward links of this source one by one //
                constraint += g.forwardNeighbors(currSource).get(i).getVariable(k);
                constraint+=" + ";
            }
            constraint = constraint.substring(0, constraint.length() - 3);

            constraint+=" - ";
            for(int j=0; j < g.reverseNeighbors(currSource).size(); j++)
            {
                // parse reverse links of the dest for this source one by one //
                constraint += g.reverseNeighbors(currSource).get(j).getVariable(k);
                constraint+=" - ";
            }
            constraint= constraint.substring(0, constraint.length() - 3);
            //constraint+=" - "+Globals.demands[k]+" = 0 ";
            constraint+=" = "+Driver_Interdiction.demands[k];
            System.out.println(constraint);
            writer.write("C"+constraintNumb+": ");
            writer.write(constraint+"\n");
            constraintNumb++; // increment the global constraint numbers //
         }

         /****** Flow Conservation Constraints for all other nodes *****/
        int currNode;
        for(int k=0; k < K; k++)
         {

            for(int node=0; node < g.getNofNodes(); node++)
            {
                if(isSourceOrDestinationforK(node,k) == false)
                {
                    constraint=""; // initialize for every loop
                    currNode = node;

                    for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
                    {
                      // parse forward links of this source one by one //
                      constraint += g.forwardNeighbors(currNode).get(i).getVariable(k);
                      constraint+=" + ";
                    }
                      constraint = constraint.substring(0, constraint.length() - 3);
                      currDest = g.getDestId(k);
                      constraint+=" - ";
                    for(int i=0; i < g.reverseNeighbors(currNode).size(); i++)
                    {
                      // parse reverse links of this source one by one //
                      constraint += g.reverseNeighbors(currNode).get(i).getVariable(k);
                      constraint+=" - ";
                    }
                      constraint= constraint.substring(0, constraint.length() - 3);
                      constraint+=" = 0";
                      System.out.println(constraint);
                      writer.write("C"+constraintNumb+": ");
                      writer.write(constraint+"\n");
                      constraintNumb++; // increment the global constraint numbers //

                }
            }


         }
    }

     public void flowConservConstraintsForFairness() throws IOException
    {

        /****** Flow Conservation Constraints containing Source and Destination *****/
         String constraint="";
         int currSource,currDest;
         int K = g.getK();
         int nextNode,previousNode;
         for(int k=0; k < K; k++)
         {
            constraint=""; // initialize for every loop
            // get the source node corresponding to this K //
            currSource = g.getSourceId(k);
            for(int i=0; i < g.forwardNeighbors(currSource).size(); i++)
            {
                // parse forward links of this source one by one //
                constraint += g.forwardNeighbors(currSource).get(i).getVariable(k);
                constraint+=" + ";
            }
            constraint = constraint.substring(0, constraint.length() - 3);
            currDest = g.getDestId(k);
            constraint+=" - ";
            for(int i=0; i < g.reverseNeighbors(currDest).size(); i++)
            {
                // parse reverse links of the dest for this source one by one //
                constraint += g.reverseNeighbors(currDest).get(i).getVariable(k);
                constraint+=" - ";
            }
            constraint= constraint.substring(0, constraint.length() - 3);
            constraint+=" = 0";
            System.out.println(constraint);
            writer.write("C"+constraintNumb+": ");
            writer.write(constraint+"\n");
            constraintNumb++; // increment the global constraint numbers //
         }


         /****** Flow Conservation Constraints for all other nodes *****/
        int currNode;
        for(int k=0; k < K; k++)
         {

            for(int node=0; node < g.getNofNodes(); node++)
            {
                if(isSourceOrDestinationforK(node,k) == false)
                {
                    constraint=""; // initialize for every loop
                    currNode = node;

                    for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
                    {
                      // parse forward links of this source one by one //
                      constraint += g.forwardNeighbors(currNode).get(i).getVariable(k);
                      constraint+=" + ";
                    }
                      constraint = constraint.substring(0, constraint.length() - 3);
                      currDest = g.getDestId(k);
                      constraint+=" - ";
                    for(int i=0; i < g.reverseNeighbors(currNode).size(); i++)
                    {
                      // parse reverse links of this source one by one //
                      constraint += g.reverseNeighbors(currNode).get(i).getVariable(k);
                      constraint+=" - ";
                    }
                      constraint= constraint.substring(0, constraint.length() - 3);
                      constraint+=" = 0";
                      System.out.println(constraint);
                      writer.write("C"+constraintNumb+": ");
                      writer.write(constraint+"\n");
                      constraintNumb++; // increment the global constraint numbers //

                }
            }


         }
    }


    /*********************    Capacity Constraints *********************/
    public void specialCapacityConstraints() throws IOException
    {
         String constraint="";
         int currNode;
         int K = g.getK();
         int simplecounter=0; // counter for xVariablesConstraintNumb

         constraintNumbInfoHolder = new int [g.getNofNodes()][g.getNofNodes()];

         for(int node=0; node < g.getNofNodes(); node++)
         {

             currNode = node;
            // apply only for forward neighbors, to avoid redundant constraints //
            for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
            {
                constraint=""; // initialize for every loop
                for(int k=0; k < K; k++)
                {
                  constraint += g.forwardNeighbors(currNode).get(i).getVariable(k);
                  constraint+=" + ";
                }
                constraint= constraint.substring(0, constraint.length() - 3);

                /* hack for populating X_Variable Array for this class only */



                /* This line stores the constraint number for this constraint so that it can later be indexed */
                constraintNumbInfoHolder[g.forwardNeighbors(currNode).get(i).getv1()][g.forwardNeighbors(currNode).get(i).getv2()]=constraintNumb;
                /***********************************************************/
                constraint+=" <= "+g.forwardNeighbors(currNode).get(i).getCapacity();
                System.out.println(constraint);
                writer.write("C"+constraintNumb+": ");
                writer.write(constraint+"\n");
                constraintNumb++; // increment the global constraint numbers //
            }

         }

    }




    /*********************    Capacity Constraints *********************/
    public void capacityConstraints() throws IOException
    {
         String constraint="";
         int currNode;
         int K = g.getK();
         int simplecounter=0; // counter for xVariablesConstraintNumb

         constraintNumbInfoHolder = new int [g.getNofNodes()][g.getNofNodes()];

         for(int node=0; node < g.getNofNodes(); node++)
         {

             currNode = node;
            // apply only for forward neighbors, to avoid redundant constraints //
            for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
            {
                constraint=""; // initialize for every loop
                for(int k=0; k < K; k++)
                {
                  constraint += g.forwardNeighbors(currNode).get(i).getVariable(k);
                  constraint+=" + ";
                }
                constraint= constraint.substring(0, constraint.length() - 3);

                /* hack for populating X_Variable Array for this class only */

                xVariablesConstraintNumb[simplecounter]=constraintNumb;
                //xVariables[simplecounter++]="X_"+g.forwardNeighbors(currNode).get(i).getv1()+
                //        g.forwardNeighbors(currNode).get(i).getv2();

                xVariables[simplecounter++]=g.forwardNeighbors(currNode).get(i).getv1()+"~"+
                        g.forwardNeighbors(currNode).get(i).getv2();

                /* This line stores the constraint number for this constraint so that it can later be indexed */
                constraintNumbInfoHolder[g.forwardNeighbors(currNode).get(i).getv1()][g.forwardNeighbors(currNode).get(i).getv2()]=constraintNumb;
                /***********************************************************/
                constraint+=" <= "+g.forwardNeighbors(currNode).get(i).getCapacity();
                System.out.println(constraint);
                writer.write("C"+constraintNumb+": ");
                writer.write(constraint+"\n");
                constraintNumb++; // increment the global constraint numbers //
            }

         }

    }


    /*************** Non-Negativity Constraints *************************/
    public void nonNegativityConstraints() throws IOException
    {
         String constraint="";
         int currNode;
         int K = g.getK();

         for(int node=0; node < g.getNofNodes(); node++)
         {

             currNode = node;
            // apply only for forward neighbors, to avoid redundant constraints //
            for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
            {
                constraint=""; // initialize for every loop
                for(int k=0; k < K; k++)
                {
                  constraint = g.forwardNeighbors(currNode).get(i).getVariable(k);
                  constraint+=" >= 0 ";
                  System.out.println(constraint);
                  writer.write("C"+constraintNumb+": ");
                  writer.write(constraint+"\n");
                  constraintNumb++; // increment the global constraint numbers //
                }

            }

         }
    }
    public void cliqueConstraints() throws IOException
    {
      String constraint="";
      int currNode;


      for(int i=0; i < g.getNofNodes(); i++)
      {
         currNode = i;
         covered[i]=1;
         for(int j=0; j < g.forwardNeighbors(currNode).size(); j++)
         {
          if(covered[g.forwardNeighbors(currNode).get(j).getv2()]!=1)
          {
           constraint="";
           for(int h=0; h < g.forwardNeighbors(currNode).get(j).getCliqueSize(); h++)
           {
              for(int k=0; k < g.getK(); k++)
              {
                constraint+=g.forwardNeighbors(currNode).get(j).getCliqueLink(h).getVariable(k)+" + ";
              }
           }
           System.out.println("Constraints for link : "+
                   g.forwardNeighbors(currNode).get(j).getv1()+"->"+g.forwardNeighbors(currNode).get(j).getv2());
           System.out.println("c no "+constraintNumb);
           // all clique variables done for this link //
//           constraint= constraint.substring(0, constraint.length() - 3);
           //constraint+=" <= 11000";
           constraint+=" <= 2100";
           System.out.println(constraint);
           writer.write("C"+constraintNumb+": ");
           writer.write(constraint+"\n");
           constraintNumb++; // increment the global constraint numbers //
          } // end of if not link already covered
         }
       }
    }

    public void closeFile() throws IOException
    {
        writer.close();
        input.close();
    }

    public boolean isSourceOrDestinationforK(int id, int k)
    {
      for(int i=0; i < g.getNofSources(); i++)
      {
        if(g.getSourceId(i) == id && id==g.sources[k])
            return true;
      }

       for(int i=0; i < g.getNofDestinations(); i++)
      {
        if(g.getDestId(i) == id && id==g.destinations[k])
            return true;
      }
      return false;

    }

    public boolean isSourceOrDestination(int id)
    {
      for(int i=0; i < g.sources.length; i++)
      {
           if(id == g.getSourceId(i))
            return Boolean.TRUE;
      }
      for(int j=0; j < g.destinations.length; j++)
      {
           if(id == g.getDestId(j))
            return Boolean.TRUE;
      }

      return Boolean.FALSE;

    }


    public void XisExactlyOne() throws IOException
    {
         String constraint="";
         int currNode;
         int K = g.getK();

         constraint=""; // initialize once
         for(int node=0; node < g.getNofNodes(); node++)
         {
            currNode = node;
            // apply only for forward neighbors, to avoid redundant constraints //
            for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
            {
                constraint += "X_"+currNode+g.forwardNeighbors(currNode).get(i).getv2();
                constraint+=" + ";
            }

         }
         /* Write One Constraint in Total */
         constraint= constraint.substring(0, constraint.length() - 3);
         constraint+=" = "+"1";
         System.out.println(constraint);
         writer.write("C"+constraintNumb+": ");
         writer.write(constraint+"\n");
         constraintNumbOfXisExactlyOne=constraintNumb;
         constraintNumb++; // increment the global constraint numbers //
    }


    public void XisBinary() throws IOException
    {
         String constraint="";
         int currNode;
         writer.write("BINARY\n");

         for(int node=0; node < g.getNofNodes(); node++)
         {
            currNode = node;
            // apply only for forward neighbors, to avoid redundant constraints //

            for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
            {
                constraint=""; // initialize for every iteration
                constraint = "X_"+currNode+g.forwardNeighbors(currNode).get(i).getv2();
                System.out.println(constraint);

                writer.write(constraint+"\n");
            }

         }
         /* Write One Constraint in Total */

    }



    /******************          Fairness Related Constraints        *******************/

    public void objFunctionFairness() throws IOException
    {
      System.out.println("coming in  objective function fairness");
      writer.newLine();
      writer.write("Maximize\n");
      writer.write("  obj: "+"P"+"\n");
      writer.write("Subject To\n");
    }

    public void fairnessConstraints() throws IOException
    {

        /****** Flow Conservation Constraints containing Source and Destination *****/
         String constraint="";
         int currSource,currDest;
         int K = g.getK();
         int nextNode,previousNode;
         for(int k=0; k < K; k++)
         {
            constraint=""; // initialize for every loop
            // get the source node corresponding to this K //
            currSource = g.getSourceId(k);
            for(int i=0; i < g.forwardNeighbors(currSource).size(); i++)
            {
                // parse forward links of this source one by one //
                constraint += g.forwardNeighbors(currSource).get(i).getVariable(k);
                constraint+=" + ";
            }

            constraint= constraint.substring(0, constraint.length() - 3);
            //constraint+=" - 10000 P >= 0";
            constraint+=" - P >= 0";
            System.out.println(constraint);
            writer.write("C"+constraintNumb+": ");
            writer.write(constraint+"\n");
            constraintNumb++; // increment the global constraint numbers //
         }
    }
    public void PhiBounds() throws IOException
    {
         writer.write("C"+constraintNumb+": ");
         constraintNumb++;
         writer.write("P >= 0"+"\n");

         writer.write("C"+constraintNumb+": ");
         constraintNumb++;
         writer.write("P <= 1"+"\n");

    }



     public void putAllXZero() throws IOException
    {
        int currNode;
        String var="";
         for(int node=0; node < g.getNofNodes(); node++)
         {
            currNode = node;
            // apply only for forward neighbors, to avoid redundant constraints //

            for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
            {
                var=""; // initialize for every iteration
                //constraint = "X_"+currNode+g.forwardNeighbors(currNode).get(i).getv2();
                var += "X_"+currNode+g.forwardNeighbors(currNode).get(i).getv2()+" = 0";
                writer.write("C"+constraintNumb+": ");
                constraintNumb++;
                writer.write(var+"\n");
            }

         }
    }

 private static String readFile(String path) throws IOException {
       File file = new File(path);
       URI uri = file.toURI();
       byte[] bytes = null;
       try{
          bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(uri));
       }catch(IOException e) { e.printStackTrace(); return "ERROR loading file "+path; }

       return new String(bytes);


}

 private static void copy(File source, File destination) throws IOException {
    long length = source.length();
    FileChannel input = new FileInputStream(source).getChannel();
    try {
        FileChannel output = new FileOutputStream(destination).getChannel();
        try {
            for (long position = 0; position < length; ) {
                position += input.transferTo(position, length-position, output);
            }
        } finally {
            output.close();
        }
    } finally {
        input.close();
    }
  }

 public int getNoOfCapacityConstraints(){
     int total=0;
     for(int node=0; node < g.getNofNodes(); node++)
     {
        total+=g.forwardNeighbors(node).size();

     }
     return total;

  }
 /*
 public Boolean isPreviouslySelected(int index)
 {

   for (int i = 0; i < previouslySelected.size(); i++)
   {
     if(previouslySelected.get(i)==index)
     {
         return Boolean.TRUE;
     }
   }
     return Boolean.FALSE;
 }
 */
 public Boolean checkUsedCapacityZeroOrNot(String X) throws UnknownObjectException, IloException, IOException
 {

    /* split X variable into its component parts */

    myAlgoCplex.updateConstraintAndSolve(-1);
    String [] Xnodes = X.split("~");
    //System.out.println("Coming here");
    //System.out.println("X = "+X);
    //System.out.println("objective value"+myAlgoCplex.cplex.getObjValue());

    double[] x = myAlgoCplex.cplex.getValues(myAlgoCplex.lp);
    for (int j = 0; j < x.length; ++j)
    {
      if(x[j] == 1000 && myAlgoCplex.lp.getNumVar(j).getType()!=IloNumVarType.Bool)
      {
            //System.out.println("String is " +myAlgoCplex.lp.getNumVar(j).getName());
            int pos1=(myAlgoCplex.lp.getNumVar(j).getName()).indexOf("(");
            int pos2=(myAlgoCplex.lp.getNumVar(j).getName()).indexOf(")");
            String temp = (myAlgoCplex.lp.getNumVar(j).getName()).substring(pos1+1,pos2);
            String [] Fnodes = temp.split("~");



            if(Xnodes[0].compareTo(Fnodes[0])==0 && Xnodes[1].compareTo(Fnodes[1])==0)
            {
               // System.out.println("xxxxxxxxxxx matched xxxxxxxxxxxxxxx");
               // System.out.println("F Variable: "+myAlgoCplex.lp.getNumVar(j).getName()+"; Value = " + x[j]);
               // System.out.println("X Variable: "+X);
                return Boolean.TRUE;

            }

      }
    }

     return Boolean.FALSE;
 }

 public double setOriginalCapacityForFairCases() throws IOException, IloException, Exception
 {
          CplexSolver_Interdiction temp = new CplexSolver_Interdiction();

          // First do all the writing stuff //
          this.objFunctionUnknownDemandsSingleRadio();
          this.flowConservConstraintsSingleRadio();
          this.constraintNoFlowOutOfDestination();
          //this.capacityConstraintsWithX();
          //this.XisExactlyOne();
          this.specialCapacityConstraints();
          this.cliqueConstraints();
          //this.XisBinary();
          //this.putAllXZero();
          this.nonNegativityConstraints();
          writer.write("End");
          writer.close();

         double originalCapacity=0;

         temp.createModelOnce(constraintFile);
         originalCapacity = temp.updateConstraintAndSolve(-1);
         //temp.closeFile();
         File file = new File(constraintFile);
         writer = new BufferedWriter(new FileWriter(file));
         constraintNumb=0;
         return originalCapacity;

 }


    /*********************    Capacity Constraints *********************/
    public void capacityConstraintsForMaxFlowFortification() throws IOException
    {
         String constraint="";
         int currNode;
         int K = g.getK();
         int simplecounter=0; // counter for xVariablesConstraintNumb

         constraintNumbInfoHolder = new int [g.getNofNodes()][g.getNofNodes()];

         for(int node=0; node < g.getNofNodes(); node++)
         {

             currNode = node;
            // apply only for forward neighbors, to avoid redundant constraints //
            for(int i=0; i < g.forwardNeighbors(currNode).size(); i++)
            {
                constraint=""; // initialize for every loop
                for(int k=0; k < K; k++)
                {
                  constraint += g.forwardNeighbors(currNode).get(i).getVariable(k);
                  constraint+=" + ";
                }
                constraint= constraint.substring(0, constraint.length() - 3);



                /* This line stores the constraint number for this constraint so that it can later be indexed */
                constraintNumbInfoHolder[g.forwardNeighbors(currNode).get(i).getv1()][g.forwardNeighbors(currNode).get(i).getv2()]=constraintNumb;
                /***********************************************************/
                constraint+=" + "+g.forwardNeighbors(currNode).get(i).getCapacity()+" x_"+g.forwardNeighbors(currNode).get(i).getv1()+" <= "+g.forwardNeighbors(currNode).get(i).getCapacity();
                System.out.println(constraint);
                writer.write("C"+constraintNumb+": ");
                writer.write(constraint+"\n");
                constraintNumb++; // increment the global constraint numbers //
            }


            // now apply to reverse nodes //
            for(int i=0; i < g.reverseNeighbors(currNode).size(); i++)
            {
                constraint=""; // initialize for every loop
                for(int k=0; k < K; k++)
                {
                  constraint += g.reverseNeighbors(currNode).get(i).getVariable(k);
                  constraint+=" + ";
                }
                constraint= constraint.substring(0, constraint.length() - 3);


                /***********************************************************/
                constraint+=" + "+g.reverseNeighbors(currNode).get(i).getCapacity()+" x_"+g.reverseNeighbors(currNode).get(i).getv2()+" <= "+g.reverseNeighbors(currNode).get(i).getCapacity();
                System.out.println(constraint);
                writer.write("C"+constraintNumb+": ");
                writer.write(constraint+"\n");
                constraintNumb++; // increment the global constraint numbers //
            }
         }

    }
   /**************************************************************************************/

    /*********************    Capacity Constraints *********************/
    public void fortificationConstraint() throws IOException
    {
         String constraint="";
         int currNode;
         int K = g.getK();
         int simplecounter=0; // counter for xVariablesConstraintNumb

         for(int node=0; node < g.getNofNodes(); node++)
         {
           currNode = node;
           constraint=""; // initialize for every node
           constraint+="x_"+g.forwardNeighbors(currNode).get(0).getv1()+" + "+"z_"+g.forwardNeighbors(currNode).get(0).getv1()+" <=  1 ";
           System.out.println(constraint);
           writer.write("C"+constraintNumb+": ");
           writer.write(constraint+"\n");
           constraintNumb++; // increment the global constraint numbers //
          }
    }
   /**************************************************************************************/
    /*********************    Capacity Constraints *********************/
    public void sourcesAndDestinationsCannotBeInterdicted() throws IOException
    {
         String constraint="";
         int currNode;
         int K = g.getK();

         for(int k=0; k < K; k++)
         {
            for(int node=0; node < g.getNofNodes(); node++)
            {
                currNode = node;
                if(isSourceOrDestinationforK(node,k) == true)
                {
                  constraint=""; // initialize for every node
                  constraint+="x_"+g.forwardNeighbors(currNode).get(0).getv1()+" = 0";
                  System.out.println(constraint);
                  writer.write("C"+constraintNumb+": ");
                  writer.write(constraint+"\n");
                  constraintNumb++; // increment the global constraint numbers //

                  /*constraint="z_"+g.forwardNeighbors(currNode).get(0).getv1()+" = 0";
                  System.out.println(constraint);
                  writer.write("C"+constraintNumb+": ");
                  writer.write(constraint+"\n");*/
                }
            }
         }

    }
   /**************************************************************************************/
    /*********************    Capacity Constraints *********************/
    public void InterdictionBudgetAndFortificationBudget() throws IOException
    {
        String constraint="";
         int currNode;
         int K = g.getK();
         int simplecounter=0; // counter for xVariablesConstraintNumb

         constraint=""; // initialize for every node
         for(int node=0; node < g.getNofNodes(); node++)
         {
           currNode = node;
           constraint+="x_"+g.forwardNeighbors(currNode).get(0).getv1()+" + ";
          }
         constraint= constraint.substring(0, constraint.length() - 3);
         constraint+= " <= "+numbOfNodesToBeInterdicted;
         System.out.println(constraint);
         writer.write("C"+constraintNumb+": ");
         writer.write(constraint+"\n");
         constraintNumb++; // increment the global constraint numbers //

         // now write fortification constraint
          constraint=""; // initialize for every node
         for(int node=0; node < g.getNofNodes(); node++)
         {
           currNode = node;
           constraint+="z_"+g.forwardNeighbors(currNode).get(0).getv1()+" + ";
          }
         constraint= constraint.substring(0, constraint.length() - 3);
         constraint+= " <= "+numbOfNodesToBeProtected;
         System.out.println(constraint);
         writer.write("C"+constraintNumb+": ");
         writer.write(constraint+"\n");
         constraintNumb++; // increment the global constraint numbers //
    }
   /**************************************************************************************/

     public void writeBinaryVariablesForFortification() throws IOException
    {
         String constraint="";
         int currNode;
         writer.write("BINARY\n");

         for(int node=0; node < g.getNofNodes(); node++)
         {
            currNode = node;
            constraint="x_"+g.forwardNeighbors(currNode).get(0).getv1();
            System.out.println(constraint);
            writer.write(constraint+"\n");
            }

       }


    public void solveOutermostOptimizationProblem(int [] identifiedNodes, double originalthru,double interdictedthru, Driver_Interdiction.SolutionType solType) throws IOException,IloException, Exception
    {
        writer = new BufferedWriter(new FileWriter(file));
        writer.write("\\ Optimization Problem with Constraints\n");
        myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;

         xVariables = new String [this.getNoOfCapacityConstraints()];
         xVariablesConstraintNumb = new int [this.getNoOfCapacityConstraints()];
          // First do all the writing stuff //
          this.objFunctionUnknownDemandsSingleRadio();
          this.flowConservConstraintsSingleRadio();
          //this.constraintNoFlowOutOfDestination();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
          for(int i=0; i < covered.length; i++)
          {
              covered[i]=0;
          }
          //this.cliqueConstraints();
          this.fortifyNodes(identifiedNodes);
          this.writeBinaryVariablesForFortification();

          //this.capacityConstraints();

          //this.nonNegativityConstraints();
          writer.write("End");
          writer.close();
         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;

       long startTime = System.currentTimeMillis();
       //myAlgoCplex.createModelOnce(constFile);
       //myAlgoCplex.solveProblem();
       //originalCapacity = myAlgoCplex.cplex.getObjValue();
       //System.out.println("Original Capacity: "+originalCapacity);

       for(int nodeCount=0; nodeCount < numbOfNodesToBeInterdicted; nodeCount++)
       {

         bestResult=100000;
         bestIndex=0;
         bestVariable="";
         lastIndex = -1;

         // Call once to get the basic solution //
         myAlgoCplex.createModelOnce(constraintFile);
         myAlgoCplex.setNumbOfLinkVariables(g.getNofEdges()/2,g.getNofNodes());
         if(nodeCount==0)
         {
           originalThroughput = myAlgoCplex.solveProblem(Boolean.FALSE);
           System.out.println("Original Throughput = "+originalThroughput);
         }
         // Repeast as many times as there are nodes in the network //
         for(int i=0; i < g.getNofNodes(); i++)
         {
            currNode = i;
            if(previouslySelected[i] == 0 && isSourceOrDestination(currNode)==Boolean.FALSE && identifiedNodes[currNode]==0)
            {
                System.out.println("ENTERING ALGO for node"+currNode+"\n");
                result = myAlgoCplex.fixInterdictionVariablesAndSolve(currNode);
                currIndex = i;
                if(result < bestResult)
                {
                    bestResult = result;
                    bestVariable = "x_"+currNode;
                    bestIndex = i;
                }

                lastIndex = currIndex;
                count++;
                System.out.println("+++++++++++++++++++++  SOlutions tried: "+count);
                totalTime+=myAlgoCplex.getTimeConsumed();
          }
         }

         if(isSourceOrDestination(bestIndex)==Boolean.FALSE)
         {
             previouslySelected[bestIndex]=1;
             System.out.println("Chboosing the best index = "+bestIndex+"node is "+previouslySelected[bestIndex]);
         }

           myAlgoCplex.fixAllSelectedIndicesForInterdiction(previouslySelected);

          } // end of number of nodes to be interdicted

         /* Put result stats */


         System.out.println();

         myAlgoCplex.printToFile();
         double currThroughput = myAlgoCplex.solveProblem(Boolean.TRUE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - globalStart;
         System.out.println("No. of nodes to be interdicted: "+Driver_Interdiction.numbOfNodesToBeInterdicted);
         if(solType == Driver_Interdiction.SolutionType.CARD)
            System.out.println("****************    CARD RESULTS    *******************");
         else if(solType == Driver_Interdiction.SolutionType.CARD_Greedy)
            System.out.println("****************    CA//rd_Greedy RESULTS    *******************");
         else if(solType == Driver_Interdiction.SolutionType.UpperLimit)
            System.out.println("****************    UpperLimit RESULTS    *******************");

         System.out.println("Original Throughput = "+(originalthru/1024)+" Gbps");
         //System.out.println("Original Interdicted Throughput = "+interdictedthru);
         System.out.println("Current Throughput = "+(currThroughput/1024)+" Gbps");
         System.out.println("Total time = "+duration*0.001+" seconds");
    }



    public void solveOutermostOptimizationProblemForFairness(int [] identifiedNodes, double originalthru,double interdictedthru) throws IOException,IloException, Exception
    {
        writer = new BufferedWriter(new FileWriter(file));
        writer.write("\\ Optimization Problem with Constraints\n");
        myAlgoCplex = new CplexSolver_Interdiction();
          double result;
          previouslySelected = new int [g.getNofEdges()/2];

          for(int i=0; i < previouslySelected.length; i++)
              previouslySelected[i]=0;

         xVariables = new String [this.getNoOfCapacityConstraints()];
         xVariablesConstraintNumb = new int [this.getNoOfCapacityConstraints()];
          // First do all the writing stuff //
          this.objFunctionFairness();
          this.fairnessConstraints();
          this.flowConservConstraintsForFairness();
          this.capacityConstraintsForMaxFlowFortification();
          this.fortificationConstraint();
          for(int i=0; i < covered.length; i++)
          {
              covered[i]=0;
          }
          this.sourcesAndDestinationsCannotBeInterdicted();
          this.InterdictionBudgetAndFortificationBudget();
           this.fortifyNodes(identifiedNodes);
          //this.cliqueConstraints();
          this.writeBinaryVariablesForFortification();
          //this.capacityConstraints();


          //this.capacityConstraints();
          //this.nonNegativityConstraints();
          writer.write("End");
          writer.close();

         int currNode,node1=0,node2=0,bestnode1=0,bestnode2=0;
         double bestResult=0;
         int bestIndex=0;
         String bestVariable="";
         int count=0;
         int linksAdded=0;
         double originalCapacity=0;
         double originalThroughput=0;

       long startTime = System.currentTimeMillis();
       //myAlgoCplex.createModelOnce(constFile);
       //myAlgoCplex.solveProblem();
       //originalCapacity = myAlgoCplex.cplex.getObjValue();
       //System.out.println("Original Capacity: "+originalCapacity);

       for(int nodeCount=0; nodeCount < numbOfNodesToBeInterdicted; nodeCount++)
       {

         bestResult=100000;
         bestIndex=0;
         bestVariable="";
         lastIndex = -1;

         // Call once to get the basic solution //
         myAlgoCplex.createModelOnce(constraintFile);
         myAlgoCplex.setNumbOfLinkVariables(g.getNofEdges()/2,g.getNofNodes());

         // Repeast as many times as there are nodes in the network //
         for(int i=0; i < g.getNofNodes(); i++)
         {
            currNode = i;
            if(previouslySelected[i] == 0 && isSourceOrDestination(currNode)==Boolean.FALSE && identifiedNodes[currNode]==0)
            {
                System.out.println("ENTERING ALGO for node"+currNode+"\n");
                result = myAlgoCplex.fixInterdictionVariablesAndSolve(currNode);
                currIndex = i;
                if(result < bestResult)
                {
                    bestResult = result;
                    bestVariable = "x_"+currNode;
                    bestIndex = i;
                }

                lastIndex = currIndex;
                count++;
                System.out.println("+++++++++++++++++++++  SOlutions tried: "+count);
                totalTime+=myAlgoCplex.getTimeConsumed();
          }
         }

           previouslySelected[bestIndex]=1;
           System.out.println("Chboosing the best index = "+bestIndex+"node is "+previouslySelected[bestIndex]);
           myAlgoCplex.fixAllSelectedIndicesForInterdiction(previouslySelected);

          } // end of number of nodes to be interdicted

         /* Put result stats */


         System.out.println();
         System.out.println("****************    FINAL RESULTS FOR FAIR  *******************");
         double currThroughput = myAlgoCplex.solveProblem(Boolean.TRUE);
         long endTime = System.currentTimeMillis();
         long duration = endTime - globalStart;
         System.out.println("V. Original Throughput = "+originalthru);
         System.out.println("Original Interdicted Throughput = "+interdictedthru);
         System.out.println("Ratio of Flows Satisfied = "+(currThroughput/Driver_Interdiction.demands[0]));
         System.out.println("Total time = "+duration*0.001+" seconds");
         //System.out.println("Ratio: "+calcMinimumRatioOfFlowsSatisfied(myAlgoCplex));
         printFlowStatistics(myAlgoCplex);
         writer.close();
    }





  public void fortifyNodes(int [] previouslySelected) throws IOException
  {
       String constraint="";
         int currNode;
         int K = g.getK();

         for(int k=0; k < K; k++)
         {
            for(int node=0; node < g.getNofNodes(); node++)
            {
                currNode = node;
                if(previouslySelected[currNode]==1)
                {
                  constraint=""; // initialize for every node
                  constraint="z_"+g.forwardNeighbors(currNode).get(0).getv1()+" = 1";
                  System.out.println(constraint);
                  System.out.println("WRITING FORTIFICATION VARIABLE VALUES");
                  writer.write("C"+constraintNumb+": ");
                  constraintNumb++;
                  writer.write(constraint+"\n");
                }
            }
         }
  }

  /* print flow statistics */
  public double printFlowStatistics(CplexSolver_Interdiction myAlgoCplex) throws UnknownObjectException, IloException
{
    double[] sources = new double [g.getNofDestinations()];
    double[] flows = new double [g.getNofDestinations()];

    for(int i=0; i < sources.length; i++)
    {
        sources[i] = g.getSourceId(i);
    }

    int destIndex=0;
    String currVariable,temp;
    String [] nodes = new String[2];
    String name;

    double[] x = myAlgoCplex.cplex.getValues(myAlgoCplex.lp);
    for (int i = 0; i < x.length; i++)
    {
          name = myAlgoCplex.lp.getNumVar(i).getName();
       if(x[i] > 0 && myAlgoCplex.lp.getNumVar(i).getType()!=IloNumVarType.Bool &&
              "P".equals(name)==Boolean.FALSE && "R".equals(name.charAt(0))==Boolean.FALSE &&
               "x".equals(String.valueOf(name.charAt(0)))==Boolean.FALSE && "z".equals(String.valueOf(name.charAt(0)))==Boolean.FALSE)
         /* extract the destination part of the variable */
       {
           currVariable = myAlgoCplex.lp.getNumVar(i).getName();

          int rightCurlyBracket = currVariable.indexOf("}");
          String kk = currVariable.substring(rightCurlyBracket-1,rightCurlyBracket);
         int K = Integer.parseInt(kk);
         int leftbrace = currVariable.indexOf("(");
         int rightbrace = currVariable.indexOf(")");
         temp = currVariable.substring(leftbrace+1,rightbrace);
         nodes = temp.split("~");

        for(int j=0; j < sources.length; j++)
        {
            //System.out.println("nodes[1] = "+nodes[1]);
            //System.out.println("destinations[j] = "+destinations[j]);
            if(Integer.parseInt(nodes[0])==sources[j] && j==K)
            {
               System.out.println("CCCVariable: "+myAlgoCplex.lp.getNumVar(i).getName()+"; Value = " + x[i]);
               flows[j] += x[i];
            }
        }

       }
    }

    /* Now array flows contains all the flows */
    double sum = 0;
    for(int i=0; i < flows.length; i++)
    {
       sum += flows[i];
    }
    return sum;
}

  public void colorDeadNodes(GephiGraph_Interdiction gGraph)
  {

  }



}
