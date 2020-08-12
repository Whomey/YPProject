package YelpProject;

import java.util.ArrayList;
import java.util.Random;

public class KMeans {

    //book keeping for clusters
    ArrayList<Cluster> clusters;
    ArrayList<Point> points;

    KMeans() {
        this.clusters = new ArrayList<Cluster>();
        this.points = new ArrayList<Point>();
    }

    //pass in a set of random points that we will calculate later
    public void buildClusters() {
        Random rand = new Random();

        for( int i = 0; i < 5; i++ ) {
            Cluster tempC = new Cluster( i );
            tempC.centroid = points.get( rand.nextInt( points.size() ) );
            clusters.add( tempC );
        }

        /*for( int i = 0; i < 5; i++ ) {
            Cluster c = clusters.get( i );
            c.printCluster();
        }*/

    }

    public ArrayList<Point> getCentroids() {
        ArrayList<Point> centroids = new ArrayList<Point>();

        for( Cluster c : clusters ) {
            Point p = new Point( c.centroid.x, c.centroid.y );
            centroids.add( p );
        }

        return centroids;
    }

    private void clearClusters() {
        for( Cluster c : clusters ) {
            c.points.clear();
        }
    }

    //calculate centroid for each cluster
    private void calcCentroid() {
        for( Cluster c : clusters ) {
            double x = 0;
            double y = 0;

            ArrayList<Point> points = c.points;
            int pointCount = points.size();

            for( Point p : points ) {
                x += p.x;
                y += p.y;
            }

            Point centroid = c.centroid;

            if( pointCount > 0 ) {
                double nx = x / pointCount;
                double ny = y / pointCount;
                centroid.x = nx;
                centroid.y = ny;
            }

        }
    }

    //calculate k means
    public void calculate() {
        int i = 0;
        boolean loop = false;

        //recalculate for each added point
        while( !loop ) {
            //clear the clusters before new assignments
            clearClusters();

            ArrayList<Point> lastCentroids = getCentroids();

            //assign points to the correct cluster
            assignCluster();

            //new centroids
            calcCentroid();

            i++;

            ArrayList<Point> currCentroids = getCentroids();

            double distance = 0;
            for( int j = 0; j < lastCentroids.size(); j++ ) {
                distance += lastCentroids.get( j ).getDistance( lastCentroids.get( j ), currCentroids.get( j ) );
            }

            if( distance == 0 ) {
                loop = true;
            }

        }
    }

    private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max;

        int cluster = 0;
        double distance = 0;

        //calculate distance and assigns point to cluster
        for( Point p : points ) {
            min = max;
            for( int i = 0; i < 5; i++ ) {
                Cluster c = clusters.get( i );
                distance = p.getDistance( p, c.centroid );

                if( distance < min ) {
                    min = distance;
                    cluster = i;
                }
            }
            p.setCluster( cluster );
            clusters.get( cluster ).addPoint( p );
        }
    }

    public void addPoint( Point p ) {
        points.add( p );
    }

    public Point getNearest( Point p ) {
        Point temp = null;
        double m = Double.MAX_VALUE;

        for( Point point : points ) {
            if( !point.b.businessID.equals( p.b.businessID ) ) {
                //grab distance between the point we're looking at
                //and all other points
                double distance = p.getDistance( p, point );

                if( distance < m ) {
                    temp = point;
                    m = distance;
                }

            }
        }

        return temp;
    }

    //n^3 complexity, may want to cache in future
    ArrayList<Business> getCurrentCluster( Business b ) {
        ArrayList<Business> temp = new ArrayList<>();

        for( Cluster c : clusters ) {
            for( Point p : c.points ) {
                if( p.b.businessID.equals( b.businessID ) ) {
                    for( Point p2 : c.points ) {
                        temp.add( p2.b );
                    }
                }
            }
        }

        return temp;
    }

}
