package projects1;
import java.util.Scanner;


public class HiddenNumbers {
    private static int num1 = 3;
    private static int num2 = 5;
    private static int num3 = 7;



    public static int getNumber(int selection) {
        if (selection == 1) {
            return num1;
        } else if (selection == 2) {
            return num2;

        } else if (selection == 3) {
            return num3;
        } else {
            return 0;
        }
    }

    public static int getNumberOptimized(int selection){
        switch(selection){
            case 1:
                return num1;

            case 2:
                return num2;

            case 3:
                return num3;

            default:
                return 0;

        }

    }

    public static void main(String[] args){
        Scanner input = new Scanner(System.in);

        System.out.println("Which number?: ");
        int select = input.nextInt();

        int num = getNumber(select);
        System.out.println("Number:" + num);
    }

}