package ufsm.csi.pilacoin.blueprint;

public interface TypeCommon {
  <T> void hold(TypeGenericStrategy obj); // Accepts Any 
  <T> T release(TypeGenericStrategy objs); // Accepts Any and Returns Any
}
