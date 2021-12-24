package jp.ac.tohoku.ecei.sf;

import java.io.IOException;

/**
   プレイヤーを表すインタフェース（クライアントの実装ではこのインタフェースを実装するクラスを準備する）．
 */
public interface Player {
    /**
       与えられた盤面と手番における，手を計算する．
       @param  board 着手前の盤面．この盤面に対し{@link ReversiBoard#move(Move,int)}を呼んではならない．
       @param  color プレイヤーの手番 ({@link ReversiBoard#WHITE}か{@link ReversiBoard#BLACK}). 
       @return 着手する手
       @throws IOException インタラクティブに，もしくはサーバに手を問いあわせるプレイヤーはこの例外を投げるかもしれない
     */
    public Move play ( ReversiBoard board, int color ) throws IOException;    
}
