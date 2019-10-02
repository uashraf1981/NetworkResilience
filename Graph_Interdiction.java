/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphpackageinterdiction;

import com.google.common.collect.Sets;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import java.awt.Dimension;
import javax.swing.JFrame;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Graph_Interdiction {

       // JUNG stuff //
      // Graph<String,String>  jungGraph;
   
   

       // Name of File for Writing Final Constraints //
        public int maxCardinality;
        public int Vposition;
        public Set SetOfAllMISForBrute; 
        public LinkedList<Integer> []  Icurr;
        public int totalGatewaysAdded=1;
        public LinkedList<Position> GWPositions;
        public LinkedList<Position> GreedyPositions;
        private int MISCounter;
        
	private int noOfNodes; // Number of vertices
        private int noOfEdges; // Number of vertices
        private int noOfSources; // Number of vertices
        private int noOfDemands; // Number of vertices
        private int noOfDestinations;
        private String fileName; 
        private int K;
	//private ArrayList<LinkedList<Edge>> neighbors; // Adjacency lists
        public LinkedList<Edge> [] forwardNeighbors;
        public LinkedList<Edge> [] reverseNeighbors;
        public  Node [] nodes;
        public LinkedList<Node> [] csNeighbors;
        public int [] sources;
        public int [] destinations;
        public int [] demands;
        
        /* Variables for Brute Force and Greedy of EAGP */
        private LinkedList<Node> MIS;

        /* Node specific methods for nodes array containing Nodes */         
        public int getNodeXPosition(int index){return nodes[index].getX();}
        public int getNodeYPosition(int index){return nodes[index].getY();}
        public String getNodeId(int index){return nodes[index].getId();}
	// Inner class to represent edges
	public class Edge {
		private int v1;
		private int v2;
		private int capacity;
                private String [] variable; // assuming                
                private LinkedList<Edge> clique;
                private double cliqueFlowRate;
		public Edge(int v1, int v2, int capacity) {
                        
                        variable = new String [K]; // assuming K=1 for now.                        
			this.v1 = v1;
			this.v2 = v2;
			this.capacity = capacity;
                        for(int kvar=0; kvar < K; kvar++)
                        {
                            this.variable[kvar] = "f"+"{k_"+kvar+"}"+"("+v1+"~"+v2+")";
                        }
                        clique = new LinkedList<Edge>();
                        
		}
                public int getv1(){return v1;}
                public int getv2(){return v2;}
                public int getK(){return K;}
                public String getVariable(int index){return this.variable[index];}
                public int getCapacity()
                {
                   return this.capacity;
                }
                public void addToClique(Edge e)
                {
                   // check if this link already exists //
                  for(int i=0; i < clique.size(); i++)
                  {
                    if(clique.get(i).getv1()==e.getv1() && clique.get(i).getv2()==e.getv2())
                       return;
                  }
                   clique.add(e);
                }
                public int getCliqueSize(){return clique.size();}
                public Edge getCliqueLink(int index){return clique.get(index);}
	}        
	public class Node {
                public float [] SINRtoOtherNodes;
                public LinkedList<Integer> InterferingNeighbors;
                public LinkedList<Float> InterferenceMeasure;
                private float MCD;
		private int x;
		private int y;
                private String id;

		public Node(String id, int x, int y) {
			this.id = id;
                        this.x = x;
			this.y = y;
		}

                public Node() {}
                public int getX(){return x;}
                public int getY(){return y;}
                public String getId(){return id;}
                public void setX(int x){this.x=x;}
                public void setY(int y){this.y=y;}
                public void setId(String id){this.id=id;}
        }
        
        public class Position {
    
            public Position(int a, int b){
                x=a;
                y=b;
            }
     
            public int x;
            public int y;
            public float metric;
            public float tempMetric;
            public float [] sourceDistances;

        }
	 
        // Constructs a graph with s vertices and no edges
	public Graph_Interdiction(String receivedFile, int k) throws FileNotFoundException {
            this.K=k;
            noOfNodes=noOfEdges=0;
            fileName = receivedFile;
            
            /* make method calls */
            this.parseFile(); // parse file and extract info
            this.addEdges();  // find wireless links between nodes <= 250m away
            parseSourceandDestinations();
            parseDemands();
            //findCliques();
            
	}

        public int getNofNodes(){return noOfNodes;}
        public int getNofEdges(){return noOfEdges;}
        public int getNofSources(){return noOfSources;}
        public int getNofDestinations(){return noOfDestinations;}
        public int getSourceId(int index){return sources[index];}
        public int getDestId(int index){return destinations[index];}
        public int getK(){return K;}
        public Edge getReverseEdge(int curr, int next)
        {
           Edge e=new Edge(0,0,0);
           for(int index=0; index < forwardNeighbors(curr).size(); index++)
           {
               if(forwardNeighbors(curr).get(index).getv2()==next)
               {
                 e=forwardNeighbors(curr).get(index);
                 break;
               }
           }           
           return e;
        }
        
        


	// Gets the neighbor list of a vertex
	public LinkedList<Edge> forwardNeighbors(int v) {
		return forwardNeighbors[v];
	}
        public LinkedList<Edge> reverseNeighbors(int v) {
		return reverseNeighbors[v];
	}
        public void print() {
		/*for (int i=0; i<noOfNodes; i++) {
			System.out.print(nodes[i].getId()+" -> ");
			for (Edge edge : neighbors(i))
				System.out.print(labels.get(edge.v2)+"("+edge.capacity+") ");
					System.out.println();
		}*/
	}
        
        public Set<Integer> getListofNeighbors(int v){
            Set<Integer> collection = new HashSet<Integer> ();
            for(int i=0; i < forwardNeighbors[v].size(); i++){
                collection.add(forwardNeighbors[v].get(i).getv2());
            }
            return collection;
        }

          public Set<Integer> getVertices(){
            Set<Integer> collection = new HashSet<Integer> ();
            for(int i=0; i < this.getNofNodes(); i++){
                collection.add(i);
            }
            return collection;
        }

   void parseFile() throws FileNotFoundException{

  
      File file = new File(fileName);
      Scanner input = new Scanner(file);
      String nextLine="";
      String[] fields;
      int lineCount=0;
      
      while(input.hasNext()) {
        nextLine = input.nextLine();
        fields = nextLine.split(" ");
        if(lineCount==5){
          fields[6] = fields[6].substring(0, fields[6].length() - 1);
          noOfNodes = Integer.parseInt(fields[6]);
          MIS = new LinkedList<Node>();
         break;
        }
//        System.out.println("fields are: "+fields[0]+fields[1]);
       
       lineCount++;
      }

 
  /* now read each nde entry and populate the graph */

  
  System.out.println("Parsing file "+fileName+"....\n"+"No. of Nodes = "+noOfNodes);

  /* Initialize lists and arrays */
  forwardNeighbors = (LinkedList<Edge>[]) new LinkedList[(noOfNodes+21)];
  reverseNeighbors = (LinkedList<Edge>[]) new LinkedList[(noOfNodes+21)];
  nodes = new Node[(noOfNodes+21)];
 for (int i=0; i < (noOfNodes+21); i++){
    forwardNeighbors[i] = new LinkedList<Edge>();
    reverseNeighbors[i] = new LinkedList<Edge>();
    nodes[i] = new Node();
  
  }  
 

 
 //jungGraph.addVertex("Vertex1");
 //jungGraph.addVertex("Vertex2");
 


  for (int i=0; i<noOfNodes; i++){
    nodes[i].setId(Integer.toString(i));     
  }
  
  /* Now add the special node V at this point */
   
  nodes[noOfNodes].setId(""+noOfNodes);
  Vposition=(noOfNodes);
  
  /* get X and Y positions of nodes */
  for(int i=0; i < noOfNodes; i++){   
   parseNodeStatsandUpdateGraph(input);
  }

  input.close();
}

   void parseNodeStatsandUpdateGraph(Scanner in){
       String l1,l2,l3;
       char nd=' ';
       int linkCapacity;
       l1 = in.nextLine();
       l2 = in.nextLine();
       l3 = in.nextLine();
       
       /* get node */
       String val = l1.substring(l1.indexOf("(")+1,l1.indexOf(")"));
       System.out.println(val);
       int node = Integer.parseInt(val);
             
       /* get X */
       String [] fields = l1.split(" ");      
       String [] fields2 = fields[fields.length-1].split("\\.");       
       int x = Integer.parseInt(fields2[0]);

       /* get Y */
       fields = l2.split(" ");
       fields2 = fields[fields.length-1].split("\\.");
       int y = Integer.parseInt(fields2[0]);

       nodes[node].setX(x);
       nodes[node].setY(y);
       System.out.println("Node "+node+" X="+x+" Y="+y);
   }

   void addEdges(){
       int linkCapacity = 400;
       for (int i=0; i<noOfNodes; i++){
        for (int j=0; j<noOfNodes; j++){
          /* check if j is a neighbor of i */
          if(j != i && distance(nodes[i],nodes[j]) <= Globals_Interdiction.distanceforBeingNeighbor){
             if(Driver_Interdiction.useVariableLinkCapacity = Boolean.FALSE){
                 Edge e1 = new Edge(i,j,1000);
                 Edge e2 = new Edge(j,i,1000);
                 forwardNeighbors[i].add(e1);
                 reverseNeighbors[i].add(e2);

                 
             }
             else {
                 //Random rn = new Random();
                 //int random = rn.nextInt(7) + 1;
                 //random= random * 10001;
                 if(i % 2 == 0 && j % 2 == 0)
                 {
                     linkCapacity = 400;
                 }
                 if(i % 2 == 0 && j % 2 != 0)
                 {
                     linkCapacity = 300;
                 }
                 if(i % 2 != 0 && j % 2 == 0)
                 {
                     linkCapacity = 500;
                 }
                 //if(i == 74 || i == 44 || i== 54 || i == 90 || i == 92 || j == 74 || j == 44 || j== 54 || j == 90 || j == 92)
                 //    linkCapacity = 10;
                 Edge e1 = new Edge(i,j,linkCapacity);
                 Edge e2 = new Edge(j,i,linkCapacity); 
                 forwardNeighbors[i].add(e1);
                 reverseNeighbors[i].add(e2);
             }
              noOfEdges+=2;
          }

        }
       }
   }
   

   float distance(Node i, Node j){
       float x = Math.abs(i.getX()-j.getX());
       x = (float) (Math.pow(x, 2));
       float y = Math.abs(i.getY()-j.getY());
       y = (float) (Math.pow(y, 2));

       float res = (float) Math.sqrt(x+y);
       return res;
   }


 public void parseSourceandDestinations() throws FileNotFoundException
 {
      File file = new File(fileName);
      Scanner input = new Scanner(file);
      String nextLine="";
      String[] fields;
      String [] tempfields;

       while(input.hasNext()) {
        nextLine = input.nextLine();
        fields = nextLine.split(" ");
      // add source nodes
        if(fields[0].equals("Sources"))
        {
            noOfSources = Integer.parseInt(fields[1]);
            int sourceIndex=0;
            sources = new int[Integer.parseInt(fields[1])];
               while(input.hasNext()) {
                  nextLine = input.nextLine();
                  fields = nextLine.split(" ");
                  if(fields[0].equals("Destinations"))                  {
                      break;
                  }
                  
                  sources[sourceIndex++] = Integer.parseInt(nodes[Integer.parseInt(fields[0])].getId());
              }
             int destIndex=0;
               // now add destinations             
             destinations = new int[Integer.parseInt(fields[1])];
             noOfDestinations = Integer.parseInt(fields[1]);
                while(input.hasNext()) {
                  nextLine = input.nextLine();
                  fields = nextLine.split(" "); 
                  if(fields[0].equals("Demands")) {
                      break;
                   } 
                 
                  destinations[destIndex++] = Integer.parseInt(nodes[Integer.parseInt(fields[0])].getId());
                   
                  }
          } // end of adding sources and dests
     }
      printSourcesDestinations();
 }

  public void parseDemands() throws FileNotFoundException
 {
  
      File file = new File(fileName);
      Scanner input = new Scanner(file);
      String nextLine="";
      String[] fields;
      String [] tempfields;

       while(input.hasNext()) {
        nextLine = input.nextLine();
        fields = nextLine.split(" ");
      // add source nodes
        if(fields[0].equals("Demands"))
        { 
            noOfDemands = Integer.parseInt(fields[1]);
            int demandIndex=0;
            //System.out.println("Demands size is "+Integer.parseInt(fields[1]));
            Driver_Interdiction.demands = new float[Integer.parseInt(fields[1])];
               System.out.println("total demands "+noOfDemands);
               while(input.hasNext()) {
                  nextLine = input.nextLine();
                  fields = nextLine.split(" ");
                  if(fields[0].equals("END")) {
                      break;
                   } 
                  System.out.println("Demand: "+Integer.parseInt(fields[0]));
                  Driver_Interdiction.demands[demandIndex++] = Integer.parseInt(fields[0]);
                  System.out.println("demand "+Driver_Interdiction.demands[demandIndex-1]);
              }
       
          } // end of adding demands
     }
      printSourcesDestinations();
 }


 public void findCliques()
 {
   int currNode;        
   int nextHopNode;

  for(int node=0; node < getNofNodes(); node++)
  {
      currNode = node;            
      for(int i=0; i < forwardNeighbors(currNode).size();i++)
      {            
        nextHopNode = forwardNeighbors(currNode).get(i).getv2();
        LinkedList<Edge> currNodeLinks = new LinkedList<Edge>();
        //currNodeLinks = this.getAllLinksin2HopNeighborhood(currNode);
        currNodeLinks = this.getAllLinksin1HopNeighborhood(currNode);

        LinkedList<Edge> nextHopNodeLinks = new LinkedList<Edge>();
        //nextHopNodeLinks = this.getAllLinksin2HopNeighborhood(nextHopNode);
        nextHopNodeLinks = this.getAllLinksin1HopNeighborhood(nextHopNode);

        // now add all 2-hop links of curr node //
        for(int j=0; j < currNodeLinks.size(); j++)
        {
          forwardNeighbors[currNode].get(i).addToClique(currNodeLinks.get(j));
        }

        // now add all 2-hop links of next node //
        for(int j=0; j < nextHopNodeLinks.size(); j++)
        {
          forwardNeighbors[currNode].get(i).addToClique(nextHopNodeLinks.get(j));
        }               
        // print all links in maximal clique of current link //
        //printCliqueLinks(forwardNeighbors[currNode].get(i));
       } // end of for for each link of current node
   } // end of loop for every node of the network
     
 }// end of method findCliques
public void printCliqueLinks(Edge e)
{
  System.out.println("Clique links for link "+e.getv1()+"->"+e.getv2());
  for(int i=0; i < e.getCliqueSize(); i++)
  {
      System.out.println(e.getCliqueLink(i).getv1()+"->"+e.getCliqueLink(i).getv2());
  }
}

/* This method is called from ConstraintGenerator to find the remaining capapcity of clique for curr link */
public double getCliqueRemainingCapcity(int v1,int v2)
{
  double capacity=0;  
  Edge e = new Edge(1,1,0);
  
  /* First find out the appropriate link */
  for(int i=0; i < forwardNeighbors(v1).size();i++)
  {
     if(forwardNeighbors(v1).get(i).getv2()==v2)
     {
        e = forwardNeighbors(v1).get(i);
     }
  }
  /*Edge e = new Edge();*/
  System.out.println("Finding Capacity for link  "+e.getv1()+"->"+e.getv2());
  for(int i=0; i < e.getCliqueSize(); i++)
  {
      System.out.println(e.getCliqueLink(i).getv1()+"->"+e.getCliqueLink(i).getv2());
      //capacity+=e.getCliqueLink(i).
  }
  return capacity;
}

public LinkedList<Edge> getAllLinksin2HopNeighborhood(int currNode)
{
 int currNextHopNode;
 LinkedList<Edge> list = new LinkedList<Edge>();
  /* first add all links within 2 hops of current Node */
 for(int i=0; i < forwardNeighbors(currNode).size();i++)
 {
     list.add(forwardNeighbors[currNode].get(i));
     list.add(getReverseEdge(forwardNeighbors[currNode].get(i).getv2(),
                             forwardNeighbors[currNode].get(i).getv1()));
    currNextHopNode = forwardNeighbors(currNode).get(i).getv2();
    for(int j=0; j < forwardNeighbors[currNextHopNode].size(); j++)  // run for every neighbor
    {     
     // adding forward link from neighbor to neighbor-neghbor and reverse //
     list.add(forwardNeighbors[currNextHopNode].get(j));
     list.add(getReverseEdge(forwardNeighbors[currNextHopNode].get(j).getv2(),
                             forwardNeighbors[currNextHopNode].get(j).getv1()));
    }
 }

 return list;
 
}

public LinkedList<Edge> getAllLinksin1HopNeighborhood(int currNode)
{
 int currNextHopNode;
 LinkedList<Edge> list = new LinkedList<Edge>();
  /* first add all links within 2 hops of current Node */
 for(int i=0; i < forwardNeighbors(currNode).size();i++)
 {
     list.add(forwardNeighbors[currNode].get(i));
     list.add(getReverseEdge(forwardNeighbors[currNode].get(i).getv2(),
                             forwardNeighbors[currNode].get(i).getv1()));        
 }

 return list;
 
}
   public void printSourcesDestinations()
   {
     System.out.println("Source Nodes: ");
     for(int i=0; i < sources.length; i++)
     {
         System.out.println(sources[i]);
     }
     System.out.println("Destination Nodes: ");
     for(int i=0; i < destinations.length; i++)
     {
         System.out.println(destinations[i]);
     }
     System.out.println("\n ******");
   }
   
   /********************    Brute Force and Greedy Algorithms for Finding MIS for Physical Model *******/
   public void findInterferingNeighborsandMCDforAllNodes(){
       int currNode;
       float interference;
       for(int node=0; node < getNofNodes(); node++)
       {
          currNode = node; 
          nodes[currNode].InterferingNeighbors = new LinkedList<Integer>();
          nodes[currNode].InterferenceMeasure = new LinkedList<Float>();
          nodes[currNode].MCD = 0;
          for(int neighbor=0; neighbor < getNofNodes(); neighbor++)
          {
              if(neighbor != currNode)
              {
                float distance = distance(nodes[currNode],nodes[neighbor]); 
                if(distance <= Globals_Interdiction.distanceforBeingInterferingNeighbor)
                {
                    nodes[currNode].InterferingNeighbors.add(neighbor);
                    interference = 50000 / (distance * distance);
                    nodes[currNode].InterferenceMeasure.add(interference);
                    nodes[currNode].MCD += interference;
                }  
              }
          }
       }
       
       for(int thisNode=0; thisNode < getNofNodes(); thisNode++)
       {
           for(int neighbor = 0; neighbor < nodes[thisNode].InterferingNeighbors.size(); neighbor++)
           {
               nodes[thisNode].MCD += (2*nodes[thisNode].InterferenceMeasure.get(neighbor));
           }
       }
       
   }
   public void findMISGreedy(){
     int MISCounter=0;
     int index = 0;
     
     Icurr = (LinkedList<Integer>[]) new LinkedList[noOfNodes];
     for (int i=0; i<noOfNodes; i++){
        Icurr[i] = new LinkedList<Integer>();
      } 
     
     LinkedList<Integer> E = new LinkedList<Integer>();
      
     
     
     for(int node=0; node < getNofNodes(); node++){
         E.add(node); 
     }
     
     int outerindex = 0;
     int smallestGCDNode = getNextSmallestElement(E);
     int outernodeID =  E.get(smallestGCDNode);
     float SINR=0;
     //System.out.println("smallest GCD node is "+smallestGCDNode);
     //while(nodes[outernodeID].MCD <= 1 && E.size() > 0){
     while(E.size() > 0){
        LinkedList<Integer> Ecurr = new LinkedList<Integer>();
        for(int i=0; i < E.size(); i++){
         Ecurr.add(E.get(i)); 
        }     
        
        while(Ecurr.size() > 0){
            //for(int a=0; a < Ecurr.size(); a++)
                 //System.out.println(""+Ecurr.get(a));
          index = getNextSmallestElement(Ecurr);
          int nodeID = Ecurr.get(index);
          
          // if SINR of current node with its 
          
          // System.out.println("nextsmallest is returning"+nodeID );
          //if(nodes[nodeID].MCD <= 1){
        
             // System.out.println("Adding element "+nodeID+" to MIS");
              Icurr[MISCounter].add(nodeID );
              float SINRsoFar=0;
              
              float [] SINRArray= new float[nodes[nodeID ].InterferingNeighbors.size()];
            /*  for(int j=0; j < SINRArray.length; j++)
              {
                   float distance = distance(nodes[nodeID],nodes[j]);
                   float interference = 100000 / (distance * distance);
                   SINRArray[j]=interference;
              }
             */ 
              for(int x = 0; x < nodes[nodeID ].InterferingNeighbors.size(); x++){
                   
                  
                   int temppos = findPosition(Ecurr,nodes[nodeID ].InterferingNeighbors.get(x));
                   float distance = distance(nodes[nodeID],nodes[x]);
                   float interference = 100000 / (distance * distance);
                   //System.out.println("interfernece between "+nodes[v].getId()+" and "+nodes[w].getId()+" is "+interference);
                    SINRsoFar+=interference;
                   if(temppos != -1 && SINRsoFar > 1)
                     Ecurr.remove(temppos);  
              }
              
              if(getNextSmallestElement(Ecurr) != -1)
                Ecurr.remove(getNextSmallestElement(Ecurr));
              
              //System.out.println("Ecurr.size after removals is"+ Ecurr.size());
               
              //int pos = findPosition(Ecurr,index);
              //System.out.println("find position says pos is "+pos);
             //if(pos != -1)             
              
              
           
           //} else{break;}
          
        }
        
        
        if(E.size() > 0){
          E.remove(getNextSmallestElement(E));
        }
        
        smallestGCDNode = getNextSmallestElement(E);
        outerindex++;
        MISCounter++;
     } 
     
     /* Now delete the smaller MIS and only select the maximal ones with max size */
     /* Note, the deleted MISes will have their first element as zero */
     int max=0;
     for(int i = 0; i < Icurr.length; i++){
           if(Icurr[i].size() > max){
               max = Icurr[i].size();
           }
       }
       for(int i = 0; i < Icurr.length; i++){
           if(Icurr[i].size() < max){
              // System.out.println("******* coing here ");
                while (Icurr[i].isEmpty()==Boolean.FALSE) {
                      Icurr[i].removeFirst();
               }
           }
       }
       //System.out.println("MIS till this point");
       //printMISSoFarForGreedy();
        
     
   }
   
   public void printMISSoFarForGreedy(){
       for(int i = 0; i < Icurr.length; i++)
       {
           System.out.println("MIS-"+i);
          for(int j = 0; j < Icurr[i].size(); j++){
            System.out.println(""+Icurr[i].get(j) +",");   
          }  
       }
 }
   
   public void printLargestMISForGreedy(){

       int largest = 0;
       int index =0;
       for(int i = 0; i < Icurr.length; i++){
           if(Icurr[i].size()> largest)
           {
               largest = Icurr[i].size();
               index = i;
           }
        }
          System.out.println("Largest MIS through Greedy has "+largest+" elements : ");
          for(int j = 0; j < Icurr[index].size(); j++){
            System.out.println(""+Icurr[index].get(j) +",");   
          }  
       
   }
   
   /* gets the next smallest element in terms of GCD */
   public int getNextSmallestElement(LinkedList<Integer> G){
       float smallest = 1000000;
       int index = -1;
       for(int i=0; i <G.size(); i++){
           //System.out.println("MCD for ["+G.get(i)+"is "+nodes[i].MCD);
           if(nodes[G.get(i)].MCD < smallest)
           {
               index = i;
               smallest = nodes[G.get(i)].MCD;
           }
       }
       return index;
   }

   
   int findPosition(LinkedList<Integer> G, int value)
   {
       for(int i=0; i <G.size(); i++){
           if(G.get(i) == value)
             return i;
       }
              return -1;
   }
   
  
   
    public java.util.Set runBruteForceAlgoForMIS(){
    maxCardinality=0;
    System.out.println("Staring brute force !");
    SetOfAllMISForBrute= new HashSet();
    long ncomb = (long) Math.pow(2.0D, this.getNofNodes());
    
    Object[] nodes = getVertices().toArray();
    for (long i = 0L; i < ncomb; i += 1L)
    {
      Set Sp = new HashSet();
      String comb = Long.toBinaryString(i);
      for (int k = 0; k < comb.length(); k++) {
        if (comb.charAt(k) == '1') {
          Sp.add(nodes[k]);
        }
      }
      if ((isIndependentSet(Sp)) && (Sp.size() > SetOfAllMISForBrute.size())) {
        SetOfAllMISForBrute = Sp;
      }
    }
    return SetOfAllMISForBrute;
  }
    
  public boolean isIndependentSet(Set<Integer> S)
  {
    boolean b = true;
    float SINR=0;
    Iterator<Integer> itr = S.iterator();
    Iterator<Integer> otherNodes = S.iterator();
    while ((b) && (itr.hasNext()))
    {
      int v = itr.next();
      SINR=0;
      while(otherNodes.hasNext())
      {
          int w = otherNodes.next(); 
          float distance = distance(nodes[v],nodes[w]);
       
                if(v != w)
                {
                    float interference = 100000 / (distance * distance);
                   //System.out.println("interfernece between "+nodes[v].getId()+" and "+nodes[w].getId()+" is "+interference);
                    SINR+=interference;
                }   
      }
      //System.out.println("SINR: "+SINR);
      if(SINR > 1)
          b = false;
      /*Set<Integer> N = getListofNeighbors(v);
      if (N != null)
      {
        Set<Integer> Nv = new HashSet(N);
        if (!Sets.intersection(S, Nv).isEmpty()) {
          b = false;
        }
      }*/
    }
    if(b == true)
    {
       if(S.size() > maxCardinality){
           maxCardinality = S.size();
           System.out.println("For Brute Force Max Cardinality so far: "+maxCardinality);
       } 
       //System.out.println("MIS: ");
     /* itr = S.iterator();
      while ((b) && (itr.hasNext()))
      {
       int v = itr.next();
       System.out.println(""+v);
      }*/
    }
    return b;
  }
  
  public void printMISForBruteForce()
  {
       System.out.println("Brute Force");
       Iterator<Integer> itr = SetOfAllMISForBrute.iterator();
     while(itr.hasNext())
     {
         int v = itr.next();
         System.out.println(v+",");
     }
  }
  
  
}
