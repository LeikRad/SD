package class1.hanoi_towers;

import class1.util.Stack;
import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int number_of_disks = -1;
        
        System.out.print("Enter the number of disks: ");
        while (number_of_disks < 0) {
            number_of_disks = scanner.nextInt();
            if (number_of_disks < 0) {
                System.out.print("Please enter a positive number: ");
            }
        }
 
        Stack tower1 = new Stack(number_of_disks, "Tower A");
        Stack tower2 = new Stack(number_of_disks, "Tower B");
        Stack tower3 = new Stack(number_of_disks, "Tower C");

        for (int i = number_of_disks; i > 0; i--) {
            tower1.push((char) i);
        }

        System.out.println("Initial state:");
        System.out.println("Tower 1: " + tower1);
        System.out.println("Tower 2: " + tower2);
        System.out.println("Tower 3: " + tower3);
        System.out.println();

        hanoi(number_of_disks, tower1, tower3, tower2);
    }

    public static void hanoi(int n, Stack source, Stack target, Stack auxiliary) {
        if (n > 0) {
            hanoi(n - 1, source, auxiliary, target);
            target.push(source.pop());
            System.out.println("Move disk " + n + " from " + source + " to " + target);
            hanoi(n - 1, auxiliary, target, source);
        }
    }
    
}
