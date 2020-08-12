package YelpProject;

import java.io.Serializable;
import java.util.ArrayList;

public class Business implements Serializable {

    float rating;
    String text;
    String businessName;
    String businessID;
    String city;
    Double latitude;
    Double longitude;
    ArrayList<String> categories;

    //our node id for the business
    int loc;

    public Business(String businessID ) {
        this.businessID = businessID;
        this.businessName = "";
        this.city = "";
        this.rating = 0;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.categories = new ArrayList<>();
    }

    //need this to compare businesses based on id for bTree
    //helps me search for businesses in my tree as well
    @Override
    public int hashCode() {
        return Math.abs( businessID.hashCode() / 10 );
    }

    public String getBusinessName() {
        return businessName;
    }

    public Float getRating() {
        return rating;
    }

    public String getCity() {
        return city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getBusinessID() { return businessID; }

    public int getLocation() { return loc; };
}
