package jp.ac.tohoku.ecei.sf;

import java.net.*;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 リバーシサーバの実装．
 */
public class ReversiServer implements Closeable {
    public static ReversiServer server;
    private final int MAX_THREAD = 20;
    private final ServerSocket serverSock;

    /* スレッドプール */
    private final ExecutorService pool;

    ReversiServer(int port ) throws Exception {
        serverSock = new ServerSocket( port );
        this.pool = Executors.newFixedThreadPool( MAX_THREAD );
    }

    /**
     ワーカスレッド
     */
    class Worker implements Runnable {
        private final Socket sock;
        public Worker( Socket s ) {
            sock = s;
        }

        private void interact( InputStream is,
                               OutputStream os ) throws IOException {
            final long threadId = Thread.currentThread().getId();
            byte[] request = new byte[4];
            byte[] color = new byte[1];
            byte[] unused = new byte[4];

            // "MOVE"まで取得
            int read = 0; int rest = 4;
            while ( rest > 0 ) {
                int r = is.read( request, read, rest );
                read += r;
                rest -= r;
            }

            // 空白を削除
            is.read(unused, 0, 1);

            // 64byte読み込んでReversiBoardを生成
            final ReversiBoard board = new ReversiBoard(is);

            is.read(unused, 0,1);

            //  手番を取得
            is.read(color, 0, 1);

            // 改行コードを削除
            is.read(unused, 0, 1);
            is.read(unused, 0, 1);

            List<Move> legalMoves = board.legalMoves(ReversiBoard.byteToColor(color[0]));
            Move move = legalMoves.get(0);
            System.out.format("[%04d] Recv: ", threadId);
            System.out.format(new String(request, StandardCharsets.US_ASCII) + " ");
            board.printRequest();
            System.out.format(" " + ReversiBoard.colorToString(ReversiBoard.byteToColor(color[0])) + "\n");

            move.writeTo(os);
            os.write("\n\r".getBytes(StandardCharsets.US_ASCII));
            os.flush();

            if(board.isPlayable(ReversiBoard.flipColor(ReversiBoard.byteToColor(color[0]))) && !board.isEndGame()){
                interact(is, os);
            }
        }

        @Override
        public void run() {
            try {
                final InputStream  is = sock.getInputStream();
                final OutputStream os = sock.getOutputStream();
                interact( is, os );
                sock.close();
            }
            catch ( IOException e ) {
                try { sock.close(); }
                catch ( IOException ee ) {}
            }
        }
    }

    public void waitConnection() {
        try {
            while ( true ) {
                final Socket sock = serverSock.accept();
                System.out.print("Connection Accepted! Client IP Address: " + sock.getInetAddress());
                try {
                    pool.execute( new Worker( sock ) );
                }
                catch (Exception e) {
                    System.out.println("Something wrong happened: the connection from " + sock + " will be closed.");
                    sock.close();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }

    @Override public void close() throws IOException {
        serverSock.close();
    }
}
