package de.bbqb.backend.api.model.entity;

public enum Timeslot {
   FOURTY_FIVE(45);

   private Integer time;

   Timeslot(Integer time) {
     this.time = time;
   }

   public Integer getTime() {
       return this.time;
   }
}
