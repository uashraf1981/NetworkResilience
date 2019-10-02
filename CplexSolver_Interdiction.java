/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphpackageinterdiction;

import ilog.concert.*;
import ilog.cplex.*;
import java.io.*;
import java.util.Iterator;
import java.util.*;
/**
 *
 * @author Administrator
 */
public class CplexSolver_Interdiction 
{
    
   public boolean flag; 
   private BufferedWriter cplexwriter;
   private BufferedWriter cplexModelDumper;
   public IloCplex cplex;
   private Iterator matrixEnum;
   public IloLPMatrix lp;
   private double timeConsumedCurrentIteration;
   public LinkedList <Integer> finalizedLinks;
   private int numbOfLinkVariables;
   private IloNumVar [] variables;
   
   public double getTimeConsumed()
   {
     return timeConsumedCurrentIteration;  
   }
   
   public CplexSolver_Interdiction() throws IOException, IloException{
    
     cplexwriter = new BufferedWriter(new FileWriter(Globals_Interdiction.path+"results.txt"));
     cplexModelDumper = new BufferedWriter(new FileWriter(Globals_Interdiction.path+"Model.txt"));
     cplexModelDumper.write("");
     cplexwriter.write("");
     flag = Boolean.FALSE;
     cplex = new IloCplex();
     finalizedLinks = new LinkedList<Integer>();
 
   }
   
  
   public double solveProblem(Boolean killNodes) throws IOException, IloException
   {      
       //cplex.setOut(new PrintStream(new OutputStream(){
	//		public void write(int b) {
	//		}
	// })); 
      // cplex.importModel(file);
      // Iterator matrixEnum = cplex.LPMatrixIterator();
      // IloLPMatrix lp = (IloLPMatrix)matrixEnum.next();
       //cplex.setParam(IloCplex.DoubleParam.EpGap, 0.015);
       //cplex.setOut(new PrintStream(new OutputStream(){
	//		public void write(int b) {
	//		}
	// })); 
       if (cplex.solve())
         {
             System.out.println("Model Feasible");                                              
         }
         else
         {
             System.out.println("Solution status = " + cplex.getStatus());
             System.out.println("Model Infeasible, Calling CONFLICT REFINER");          
         }
      
       /* print relevant results */
       System.out.println("\n-----------------   SOLUTION    ---------------\n");
       cplexwriter.write("\n-----------------   SOLUTION    ---------------\n");
       System.out.println("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps");
       cplexwriter.write("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps\n");
       System.out.println("Time consumed: "+cplex.getCplexTime()+" s");
       cplexwriter.write("Time consumed: "+cplex.getCplexTime()+" s\n");
       System.out.println("");
       double[] x = cplex.getValues(lp);
       for (int j = 0; j < x.length; ++j)
       {
           if(x[j] > 0)
           {
             System.out.println("Variable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]);
             cplexwriter.write("Variable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]+"\n");
             
             System.out.println("Substring is "+lp.getNumVar(j).getName().substring(0,1));
             if(lp.getNumVar(j).getName().substring(0,1).compareTo("x")==0)
             {
               String s = lp.getNumVar(j).getName().substring(2);
               int n = Integer.parseInt(s);
               if(killNodes == Boolean.TRUE)
               {
                 System.out.println(n);
                 Globals_Interdiction.nodeIsAlive[n]=0;
                 System.out.println("Node "+n+" is dead");
               }
             } 
           }
       }
    
        //cplexwriter.close();
      
        return cplex.getObjValue();
     }
   
   public void createModelOnce(String file) throws IOException, IloException
   {      
      
   
      IloModel model;       
      cplex.importModel(file);
      model = cplex.getModel();           
      matrixEnum = cplex.LPMatrixIterator();
      lp = (IloLPMatrix)matrixEnum.next();
      cplex.setParam(IloCplex.DoubleParam.EpGap, 0.015);
     
     /*
       
       double val=20.0;
       IloRange ir = lp.getRange(0);
       IloRange ir1 = lp.getRange(1);
       System.out.println("Row is: "+ir.getExpr()+ir.getUB());
       cplex.exportModel("G:\\temp\\new.lp");
       ir.setUB(val);
       ir.
       ir1.setBounds(0,val);
       System.out.println("Row is: "+ir.getExpr()+ir.getUB());
       cplex.exportModel("G:\\temp\\new2.lp");
       * */
       
       
   }
   
   public double updateConstraintAndSolve(int currRow) throws IOException, IloException
   {
      IloRange irNew=null; 
       if(currRow != -1)
       {
        irNew = lp.getRange(currRow);
        irNew.setUB(2000.0);        
        //System.out.println("this row = "+irNew.toString());
       }
       updateUBOfFinalizedLinks();
       
       if (cplex.solve())
         {
             System.out.println("Model Feasible");                                              
         }
         else
         {
             System.out.println("Solution status = " + cplex.getStatus());
             System.out.println("Model Infeasible, Calling CONFLICT REFINER");          
         }
      if(flag==Boolean.TRUE)
      {
       /* print relevant results */
       System.out.println("\n-----------------   SOLUTION    ---------------\n");
       cplexwriter.write("\n-----------------   SOLUTION    ---------------\n");
       System.out.println("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps");
       cplexwriter.write("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps\n");
       System.out.println("Time consumed: "+cplex.getCplexTime()*0.001+" s");
       cplexwriter.write("Time consumed: "+cplex.getCplexTime()*0.001+" s\n");
       System.out.println("");
       double[] x = cplex.getValues(lp);
      /* for (int j = 0; j < x.length; ++j)
       {
           if(x[j] > 0)
           {
             System.out.println("Variable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]);
             cplexwriter.write("Variable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]+"\n");
           }
       }
    */
       
      }
      timeConsumedCurrentIteration = cplex.getCplexTime();
       
     
      return cplex.getObjValue();
   }
   public void updateXIsOneContraint(int  constraintNumbOfXisExactlyOne, int upperBound) throws IloException{
                            
       IloRange irNew2 = lp.getRange(constraintNumbOfXisExactlyOne);
       irNew2.setBounds(upperBound,upperBound);
       System.out.println("Lower Bound for X constraint "+irNew2.getExpr()+"="+irNew2.getLB()+" Upper Bound="+irNew2.getUB());
   }
   
   public String SolveForSingleXAndFixThatX(float newLinkCapacity) throws IOException, IloException
   {
       String variable=""; 
       IloNumVar currentXVariable;
       if (cplex.solve())
         {
             System.out.println("Model Feasible");                                              
         }
         else
         {
             System.out.println("Solution status = " + cplex.getStatus());
             System.out.println("Model Infeasible, Calling CONFLICT REFINER");          
         }
      if(flag==Boolean.TRUE)
      {
       /* print relevant results */
       System.out.println("\n-----------------   SOLUTION    ---------------\n");
       cplexwriter.write("\n-----------------   SOLUTION    ---------------\n");
       System.out.println("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps");
       cplexwriter.write("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps\n");
       System.out.println("Time consumed: "+cplex.getCplexTime()+" s");
       cplexwriter.write("Time consumed: "+cplex.getCplexTime()+" s\n");
       System.out.println("");
       double[] x = cplex.getValues(lp);
       for (int j = 0; j < x.length; ++j)
       {
           if(x[j] > 0)
           {
             System.out.println("XVariable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]);
             cplexwriter.write("XVariable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]+"\n");
             variable=lp.getNumVar(j).getName();
             currentXVariable = lp.getNumVar(j);  
  
             /* Fix this optimal Variable so that next time we continue from here onwards */             
             if(lp.getNumVar(j).getType()==IloNumVarType.Bool )
             {
              
                //currentXVariable.setLB(1);
                //currentXVariable.setUB(2);
                System.out.println("Lower Bound for Variable "+currentXVariable.toString()+"="+currentXVariable.getLB()+" Upper Bound="+currentXVariable.getUB());
             } 
           }
       }
    
       
      }
        timeConsumedCurrentIteration = cplex.getCplexTime();        
        return variable;
   }
   
   /********************************************************/
   public double solveForGreedy(int currRow) throws IOException, IloException
   {
         
       if(currRow != -1)
       {
           IloRange irNew = lp.getRange(currRow);
           irNew.setUB(2000.0);
       }  
       
       if (cplex.solve())
         {
             System.out.println("Model Feasible");                                              
         }
         else
         {
             System.out.println("Solution status = " + cplex.getStatus());
             System.out.println("Model Infeasible, Calling CONFLICT REFINER");          
         }
      if(flag==Boolean.TRUE)
      {
       /* print relevant results */
       System.out.println("\n-----------------   SOLUTION    ---------------\n");
       cplexwriter.write("\n-----------------   SOLUTION    ---------------\n");
       System.out.println("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps");
       cplexwriter.write("Optimal Solution Value: "+(cplex.getObjValue()/1024)+" Mbps\n");
       System.out.println("Time consumed: "+cplex.getCplexTime()+" s");
       cplexwriter.write("Time consumed: "+cplex.getCplexTime()+" s\n");
       System.out.println("");
       double[] x = cplex.getValues(lp);
       for (int j = 0; j < x.length; ++j)
       {
           if(x[j] > 0)
           {
             System.out.println("Variable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]);
             cplexwriter.write("Variable: "+lp.getNumVar(j).getName()+"; Value = " + x[j]+"\n");
           }
       }
    
       
      }
        timeConsumedCurrentIteration = cplex.getCplexTime();
        return cplex.getObjValue();
   }
   /********************************************************/
   
   public void updateTopology(int currRow) throws IOException, IloException
   {       
       IloRange irNew = lp.getRange(currRow);
       irNew.setUB(2000.0);
   }
   
   public void resetTopology(int currRow) throws IOException, IloException
   {
     IloRange irNew = lp.getRange(currRow);
     //irNew.setBounds(0,1000);
     irNew.setUB(1000);
       
     System.out.println("reduced row = "+irNew.toString());         

   }
   
   
    public void printToFile() throws IOException, IloException
   {       
         cplex.exportModel(Globals_Interdiction.path+"ModelWrittenByCPLEX.lp");
   }
   
   public void closeFile()throws IOException
   {
        cplexwriter.close();
   }
   public void addFinalizedLink(int currLink)
   {
       finalizedLinks.add(currLink);
   }
   public void updateUBOfFinalizedLinks() throws IloException
   {
       for(int i=0; i < finalizedLinks.size();i++)
       {
           IloRange irNew = lp.getRange(finalizedLinks.get(i));
           irNew.setUB(2000.0);   
           //System.out.println("Have set upper bound of "+irNew.toString());
           //System.out.println("Number of links finalized= "+finalizedLinks.size());
       }
           
   }  
   
   public void setNumbOfLinkVariables(int links, int nodes) throws IloException
   {
       numbOfLinkVariables = links;           
       int length = (lp.getNcols());       
       variables = new IloNumVar [length];
       variables = lp.getNumVars();       
       System.out.println("============= List of Variables ========== \n");
       // note that the first |links| variables are link variables
       for(int i=0; i < length; i++)
       {
           System.out.println(variables[i].toString());
       }
   }
   /* Methods for Interdiction */
   public double fixInterdictionVariablesAndSolve(int currIndex) throws IloException, IOException
   {
       int index = findIndex(Integer.toString(currIndex));
       variables[index].setLB(1);
       variables[index].setUB(1);
       double result = this.solveProblem(Boolean.FALSE);
       variables[index].setLB(0);
       variables[index].setUB(0);
       return result;
   }
   
   public void fixAllSelectedIndicesForInterdiction(int [] previouslySelected) throws IloException
   {
      System.out.println("IMCOMING\n");
      for(int i=0; i < previouslySelected.length; i++)
      {
          if(previouslySelected[i] == 1)
          {
               int index = findIndex(Integer.toString(i));
               variables[index].setLB(1);
               variables[index].setUB(1);
               
               System.out.println("FIXING "+variables[index].toString()+"\n");
          }
      }
   }
   
    public void fixNodesForInterdiction(int [] previouslySelected) throws IloException
   {
      System.out.println("IMCOMING\n");
      /*for(int i=0; i < previouslySelected.length; i++)
      {
          if(previouslySelected[i] == 1)
          {
               int index = findIndex(Integer.toString(i));
               variables[index].setLB(1);
               variables[index].setUB(1);
               
               System.out.println("FIXING "+variables[index].toString()+"\n");
          }
      }
      */
      
               variables[74].setLB(1);
               variables[74].setUB(1);
               
               variables[44].setLB(1);
               variables[44].setUB(1);
               
               variables[54].setLB(1);
               variables[54].setUB(1);
               
               variables[90].setLB(1);
               variables[90].setUB(1);
               
               variables[92].setLB(1);
               variables[92].setUB(1);
      
      //if(i == 74 || i == 44 || i== 54 || i == 90 || i == 92 || j == 74 || j == 44 || j== 54 || j == 90 || j == 92)
                 //    linkCapacity = 10;
   }
   
   public void fortify(int [] previouslySelected) throws IloException
   {
      for(int i=0; i < previouslySelected.length; i++)
      {
          if(previouslySelected[i] == 1)
          {    
               int index = findIndex(Integer.toString(previouslySelected[i]));
               variables[index].setLB(1);
               variables[index].setUB(1);
          }
      }
   }
   
   public int findIndex(String var) throws IloException
   {
       String varName = "x_"+var;
       for(int i=0; i < variables.length; i++)
       {
           
           if(varName.equals(variables[i].toString())==true)
           {               
               return i;
           }
       }
       return -1;
   }
   
   public int [] solveGreedy1forMaxFlow(Graph_Interdiction g, int numbOfNodesToBeProtected, ConstraintGenerator_Interdiction obj) throws IloException
   {
        double[] values = cplex.getValues(lp);
        int currNode,flow=0,index=0;
        int [] removed = new int [g.getNofNodes()];
        int [] aggFlow = new int [g.getNofNodes()];
        for(int i=0; i < removed.length; i++)
        {
            removed[i]=0;
            aggFlow[i]=0;
        }
        for(int i=0; i < g.getNofNodes(); i++)
        {
          currNode = i;
          flow=0;
         
           for(int k=0; k < g.getK();k++)
           {    
            for(int j=0; j < g.forwardNeighbors(currNode).size(); j++)
            {             
              index = findIndexOfFlowVariable(g.forwardNeighbors(currNode).get(j).getVariable(k));
              //System.out.println("The current variable is: "+variables[index].toString()+" and value is: "+values[index]);
              aggFlow[currNode] += values[index];                           
            }        
           } 
          
        }
        
        int max=1,bestindex=0,temp;
        // now pickup the most heavily used nodes //
        for(int i=0; i < numbOfNodesToBeProtected; i++)
        {
            max=1;
            for(int j=0; j < g.getNofNodes();j++)
            {
                int amount = aggFlow[j];
                if(amount > max && removed[j] == 0 && obj.isSourceOrDestination(j)== Boolean.FALSE)
                {
                    
                    max=amount;
                    bestindex=j;
                }
            }
            removed[bestindex]=1;
        }
        
        System.out.println("============   NODES SELECTED FOR FORTIFICATION   ==========");
        /* now fortify these greedily selected nodex */
        for(int i=0; i < g.getNofNodes(); i++)
        {
            if(removed[i]==1)
            {
               
               int ind = findIndexforFortification(""+i);               
               System.out.println(variables[ind].toString()+" with traffic "+aggFlow[i]);
               variables[ind].setLB(1);
               variables[ind].setUB(1);
            }
        }
   return removed;
   }
   
   public int [] solveGreedy_2_forMaxFlow(Graph_Interdiction g, int numbOfNodesToBeProtected) throws IloException
   {
        double[] values = cplex.getValues(lp);
        int currNode,flow=0,index=0,indexforreverselink;
        int [] removed = new int [g.getNofNodes()];
        int [] aggFlow = new int [g.getNofNodes()];
        for(int i=0; i < removed.length; i++)
        {
            removed[i]=0;
            aggFlow[i]=0;
        }
        for(int i=0; i < g.getNofNodes(); i++)
        {
          currNode = i;
          flow=0;
          int totallinks=0;
          
           for(int k=0; k < g.getK();k++)
           {    
                       
           for(int j=0; j < g.forwardNeighbors(currNode).size(); j++)
            {             
              index = findIndexOfFlowVariable(g.forwardNeighbors(currNode).get(j).getVariable(k));                            
              aggFlow[currNode] += values[index];   
            
            }        
           } 
          
        }
        
        /*
        // Weed out unsaturated links //
        for(int i=0; i < g.getNofNodes(); i++)
        { currNode = i;
          if(aggFlow[currNode] < (g.forwardNeighbors(currNode).size()*1000))
          {
              removed[currNode]=-1;
              System.out.println("removing node "+currNode+" as it has links="+g.forwardNeighbors(currNode).size()+" but traffic of "+aggFlow[currNode]);
          }
        }  
        */
        
        
        int bestindex=0,temp;
        float max,thismax, denominator;
        // now pickup the most heavily used nodes //
        for(int i=0; i < numbOfNodesToBeProtected; i++)
        {
            max=0;
            for(int j=0; j < g.getNofNodes();j++)
            {
                denominator = ((g.forwardNeighbors(j).size()*1000)-aggFlow[j]);
                if(denominator == 0)
                    denominator = 1;
                //thismax=(aggFlow[j])/denominator;
                thismax=(aggFlow[j])/g.forwardNeighbors(j).size();
                //if(aggFlow[j] > max && removed[j] == 0)
                if(thismax > max && removed[j] == 0)
                {
                    //max=aggFlow[j];
                    max= thismax;
                    bestindex=j;
                }
            }
            removed[bestindex]=1;
        }
        
        System.out.println("============   NODES SELECTED FOR FORTIFICATION   ==========");
        /* now fortify these greedily selected nodex */
        for(int i=0; i < g.getNofNodes(); i++)
        {
            if(removed[i]==1)
            {
               
               int ind = findIndexforFortification(""+i);               
               System.out.println(variables[ind].toString()+" with traffic "+aggFlow[i]);
               variables[ind].setLB(1);
               variables[ind].setUB(1);
            }
        }
   return removed;
   }
   public int findIndexOfFlowVariable(String var) throws IloException
   {
       
       for(int i=0; i < variables.length; i++)
       {
           
           if(var.equals(variables[i].toString())==true)
           {               
               return i;
           }
       }
       return -1;
   }
   
   public int findIndexforFortification(String var) throws IloException
   {
       String varName = "z_"+var;
       for(int i=0; i < variables.length; i++)
       {
           
           if(varName.equals(variables[i].toString())==true)
           {               
               return i;
           }
       }
       return -1;
   }
   
}


