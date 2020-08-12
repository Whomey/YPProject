package YelpProject;
import org.json.*;
import java.io.*;
import java.util.HashMap;

public class Controller {

    JSONObject obj = new JSONObject();
    HashMap<String, Business> businesses = new HashMap<>();
    HashMap<String, String> bNameToID = new HashMap<>();

    public Controller() {}

    //later we will pass in file names
    public void parseFile( String fileName ) {

        try {
            int count = 0;
            BufferedReader br = new BufferedReader( new FileReader( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/YelpJSON/" + fileName ) );
            String line = br.readLine();

            //each line is a JSON object we can parse and eventually build similarities
            //parser should just return strings for the hashtable to handle (k/v : word/count)
            //from there base these tables into similarity metric
            while( ( line != null ) && ( count <= 10000 ) ) {
                //"review_id", "business_id", "stars", "useful", "text"

                JSONObject obj = new JSONObject( line );

                if( fileName.equals("review.json") ) {
                    //if the business doesn't exist, add it
                    if( ( obj.getString( "text" ) != "" ) && ( !businesses.containsKey( obj.getString( "business_id" ) ) ) ) {

                        Business tempR = new Business( obj.getString( "business_id" ) );

                        tempR.text = obj.getString( "text" );

                        //creates our hashmap of business and their review objects
                        businesses.put( obj.getString("business_id" ), tempR );

                        count++;
                    }
                    //otherwise just concat another reviews text
                    else if( businesses.containsKey( obj.getString( "business_id" ) ) ) {
                        businesses.get(obj.getString("business_id")).text += obj.getString("text");
                    }
                }

                if( fileName.equals("business.json") ) {

                        String tempbID = obj.getString("business_id");

                        if (businesses.get(tempbID) != null) {

                            String categories = "";

                            Float rating = obj.getFloat( "stars" );
                            String tempbName = obj.getString("name");
                            String tempbCity = obj.getString("city");
                            Double lat = obj.getDouble( "latitude" );
                            Double lon = obj.getDouble( "longitude" );

                            //need this to avoid null pointers with json objects
                            Object o = obj.get( "categories" );
                            if( !JSONObject.NULL.equals( o ) ) {
                                categories = (String) obj.get("categories");
                            }

                            businesses.get(tempbID).businessName = tempbName;
                            businesses.get(tempbID).city = tempbCity;
                            businesses.get(tempbID).latitude = lat;
                            businesses.get(tempbID).longitude = lon;
                            businesses.get(tempbID).rating = rating;

                            for( String word : categories.split( "," ) ) {
                                businesses.get( tempbID ).categories.add( word );
                            }

                            count++;
                        }
                }


                line = br.readLine();
            }

        } catch( Exception e ) {
            e.printStackTrace();
        }

        for( String s : businesses.keySet() ) {
            if( businesses.get( s ).businessName != null ) {
                bNameToID.put( businesses.get( s ).businessName + " : " + businesses.get( s ).city, s );
            }
        }

    }

    public String getRecommendation( Business selected ) {

        String tempBusiness = "";
        double similarity = 0;
        int i = 0;

        for( String id : businesses.keySet() ) {
            Business temp = businesses.get( id );
            if (temp.businessName != null && temp.businessName != selected.businessName) {
                if (temp.city.equals(selected.city) && temp.rating > 2.5) {
                    if( getSimilarity(selected, temp) > similarity ) {
                        tempBusiness = temp.businessName + " - City : " + temp.city;
                        similarity = getSimilarity(selected, temp);
                        i++;
                    }
                }
            }
        }

        return tempBusiness;
    }

    public double getSimilarity( Business s, Business t ) {

        System.out.println( s.businessName );
        System.out.println( t.businessName );

        CustomHashTable freqTable = new CustomHashTable();
        double sim = 0;

        //compare reviews, cities, and rating

        //build frequency table
        for (String word : t.text.split(" ")) {
            String temp = word.replaceAll("[()\".,;&^-]", "");
            if (!freqTable.exists( temp )) {
                freqTable.put( temp, frequency( t.text, temp ) );
            }
        }

        //calculate similarity by adding the count from the other doc into our overall similarity return
        for (String word : s.text.split(" ")) {
            String temp = word.replaceAll("[()\".,;&^-]", "");
            if ( freqTable.exists( temp ) ) {
                sim += freqTable.get( temp );
            }
        }

        return sim;
    }

    double frequency( String doc, String word ) {
        double res = 0;
        String[] docArray = doc.split(" ");

        for( String s : docArray ) {
            if( word.equals( s ) && s.length() >= 3 ) {
                res++;
            }
        }

        return res / docArray.length;
    }


}
