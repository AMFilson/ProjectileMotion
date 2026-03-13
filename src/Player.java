public class Player {
  /*The plan is to define a player with the requisite properties, methods, getters and setters*/
  /*There should be something to track scores between games too*/
  /*And a static method to track total amount of games played?*/
  private String name;
  private int power;
  private int angle;

  public Player(String name, int power, int angle){
    this.name = name;
    this.power = power;
    this.angle = angle;
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


}
