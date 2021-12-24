package jp.ac.tohoku.ecei.sf;

import java.io.IOException;
import java.util.Random;

/**
 メイン関数を提供．
 */
public class Main {
    private static String usage() {
        return ("Usage: java -jar jikkenC-reversi.jar (client HOST PORT|server PORT|single PORT)");
    }

    private static void initService(String[] args) {
        if ( args.length == 0 ) {
            System.out.println( usage() );
            return;
        }
        if ( args[0].startsWith( "c" ) ) {
            if ( args.length < 3 ) {
                System.out.println( usage() );
                return;
            }
            final String host = args[1];
            int    port = Integer.parseInt( args[2] );

            startClient( host, port );
        }
        else {
            if ( args.length < 2 ) {
                System.out.println( usage() );
                return;
            }
            int    port = Integer.parseInt( args[1] );

            startServer( port );
        }
    }

    private static void startServer( int port ) {
        try {
            ReversiServer.server = new ReversiServer( port );
            ReversiServer.server.waitConnection();
        }
        catch ( Exception e ) {
            System.out.println( "Starting server failed." );
        }
    }

    private static void startClient( String host, int port ) {
        try {
            AppClient.client = new AppClient( host, port );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.out.println( "Connection failed." );
        }
    }

    private static void endService(String[] args) throws IOException {
        if ( args[0].startsWith( "c" ) ) {
            AppClient.client.close();
        }
        else {
            ReversiServer.server.close();
        }
    }

    public static void main( String[] args ) throws IOException {
        initService(args);

        Player you = new HumanPlayer();
        Player remotePlayer = new RemotePlayer();

        ReversiBoard board;
        boolean isYourColorBlack = new Random().nextBoolean();

        if ( isYourColorBlack ) {
            System.out.println("You are a black player.");
            board = Game.game( you, remotePlayer );
        }
        else {
            System.out.println("You are a white player.");
            board = Game.game( remotePlayer, you );
        }

        System.out.println("-----------------------------------");
        System.out.println("Game finished.");
        board.print();


        System.out.println( "You played " + (isYourColorBlack ? "black" : "white") + ".");

        endService(args);
    }
}
