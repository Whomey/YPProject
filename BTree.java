package YelpProject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.util.ArrayList;

class BTree implements java.io.Serializable {

    int blockCount;
    public Block root;
    int height;

    BTree() {

        //initialize tree with empty root (means I don't have to handle a null root case on insert)
        blockCount = 0;
        Block tempB = new Block( blockCount );
        tempB.currentKeySize = 0;
        tempB.leaf = 1;
        height = 0;

        root = tempB;
        root.writeBlock();
    }

    void insert( Business b ) {

        Block temp = root;

        //child is always n + 1, so our base case has to create a new block when we reach 31 blocks
        if ( root.currentKeySize == 31 ) {

            height++;
            blockCount++;
            Block newBlock = new Block( blockCount );

            //prepare for split
            root = newBlock;

            //move root node to first child node and assign it's offset
            newBlock.children[0] = temp.offset;
            newBlock.currentChildSize++;

            newBlock.currentKeySize = 0;
            newBlock.leaf = 0;

            //split rotations
            split( newBlock, temp );

            //now insert
            insertIntoExisting( newBlock, b );
        } else {
            //otherwise regular insert
            insertIntoExisting( temp, b );
        }
    }


    private void insertIntoExisting( Block bl, Business b ) {

        //we can't search/insert at n, use n - 1
        int i = bl.currentKeySize - 1;

        //if we're at a leaf we can insert
        if ( bl.leaf == 1 ) {

            //find the index, working from last block to avoid trying to swap 0 index
            while ( ( i > -1 ) && ( b.hashCode() < bl.bKeys[i].hashCode() ) ) {
                //shift keys up
                bl.bKeys[i + 1] = bl.bKeys[i];
                i--;
            }
            i++;

            //assign out business to this index
            //increment key count
            bl.bKeys[i] = b;
            bl.currentKeySize++;

            bl.writeBlock();

        } else {

            //otherwise find our index, read in that child block we're going to insert at until we find a root
            while ( ( i > -1 ) && ( b.hashCode() < bl.bKeys[i].hashCode() ) ) {
                i--;
            }

            i++;

            Block temp = bl.readBlock( bl.children[i] );

            //check for full block, if so we need to restructure before inserting
            if ( temp != null && temp.currentKeySize == 31 ) {

                //System.out.println("SPLIT!");
                split( bl, temp );

                //recurse with next child since we're still looking/placing
                if ( b.hashCode() > bl.bKeys[i].hashCode() ) {
                    temp = bl.readBlock( bl.children[i + 1] );
                }
            }

            //similar as base insert, using new block after we manipulate the tree with split
            if( temp != null ) {
                //System.out.println( temp.offset );
                insertIntoExisting( temp, b );
            }
        }
    }


    void writeTreeTF() throws IOException{

        RandomAccessFile raf = new RandomAccessFile( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/tree.ser", "rw" );
        raf.seek( 0 );
        FileChannel fc = raf.getChannel();
        ByteBuffer bb = ByteBuffer.allocate( 16000 );

        bb.putInt( blockCount );
        bb.putInt( height );

        bb.putLong( root.offset );
        bb.putInt( root.leaf );
        bb.putInt( root.currentKeySize );

        for ( int i = 0; i < root.currentKeySize; i++ ){

            Business current = root.bKeys[i];

            byte [] bName = current.businessName.getBytes();
            bb.putInt( bName.length );
            bb.put( bName );

            byte [] bid = current.businessID.getBytes();
            bb.putInt( bid.length );
            bb.put( bid );

            byte [] city = current.city.getBytes();
            bb.putInt( city.length );
            bb.put( city );

            bb.putDouble( current.latitude );
            bb.putDouble( current.longitude );

            bb.putFloat( current.rating );

            bb.putInt( current.categories.size() );

            for( String s : current.categories ) {
                byte[] cBytes = s.getBytes();
                bb.putInt( cBytes.length );
                bb.put( cBytes );
            }

        }

        bb.putInt( root.currentChildSize );

        for ( int i = 0; i < root.currentChildSize; i++ ){
            bb.putLong( root.children[i] );
        }


        bb.flip();
        fc.write(bb);
        bb.clear();
        fc.close();
        raf.close();

    }

    private void split( Block currBlock, Block childBlock ) {

        //moving around children and blocks
        //splitting up child block into two blocks, find the correct index for the new blocks and point at them
        //then write to file

        //essentially a bunch of shifting of keys
        blockCount++;

        //create temp block at next offset location
        Block tempBlock = new Block( blockCount );

        //set tempBlock's leaf based on the child blocks leaf value
        tempBlock.leaf = childBlock.leaf;

        //move second half of children block keys to our new block
        for ( int i = 0; i < 15; i++ ) {

            //System.out.println(K - 1);
            //System.out.println(i + K);

            //i + 16 points to the second half (for some reason this doesn't like i to just point right at 16 without breaking)
            tempBlock.bKeys[i] = childBlock.bKeys[i + 16];
            tempBlock.currentKeySize++;

            //otherwise set child blocks back to empty and decrease it's key size
            childBlock.bKeys[i + 16] = null;
            childBlock.currentKeySize--;
        }

        //if the child block is not a leaf we can move the second half of it's children to our new block
        if ( childBlock.leaf == 0 ) {

            for ( int i = 0; i < 16; i++ ) {

                tempBlock.children[i] = childBlock.children[i + 16];
                tempBlock.currentChildSize++;

                childBlock.children[i + 16] = null;
                childBlock.currentChildSize--;
            }
        }

        //similar to insert
        int k = currBlock.currentKeySize - 1;

        //find index if childblocks last block is less then the currblocks last block
        while ( k > -1 && childBlock.bKeys[15].hashCode() < currBlock.bKeys[k].hashCode() ) {
            //shift up currBlocks keys
            currBlock.bKeys[k + 1] = currBlock.bKeys[k];
            k--;
        }
        k++;

        currBlock.bKeys[k] = childBlock.bKeys[15];
        currBlock.currentKeySize++;

        childBlock.bKeys[15] = null;
        childBlock.currentKeySize--;

        //another shift up
        int j = currBlock.currentChildSize - 1;
        while ( j > k ) {
            currBlock.children[j + 1] = currBlock.children[j];
            j--;
        }

        j++;

        //place tempBlock at currBlocks found offset
        currBlock.children[j] = tempBlock.offset;
        currBlock.currentChildSize++;

        currBlock.writeBlock();
        childBlock.writeBlock();
        tempBlock.writeBlock();

    }


    static BTree readTreeFF() {

        try {

            Block temp = new Block( 0 );
            RandomAccessFile raf = new RandomAccessFile( "C:/Users/murph/IdeaProjects/CSC365Project2MMurphy/src/YelpProject/tree.ser", "rw" );
            raf.seek( 0 );

            FileChannel fc = raf.getChannel();
            ByteBuffer bb = ByteBuffer.allocate( 16000 );
            fc.read( bb );
            bb.flip();

            BTree bt = new BTree();
            bt.root = temp;

            bt.blockCount = bb.getInt();
            bt.height = bb.getInt();

            temp.offset = bb.getLong();
            temp.leaf = bb.getInt();

            temp.currentKeySize = bb.getInt();

            for ( int i = 0; i < temp.currentKeySize; i++ ) {

                int nameSize = bb.getInt();
                byte [] nBuffer = new byte[nameSize];
                bb.get( nBuffer );
                String bName = new String( nBuffer );

                int bidSize = bb.getInt();
                byte [] bidBuffer = new byte[bidSize];
                bb.get( bidBuffer );
                String bid = new String( bidBuffer );

                int citySize = bb.getInt();
                byte [] cBuffer = new byte[citySize];
                bb.get( cBuffer );
                String city = new String( cBuffer );

                Double latitude = bb.getDouble();
                Double longitude = bb.getDouble();

                float rating = bb.getFloat();

                ArrayList<String> categories = new ArrayList<>();
                int catSize = bb.getInt();

                for( int j = 0; j < catSize; j++ ) {
                    byte [] catBuffer = new byte[bb.getInt()];
                    bb.get( catBuffer );
                    categories.add( new String( catBuffer ) );
                }

                Business b = new Business( bid );
                b.rating = rating;
                b.businessName = bName;
                b.city = city;
                b.latitude = latitude;
                b.longitude = longitude;
                b.categories = categories;

                temp.bKeys[i] = b;
            }

            temp.currentChildSize = bb.getInt();
            for ( int i = 0; i < temp.currentChildSize; i++ ){
                temp.children[i] = bb.getLong();
            }

            bb.clear();
            fc.close();
            raf.close();
            return bt;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //using this to return the block a business is in for clustering
    //use return null as a "false" case
    Business getBusiness( String id, Block bl ) {
        int i = 0;

        int bid = Math.abs( id.hashCode()/10 );

        //find the index location within the block
        while ( ( i < bl.currentKeySize - 1 ) && ( bid > bl.bKeys[i].hashCode() ) ) {
            //System.out.println( bl.bKeys[i].businessName );
            //System.out.println( bl.bKeys[i].businessID );
            i++;
        }

        if( bl.bKeys[i] == null ) {
            System.out.println("NULL");
            return null;
        } else if ( ( i <= bl.currentKeySize ) && ( bid == bl.bKeys[i].hashCode() ) ) {
            //returns the block the key is in
            return bl.bKeys[i];
        } else if (bl.leaf == 1) {
            return null;
        } else {
            if ( bid < bl.bKeys[i].hashCode() ) {
                //System.out.println( bl.bKeys[i].businessName );
                //System.out.println( bl.bKeys[i].businessID );
                return getBusiness( id, bl.readBlock( bl.children[i] ) );
            } else {
                //System.out.println( bl.bKeys[i].businessName );
                //System.out.println( bl.bKeys[i].businessID );
                return getBusiness( id, bl.readBlock( bl.children[i+1] ) );
            }
        }
    }


}