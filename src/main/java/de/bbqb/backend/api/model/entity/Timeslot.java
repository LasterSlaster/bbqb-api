package de.bbqb.backend.api.model.entity;

public enum Timeslot {
   FORTY_FIVE(45, 800L),
   NINETY(90, 1300L);

   private Integer time;
   private Long cost; // TODO: Extract cost from enum and move them to an external properties file or something

    /**
     *
     * @param time duration of the timeslot in minutes
     * @param cost cost of the timeslot in Cent(€)
     */
   Timeslot(Integer time, Long cost) {
     this.time = time;
       this.cost = cost;
   }

   public Integer getTime() {
       return this.time;
   }
   public Long getCost() { return this.cost; }
}
