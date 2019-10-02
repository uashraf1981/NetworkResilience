/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphpackageinterdiction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Administrator
 */
public class GraphViz_Interdiction {
    private String outputFile,inputFile;
    private Graph_Interdiction g;
    private File outFile;
    BufferedWriter writer;

     

    public GraphViz_Interdiction(String outFile, Graph_Interdiction graphObj) throws IOException, InterruptedException{
      outputFile = outFile;
      g = graphObj;
      generateDOTGraph();
      writer.close();
      runGraphViz();
    }
    public void runGraphViz() throws IOException, InterruptedException
    {                
        Process p = Runtime.getRuntime().exec("gvedit "+outputFile);
        //Thread.sleep(1000);
        //p.destroy();
    }
    public void generateDOTGraph() throws IOException
    {
        // preliniary stuff //    
        outFile = new File(outputFile);
        writer = new BufferedWriter(new FileWriter(outFile));
        writer.write("digraph G {\n");
        int currNode;
        int K = g.getK();
        String constraintString;

     // First write node positions in file */
     for(int i=0; i < g.getNofNodes(); i++)
     {
       // write X, Y coordinates of current node // [pos="0,0!",pin="true"];        
       writer.write(""+g.getNodeId(i)+" [pos=\""+g.getNodeXPosition(i)+","+
               g.getNodeYPosition(i)+"!\",pin=\"true\"] ;"+"\n");
     }
     writer.write("\n");
     //now handle the links     format:  A -> B  [ label=" AB" ] ;
     for(int i=0; i < g.getNofNodes(); i++)
     {
        currNode = i;
         for(int j=0; j < g.forwardNeighbors(currNode).size(); j++)
         {
                constraintString = "";
                //for(int k=0; k < K; k++)
               // {
                  //  constraintString += g.forwardNeighbors(currNode).get(j).getVariable(k)+" ";
                  constraintString += g.forwardNeighbors(currNode).get(j).getCapacity();
                //}
                  writer.write(""+g.getNodeId(currNode)+"->"+g.forwardNeighbors(currNode).get(j).getv2()+
                          " [ label=\" "+constraintString+"\" ] ;\n");
                
          }

      }
     writer.write("\n }");
  }

}
