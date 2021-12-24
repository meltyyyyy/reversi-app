package jp.ac.tohoku.ecei.sf;

/**
   A class for pairs. 
 */
final class Pair<T,U> {
    private final T fst;
    private final U snd;

    public Pair( T fst, U snd ) {
        this.fst = fst;
        this.snd = snd;
    }
    
    /**
       Extracts the first element. 
       @return The first element. 
     */
    public T getFst() { return fst; }
    /**
       Extracts the second element. 
       @return The second element. 
     */
    public U getSnd() { return snd; }    
}
