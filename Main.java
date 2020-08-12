package YelpProject;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;

public class Main extends  Application {

    TableView businessTable = new TableView();
    TableView spanningTable = new TableView();
    TableView listedPathTable = new TableView();
    Dijkstra dijk = new Dijkstra();
    Label disjoint = new Label();

    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();

    LineChart<Double,Double> pathVisual = new LineChart(xAxis,yAxis);;

    @Override
    public void start(Stage primaryStage) throws Exception {

        //set up graph data
        Graph g = new Graph();
        //Graph g2 = new Graph();

        g.buildGraphFromFile();
        g.reAssignEdges();

        //for memory safety with disjoint manipulation on sets while running
       // g2.buildGraphFromFile();
        //g2.reAssignEdges();

        //int dis = g2.disjoint( g2, 0 );

        ArrayList<Business> b = new ArrayList<Business>();

        for( int i = 0; i < g.getNodeCount(); i++ ) {
            System.out.println( g.nodes[i].b.businessName );
            b.add( g.nodes[i].b );
        }

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Business Suggest");
        GridPane gp = new GridPane();

        businessTable.setEditable(true);
        TableColumn bName = new TableColumn( "Business" );
        TableColumn bid = new TableColumn( "Business ID" );

        businessTable.getColumns().addAll( bName, bid );

        ObservableList<Business> bData = FXCollections.observableArrayList(
                b
        );

        bName.setCellValueFactory(new PropertyValueFactory<Business,String>("businessName"));
        bid.setCellValueFactory(new PropertyValueFactory<Business,String>("businessID"));
        businessTable.setItems( bData );

       Button generateRec = new Button( "Find Tree" );
       Button disPath = new Button( "Display Path" );

        spanningTable.setEditable(true);

       TableColumn name = new TableColumn("Business");
       TableColumn rating = new TableColumn("Rating");
       TableColumn city = new TableColumn("City");
       TableColumn lat = new TableColumn("Latitude");
       TableColumn lon = new TableColumn("Longitude");

        spanningTable.getColumns().addAll( name, rating, city, lat, lon );

       //nearest set up
        listedPathTable.setEditable(true);

        TableColumn nName = new TableColumn("Business");
        TableColumn nCity = new TableColumn("City");
        TableColumn nLat = new TableColumn("Latitude");
        TableColumn nLon = new TableColumn("Longitude");

        listedPathTable.getColumns().addAll( nName, nCity, nLat, nLon );

       generateRec.setOnAction(event -> {
           ObservableList selectedIndices = businessTable.getSelectionModel().getSelectedItems();

           for( int i = 0; i < spanningTable.getItems().size(); i++ ) {
               spanningTable.getItems().clear();
           }

           for( Object o : selectedIndices ) {

               //collecting all accessible paths from selected node
               Business bo = ( Business ) o;
               int location = bo.loc;
               GraphNode gn = g.nodes[location];

               dijk.dijkstra( g, gn );

               ArrayList<Business> connected = new ArrayList<>();

               for( int i = 0; i < g.getNodeCount(); i++ ) {
                   if( g.nodes[i].path.size() != 0 ) {
                       connected.add( g.nodes[i].b );
                   }
               }

               ObservableList<Business> data = FXCollections.observableArrayList(
                       connected
               );

               name.setCellValueFactory(new PropertyValueFactory<Business,String>("businessName"));
               rating.setCellValueFactory(new PropertyValueFactory<Business,Float>("rating"));
               city.setCellValueFactory(new PropertyValueFactory<Business,String>("city"));
               lat.setCellValueFactory(new PropertyValueFactory<Business,Double>("latitude"));
               lon.setCellValueFactory(new PropertyValueFactory<Business,Double>("longitude"));
               spanningTable.setItems(data);

           }
       });

        disPath.setOnAction(event -> {
            for( int i = 0; i < listedPathTable.getItems().size(); i++ ) {
                listedPathTable.getItems().clear();
            }
            pathVisual.getData().clear();

            Business srcNode = ( Business ) businessTable.getSelectionModel().getSelectedItem();
            Business dstNode = ( Business) spanningTable.getSelectionModel().getSelectedItem();

            //our set up
            GraphNode src = g.nodes[srcNode.loc];
            GraphNode dst = g.nodes[dstNode.loc];
            ArrayList<GraphNode> path = new ArrayList<GraphNode>();
            path.add( path.size(), dst );

            GraphNode tempNode = dst.parent;

            //bottom-up traverse build path to src
            while( tempNode != null ) {
                path.add( tempNode );
                tempNode = tempNode.parent;
            }

            //collect business data from path
            //can probably combine with the above
            ArrayList<Business> pathData = new ArrayList<>();
            for( int i = path.size() - 1; i > -1; i-- ) {
                pathData.add( path.get(i).b );
            }
            ObservableList<Business> ndata = FXCollections.observableArrayList(
                    pathData
            );


            nName.setCellValueFactory(new PropertyValueFactory<Business, String>("businessName"));
            nCity.setCellValueFactory(new PropertyValueFactory<Business, String>("city"));
            nLat.setCellValueFactory(new PropertyValueFactory<Business, Double>("latitude"));
            nLon.setCellValueFactory(new PropertyValueFactory<Business, Double>("longitude"));
            listedPathTable.setItems(ndata);

            XYChart.Series series = new XYChart.Series();

            for( Business bpd : pathData ) {
                System.out.println( bpd.businessName );
                System.out.println( bpd.longitude );
                System.out.println( bpd.latitude );
                double x = bpd.longitude;
                double y = bpd.latitude;
                series.getData().add( new XYChart.Data( x, y ) );
            }
            pathVisual.getData().add( series );


        });

        xAxis.setLabel("Longitude");
        yAxis.setLabel("Latitude");

        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        //disjoint.setText("Disjoint Sets: " + dis );
        pathVisual.autosize();
        businessTable.setPrefWidth( 400 );
        generateRec.setPrefWidth( 100 );
        disPath.setPrefWidth( 100 );
        spanningTable.setPrefWidth( 400 );
        spanningTable.setPrefHeight( 200 );
        listedPathTable.setPrefHeight( 400 );
        listedPathTable.setPrefHeight( 200 );

        //can place many objects in here
        HBox hbox = new HBox( businessTable, generateRec, disPath, spanningTable );
        HBox hbox2 = new HBox( listedPathTable, pathVisual );
        VBox vbox = new VBox( hbox2 );

        gp.add( hbox, 0, 0, 1, 1 );
        gp.add( vbox, 0, 1, 1, 1 );
        Scene scene = new Scene( gp, 1000, 600 );

        primaryStage.setScene( scene );
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);


        /*Controller j = new Controller();
        j.parseFile( "review.json" );
        //grabs name, id, and city
        j.parseFile( "business.json" );

        Graph builder = new Graph();

        ArrayList<Business> b = new ArrayList<Business>();

        int k = 0;
        for( String s : j.bNameToID.keySet() ) {
            String bid = j.bNameToID.get( s );
            builder.add( new GraphNode( j.businesses.get( bid ) ) );
            k++;
        }

        //just so stuff we no longer need to save edges and nodes to file
        builder.BuildGraph();

        for( int i = 0; i < 10000; i++ ) {
            if( builder.nodes[i] != null ) {
                builder.write(builder.nodes[i]);
                builder.writeEdges(builder.nodes[i]);
            }
        }*/

    }


}
