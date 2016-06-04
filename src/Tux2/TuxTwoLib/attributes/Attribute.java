package Tux2.TuxTwoLib.attributes;

import Tux2.TuxTwoLib.attributes.Attributes;
import Tux2.TuxTwoLib.attributes.Operation;
import com.google.common.base.Preconditions;
import java.util.UUID;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import org.bukkit.inventory.ItemStack;

public class Attribute {

   String type;
   int operation;
   double amount;
   UUID uuid;


   public Attribute(String type, int operation, double amount, UUID uuid) {
      this.operation = -1;
      this.amount = 0.0D;
      this.type = type;
      this.operation = operation;
      this.amount = amount;
      this.uuid = uuid;
   }

   public Attribute(String type, double amount, UUID uuid) {
      this(type, Operation.ADD_NUMBER.id, amount, uuid);
   }

   public Attribute(String type, int operation, double amount) {
      this(type, operation, amount, UUID.randomUUID());
   }

   public Attribute(String type, double amount) {
      this(type, Operation.ADD_NUMBER.id, amount, UUID.randomUUID());
   }

   public Attribute() {
      this((String)null, Operation.ADD_NUMBER.id, 0.0D, UUID.randomUUID());
   }

   public Attribute setType(String type) {
      this.type = type;
      return this;
   }

   public Attribute setOperation(Operation operation) {
      this.operation = operation.id;
      return this;
   }

   public Attribute setOperation(int operation) {
      this.operation = operation;
      return this;
   }

   public Attribute setAmount(double amount) {
      this.amount = amount;
      return this;
   }

   public Attribute setUUID(UUID uuid) {
      this.uuid = uuid;
      return this;
   }

   public String getType() {
      return this.type;
   }

   public int getOperation() {
      return this.operation;
   }

   public double getAmount() {
      return this.amount;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   NBTTagCompound write() throws InstantiationException, IllegalAccessException {
      Preconditions.checkNotNull(this.type, "Type cannot be null.");
      if(this.operation == -1) {
         this.operation = Operation.ADD_NUMBER.id;
      }

      if(this.uuid == null) {
         this.uuid = UUID.randomUUID();
      }

      NBTTagCompound tag = new NBTTagCompound();
      tag.setString("AttributeName", this.type);
      tag.setString("Name", this.type);
      tag.setInt("Operation", this.operation);
      tag.setDouble("Amount", this.amount);
      tag.setLong("UUIDMost", this.uuid.getMostSignificantBits());
      tag.setLong("UUIDLeast", this.uuid.getLeastSignificantBits());
      return tag;
   }

   public boolean equals(Object o) {
      if(!(o instanceof Attribute)) {
         return false;
      } else {
         Attribute a = (Attribute)o;
         return this.uuid.equals(a.uuid) || this.type == a.type && this.operation == a.operation && this.amount == a.amount;
      }
   }

   static Attribute fromTag(NBTTagCompound c) throws IllegalArgumentException {
      Attribute a = new Attribute();
      if(!c.hasKey("AttributeName")) {
         throw new IllegalArgumentException("No AttributeName specified.");
      } else {
         a.setType(c.getString("AttributeName"));
         if(c.hasKey("Operation")) {
            a.setOperation(Operation.fromID(c.getInt("Operation")));
            if(!c.hasKey("Amount")) {
               throw new IllegalArgumentException("No Amount specified.");
            } else {
               a.setAmount(c.getDouble("Amount"));
               if(c.hasKey("UUIDMost") && c.hasKey("UUIDLeast")) {
                  a.setUUID(new UUID(c.getLong("UUIDLeast"), c.getLong("UUIDMost")));
               } else {
                  a.setUUID(UUID.randomUUID());
               }

               return a;
            }
         } else {
            throw new IllegalArgumentException("No Operation specified.");
         }
      }
   }

   public ItemStack apply(ItemStack is, boolean replace) {
      return Attributes.apply(is, this, replace);
   }
}
