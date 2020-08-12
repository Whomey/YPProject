package YelpProject;

import java.util.ArrayList;
import java.util.Random;

public class Cluster {

    //using K-means clustering

    Point centroid;
    ArrayList<Point> points;
    int clusterID;


    public Cluster( int clusterID ) {
        this.centroid = null;
        this.clusterID = clusterID;
        this.points = new ArrayList<Point>();
    }

    public void setCentroid( Point c ) {
        centroid = c;
    }

    public void addPoint( Point p ) {
        points.add( p );
    }

    public ArrayList<Point> getCluster() {
        return points;
    }

    public void printCluster() {
        System.out.println( "Cluster: " + clusterID );
        System.out.println( "CentroidX: " + centroid.x );
        System.out.println( "CentroidY: " + centroid.y );
        System.out.println( "Points: " );
        for( Point p : points ) {
            System.out.println( p.b.businessID );
            System.out.println( p.b.businessName );
            System.out.println( p.x );
            System.out.println( p.y );
            System.out.println("\n");
        }
        System.out.println("\n");
    }

}
