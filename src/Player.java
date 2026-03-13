import java.math.*;

public class Player {
  /*The plan is to define a player with the requisite properties, methods, getters and setters*/
  /*There should be something to track scores between games too*/
  /*And a static method to track total amount of games played?*/
  private String name;
  private int power;
  private int angle;
  private int startingPosition;
/*  private double shot;*/

  public Player(String name, int power, int angle){
    this.name = name;
    this.power = power;
    this.angle = angle;/*
    this.shot = shot;*/
  }

  public String getName() {
    return name;
  }

  public int getPower(){
    return power;
  }

  public int getAngle(){
    return angle;
  }

  public double getShot(){
    final double GRAVITY = -9.81;
    /*determine the time it takes for a shot to land*/
   double timeToLand = (GRAVITY / (2 * getPower() * Math.sin(getAngle()));
   /*get the x-coordinate or the shot*/
    (getPower() * Math.cos(getAngle()) * timeToLand + ;
  }

  public void setStartingPosition(){
    startingPosition = (int) (Math.random() * 120);
  }


}
