package class1.a_l_a_l;

import java.util.Scanner;
public class Main {
    

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        
        // YetAnotherBigIntRepresentation a = new YetAnotherBigIntRepresentation(getNumber(sc));
        // YetAnotherBigIntRepresentation b = new YetAnotherBigIntRepresentation(getNumber(sc));
        YetAnotherBigIntRepresentation c = new YetAnotherBigIntRepresentation(getNumber(sc));
        YetAnotherBigIntRepresentation d = new YetAnotherBigIntRepresentation(getNumber(sc));
        // System.out.println("-----------------------------------------");
    
        // System.out.println("Number 1 : " + a);
        // System.out.println("Number 2 : " + b);
        // a.add(b);
        // System.out.println("Sum : " + a);
        // sc.close();

        System.out.println("-----------------------------------------");
        System.out.println("Number 3 : " + c);
        System.out.println("Number 4 : " + d);

        c.multiply(d);

        System.out.println("Product : " + c);


    }

    public static String getNumber(Scanner sc) {
        String number = "";
        while (true) {
            System.out.println("Enter a number: ");
            number = sc.nextLine();
            if (number.matches("[0-9]+")) {
                break;
            }
            System.out.println("Invalid input. Please enter a number.");
        }
        return number;
    }
}
