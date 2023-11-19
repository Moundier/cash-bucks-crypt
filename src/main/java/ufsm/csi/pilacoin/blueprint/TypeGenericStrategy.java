package ufsm.csi.pilacoin.blueprint;

public interface TypeGenericStrategy {

  <T> void update(T type); // Block.java || BigInteger.java
}
