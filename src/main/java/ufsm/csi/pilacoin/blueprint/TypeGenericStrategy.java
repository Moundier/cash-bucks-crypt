package ufsm.csi.pilacoin.blueprint;

public interface TypeGenericStrategy {
  <T> void change(T type); // Block.java || BigInteger.java
}
