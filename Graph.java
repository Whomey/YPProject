package YelpProject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class Graph {

    GraphNode[] nodes;

    Graph() {
        this.nodes = new GraphNode[10000];
    }

    public void add( GraphNode gn ) {
        if ( nodes[gn.id] == null ) {
            nodes[gn.id] = gn;
        }
    }

    //because there's a lot of manipulation/swapping, we need to actually count non-null nodes in graph
    int getNodeCount() {
        int count = 0;

        for ( GraphNode node : nodes ) {
            if ( node != null ) {
                count++;
            }
        }
        return count;
    }

    //creating our edge list
    //not used since we are reading from file
    public void BuildGraph() {

        //fill up the neighbor list, replace if the largest distance in neighbor list greater than distance we're currently looking at
        for ( int i = 0; i < getNodeCount(); i++ ) {

            System.out.println( "Building Edge: " + i + " - " + this.nodes[i].b.businessName );
            GraphNode[] neighborList = new GraphNode[4];

            GraphNode currNode = this.nodes[i];
            //System.out.println( currNode.b.businessName );

            for( int j = 0; j < getNodeCount() - 1; j++ ) {
                if( i != j ) {
                    //System.out.println(j);
                    //System.out.println(this.nodes[j].b.businessName);

                    //distance of currnode to the node we're looking at using haversine
                    double dist = currNode.calcDistance( this.nodes[j] );
                    //System.out.println(dist);

                    //probably not the best time complexity but since we're just looking at 4 we should be fine
                    for( int k = 0; k < neighborList.length; k++ ) {
                        if( neighborList[k] == null ) {
                            neighborList[k] = this.nodes[j];
                        } else {
                            Arrays.sort( neighborList );
                            //find spot if this distance is less than any of the node distances in neighbor array
                            //we replace that neighbor with the new one
                            int spot = 0;
                            while( spot != 4 ) {
                                if ( dist > currNode.calcDistance( neighborList[spot] )  ) {
                                    spot++;
                                } else {
                                    neighborList[spot] = this.nodes[j];
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            //now add this edge to our nodes list of edges
            //System.out.println( currNode.b.businessName );
            for( int f = 0; f < currNode.edges.length; f++ ) {
                //System.out.println( neighborList[f] );
                //System.out.println( currNode.calcDistance( neighborList[f] ) );
                //distance to our node can't be 0 unless it's the same node or we're just dealing with two double.max_values
                currNode.edges[f] = new Edge( neighborList[f], neighborList[f].distance );
            }
            //System.out.println( "\n" );


        }
    }

    //reassigns read in edges to read in graph
    void reAssignEdges() {
        for ( int i = 0; i < this.getNodeCount(); i++ ) {
            GraphNode current = nodes[i];
            for ( int j = 0; j < current.edges.length; j++ ) {
                current.edges[j].loc = this.nodes[current.edges[j].loc.id];
            }
        }
    }

    //had to take outside of recursion to avoid issues
    private int lastIndex() {
        int lastIndex = 0;

        for ( int i = 0; i < nodes.length; i++ ) {
            if ( nodes[i] != null ) {
                lastIndex = i;
            }
        }

        return lastIndex;
    }

    private GraphNode getFirstGivenIndex(Graph g, int index) {
        for ( int i = index; i < g.nodes.length; i++ ) {
            if ( g.nodes[i] != null ) {
                return g.nodes[i];
            }
        }
        return null;
    }

    //recursively reduce all subsets
    //best solution I could work out on short notice but seems to work
    int disjoint( Graph g, int dSets ) {

        GraphNode temp = getFirstGivenIndex( g, dSets );
        Graph subset = new Graph();
        Dijkstra d = new Dijkstra();

        //obvious base case for null/no nodes
        if ( temp == null || g.getNodeCount() == 0 ) {
            return dSets;
        }

        //develop path for node
        d.dijkstra( this, temp );

        for ( int i = temp.id; i < g.lastIndex(); i++ ) {
            if ( g.nodes[i] != null ) {
                //if no path for this we can add it to the subset
                //essentially we're cutting down the number of unique paths left expanded from the current nodes path
                //starting from root
                if ( g.nodes[i].path.size() == 0 ) {
                    subset.add( g.nodes[i] );
                }
            }
        }

        dSets++;
        return disjoint( subset, dSets );

    }

    //pretty much the same as my implementation for blocks
    //nodes/edges
    public void write( GraphNode gn ) {
        try {
            RandomAccessFile file = new RandomAccessFile( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/nodes.ser", "rw");
            file.seek(gn.id * 1024 );
            FileChannel fc = file.getChannel();
            ByteBuffer bb = ByteBuffer.allocate( 1024 );
            Business current = gn.b;

            bb.putInt( gn.id );

            byte[] id = current.businessID.getBytes();
            bb.putInt( id.length );
            bb.put( id );

            byte[] name = current.businessName.getBytes();
            bb.putInt( name.length );
            bb.put( name );

            byte[] city = current.city.getBytes();
            bb.putInt( city.length );
            bb.put( city );

            bb.putFloat( current.rating );

            bb.putDouble( current.latitude );
            bb.putDouble( current.longitude );

            //put dest id of edges so reader knows where to go read, and put weight
            bb.putInt( gn.edges.length );

            for ( int i = 0; i < gn.edges.length; i++ ) {
                bb.putInt( gn.edges[i].loc.id );
                bb.putDouble( gn.edges[i].weight );
            }

            bb.flip();
            fc.write( bb );
            bb.clear();
            fc.close();
            file.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    public void writeEdges( GraphNode gn ) throws IOException {

        RandomAccessFile file = new RandomAccessFile( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/edges.ser", "rw");

        //64 should be enough for just edge data
        file.seek(gn.id * 64 );
        FileChannel fc = file.getChannel();
        ByteBuffer bb = ByteBuffer.allocate( 64 );

        bb.putInt( gn.edges[0].loc.id );
        bb.putInt( gn.edges[1].loc.id );
        bb.putInt( gn.edges[2].loc.id );
        bb.putInt( gn.edges[3].loc.id );

        bb.flip();
        fc.write( bb );
        bb.clear();
        fc.close();
        file.close();

    }

    static GraphNode read( long offset ) {

        try {
            GraphNode gn = new GraphNode( null );
            RandomAccessFile file = new RandomAccessFile( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/nodes.ser", "rw");
            file.seek(offset * 1024 );
            FileChannel fc = file.getChannel();
            ByteBuffer bb = ByteBuffer.allocate( 1024 );
            fc.read(bb);
            bb.flip();

            gn.id = bb.getInt();

            int gidSize = bb.getInt();
            byte[] gidBuffer = new byte[gidSize];
            bb.get( gidBuffer );
            String id = new String( gidBuffer );

            int nameSize = bb.getInt();
            byte[] nameBuffer = new byte[nameSize];
            bb.get( nameBuffer );
            String name = new String( nameBuffer );

            int citySize = bb.getInt();
            byte[] cityBuffer = new byte[citySize];
            bb.get( cityBuffer );
            String city = new String( cityBuffer );

            Float rating = bb.getFloat();

            Double latitude = bb.getDouble();
            Double longitude = bb.getDouble();

            int numEdges = bb.getInt();
            for ( int i = 0; i < numEdges; i++ ) {
                gn.edges[i] = new Edge( new GraphNode(null ) );
                gn.edges[i].loc.id = bb.getInt();
                gn.edges[i].weight = bb.getDouble();
            }

            Business b = new Business( name );

            b.businessID = id;
            b.businessName = name;
            b.longitude = longitude;
            b.latitude = latitude;
            b.city = city;
            b.rating = rating;
            gn.b = b;

            bb.clear();
            fc.close();
            file.close();

            return gn;

        } catch ( IOException e ) {
            System.out.println("Null Read of Node");
            return null;
        }

    }

    //builds our graph from file, loc is our index of how we're reading it as well
    void buildGraphFromFile() {

        //exact size of objects in file, otherwise underflow
        for ( int i = 0; i < 9641; i++ ) {

            this.nodes[i] = Graph.read(i);

            if( this.nodes[i] != null ) {
                this.nodes[i].b.loc = i;
            }

        }
    }
}
