package jp.ac.tohoku.ecei.sf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
   スレッドを使わないエコーサーバの実装．
   同時に通信できるのは一つのクライアントだけなので
   サーバとしての利便性は低い．コードを読む用．
 */
public class EchoServerSingle implements Closeable {
    private final ServerSocket sock; 
    
    public EchoServerSingle( int port ) throws Exception {
        this.sock = new ServerSocket( port );
        this.sock.setReuseAddress( true );
    }


    static protected void interact( InputStream is, OutputStream os ) {
        byte[] buf = new byte[1];
        try {
            while ( is.read( buf, 0, 1 ) > 0 ) {
                if ( buf[0] == 0x04 ) { // Ctrl+D 
                    break;
                }
                os.write( buf ); 
            }
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void waitConnection() {
        try {
            while ( true ) {
                final Socket conn = sock.accept();
                try {
                    InputStream  is = conn.getInputStream();
                    OutputStream os = conn.getOutputStream(); 
                    interact( is, os );
                    conn.close();
                }
                catch ( IOException e ) {
                    System.out.println("Something wrong happens: Shutdown the connection from " + conn + ".");
                    conn.close();
                }
            }
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            sock.close(); 
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

