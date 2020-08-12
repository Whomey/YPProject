package YelpProject;

public class Point {

    Double x;
    Double y;
    Double posVal;
    Business b;
    int cluster;

    public Point( double x, double y, Business b ) {
        this.x = x;
        this.y = y;
        this.b = b;

        //used for calculating physical distance by using P2 - P1
        this.posVal = x + y;
    }

    //good for testing that cluster is working
    public Point( double x, double y ) {
        this.x = x;
        this.y = y;
    }

    public void setCluster( int cluster ) {
        this.cluster = cluster;
    }

    public double getDistance( Point p, Point centroid ) {
        return Math.sqrt( Math.pow( ( centroid.y - p.y  ), 2 )
                + Math.pow( ( centroid.x - p.x), 2 ));
    }

}
