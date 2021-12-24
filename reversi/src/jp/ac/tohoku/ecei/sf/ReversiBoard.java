package jp.ac.tohoku.ecei.sf;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
   リバーシの盤面を表すクラス
 */
public final class ReversiBoard implements Sendable {
    private int[][] board;

    /**
       石がないことを表す定数
     */
    public static final int NONE     = 0;
    /**
       黒石および黒手番を表す定数
     */
    public static final int BLACK    = 1;
    /**
       白石および白手番を表す定数
    */
    public static final int WHITE    = 2;


    /**
       石の色を文字列に変換する．

       @param  c 石あるいは手番の色 ({@link #WHITE}, {@link #BLACK}, or {@link #NONE})
       @return {@link #WHITE}に対し{@code "O"},
               {@link #BLACK}に対し{@code "X"},
               {@link #NONE}に対し{@code "-"}
     */
    static String colorToString(final int c) {
        if ( c == WHITE ) { return "O"; }
        if ( c == BLACK ) { return "X"; }
        return "-";
    }


    /**
       石の色をバイト表現に変換する．

       @param  c 石の色 ({@link #WHITE}, {@link #BLACK}, or {@link #NONE}).
       @return {@link #WHITE}に対し{@code 0x4F},
               {@link #BLACK}に対し{@code 0x58},
               そして{@link #NONE}に対し{@code 0x2D}
     */
    public static byte colorToByte(final int c ) {
        if ( c == WHITE ) { return 0x4F; } // O
        if ( c == BLACK ) { return 0x58; } // X
        return 0x2D; // -
    }

    /**
       {@link color2Byte}の逆関数

       @param  b 石の色のバイト表現
       @return 対応する石の色
     **/
    public static int byteToColor(final byte b ) {
        if ( b == 0x4F ) { return WHITE; }
        if ( b == 0x58 ) { return BLACK; }
        return NONE;
    }
    

    // For convenience in iteration.
    static private final int[] allRows = {1,2,3,4,5,6,7,8};
    static private final int[] allCols = {1,2,3,4,5,6,7,8};
    static private final List<Move> allMoves = product(allRows, allCols);

    private static List<Move> product( int[] a, int[] b ) {
        List<Move> r = new ArrayList<Move>();
        for ( int i : a ) {
            for ( int j : b ) {
                r.add( new Move(i,j) );
            }
        }
        return r;
    }
        
    /**
       違法手の着手を表す例外
     */
    public static class IllegalMoveException extends Exception {
        private final Move illegalMove;
        public IllegalMoveException( Move m ) {
            super("IllegalMoveException");
            this.illegalMove = m;
        }
        /**
           着手された違法手を返す
           @return 違法手
         */
        public Move getMove() {
            return illegalMove;
        }
    }
    
    /**
       与えられた石の色の裏の色を求める．

       つまり{@link #BLACK}を{@link #WHITE}にし，{@link #WHITE}を{@link #BLACK}にする．
       @param  c 石の色
       @return 裏の色
     */
    public static int flipColor( int c ) {
        switch( c ) {
        case WHITE: return BLACK;
        case BLACK: return WHITE;
        }
        return NONE;
    }



    /**
       初期盤面を表す{@link ReversiBoard}オブジェクトを作成する．
       
       この関数は以下の盤面を表す{@link ReversiBoard}オブジェクトをコンストラクトする．
       <pre>
 |A B C D E F G H
-+----------------
1|- - - - - - - -
2|- - - - - - - -
3|- - - - - - - -
4|- - - O X - - -
5|- - - X O - - -
6|- - - - - - - -
7|- - - - - - - -
8|- - - - - - - -
       </pre>
     */
    public ReversiBoard () {
        this.board = new int[10][10]; // 8x8 board with sentinel
        this.board[4][4] = WHITE; this.board[5][5] = WHITE;
        this.board[4][5] = BLACK; this.board[5][4] = BLACK;
    }

    /**
       与えれた盤面の{@link ReversiBoard}オブジェクトを作成する．
       @param board 番兵付きの10x10配列．
                    配列のそれぞれの要素は{@link #WHITE}, {@link #BLACK}もしくは{@link #NONE}のいずれかでなければらない
     */
    public ReversiBoard( int[][] board ) {
        this();
        for ( int i = 1; i <= 8; i++ ) {
            for ( int j = 1; j <= 8; j++ ) {
                this.board[i][j] = board[i][j];
            }
        }
    }

    /**
       {@link ReversiBoard}オブジェクトを複製する．新しいオブジェクト
       は盤面の状態を引き継ぐが，元のオブジェクトと共有はしない．
     */

    @Override
    public ReversiBoard clone() {
        ReversiBoard b = new ReversiBoard( this.board );
        return b; 
    }

    private boolean isLegalMove(int i, int j, int c) {
        if ( i <= 0 || i > 8 || j <= 0 || j > 8 ) return false;
        if ( board[i][j] == NONE ) {
            return isEffectiveMove(i,j,c);
        }
        return false;
    }

    /**
       与えられた手番において，与えられた手が合法手か否かを判定する．

       @param  m 手 (非{@code null}). 
       @param  c 手番 ({@link #WHITE}か{@link #BLACK}) 
       @return {@code m}が手番{@code c}における合法手なら{@code true}，そうでなければ{@code false}      
     */    
    public boolean isLegalMove( Move m, int c) {
        if ( m.isPassed() ) {
            return !isPlayable( c );
        }
        return isLegalMove( m.getRowIndex(), m.getColIndex(), c );
    }

    /**
       与えられた手番における，全ての合法手を返す．

       @param  c 手番 ({@link #WHITE} か {@link #BLACK})
       @return 合法手のリスト（非 {@code null}）
     */
    public List<Move> legalMoves( final int c ) {
        List<Move> r = new LinkedList<Move>();
        for ( Move m : allMoves ) {
            if (isLegalMove(m,c)) { r.add(m); }
        }
        return r;
    }


    /**
       着手可能かどうか，つまり与えられた手番において合法手が存在するかどうかを判定する．

       @param  c 手番 ({@link #WHITE} か {@link #BLACK})
       @return 合法手が存在する場合は{@code true}，そうでなければ{@code false}
     */
    public boolean isPlayable( final int c ) {
        for ( Move m : allMoves ) {
            if ( isLegalMove(m,c) ) {
                return true;
            }
        }
        return false;
    }

    /**
       終局しているか否か，つまり何れのプレイヤーにも合法手がないかどうかを判定する．


       @return 終局していれば{@code true}，そうでなければ{@code false}
     */
    public boolean isEndGame() {
        return !isPlayable( WHITE ) && !isPlayable( BLACK );
    }

    private boolean isEffectiveMove(int i, int j, int c) {
        return !(flippableIndices(i,j,c).isEmpty());
    }

    private List<Move> flippableIndices(int i, int j, int c) {
        List<Move> s = new LinkedList<Move>();
        for ( int di = -1; di <= 1; di++ ) {
            for ( int dj = -1 ; dj <= 1; dj++ ) {
                if ( di == 0 && dj == 0 ) continue;
                flippableIndicesLine(i+di,j+dj,di,dj,c,s);
            }
        }
        return s;
    }

    private void flippableIndicesLine(int i, int j, final int di, final int dj, final int c, List<Move> list) {
        List<Move> s = new LinkedList<Move>();
        boolean flag = false;
        final int oc = flipColor( c );
        while (true) {
            if ( board[i][j] == oc ) {
                s.add( new Move( i, j ) );
                i += di; j += dj;
                flag = true;
            }
            else {
                break;
            }
        }
        if (!(flag && board[i][j] != c)) {
            list.addAll(s);
        }
    }

    /**
       手を盤面に反映する．
 
       @param  mv 手 (非{@code null})
       @param  c  石の色 ({@link #WHITE}か{@link #BLACK})
       @throws IllegalMoveException 違法手が指された場合
     */
    public void move( Move mv, int c ) throws IllegalMoveException {
        if ( mv.isPassed() ) { 
            if ( isPlayable( c ) ) {
                throw new IllegalMoveException(mv);
            }
            return;
        }
        int i = mv.getRowIndex();
        int j = mv.getColIndex();
        List<Move> moves = flippableIndices(i,j,c);
        if ( moves.isEmpty() ) throw new IllegalMoveException(mv);
        for (Move m: moves) {
            board[m.getRowIndex()][m.getColIndex()] = c;
        }
        board[i][j] = c;
    }

    private void move( int i, int j, int c ) throws IllegalMoveException {
        move( new Move(i,j), c);
    }


    /**
       行{@code i}，列{@code j}にある石を返す
       @param   i 行インデックス
       @param   j 列インデックス
       @return  その場所の石の色 ({@link #BLACK}, {@link #WHITE}, or {@link #NONE}) 
     */
    public int get( int i, int j ) {
        return board[i][j];
    }


    private int stoneCounts( int c ) {
        int s = 0;
        for ( int i = 1; i <= 8; i++ ) {
            for ( int j = 1; j <= 8; j++ ) {
                if ( board[i][j] == c ) s += 1;
            }
        }
        return s;
    }


    /**
       盤面をバイト列へとシリアライズする．

       たとえば，以下の文字列のASCIIコードに対応するバイト列
       <pre>
 ---------------------------OX------XXX--------------------------</pre> 
       が，以下の状態を表す{@link ReversiBoard}オブジェクトに対し出力される．
       <pre>
 |A B C D E F G H
-+----------------
1|- - - - - - - -
2|- - - - - - - -
3|- - - - - - - -
4|- - - O X - - -
5|- - - X X X - -
6|- - - - - - - -
7|- - - - - - - -
8|- - - - - - - -
       </pre>
       すなわち，出力の{@code k}バイト目は，{@code color2Byte(this.get(k/8,k%8))}となる．
       

       
       @param  os 出力先の{@link OutputStream}
       @throws IOException {@code os.write()}が例外を投げた場合
     */
    @Override 
    public void writeTo( OutputStream os ) throws IOException {
        for ( int i = 1; i <= 8; i++ ) {
            for ( int j = 1; j <= 8; j++ ) {
                os.write( colorToByte( this.get(i,j) ) );
            }
        }
    }


    /**
       {@link #writeTo}に対応するデシリアライザ．


       入力ストリーム {@code is}から64バイト読み込み，その値に応じて{@link ReversiBoard}を構築する．
       入力の{@code k}バイト目が{@code b}であったとき，{@code k/8}行{@code k%8}列の石が{@code byte2Color( b )}
       となる．

       @param  is 入力元{@link InputStream}
       @throws IOException {@code is.read()}が例外を投げた場合
     */
    public ReversiBoard( InputStream is ) throws IOException {
        this();
        for ( int i = 1; i <= 8; i++ ) {
            for ( int j = 1; j <= 8; j++ ) {
                int b = is.read();
                if ( b < 0 ) throw new IOException( "Invalid board format" ); 
                this.board[i][j] = byteToColor( (byte) b );
            }
        }
        System.out.print("\n");
    }

    /**
       この盤面の文字列表現を求める．

       @return 盤面の文字列表現
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");

        sb.append(" |A B C D E F G H " + nl);
        sb.append("-+----------------" + nl);
        for (int i = 1; i <= 8; i++) {
            sb.append( i + "|" );
            for (int j = 1; j <= 8; j++) {
                sb.append( colorToString(board[i][j]) + " " );
            }
            sb.append( nl );
        }
        sb.append( "Black: " + stoneCounts( BLACK ) + nl);
        sb.append( "White: " + stoneCounts( WHITE ) + nl );
        return sb.toString();
    }

    /**
       この盤面の，合法手の情報を付けくわえた文字列表現を求める．

       @param  c 合法手の判定の基準となる手番の色 ({@link #WHITE}か{@link #BLACK})
       @return 盤面の文字列表現
     */
    public String toString( int c ) {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");

        sb.append(" |A B C D E F G H " + nl);
        sb.append("-+----------------" + nl);
        for (int i = 1; i <= 8; i++) {
            sb.append( i + "|" );
            for (int j = 1; j <= 8; j++) {
                if ( board[i][j] == NONE && isLegalMove( i, j, c ) ) {
                    sb.append( ". " );
                }
                else {
                    sb.append( colorToString(board[i][j]) + " " );
                }
            }
            sb.append( nl );
        }
        sb.append( "Black: " + stoneCounts( BLACK ) + nl);
        sb.append( "White: " + stoneCounts( WHITE ) + nl);
        return sb.toString();
    }

    /**
       {@link #toString()}の結果を標準出力へと出力する．
     */
    public void print() {
        System.out.print( this.toString() );
    }

    /**
       {@link #toString(int)}の結果を標準出力へと出力する
       @param c 手番の色 ({@link #WHITE}か{@link #BLACK})
     */
    public void print( int c ) {
        System.out.print( this.toString(c) );
    }

    public void printRequest() {
        for ( int i = 1; i <= 8; i++ ) {
            for ( int j = 1; j <= 8; j++ ) {
                if(this.get(i,j) == 1){
                    System.out.print("X");
                } else if (this.get(i, j) == 2) {
                    System.out.print("O");
                } else {
                    System.out.print("-");
                }
            }
        }
    }
}
