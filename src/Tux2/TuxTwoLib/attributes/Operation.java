package Tux2.TuxTwoLib.attributes;


public enum Operation {

   ADD_NUMBER("ADD_NUMBER", 0, 0),
   MULTIPLY_PERCENTAGE("MULTIPLY_PERCENTAGE", 1, 1),
   ADD_PERCENTAGE("ADD_PERCENTAGE", 2, 2);
   public int id;
   // $FF: synthetic field
   private static final Operation[] ENUM$VALUES = new Operation[]{ADD_NUMBER, MULTIPLY_PERCENTAGE, ADD_PERCENTAGE};


   private Operation(String var1, int var2, int id) {
      this.id = id;
   }

   public static Operation fromID(int id) {
      Operation[] var4;
      int var3 = (var4 = values()).length;

      for(int var2 = 0; var2 < var3; ++var2) {
         Operation o = var4[var2];
         if(o.id == id) {
            return o;
         }
      }

      return null;
   }
}
