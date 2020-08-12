package YelpProject;

import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Block implements Serializable {

    private long parentOffset;

    //business keys and values
    //using an array of business we can leverage hashing the objects to do BTree traversals
    //and searches, all while keeping Key/Value pairs in the node
    Business[] bKeys;

    //children/cKeys offset array
    Long[] children;

    //our bookkeeping for tree manipulation
    int currentKeySize;
    int currentChildSize;
    long offset;
    int leaf;

    public Block( long offset ) {
        this.offset = offset;
        this.bKeys = new Business[31];
        this.children = new Long[32];
        this.currentChildSize = 0;
        this.currentKeySize = 0;
    }

    public void writeBlock() {

        try {

            RandomAccessFile f = new RandomAccessFile( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/blocks.ser", "rw" );
            f.seek( this.offset * 8000 );
            FileChannel fc = f.getChannel();
            ByteBuffer bb = ByteBuffer.allocate( 8000 );

            bb.putLong( this.offset );
            bb.putInt( this.leaf );
            bb.putInt( this.currentKeySize );

            for ( int i = 0; i < this.currentKeySize; i++ ){

                Business current = this.bKeys[i];

                byte [] bid = current.businessID.getBytes();
                bb.putInt( bid.length );
                bb.put( bid );

                byte [] bName = current.businessName.getBytes();
                bb.putInt( bName.length );
                bb.put( bName );

                byte [] city = current.city.getBytes();
                bb.putInt( city.length );
                bb.put( city );

                bb.putFloat( current.rating );
                bb.putDouble( current.latitude );
                bb.putDouble( current.longitude );

                bb.putInt( current.categories.size() );

                for( String s : current.categories ) {
                    byte[] cBytes = s.getBytes();
                    bb.putInt( cBytes.length );
                    bb.put( cBytes );
                }

            }

            bb.putInt( this.currentChildSize );

            for ( int i = 0; i < this.currentChildSize; i++ ){
                bb.putLong( this.children[i] );
            }

            bb.flip();
            fc.write(bb);
            bb.clear();
            fc.close();
            f.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Block readBlock( long offset ) {

        try {

            Block temp = new Block( 0 );
            RandomAccessFile file = new RandomAccessFile( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/blocks.ser", "rw" );
            file.seek(offset * 8000 );
            FileChannel fc = file.getChannel();
            ByteBuffer bb = ByteBuffer.allocate( 8000 );

            fc.read( bb );
            bb.flip();

            temp.offset = bb.getLong();
            temp.leaf = bb.getInt();
            temp.currentKeySize = bb.getInt();

            for ( int i = 0; i < temp.currentKeySize; i++ ) {

                int bidSize = bb.getInt();
                byte [] bidBuffer = new byte[bidSize];
                bb.get( bidBuffer );
                String bid = new String( bidBuffer );

                int nameSize = bb.getInt();
                byte [] nBuffer = new byte[nameSize];
                bb.get( nBuffer );
                String bName = new String( nBuffer );

                int cSize = bb.getInt();
                byte [] cBuffer = new byte[cSize];
                bb.get( cBuffer );
                String city = new String( cBuffer );

                Float rating = bb.getFloat();
                Double latitude = bb.getDouble();
                Double longitude = bb.getDouble();

                ArrayList<String> categories = new ArrayList<>();
                int catSize = bb.getInt();

                for( int j = 0; j < catSize; j++ ) {
                    byte [] catBuffer = new byte[bb.getInt()];
                    bb.get( catBuffer );
                    categories.add( new String( catBuffer ) );
                }

                Business b = new Business( bid );
                b.rating = rating;
                b.latitude = latitude;
                b.longitude = longitude;
                b.businessName = bName;
                b.city = city;
                b.categories = categories;
                temp.bKeys[i] = b;
            }

            temp.currentChildSize = bb.getInt();
            for ( int i = 0; i<temp.currentChildSize; i++ ) {
                temp.children[i] = bb.getLong();
            }

            bb.clear();
            fc.close();
            file.close();
            return temp;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
