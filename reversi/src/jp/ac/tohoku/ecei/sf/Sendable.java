package jp.ac.tohoku.ecei.sf;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException; 

/**
   送信用のバイト列に変換できる（つまり，直接化可能な）オブジェクトを表すインタフェース．{@link java.io.Serializable}とは目的が異なる．
   <p>
   Javaはコンストラクタの抽象化能力の制限のために，この実験では
   逆方向の処理ができることを表すインターフェースは提供しない．
   この問題およびその対処法について興味があれば「GoF本」を参照のこと（たしか書いてあったはず）．

 */
public interface Sendable {
    public abstract void writeTo( OutputStream os ) throws IOException; 
}
