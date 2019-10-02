/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphpackageinterdiction;

import com.itextpdf.text.PageSize;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GephiGraph_Interdiction {

private Graph_Interdiction g;
private String file;

  public GephiGraph_Interdiction(String f)
  {
    file = f;
  }

 public void printGraphToPDF(Graph_Interdiction g){
  /* Initialization stuff for Gephi */
  ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
  pc.newProject();
  Workspace workspace = pc.getCurrentWorkspace();
  GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
  Node[] nodeArray = new Node[g.getNofNodes()];
  DirectedGraph directedGraph = graphModel.getDirectedGraph();
          
  /* allocate space for and create the nodes */
  for(int i=0; i < g.getNofNodes();i++){
      nodeArray[i] = graphModel.factory().newNode(String.valueOf(i));
      nodeArray[i].getNodeData().setLabel(String.valueOf(i));
      nodeArray[i].getNodeData().setX(g.nodes[i].getX());
      nodeArray[i].getNodeData().setY(g.nodes[i].getY());
      nodeArray[i].getNodeData().setSize(20);
      //nodeArray[i].getNodeData().setColor(0f, 0f, 0f); // black color
      nodeArray[i].getNodeData().setColor(0.1f, 0.1f, 0.1f);   // white color
      //nodeArray[i].getNodeData().setColor(255f, 255f, 255f);
      //System.out.println("nodeArray["+i+"] ="+nodeArray[i]+" X="+g.nodes[i].getX()+" Y="+g.nodes[i].getY());
      directedGraph.addNode(nodeArray[i]);            
  }
  for(int i=0; i < g.getNofNodes();i++){
      for(int j=0; j < g.forwardNeighbors[i].size();j++){
        int firstNode = g.forwardNeighbors[i].get(j).getv1();
        int secNode = g.forwardNeighbors[i].get(j).getv2();
        //System.out.println("first node="+firstNode+"sec node = "+secNode);
        Edge e = graphModel.factory().newEdge(nodeArray[firstNode],nodeArray[secNode]);
       
        e.getEdgeData().setColor(0f, 0f, 0f);       
        e.getEdgeData().setSize(0.00f);
        directedGraph.addEdge(e);
      }
  }


     
  
     PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
     PreviewModel previewModel = previewController.getModel();    
     previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
     previewModel.getProperties().putValue(PreviewProperty.DIRECTED, Boolean.FALSE);
     previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS,new Float(0.001f));     
     previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS,Boolean.TRUE);         
     //previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_OUTLINE_SIZE,30);
     //previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE,Boolean.TRUE);
     //previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS,Boolean.TRUE);
     //previewModel.getProperties().putValue(PreviewProperty.,Boolean.TRUE);
     previewModel.getProperties().putValue(PreviewProperty.CATEGORY_EDGE_ARROWS,Boolean.FALSE);
     previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(EdgeColor.Mode.ORIGINAL));
     //previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.DARK_GRAY));
     //previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGES,Boolean.FALSE);
     //previewModel.getProperties().putValue(PreviewProperty.ARROW_SIZE,25);     
     previewController.refreshPreview();
      
      /* GePhi stuff for writing a PDF */
   ExportController ec = Lookup.getDefault().lookup(ExportController.class);
    try { ec.exportFile(new File(file));
      } catch (IOException ex) {
         ex.printStackTrace();
         return;
      }
    //PDF Exporter config and export to Byte array
    PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
    pdfExporter.setPageSize(PageSize.A0);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ec.exportStream(baos, pdfExporter);
    byte[] pdf = baos.toByteArray();

 }

 public void printGraphToPDFAndKillDeadNodes(Graph_Interdiction g){
  /* Initialization stuff for Gephi */
  ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
  pc.newProject();
  Workspace workspace = pc.getCurrentWorkspace();
  GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
  Node[] nodeArray = new Node[g.getNofNodes()];
  DirectedGraph directedGraph = graphModel.getDirectedGraph();
          
  /* allocate space for and create the nodes */
  for(int i=0; i < g.getNofNodes();i++){
      nodeArray[i] = graphModel.factory().newNode(String.valueOf(i));
      nodeArray[i].getNodeData().setLabel(String.valueOf(i));
      nodeArray[i].getNodeData().setX(g.nodes[i].getX());
      nodeArray[i].getNodeData().setY(g.nodes[i].getY());
      nodeArray[i].getNodeData().setSize(15);
      //nodeArray[i].getNodeData().setColor(0f, 0f, 0f); // black color
      
      if(Globals_Interdiction.nodeIsAlive[i]==0)
      {
          //System.out.println("Node "+i+" is colored diff");
          //nodeArray[i].getNodeData().setColor(0.75f, 0.75f, 0.75f);   // white color
          nodeArray[i].getNodeData().setColor(0.0f, 0.0f, 0.0f);   // white color
      }else {
          //nodeArray[i].getNodeData().setColor(0.25f, 0.25f, 0.25f);   // white color
          nodeArray[i].getNodeData().setColor(0.0f, 0.0f, 0.0f);   // white color
      }
      //nodeArray[i].getNodeData().setColor(255f, 255f, 255f);
      //System.out.println("nodeArray["+i+"] ="+nodeArray[i]+" X="+g.nodes[i].getX()+" Y="+g.nodes[i].getY());
      directedGraph.addNode(nodeArray[i]);            
  }
  for(int i=0; i < g.getNofNodes();i++){
      for(int j=0; j < g.forwardNeighbors[i].size();j++){
        int firstNode = g.forwardNeighbors[i].get(j).getv1();
        int secNode = g.forwardNeighbors[i].get(j).getv2();
        //System.out.println("first node="+firstNode+"sec node = "+secNode);
        Edge e = graphModel.factory().newEdge(nodeArray[firstNode],nodeArray[secNode]);
       
        e.getEdgeData().setColor(0f, 0f, 0f);       
        e.getEdgeData().setSize(0.00f);
        directedGraph.addEdge(e);
      }
  }


     
  
     PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
     PreviewModel previewModel = previewController.getModel();    
     previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
     previewModel.getProperties().putValue(PreviewProperty.DIRECTED, Boolean.FALSE);
     previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS,new Float(0.001f));     
     previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS,Boolean.TRUE);         
     //previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_OUTLINE_SIZE,30);
     //previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE,Boolean.TRUE);
     //previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS,Boolean.TRUE);
     //previewModel.getProperties().putValue(PreviewProperty.,Boolean.TRUE);
     previewModel.getProperties().putValue(PreviewProperty.CATEGORY_EDGE_ARROWS,Boolean.FALSE);
     previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(EdgeColor.Mode.ORIGINAL));
     //previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.DARK_GRAY));
     //previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGES,Boolean.FALSE);
     //previewModel.getProperties().putValue(PreviewProperty.ARROW_SIZE,25);     
     previewController.refreshPreview();
      
      /* GePhi stuff for writing a PDF */
   ExportController ec = Lookup.getDefault().lookup(ExportController.class);
    try { ec.exportFile(new File(file));
      } catch (IOException ex) {
         ex.printStackTrace();
         return;
      }
    //PDF Exporter config and export to Byte array
    System.out.println("writing the pdf");
    PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
    pdfExporter.setPageSize(PageSize.A0);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ec.exportStream(baos, pdfExporter);
    byte[] pdf = baos.toByteArray();

 }

}
