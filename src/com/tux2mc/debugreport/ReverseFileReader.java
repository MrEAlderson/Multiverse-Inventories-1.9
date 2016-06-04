package com.tux2mc.debugreport;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ReverseFileReader {

   private RandomAccessFile randomfile;
   private long position;


   public ReverseFileReader(String filename) throws Exception {
      this.randomfile = new RandomAccessFile(filename, "r");
      this.position = this.randomfile.length();
      this.randomfile.seek(this.position);
      String thisLine = this.randomfile.readLine();

      while(thisLine == null) {
         --this.position;
         this.randomfile.seek(this.position);
         thisLine = this.randomfile.readLine();
         this.randomfile.seek(this.position);
      }

   }

   public String readLine() throws Exception {
      String finalLine = "";
      if(this.position < 0L) {
         return null;
      } else {
         while(this.position >= 0L) {
            this.randomfile.seek(this.position);
            byte thisCode = this.randomfile.readByte();
            char thisChar = (char)thisCode;
            if(thisCode == 13 || thisCode == 10) {
               this.randomfile.seek(this.position - 1L);
               byte nextCode = this.randomfile.readByte();
               if(thisCode == 10 && nextCode == 13 || thisCode == 13 && nextCode == 10) {
                  --this.position;
               }

               --this.position;
               break;
            }

            finalLine = thisChar + finalLine;
            --this.position;
         }

         return finalLine;
      }
   }

   public void close() {
      try {
         this.randomfile.close();
      } catch (IOException var2) {
         ;
      } catch (Exception var3) {
         ;
      }

   }
}
