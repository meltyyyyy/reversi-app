package jp.ac.tohoku.ecei.sf;

import java.io.IOException;

/**
   リバーシのゲームロジックを実装するクラス．

   このクラスはリバーシの1ゲームを行うstatic 
   メソッド{@link #game(Player, Player)}を提供する．
 */
public class Game {

    /**
       リバーシのゲームを行う
 
       @param   p1 黒番プレイヤー (must not be {@code null}). 
       @param   p2 白番プレイヤー (must not be {@code null}). 
       @return  ゲームが終了した時点における盤面
       @throws  IOException {@link Player#play(ReversiBoard, int)}が例外を投げた場合      
     */
    public static ReversiBoard game( Player p1, Player p2 ) throws IOException {
        ReversiBoard board = new ReversiBoard();
        Player q1 = p1; 
        Player q2 = p2;
        int    c  = ReversiBoard.BLACK;

        if(p1.getClass() == RemotePlayer.class){
            AppClient.client.send(board, c);
        }

        while ( !board.isEndGame() ) {
            if ( board.isPlayable( c ) ) {
                try {
                    Move m = q1.play( board, c );
                    board.move( m, c );
                } 
                catch (ReversiBoard.IllegalMoveException e) {
                    board.print( c );
                    System.out.println( "Illegal move: " + e.getMove() );
                    return board;
                }
            }
            c = ReversiBoard.flipColor( c );
            Player t = q1;
            q1 = q2;
            q2 = t;                
        }
        return board;
    }
}
