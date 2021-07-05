public enum TicToe {
    X("X"), O("O");
    String tictoe;

    TicToe(String tictoe) {
        this.tictoe = tictoe;
    }

    public String getTictoe() {
        return tictoe;
    }
}

