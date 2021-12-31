package jp.ac.tohoku.ecei.sf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 リバーシサーバの実装．
 */
public class ReversiServer implements Closeable {
    public static ReversiServer server;
    private final int MAX_THREAD = 20;
    private final ServerSocket serverSock;
    private final List<String> requests = Arrays.asList("MOVE","QUIT","NOOP");

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
            byte[] byteRequest = new byte[4];
            // リクエスト内容を取得
            int read = 0; int rest = 4;
            while ( rest > 0 ) {
                int r = is.read( byteRequest, read, rest );
                read += r;
                rest -= r;
            }
            final String stringRequest = new String(byteRequest, StandardCharsets.US_ASCII);

            if(requests.contains(stringRequest)){
                if(stringRequest.equals(requests.get(0))){
                    moveRequest(is, os, threadId);
                }else if(stringRequest.equals(requests.get(1))){
                    quitRequest(threadId);
                }else if(stringRequest.equals(requests.get(2))){
                    noopRequest(os);
                }
            } else {
                System.out.format("[%04d] Recv: Received Illegal Command. Connection Closed.\n", threadId);
                sock.close();
            }
        }

        private void noopRequest(OutputStream os) throws IOException {
            final String status = "OK\r\n";

            os.write(status.getBytes(StandardCharsets.US_ASCII));
            os.flush();
        }

        private void quitRequest(long threadId) throws IOException {
            System.out.format("[%04d] Recv: QUIT\r\n", threadId);
            sock.close();
        }

        private void moveRequest(InputStream is, OutputStream os,long threadId) throws IOException {
            byte[] color = new byte[1];
            byte[] unused = new byte[4];

            // 空白を削除
            is.read(unused, 0, 1);

            // 64byte読み込んでReversiBoardを生成
            final ReversiBoard board = new ReversiBoard(is);

            // 空白を削除
            is.read(unused, 0,1);

            //  手番を取得
            is.read(color, 0, 1);

            // 改行コードを削除
            is.read(unused, 0, 1);
            is.read(unused, 0, 1);

            // 合法手の中から一手を選択
            List<Move> legalMoves = board.legalMoves(ReversiBoard.byteToColor(color[0]));
            Collections.shuffle(legalMoves);
            Move move = legalMoves.get(0);

            System.out.format("[%04d] Recv: ", threadId);
            System.out.format("MOVE" + " ");
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
                System.out.print("Connection Accepted! Client IP Address: " + sock.getInetAddress() + "\n");
                try {
                    pool.execute( new Worker( sock ) );
                }
                catch (Exception e) {
                    System.out.println("Something wrong happened: the connection from " + sock + " will be closed.\n");
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
