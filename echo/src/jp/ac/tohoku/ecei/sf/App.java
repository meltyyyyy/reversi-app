package jp.ac.tohoku.ecei.sf;

/**
   メイン関数を提供．
 */
public class App {

    private static String usage() {
        return ("Usage: java -jar echo.jar (client HOST PORT|server PORT|single PORT)");
    }

    private static void startServer( int port ) {
        EchoServer s = null;
        try {
            s = new EchoServer( port );
            s.waitConnection();
            s.close(); 
        }
        catch ( Exception e ) {
            System.out.println( "Starting server failed." );
        }

        try {
            if ( s != null ) s.close(); 
        } 
        catch ( Exception e ) {}
    }


    private static void startServerSingle( int port ) {
        EchoServerSingle s = null;
        try {
            s = new EchoServerSingle( port );
            s.waitConnection();
            s.close(); 
        }
        catch ( Exception e ) {
            System.out.println( "Starting server failed." );
        }

        try {
            if ( s != null ) s.close(); 
        } 
        catch ( Exception e ) {}        
    }

    private static void startClient( String host, int port ) {
        EchoClient c = null;
        try {
            c = new EchoClient( host, port ); 
            c.communicate();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.out.println( "Connection failed." );
        }
        try {
            if (c != null) c.close();
        }
        catch ( Exception e ) {}
    }

    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            System.out.println( usage() ); 
            return; 
        }
        if ( args[0].startsWith( "c" ) ) {
            if ( args.length < 3 ) {
                System.out.println( usage() );
                return;
            }
            String host = args[1];
            int    port = Integer.parseInt( args[2] ); 
            
            startClient( host, port );             
        }
        else {
            if ( args.length < 2 ) {
                System.out.println( usage() );
                return; 
            }
            int    port = Integer.parseInt( args[1] ); 

            if ( args[0].startsWith( "single" ) ) {
                startServerSingle( port );
            }            
            else {
                startServer( port ); 
            }
        }
    }
}
