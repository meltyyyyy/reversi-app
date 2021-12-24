package jp.ac.tohoku.ecei.sf;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   リバーシの「手」を表すクラス
 */
public class Move implements Sendable {
    /**
       行インデックス
     */
    protected final int i;
    /**
       列インデックス
     */
    protected final int j;

    /**
       パスか否か
     */
    protected final boolean _isPassed;


    /**
       手の行インデックスを返す．

       この値は {@link #isPassed()} が {@code false} であるときのみ有効である．

       @return この手の行インデックス
     */
    public int getRowIndex() {
        return i;
    }

    /**
       手の列インデックスを返す．

       この値は {@link #isPassed()} が {@code false} であるときのみ有効である．

       @return 列インデックス
     */
    public int getColIndex() {
        return j;
    }

    /**
       手がパスを表しているかどうかを返す．

       @return オブジェクトがパスを表している場合は{@code True} 
       そうでなければ{@code False}を返す
     */
    public boolean isPassed() {
        return _isPassed;
    }


    /**
       行{@code i}列{@code j}への着手を表す{@link Move}オブジェクトを作成する．

       @param i 行インデックス (must be 1 .. 8).
       @param j 列インデックス (must be 1 .. 8).
     */
    public Move( int i, int j ) {
        this.i = i;
        this.j = j;
        this._isPassed = false;
    }

    /**
       パスを表す{@link Move}オブジェクトを作成する．
     */
    public Move() {
        this.i = this.j = 4;
        this._isPassed  = true;
    }

    /**
       このオブジェクトをバイト列へとシリアライズする．

       たとえば，{@code new Move(6,8).writeTo(os)}は{@code os}
       にバイト列{@code 0x46}，{@code 0x38}（ASCIIで{@code F8}）を
       出力する．また，オブジェクトがパスを表している場合は
       バイト列{@code 0x58}，{@code 0x58}（ASCIIで{@code XX}）を出力する．
       
       @param os 出力先の{@link OutputStream}
       @throws IOException {@code os.write()}がエラーを投げた場合
     */
    @Override 
    public void writeTo( OutputStream os ) throws IOException {
        if ( this.isPassed() ) {
            os.write( 0x58 );
            os.write( 0x58 );
        }
        else {
            os.write( (byte) (0x41 + (this.j - 1)) );
            os.write( (byte) (0x31 + (this.i - 1)) );
        }
    }

    /**
       入力ストリームからバイト列を読み込み，それに応じて{@link Move}オブジェクトを作成する．

       入力ストリーム{@code is}からは最低2バイトが読み込み可能でなけれ
       ばならない．このメソッドは{@code is}から2バイト読み込み，それが
       適切な「手」を表しているならば{@link Move}オブジェクトを作成す
       る．本メソッドは{@link #writeTo}に対応するデシリアライザとなっている．



       @param  is 入力元の{@link InputStream}
       @throws IOException 読み込みに失敗，または読まれたバイト列が適切な形式でなかった場合
     */
    public Move( InputStream is ) throws IOException {
        byte[] buf = new byte[4];
        int read = 0; int rest = 4;
        while ( rest > 0 ) {
            int r = is.read( buf, read, rest );
            if ( r < 0 ) 
                throw new IOException("Input stream is ended too early");
            read += r;
            rest -= r; 
        }

        if ( buf[0] == 0x58 && buf[1] == 0x58 ) { // XX
            this.i = this.j = 4; 
            this._isPassed = true;                
        }
        else if ( buf[0] >= 0x41 && buf[0] <= 0x48 &&   // A to H
                  buf[1] >= 0x31 && buf[1] <= 0x38 ) {  // 1 to 8
            this.i = buf[1] - 0x31 + 1;
            this.j = buf[0] - 0x41 + 1;
            this._isPassed = false;
        }
        else {
            throw new IOException("Invalid move format");
        }
    }

    /**
       オブジェクトを文字列に変換する．

       このメソッドの出力は{@link #writeTo}や{@link #Move(InputStream)}との互換性はない．
     */    
    @Override
    public String toString() {
        if ( this.isPassed() ) {
            return "Passed";
        } 
        else {
            StringBuilder sb = new StringBuilder(2);
            sb.append( (char)( 'A' + (this.j - 1) ) );
            sb.append( (char)( '1' + (this.i - 1) ) );
            return new String(sb);
        }
    }
}
