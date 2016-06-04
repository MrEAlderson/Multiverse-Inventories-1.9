package com.tux2mc.debugreport;


public class StringReplacers {

   String regex = "";
   String replacement = "";


   public StringReplacers(String regex, String replacement) {
      this.regex = regex;
      this.replacement = replacement;
   }

   public String getRegex() {
      return this.regex;
   }

   public String getReplacement() {
      return this.replacement;
   }
}
