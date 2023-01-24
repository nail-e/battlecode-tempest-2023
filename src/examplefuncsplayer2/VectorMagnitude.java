package projects1;
import java.util.Scanner;

public class VectorMagnitude{

    public static float getMagnitude(float t, float b){
        return (float) Math.sqrt(t * b + t * b );
    }







    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("x: ");
        float t = input.nextFloat();
        System.out.println("y: ");
        float b = input.nextFloat();
        float mag = getMagnitude(t,b);
        System.out.println("Magnitude: " + mag);


    }
}