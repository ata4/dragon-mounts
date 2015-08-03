package info.ata4.minecraft.dragon.util;

/**
 * User: The Grey Ghost
 * Date: 12/07/2014
 * very simple class to group two objects into a pair
 */
public final class Pair<A, B>
{
  public Pair(A i_first, B i_second) {
    first = i_first;
    second = i_second;
  }

  public A getFirst() {
    return first;
  }

  public B getSecond() {
    return second;
  }

  public void setFirst(A first) {
    this.first = first;
  }

  public void setSecond(B second) {
    this.second = second;
  }

  private A first;
  private B second;

  @Override
  public String toString() {
    return "(" + first + "," + second + ")";
  }

}