package jp.ac.tohoku.ecei.sf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
 
/**
   エコークライアントの実装
 */
public class EchoClient implements Closeable { 
    private final Socket sock; 
    private final InputStream  is; 
    private final OutputStream os; 

    public EchoClient( String host, int port ) throws Exception {
        this ( new Socket( host, port ) );
    }
    
    public EchoClient( Socket sock ) throws Exception {
        this.sock = sock;
        this.is   = sock.getInputStream();
        this.os   = sock.getOutputStream();
    }

    public void communicate() {
        InputStream in = System.in;
        OutputStream out = System.out;
        byte buf[] = new byte[1];

        try {
            while ( in.read( buf ) > 0 ) { 
                this.os.write( buf );
                this.os.flush();

                if ( (this.is.read( buf ) ) <= 0 ) {
                    System.err.println("Connection is closed by the foreign host.");
                    break;
                }

                out.write( buf );
            }
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }                    
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }
}
