import Model.Entity;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import net.sf.jsqlparser.JSQLParserException;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class GraphComponent {

//    EntitiesGraph graphBuilder;
//
//    public GraphComponent(EntitiesGraph graphBuilder) {
//        this.graphBuilder = graphBuilder;
//    }

    public mxGraphComponent getApplet(Integer srcid){
        ListenableGraph<Entity, DefaultEdge> g = null;
        try {
            EntitiesGraph entitiesGraph = new EntitiesGraph(srcid);
            g = entitiesGraph.getEntityGraph();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        Dimension DEFAULT_SIZE = new Dimension(1000, 800);
        JGraphXAdapter<Entity, DefaultEdge> jgxAdapter;

        JApplet applet = new JApplet();
        // create a JGraphT graph
        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter<>(g);

        applet.setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);

        // positioning via jgraphx layouts
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);

        // center the circle
        int radius = 40;
        //layout.setX0((DEFAULT_SIZE.width / 2.0 ) - radius - 100);
        //layout.setY0((DEFAULT_SIZE.height / 2.0 ) - radius );
        layout.setRadius(radius);
        //layout.setMoveCircle(true);

        //STYLE_NOEDGESTYLE
        //ol.set(true);
        layout.execute(jgxAdapter.getDefaultParent());

//        mxHierarchicalLayout mxLayout = new mxHierarchicalLayout(jgxAdapter);
//        mxLayout.execute(jgxAdapter.getDefaultParent());
//        mxLayout.

        return component;
    }
}
