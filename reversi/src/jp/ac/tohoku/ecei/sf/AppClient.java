package jp.ac.tohoku.ecei.sf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class AppClient implements Closeable {
    public static AppClient client = null;
    private final Socket sock;
    private final InputStream  is;
    private final OutputStream os;

    public AppClient(String host, int port ) throws Exception {
        this ( new Socket( host, port ) );
    }

    public AppClient(Socket sock ) throws Exception {
        this.sock = sock;
        this.is   = sock.getInputStream();
        this.os   = sock.getOutputStream();
    }

    public void send(ReversiBoard board,int color) {
        try {
            os.write("MOVE".getBytes(StandardCharsets.US_ASCII));
            os.write(" ".getBytes(StandardCharsets.US_ASCII));
            board.writeTo(os);
            os.write(" ".getBytes(StandardCharsets.US_ASCII));
            os.write(ReversiBoard.colorToByte(color));
            os.write("\r\n".getBytes(StandardCharsets.US_ASCII));
            os.flush();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void quit(){
        try {
            final String quitRequest = "QUIT" + "\r\n";
            os.write(quitRequest.getBytes(StandardCharsets.US_ASCII));
            os.flush();
            sock.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public Move recv() throws IOException {
        return new Move(is);
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }
}
