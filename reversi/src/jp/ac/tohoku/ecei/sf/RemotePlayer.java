package jp.ac.tohoku.ecei.sf;

import java.io.IOException;
import java.util.List;

public class RemotePlayer implements Player {
    public Move play ( ReversiBoard board, int color ) throws IOException {
        List<Move> moves = board.legalMoves( color );
        if ( moves.isEmpty() ) {
            return new Move();
        }

        final Move mv = AppClient.client.recv();

        board.print( color );
        System.out.println("Remote player played " + mv);
        return mv;
    }
}