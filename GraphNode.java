package YelpProject;

import java.util.LinkedList;

public class GraphNode implements Comparable {

    double distance;
    int id;

    Edge[] edges;
    //because lower is closer, we assume infinite distance otherwise
    double minDistance = Double.POSITIVE_INFINITY;
    GraphNode parent;

    //has to be static to make sure we're not assigning id of 0 to every node
    private static int nodeCount = 0;
    LinkedList<GraphNode> path;
    Business b;

    GraphNode( Business b ) {
        this.id = nodeCount++;
        this.b = b;
        this.edges = new Edge[4];
        this.path = new LinkedList<>();
        this.distance = Double.MAX_VALUE;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {

        //earths radius
        final int radius = 6371;

        double latDist  = Math.toRadians( lat2 - lat1 );
        double lonDist = Math.toRadians( lon2 - lon1 );

        //conversion for accuracy
        lat1 = Math.toRadians( lat1 );
        lat2   = Math.toRadians( lat2 );

        double a = Math.pow( Math.sin( latDist / 2 ), 2 ) +
                Math.pow( Math.sin( lonDist / 2 ), 2 ) *
                Math.cos( lat1 ) * Math.cos( lat2 );

        double c = 2 * Math.asin(Math.sqrt(a));

        return c * radius;
    }

    //saves me a bunch of hassle for when I build my graph
    double calcDistance( GraphNode gn ){
        double distance = haversine( this.b.latitude, this.b.longitude, gn.b.latitude, gn.b.longitude );
        gn.distance = distance;
        return distance;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
