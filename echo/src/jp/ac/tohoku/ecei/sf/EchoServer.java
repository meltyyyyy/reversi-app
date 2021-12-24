package jp.ac.tohoku.ecei.sf;

import java.net.*;
import java.io.OutputStream; 
import java.io.InputStream;
import java.io.Closeable; 
import java.io.IOException;
import java.util.concurrent.*;
 
/**
   エコーサーバの実装．
 */
public class EchoServer implements Closeable {
    private final int MAX_THREAD = 20; 
    private final ServerSocket sock; 

    /* スレッドプール */
    private final ExecutorService pool;  

    EchoServer( int port ) throws Exception {
        sock = new ServerSocket( port ); 
        this.pool = Executors.newFixedThreadPool( MAX_THREAD ); 
    }

    /**
       ワーカスレッド
     */
    class Worker implements Runnable {
        private final Socket conn; 
        public Worker( Socket s ) {
            conn = s; 
        } 

        private void interact( InputStream is, 
                               OutputStream os ) {
            final long threadId = Thread.currentThread().getId();
            byte[] buf = new byte[1];
            try {
                while ( is.read( buf, 0, 1 ) > 0 ) {
                    System.out.format("[%04d] Recv: <%02x>\n", threadId, buf[0]);
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

        @Override
        public void run() {
            try {
                final InputStream  is = conn.getInputStream();
                final OutputStream os = conn.getOutputStream();            
                interact( is, os );
                conn.close();
            } 
            catch ( IOException e ) {
                try { conn.close(); }
                catch ( IOException ee ) {}
            }
        }
    }
    
    public void waitConnection() {
        try {
            while ( true ) {
                final Socket conn = sock.accept();
                try {
                    pool.execute( new Worker( conn ) );
                }
                catch (Exception e) {
                    System.out.println("Something wrong happened: the connection from " + conn + " will be closed.");
                    conn.close(); 
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }

    @Override public void close() throws IOException {
        sock.close();
    }
}
