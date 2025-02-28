package class1.palindrome;

import class1.util.Queue;
import class1.util.Stack;

import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        System.out.println(isPalindrome(input));
        scanner.close();
    }

    public static boolean isPalindrome(String input) {
        Queue queue = new Queue(input.length());
        
        Stack stack = new Stack(input.length());

        for (int i = 0; i < input.length(); i++) {
            queue.add(input.charAt(i));
            stack.push(input.charAt(i));
        }

        for (int i = 0; i < input.length(); i++) {
            if (queue.remove() != stack.pop()) {
                return false;
            }
        }
        return true;
    }
}