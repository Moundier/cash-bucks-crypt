package ufsm.csi.pilacoin.blueprint;

public interface TypeCommon {

  <T> void subscribe(TypeGenericStrategy obj); // Accepts Any 
  <T> T unsubscribe(TypeGenericStrategy objs); // Accepts Any and Returns Any
}
