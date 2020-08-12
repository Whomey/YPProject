package YelpProject;

import java.util.PriorityQueue;

//more of a helper class since we don't really need this unless we're using it once or twice
public class Dijkstra {

    public void dijkstra( Graph g, GraphNode gn ) {

        //need to clear spanning tree after creation
        //more for gui, but prevents issues
        for( int i = 0; i < g.getNodeCount(); i++ ) {
            if( g.nodes[i].path.size() != 0 ) {
                g.nodes[i].path.clear();
            }
        }

        //we can't revisit nodes we've already had edges to
        PriorityQueue<GraphNode> untouched = new PriorityQueue<GraphNode>();

        //starting node can't have a distance set
        gn.minDistance = 0;

        //add our node as starting point for tree
        untouched.add( gn );

        while ( !untouched.isEmpty() ) {

            //first node in the pq
            GraphNode curr = untouched.poll();

            //start the at 'root' node and check all of it's edges
            for ( Edge next : curr.edges ) {

                //create distance from edge list node
                Double tempDist = curr.minDistance + next.weight;

                //so we can compare against the other nodes distance
                GraphNode peekNode = next.loc;

                //if our new minDistance is better
                if ( tempDist < peekNode.minDistance ) {

                    GraphNode tempNode = next.loc;

                    //remove from our pq since we've visited
                    untouched.remove( tempNode );

                    //set up new distance and clear the old path
                    tempNode.minDistance = tempDist;
                    tempNode.path.clear();

                    //add/update path
                    tempNode.path.addAll( curr.path );

                    //add current node to path
                    tempNode.path.add( curr );
                    peekNode.parent = curr;
                    untouched.add( tempNode );
                }
            }
        }
    }

}
