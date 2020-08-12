package YelpProject;

public class Edge {

    GraphNode loc;
    double weight;

    public Edge( GraphNode loc ) {
        //testing point
        this.loc = loc;
    }

    public Edge( GraphNode loc, double weight ) {
        this.loc = loc;
        this.weight = weight;
    }

}
