package YelpProject;

import java.io.Serializable;
import java.util.ArrayList;

public class CustomHashTable implements Serializable {

    int capacity;
    int currentCap;
    double hashCap;
    ArrayList<String> keySet;
    Node[] table;

    //capacity should be around 100-200 for us (base 2), considering there's not that many words going to be used
    public CustomHashTable() {

        this.currentCap = 0;
        this.capacity = 512;
        this.hashCap = .75;
        table = new Node[capacity];
        keySet = new ArrayList<String>();

    }

    public int customHash( String word ) {
        int hash = 0;

        for( char c : word.toCharArray() ) {
            hash = hash ^ ( c * 129 );
        }

        return Math.abs( hash % capacity );
    }

    public void put( String key, double value ) {

        //check if exists, if yes, iterate, if no insert, after each insert, check if we need to resize
        int hashcode = customHash( key );
        Node newNode = new Node( key, value );

        //for traversal
        Node temp;

        //base case
        if( table[hashcode] == null ) {
            table[hashcode] = newNode;

            //key bookkeeping
            addKey( keySet, key );

        }
        else {
            //if it does exists we point to the head of the linked structure
            temp = table[hashcode];

            while( true ) {
                if( temp.next == null ) {
                    temp.next = new Node( key, value );
                    addKey( keySet, key );

                    //check/iterate cap for rehash
                    currentCap++;

                    if( currentCap >= ( table.length * hashCap ) ) {
                        rehash();
                    }

                    return;
                }
                else {
                    temp = temp.next;
                }
            }

        }

    }

    private void rehash() {
        //expanding table cap by rehashing and inserting old

        //hold old table
        Node[] oldTable = table;
        Node next;
        //reset old cap
        currentCap = 0;
        //reset table to new size
        table = new Node[table.length * 2];

        //for each node in the 'old table', put it's nodes in the new one
        for( Node n : oldTable ) {
            next = n;
            while( true ) {
                if( next == null ) {
                    break;
                }
                else {
                    this.put( next.key, next.value );
                    next = next.next;
                }
            }
        }
    }

    public double get( String key ) {

        int hashCode = customHash( key );

        if( table[hashCode] == null ) {
            return 0;
        }
        else {
            Node temp = table[hashCode];

            while( true ) {
                //base case
                if( temp == null ) {
                    return 0;
                }
                else if( key.equals( temp.key ) ) {
                    return temp.value;
                }
                else {
                    temp = temp.next;
                }
            }
        }
    }

    public boolean exists( String key ) {
        //same as get but just returns bool

        int hashCode = customHash( key );

        if( table[hashCode] == null ) {
            return false;
        }
        else {
            Node temp = table[hashCode];

            while( true ) {
                //base case
                if( temp == null ) {
                    return false;
                }
                else if( key.equals( temp.key ) ) {
                    return true;
                }
                else {
                    temp = temp.next;
                }
            }
        }
    }


    //helper function
    public void addKey( ArrayList<String> keySet, String key ) {
        if( !keySet.contains( key ) ) {
            keySet.add( key );
        }
    }

    public void printTable() {

        for( String s : keySet ) {
            System.out.println( s + " : " + get( s ) );
        }

    }

}
